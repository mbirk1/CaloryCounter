# Prompt: OpenFoodFacts-CSV-Import, Schema-Erweiterung, Pagination & Tabellen-Aktionen (Calory Counter)

> Zum direkten Copy-Paste für einen Coding-Agenten. Baut auf dem bestehenden
> hexagonalen Backend (`de.birk.calory`) und dem Angular-19-Frontend auf.

---

```
Du bist Senior Full-Stack-Entwickler an "Calory Counter" (Spring Boot 3.3.0 /
Java 22 Backend, hexagonale Architektur; Angular 19 Frontend). Baue eine
CSV-Importfunktion für Lebensmitteldaten aus dem OpenFoodFacts-Datenexport,
erweitere dafür die Datenbanktabelle sinnvoll, ergänze Pagination (Backend +
Frontend) und überarbeite die Lebensmittel-Tabelle im Frontend um eine klar
definierte Aktionen-Spalte. Halte dich strikt an die bestehenden Konventionen
(Checkstyle Google-Style, ArchUnit-Layer-Regeln, JaCoCo-Coverage ≥90 %/90 %/
80 %, Angular-Codestil ohne Semikolon/Single-Quotes).

## 1. Ausgangslage

Aktuelles Schema (`tab_calory_food`): `id, name, calory_count, grams`.
`FoodRepository extends JpaRepository<FoodPersistence, UUID>` liefert aktuell
ungepaginiert `findAll()`, `FoodRestController.getAllFoods()` gibt eine
komplette `List<FoodDetailsDto>` zurück, das Frontend (`FoodStore`,
`food-table.component`) rendert diese Liste komplett ohne Paging. Das MUSS
sich ändern, weil die Zieldatenquelle sehr groß ist (siehe Abschnitt 2).

## 2. Datenquelle: Fakten zur bereitgestellten CSV

Die vom Nutzer bereitgestellte Datei ist der volle OpenFoodFacts-Weltexport
(`en.openfoodfacts.org.products.csv.gz`). Wichtige, verifizierte Eigenschaften:

- **Tab-separiert, nicht komma-separiert** – trotz `.csv`-Endung. Der Parser
  muss mit Delimiter `\t` konfiguriert werden.
- **Gzip-komprimiert**, 1,2 GB komprimiert – vermutlich mehrere Millionen
  Zeilen (ein vollständiges Auszählen brach nach 40 Sekunden ergebnislos ab).
  Die Datei sollte NICHT vollständig in den Speicher geladen werden.
- **211 Spalten**, u. a. `code` (Barcode), `product_name`, `brands`/
  `brands_en`, `categories_en`, `energy-kcal_100g`, `energy_100g` (kJ),
  `fat_100g`, `saturated-fat_100g`, `carbohydrates_100g`, `sugars_100g`,
  `fiber_100g`, `proteins_100g`, `salt_100g`, `sodium_100g`, `image_url`,
  `completeness`.
- Alle Nährwertspalten sind bereits **auf 100 g normiert** (`_100g`-Suffix) –
  das passt gut zum bestehenden `Food`-Modell (`calory` je `grams`), wenn man
  `grams` beim Import fix auf `100` setzt.
- **Datenqualität schwankt stark**: viele Zeilen haben keinen Produktnamen,
  keine Nährwerte oder unrealistisch hochpräzise Fließkommazahlen (z. B.
  `580.645161290323`). Ein Rohimport ohne Filterung ist nicht sinnvoll.

## 3. Entscheidung: unbegrenzter, asynchroner Streaming-Import + Pagination

Der Import soll **keine künstliche Zeilenobergrenze** haben, dafür aber einen
strikten Qualitätsfilter (siehe 4). Da selbst nach Filterung mit
hunderttausenden Zeilen zu rechnen ist, MUSS der Import:

- als **Stream** verarbeitet werden (kein Laden der kompletten Datei in den
  Speicher),
- in **Batches** (empfohlen: 1.000 Datensätze) über `saveAll()` persistiert
  werden, jeweils in einer eigenen, kurzen Transaktion (NICHT eine einzige
  Transaktion über den gesamten Import – der Hibernate-Session-Cache würde
  sonst unbegrenzt wachsen),
- **asynchron** laufen: Der HTTP-Request eines synchronen Imports würde bei
  dieser Datenmenge Minuten bis Stunden dauern und jeden vernünftigen
  Timeout sprengen.

Weil eine unbegrenzte Datenmenge jetzt regulär in der Tabelle landen kann,
wird zusätzlich **Pagination** für den Food-Endpunkt und die Frontend-Tabelle
eingeführt – beides gehört zusammen und ist Teil dieses Tickets.

## 4. Datenqualitätsfilter (Pflichtfelder für den Import)

Eine CSV-Zeile wird **nur** importiert, wenn alle drei Pflichtfelder gültig
befüllt sind:

1. `product_name` – nicht leer/blank.
2. Kalorien – `energy-kcal_100g` vorhanden UND numerisch parsebar; falls
   leer, ersatzweise `energy_100g` (kJ) vorhanden und in kcal umrechnen
   (`kcal = kJ / 4.184`). Ist auch das leer/unparsebar → Zeile verwerfen.
3. `brands` (Hersteller) – nicht leer/blank. Bei mehreren kommagetrennten
   Marken wird die erste verwendet.

Alle anderen neuen Felder (siehe Spalten-Mapping) sind optional – fehlende
Werte werden als `NULL` gespeichert, führen aber NICHT zum Verwerfen der
Zeile. Jede verworfene Zeile wird im Job-Status mitgezählt (nicht einzeln
geloggt, um bei Millionen Zeilen keine Logs zu fluten).

Numerische Werte werden vor dem Speichern auf 2 Nachkommastellen gerundet
(`BigDecimal.setScale(2, RoundingMode.HALF_UP)`), passend zur bestehenden
Spaltenpräzision `numeric(6,2)`.

## 5. Spalten-Mapping (CSV → Datenbank)

| OpenFoodFacts-Spalte | Neue/bestehende DB-Spalte | Pflicht |
|---|---|---|
| `code` | `external_id` (unique) | nein (aber empfohlen für Re-Import-Idempotenz) |
| `product_name` | `name` (bestehend) | **ja** |
| `energy-kcal_100g` (Fallback `energy_100g`/4.184) | `calory_count` (bestehend) | **ja** |
| – (fix `100`) | `grams` (bestehend) | **ja** |
| `brands` (erster Eintrag) | `brand` | **ja** |
| `categories_en` (erster Eintrag) | `category` | nein |
| `fat_100g` | `fat` | nein |
| `saturated-fat_100g` | `saturated_fat` | nein |
| `carbohydrates_100g` | `carbohydrates` | nein |
| `sugars_100g` | `sugar` | nein |
| `fiber_100g` | `fiber` | nein |
| `proteins_100g` | `protein` | nein |
| `salt_100g` | `salt` | nein |
| `sodium_100g` | `sodium` | nein |
| `image_url` | `image_url` | nein |
| – (Konstante) | `source = 'OPENFOODFACTS'` | – |

Manuell über `POST /api/food` angelegte Lebensmittel erhalten weiterhin
`source = 'MANUAL'` (Default) und `external_id = NULL`.

## 6. Datenbank-Migration

Neue Flyway-Migration (nächste freie Versionsnummer nach der bestehenden
Auth-Migration, z. B. `V20260719__extend_food_nutrition_and_import_source.sql`):

```sql
ALTER TABLE tab_calory_food ADD COLUMN source varchar(50) NOT NULL DEFAULT 'MANUAL';
ALTER TABLE tab_calory_food ADD COLUMN external_id varchar(64);
ALTER TABLE tab_calory_food ADD COLUMN brand varchar(255);
ALTER TABLE tab_calory_food ADD COLUMN category varchar(255);
ALTER TABLE tab_calory_food ADD COLUMN fat numeric(6,2);
ALTER TABLE tab_calory_food ADD COLUMN saturated_fat numeric(6,2);
ALTER TABLE tab_calory_food ADD COLUMN carbohydrates numeric(6,2);
ALTER TABLE tab_calory_food ADD COLUMN sugar numeric(6,2);
ALTER TABLE tab_calory_food ADD COLUMN fiber numeric(6,2);
ALTER TABLE tab_calory_food ADD COLUMN protein numeric(6,2);
ALTER TABLE tab_calory_food ADD COLUMN salt numeric(6,2);
ALTER TABLE tab_calory_food ADD COLUMN sodium numeric(6,2);
ALTER TABLE tab_calory_food ADD COLUMN image_url varchar(500);
ALTER TABLE tab_calory_food ADD CONSTRAINT ux_tab_calory_food_external_id UNIQUE (external_id);
```

Bestehende Zeilen (manuell angelegt) bleiben durch die `NOT NULL DEFAULT`-
Klausel bei `source` valide, alle neuen Spalten sind nullable → bricht
weder bestehende Daten noch bestehende Tests (`FoodUnitTest`,
`FoodDtoConverterUnitTest` etc.), da `Food.validate()` unverändert bleibt
und nur `name`/`calory`/`grams` prüft.

## 7. Backend-Architektur

### 7.1 Domain, Persistence, DTOs

- `domain.food.Food`: um die elf neuen optionalen Felder erweitern
  (`brand, category, fat, saturatedFat, carbohydrates, sugar, fiber,
  protein, salt, sodium, imageUrl, source, externalId`). `validate()`
  bleibt unverändert (nur Pflichtfelder aus dem Bestand).
- `adapter.secondary.model.FoodPersistence`: gleiche Felder als `@Column`.
- `adapter.secondary.FoodRepository`: `Optional<FoodPersistence>
  findByExternalId(String externalId)` ergänzen.
- `adapter.primary.model.FoodDetailsDto`: um dieselben Felder erweitern,
  damit die neue Detailansicht im Frontend alle Werte bekommt.
- `usecase.food.converter.*`: `FoodPersistenceConverter` und
  `FoodDetailsDtoConverter` entsprechend erweitern (bestehende Tests
  anpassen, nicht brechen).

### 7.2 CSV-Import-Usecase

- Neue Maven-Dependency: `org.apache.commons:commons-csv` (streaming-
  fähiger Parser mit konfigurierbarem Delimiter).
- `usecase.food.importer.FoodCsvImportUsecase` (o. ä. Package):
  - Nimmt einen `InputStream` entgegen, erkennt Gzip (Magic Bytes oder
    Dateiname `.gz`) und wrappt ihn bei Bedarf in `GZIPInputStream` –
    der Nutzer muss die Datei NICHT manuell entpacken.
  - Parst mit `CSVFormat.DEFAULT.withDelimiter('\t').withFirstRecordAsHeader()`
    per Iterator (`CSVParser.iterator()`), NICHT `getRecords()` (würde alles
    in den Speicher laden).
  - Wendet den Qualitätsfilter aus Abschnitt 4 pro Zeile an.
  - Mappt gültige Zeilen auf `FoodPersistence` (neue Klasse
    `usecase.food.importer.FoodCsvRowMapper`, reine Mapping-Logik, gut
    isoliert unit-testbar).
  - Persistiert in Batches von 1.000 über `foodRepository.saveAll(batch)`,
    jede Batch-Methode eigenständig `@Transactional`.
  - Re-Import-Sicherheit: Da ein Pre-Select pro Zeile bei dieser Datenmenge
    zu langsam wäre, wird **optimistisch** batch-inserted. Schlägt ein
    Batch wegen des Unique-Constraints auf `external_id` fehl (Datei wurde
    bereits importiert), wird dieser Batch satzweise erneut versucht und
    Duplikate werden übersprungen (gezählt, nicht einzeln geloggt).
  - Schreibt laufend Fortschritt in eine `ImportJobRegistry` (siehe 7.3).

### 7.3 Asynchrone Ausführung & Job-Status

- `usecase.food.importer.ImportJobRegistry`: `@Component`, hält
  `ConcurrentHashMap<UUID, FoodImportJobStatus>` im Speicher (für eine
  Single-Instance-Anwendung ausreichend; DB-Persistenz der Job-Historie ist
  eine mögliche spätere Erweiterung, kein Teil dieses Tickets).
- `FoodImportJobStatus`: Status (`RUNNING, COMPLETED, FAILED`),
  `processedRows, importedCount, skippedCount, errorCount, startedAt,
  finishedAt`.
- `FoodCsvImportUsecase` läuft über `@Async` (dafür `@EnableAsync` in einer
  Konfigurationsklasse, z. B. Ergänzung in `ApplicationConfig`, mit
  dediziertem `Executor`-Bean statt dem Default-`SimpleAsyncTaskExecutor`).

### 7.4 REST-Endpunkte

- `POST /api/food/import` (multipart/form-data, Feldname `file`): startet
  den Import asynchron, antwortet sofort mit `202 Accepted` und
  `{ jobId }`.
- `GET /api/food/import/{jobId}`: liefert den aktuellen `FoodImportJobStatus`
  als DTO.
- `GET /api/food?page=0&size=20&sort=name`: **ersetzt** die bisherige
  Rückgabe (komplette Liste) durch eine paginierte Antwort (siehe 8). Dies
  ist ein Breaking Change des bestehenden Contracts – Frontend muss
  entsprechend angepasst werden (siehe 10.3).

### 7.5 Konfiguration

In `application.properties`/`application-dev.properties` ergänzen:

```
spring.servlet.multipart.max-file-size=2GB
spring.servlet.multipart.max-request-size=2GB
spring.jpa.properties.hibernate.jdbc.batch_size=1000
spring.jpa.properties.hibernate.order_inserts=true
```

## 8. Pagination (Backend)

- `FoodRepository` erbt `findAll(Pageable)` bereits von `JpaRepository` –
  keine Codeänderung nötig.
- `usecase.food.FindFoodUsecase`: neue Methode `findAllFoods(int page, int
  size)`, baut `PageRequest.of(page, size, Sort.by("name"))`, konvertiert
  `Page<FoodPersistence>` zu `PageResponseDto<FoodDetailsDto>`.
- Neues generisches DTO `adapter.primary.model.PageResponseDto<T>`:
  `{ content: List<T>, page, size, totalElements, totalPages, last }`
  (bewusst ein eigenes DTO statt Spring's `Page<T>` direkt zu
  serialisieren – konsistenter, kontrollierter API-Vertrag).
- `FoodRestController.getAllFoods(@RequestParam(defaultValue="0") int page,
  @RequestParam(defaultValue="20") int size)` liefert `PageResponseDto<
  FoodDetailsDto>`.

## 9. QA-Vorgaben (bestehender Rahmen, MUSS eingehalten werden)

- **ArchUnit**: Import-Klassen liegen in `usecase.food.importer` (Usecase-
  Layer, darf beliebige Bibliotheken wie Commons-CSV nutzen), Endpunkte in
  `adapter.primary`, Persistenz in `adapter.secondary` – bestehende
  Layer-Regeln bleiben unverletzt. `mvn test -Dtest=ArchUnitTest` muss grün
  bleiben.
- **Checkstyle**: Google-Style, Javadoc mit `@author`, 2-Space-Einrückung,
  max. 100 Zeichen/Zeile, wie im gesamten Projekt.
- **JaCoCo**: ≥90 % Line/Method, ≥80 % Branch – auch für
  `usecase.food.importer.*`.
- **Unit-Tests** (JUnit 5 + AssertJ, AAA-Kommentarstil):
  - `FoodCsvRowMapperUnitTest`: gültige Zeile, fehlender Name, fehlende
    Kalorien, fehlender Hersteller (jeweils → verworfen), kJ-Fallback für
    Kalorien, Rundung auf 2 Nachkommastellen.
  - `FoodCsvImportUsecaseUnitTest`: mit gemocktem `FoodRepository` –
    Batch-Aufteilung, Zählung von importiert/übersprungen/Fehler, Verhalten
    bei Unique-Constraint-Konflikt.
- **Integrationstest**: `FoodImportRestControllerTest` (`@IntegrationTest`,
  `AbstractTestBase`, MockMvc `.multipart()`) mit einer kleinen
  Test-Fixture-Datei `src/test/resources/csv/sample-food-import.csv`
  (10–20 Zeilen, tab-separiert, Header + Mix aus gültigen/ungültigen
  Zeilen), analog zum bestehenden `http-bodies`-Muster. Test wartet auf
  `COMPLETED`-Status und prüft DB-Inhalt über `FoodRepository`.
- **Pagination-Tests**: `FoodRestControllerTest` um Fälle für
  `page`/`size`-Parameter und Antwortstruktur (`totalElements`,
  `totalPages`) erweitern.

## 10. Frontend

### 10.1 Aktionen-Spalte in `food-table` (Kernanforderung)

`food-table.component.html` bekommt eine Spalte "Aktionen" mit **genau drei**
Icons pro Zeile:

1. **Detailansicht** (Augen-Icon `faEye`) – öffnet einen neuen, read-only
   `app-food-detail-dialog` (CDK Dialog, analog `AddFoodDialogComponent`)
   mit allen Feldern des `FoodModel` (Name, Kalorien, Gramm, Marke,
   Kategorie, Fett, gesättigte Fettsäuren, Kohlenhydrate, Zucker,
   Ballaststoffe, Eiweiß, Salz, Natrium, Bild falls vorhanden). Kein
   zusätzlicher HTTP-Call nötig – die Daten liegen bereits im `FoodStore`.
2. **Löschen** (`faTrash`) – bestehende Logik aus `food-table.component.ts`
   (`delete(id)` → `foodStore.delete(id)`) beibehalten, inkl. der
   `isInARecipe`-Sperre (Icon disabled/grau, wenn das Food Teil eines
   Rezepts ist).
3. **Platzhalter** (`faPen`, disabled, `aria-label="Bald verfügbar"`, Tooltip
   "Bald verfügbar", ohne Klick-Handler) – bewusst als "blindes" Icon für
   eine spätere Bearbeiten-Funktion reserviert.

Icon-Set: **Font Awesome** (bereits npm-Dependency) statt der aktuell in
`food-table` genutzten Material-Symbols-Klassen – vereinheitlicht das Icon-
System endgültig (offener Punkt aus dem vorherigen Frontend-Ticket).

Aufräumen: `food.component.ts` enthält tote Stub-Methoden `editFood()` und
`deleteFood()`, die nirgends aufgerufen werden (die Tabelle regelt Löschen
bereits selbst) – entfernen.

### 10.2 Detail-Dialog

- Neue Komponente `components/dialogs/food-detail-dialog/`, analog zu
  `add-food-dialog`, aber rein lesend (kein `FormGroup`, keine
  `app-button[type=submit]`), Layout als zweispaltiges Grid wie im
  bestehenden Formular-Muster.
- `FoodModel` um die neuen optionalen Felder erweitern (`brand?, category?,
  fat?, saturatedFat?, carbohydrates?, sugar?, fiber?, protein?, salt?,
  sodium?, imageUrl?, source?, externalId?`).

### 10.3 Pagination (Frontend)

- Neues Model `models/PageResponse.ts`: `{ content: T[], page, size,
  totalElements, totalPages, last }`.
- `FoodStore` anpassen: `resource()`-Loader ruft `GET /api/food?page=..&
  size=..` auf, hält zusätzlich Signals `currentPage`, `totalPages`,
  `totalElements`; `foods` liest `.content` aus der paginierten Antwort;
  neue Methoden `nextPage()`, `previousPage()`, `goToPage(n)`.
- Neue wiederverwendbare Komponente `app-pagination` (Prev/Next-Buttons +
  Seitenanzeige "Seite X von Y"), unterhalb der Tabelle in
  `food-table.component.html` eingebunden. Bewusst generisch gebaut, damit
  `recipe-table` sie später mitnutzen kann.

### 10.4 Import-Trigger im Frontend (damit die Funktion nutzbar ist)

- In `food.component.html`: Button "CSV importieren" neben "Lebensmittel
  hinzufügen", öffnet einen einfachen Datei-Upload-Dialog.
- Nach Start: Polling von `GET /api/food/import/{jobId}` (z. B. alle 2s)
  mit Fortschrittsanzeige (`processedRows`, `importedCount`,
  `skippedCount`); bei `COMPLETED` Erfolgsmeldung + `foodResource.reload()`.
- Kein RBAC/Admin-Schutz vorhanden (Rollenmodell existiert noch nicht) –
  Endpoint ist für jeden authentifizierten User erreichbar; als
  `//TODO Marius` im Controller vermerken, sobald Rollen existieren auf
  Admin einschränken.

## 11. Out of Scope

- DB-persistente Job-Historie (aktuell In-Memory, reicht für Single-
  Instance-Betrieb).
- RBAC/Admin-Rolle für den Import-Endpoint (Rollenmodell existiert noch
  nicht, siehe TODO oben).
- Nutriscore/Nova-Group & weitere OFF-Metadaten – bewusst nicht
  übernommen, um den Scope nicht unnötig zu vergrößern.
- Suche/Filter über Marke/Kategorie im Frontend (Folge-Ticket, Pagination
  ist hier die Grundlage dafür).

## 12. Definition of Done

- `mvn clean verify` (Checkstyle + Unit-/Integrationstests + JaCoCo-Check)
  läuft fehlerfrei durch.
- Import einer kleinen Testdatei über `POST /api/food/import` liefert nach
  Abschluss korrekte Zähler (importiert/übersprungen wegen fehlendem
  Namen/Kalorien/Hersteller).
- `GET /api/food?page=0&size=20` liefert korrekt paginierte Antwort;
  Frontend-Tabelle zeigt Pagination-Steuerung und navigiert korrekt.
- Aktionen-Spalte zeigt exakt drei Icons mit dem beschriebenen Verhalten;
  `npm run lint` und `npm run format` fehlerfrei.
```
