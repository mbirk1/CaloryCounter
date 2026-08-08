# Prompt: CSV-Import-Absturz (numeric overflow), hängender Job & schließbares Popup fixen

> Zum direkten Copy-Paste für einen Coding-Agenten. Bezieht sich auf das
> bereits implementierte CSV-Import-Feature (`usecase.food.importer.*`,
> `csv-import-progress-dialog`).

---

```
Du bist Senior Full-Stack-Entwickler an "Calory Counter". Das kürzlich
gebaute CSV-Import-Feature wirft beim Import der OpenFoodFacts-Daten einen
"numeric field overflow"-Fehler (SQLState 22003) und das Fortschritts-Popup
lässt sich per Backdrop-Klick schließen, obwohl der Import noch läuft. Behebe
beides und ergänze einen sauberen Rollback-Mechanismus für echte
Job-Fehler. Halte dich an die bestehenden QA-Vorgaben (Checkstyle, ArchUnit,
JaCoCo ≥90 %/90 %/80 %, bestehende Testkonventionen).

## 1. Fehleranalyse: numeric field overflow

Log-Auszug (gekürzt):

```
insert into tab_calory_food (... calory_count ...) values ((...'10447.76'...))
was aborted: ERROR: numeric field overflow
Detail: A field with precision 6, scale 2 must round to an absolute value less than 10^4.
```

`tab_calory_food.calory_count` ist `numeric(6,2)` (Maximalwert 9999,99).
Betroffene Zeilen aus der OpenFoodFacts-CSV enthalten für `energy-kcal_100g`
Werte wie `10447.76` oder `14000.00` – physikalisch für "kcal pro 100 g"
unmöglich (realistisches Maximum liegt bei ca. 900 kcal/100g, erreicht von
reinem Fett/Öl). Das sind fehlerhafte Rohdaten aus der Quelle, keine
Rechenfehler unsererseits – `FoodCsvRowMapper` (siehe
`backend/src/main/java/de/birk/calory/usecase/food/importer/FoodCsvRowMapper.java`)
validiert aktuell nur, DASS ein Kalorienwert vorhanden und parsebar ist,
NICHT ob er plausibel ist. Diese Werte laufen ungefiltert bis zum `INSERT`
durch und sprengen dort die Spaltenpräzision.

**Wahrscheinliche Ursache, warum der Import dabei komplett hängen bleibt
(nicht nur die eine Zeile überspringt):** `FoodCsvImportUsecase.persistBatch()`
und `.persistOneByOne()` fangen aktuell ausschließlich
`org.springframework.dao.DataIntegrityViolationException` ab. Diese
Exception-Klasse deckt laut Spring/Hibernate typischerweise
Integritätsverletzungen (SQLState-Klasse `23`: unique, not-null, FK, check)
ab – ein "numeric field overflow" ist aber eine *Data Exception*
(SQLState-Klasse `22`) und wird von Hibernates PostgreSQL-Exception-
Übersetzung sehr wahrscheinlich als andere `DataAccessException`-Unterklasse
(z. B. `InvalidDataAccessResourceUsageException`/generische
`DataAccessException`) durchgereicht – **nicht** als
`DataIntegrityViolationException`. Der bestehende `catch`-Block greift dann
nicht, die Exception läuft durch `processRecords()` und `importFromFile()`
(dessen `try/catch` nur `IOException` behandelt) unabgefangen bis in die
`@Async void dispatch()`-Methode von `FoodCsvImportDispatcher` durch. Für
`@Async void`-Methoden loggt Spring das nur über den
`SimpleAsyncUncaughtExceptionHandler` und verwirft es sonst – `status.complete()`
bzw. `status.fail()` werden nie aufgerufen, der Job bleibt für immer im
Zustand `RUNNING` hängen, und das Frontend (`CsvImportService.pollUntilFinished()`)
pollt endlos weiter, ohne dass der Nutzer je eine Rückmeldung bekommt.

Verifiziere diese Analyse lokal (z. B. testweise mit `catch (Exception e) {
log.error("type={}", e.getClass(), e); }` um die tatsächliche Exception-Klasse
zu bestätigen), behebe den Bug aber unabhängig vom exakten Ergebnis mit den
folgenden drei Maßnahmen – sie sind zusammen robust, egal welche konkrete
Exception-Unterklasse Postgres/Hibernate tatsächlich wirft.

## 2. Fix 1 (primär): Plausibilitätsprüfung in `FoodCsvRowMapper`

Erweitere `FoodCsvRowMapper` um Wertebereichsprüfungen, BEVOR Werte in ein
`FoodPersistence`-Objekt gemappt werden:

- **Kalorien** (`energy-kcal_100g`, ggf. aus kJ umgerechnet): nur gültig im
  Bereich `[0, 900]` (kcal pro 100 g). Außerhalb dieses Bereichs ist der Wert
  physikalisch nicht plausibel → die gesamte Zeile wird verworfen (wie beim
  bisherigen "Kalorien fehlen"-Fall, `calories()` gibt `null` zurück,
  `map()` liefert `Optional.empty()`).
- **Makronährstoffe** (`fat, saturated-fat, carbohydrates, sugars, fiber,
  proteins, salt, sodium` – alles `_100g`-Werte): gültiger Bereich `[0, 100]`
  (Gramm pro 100 g Produkt kann nie über 100 liegen). Liegt ein einzelner
  Wert außerhalb, wird NUR dieses Feld auf `null` gesetzt (wie beim
  bisherigen "Wert fehlt/unparsebar"-Fall) – die Zeile wird nicht komplett
  verworfen, da Name/Kalorien/Marke weiterhin gültig sein können.

Konkret: neue Konstanten `MAX_PLAUSIBLE_CALORIES = new BigDecimal("900")` und
`MAX_PLAUSIBLE_MACRO_GRAMS = new BigDecimal("100")`, Bereichsprüfung
`>= BigDecimal.ZERO && <= max` in `calories()` bzw. in einer neuen
Hilfsmethode, die von `decimal()` für die Makronährstoff-Spalten zusätzlich
aufgerufen wird.

Das ist die eigentliche Behebung: Damit erreicht kein Wert mehr die
Datenbank, der die Spaltenpräzision `numeric(6,2)` (Maximalwert 9999,99)
überhaupt sprengen könnte.

## 3. Fix 2 (defensiv): Catch-Block verbreitern

In `FoodCsvImportUsecase.persistBatch()` und `.persistOneByOne()`:
`catch (DataIntegrityViolationException e)` → `catch (DataAccessException e)`
(Import `org.springframework.dao.DataAccessException`, die gemeinsame
Oberklasse aller Spring-Datenzugriffsfehler). Das fängt sowohl echte
Integritätsverletzungen (z. B. Unique-Constraint bei Re-Import, bereits
funktionierend) als auch alle anderen JDBC/JPA-Fehler ab, unabhängig von der
genauen SQLState-Klassifizierung – ein einzelner kaputter Datensatz in
einem Batch darf niemals die anderen 999 gültigen Datensätze mitreißen oder
den gesamten Job zum Absturz bringen.

## 4. Fix 3: Job darf nicht in RUNNING hängen bleiben + Rollback bei echtem Fehler

`FoodCsvImportUsecase.importFromFile()` erweitern:

```java
void importFromFile(Path filePath, FoodImportJobStatus status) {
  try (...) {
    processRecords(parser, status);
    status.complete();
  } catch (IOException e) {
    LOG.error("CSV-Import-Job {} beim Lesen der Datei fehlgeschlagen", status.getJobId(), e);
    rollbackAndFail(status);
  } catch (RuntimeException e) {
    LOG.error("CSV-Import-Job {} unerwartet fehlgeschlagen", status.getJobId(), e);
    rollbackAndFail(status);
  } finally {
    deleteQuietly(filePath);
  }
}

private void rollbackAndFail(FoodImportJobStatus status) {
  this.foodRepository.deleteByImportJobId(status.getJobId());
  status.fail();
}
```

(SLF4J-`Logger` als `private static final` Feld ergänzen – aktuell wird der
`IOException`-Fall komplett stillschweigend verschluckt, das erschwert genau
diese Art von Debugging unnötig.)

Wichtig, dieser Fehlerbehandlung liegt eine bewusste Unterscheidung
zugrunde, die du beibehalten sollst:

- **Einzelne fehlerhafte Zeile** (Validierung schlägt fehl, Duplikat bei
  Re-Import, einzelner DB-Fehler beim `persistOneByOne`-Fallback): wird
  übersprungen und gezählt (`skippedCount`/`errorCount`), der Job läuft
  normal weiter und endet als `COMPLETED`. Das bleibt unverändert – bei
  Millionen Zeilen schwankender Datenqualität darf nicht jede einzelne
  schlechte Zeile den gesamten Import abbrechen.
- **Unerwarteter/technischer Fehler auf Job-Ebene** (z. B. defekte
  Dateistruktur, die den Parser selbst zum Absturz bringt, DB-Verbindung
  weg, Programmfehler): bricht den GESAMTEN Job ab, Status wird `FAILED`,
  und alles, was dieser Job bereits importiert hat, wird zurückgerollt.

Falls du stattdessen willst, dass wirklich JEDE einzelne fehlerhafte Zeile
(auch die jetzt schon behandelten Validierungsfehler) den kompletten Import
abbricht und zurückrollt, sag Bescheid – das würde diesen Fix-Prompt an
einer zentralen Stelle anders aufziehen.

### Datenbank & Repository für den Rollback

Neue Flyway-Migration (nächste freie Versionsnummer, z. B.
`V20260720__add_food_import_job_id.sql`):

```sql
ALTER TABLE tab_calory_food ADD COLUMN import_job_id uuid;
CREATE INDEX idx_tab_calory_food_import_job_id ON tab_calory_food (import_job_id);
```

- `FoodPersistence`: neues Feld `importJobId` (`@Column(name =
  "import_job_id")`) mit Getter UND Setter (bewusst als Setter, nicht über
  den Konstruktor – so bleibt `FoodCsvRowMapper.map(CSVRecord)` unverändert
  und alle bestehenden `FoodCsvRowMapperUnitTest`-Fälle funktionieren weiter).
  In `FoodCsvImportUsecase.mapRow()` nach dem Mapping setzen:
  `mapped.ifPresent(f -> f.setImportJobId(status.getJobId()))`.
- `FoodRepository`: Bulk-Delete statt Spring Datas Standard-`deleteBy...`
  (das würde jede Zeile erst einzeln laden), aus Performance-Gründen als
  eigene `@Modifying`-Query:

  ```java
  @Modifying
  @Query("delete from FoodPersistence f where f.importJobId = :jobId")
  void deleteByImportJobId(@Param("jobId") UUID jobId);
  ```

## 5. Fix 4: Popup nicht per Backdrop-Klick schließbar während des Imports

In `frontend/src/app/pages/food/food.component.ts`, `onCsvFileSelected()`:

```ts
this.dialog.open(CsvImportProgressDialogComponent, { disableClose: true })
```

`disableClose` blockiert Backdrop-Klick und Escape-Taste; der bereits
vorhandene "Schließen"-Button in `csv-import-progress-dialog.component.html`
(Fälle `completed` und `failed`) ruft weiterhin `dialogRef.close()`
programmatisch auf, was von `disableClose` nicht betroffen ist – die
Anforderung "während des Imports nicht schließbar, danach schon" ist damit
mit einer einzigen Zeile erfüllt.

Optional (nice-to-have, falls gewünscht): Damit auch der Backdrop-Klick nach
Abschluss wieder funktioniert (statt nur der explizite Button), in
`CsvImportProgressDialogComponent` per `effect()` auf
`csvImportService.phase()` reagieren und `this.dialogRef.disableClose =
false` setzen, sobald die Phase `completed` oder `failed` erreicht ist.

## 6. QA-Vorgaben (bestehender Rahmen)

- **Unit-Tests** (`FoodCsvRowMapperUnitTest`): neue Fälle für Kalorienwert
  über 900 (Zeile verworfen), Kalorienwert genau 900/0 (Grenzwerte, gültig),
  negativer Kalorienwert (verworfen), Makronährstoff über 100 (nur dieses
  Feld `null`, Rest der Zeile bleibt gültig).
- **Unit-Tests** (`FoodCsvImportUsecaseUnitTest`): Fall simulieren, in dem
  `foodRepository.saveAll()` eine `DataAccessException` wirft, die KEINE
  `DataIntegrityViolationException` ist (z. B. gemockte generische
  `DataAccessException`) → verifizieren, dass `persistOneByOne` trotzdem
  greift. Neuer Fall: `processRecords()` wirft eine unerwartete
  `RuntimeException` → verifizieren, dass `foodRepository.deleteByImportJobId()`
  aufgerufen und `status.fail()` gesetzt wird (nicht `status.complete()`).
- **Integrationstest**: `deleteByImportJobId` mit zwei unterschiedlichen
  Job-IDs befüllen, verifizieren, dass nur die Zeilen des angegebenen Jobs
  gelöscht werden.
- **Frontend**: `food.component.spec.ts` (ggf. neu anlegen, falls nicht
  vorhanden) – verifizieren, dass `dialog.open` mit `{ disableClose: true }`
  aufgerufen wird.
- `mvn clean verify` und `npm run lint` müssen fehlerfrei durchlaufen.

## 7. Definition of Done

- Ein erneuter Import der Datei, die vorher den Absturz auslöste, läuft bis
  `COMPLETED` durch; die betroffenen Zeilen (Kalorienwert > 900) landen in
  `skippedCount`, nicht in der Datenbank.
- Ein absichtlich provozierter, echter Fehler (z. B. defekte CSV-Struktur)
  führt zu `FAILED` UND dazu, dass zuvor in diesem Job importierte Zeilen aus
  `tab_calory_food` wieder entfernt sind.
- Das Import-Popup lässt sich während `uploading`/`processing` nicht per
  Backdrop-Klick oder Escape schließen, wohl aber über den
  "Schließen"-Button nach `completed`/`failed`.
```
