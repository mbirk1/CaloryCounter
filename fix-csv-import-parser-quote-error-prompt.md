# Prompt: CSV-Parser-Absturz bei eingebetteten Anführungszeichen beheben

> Zum direkten Copy-Paste für einen Coding-Agenten. Folgefehler zum bereits
> behobenen `numeric field overflow`-Bug im CSV-Import-Feature
> (`usecase.food.importer.FoodCsvImportUsecase`).

---

```
Du bist Senior Backend-Entwickler an "Calory Counter". Der CSV-Import
schlägt jetzt sauber fehl (kein Hängenbleiben mehr, korrektes Logging – der
vorherige Resilienz-Fix greift also wie vorgesehen), aber der eigentliche
Import bricht bei einer bestimmten Zeile mit einem Parser-Fehler ab. Behebe
die Ursache. Halte dich an die bestehenden QA-Vorgaben (Checkstyle,
ArchUnit, JaCoCo ≥90 %/90 %/80 %, bestehende Testkonventionen).

## 1. Einordnung: Das ist kein Rückfall des letzten Fixes

Log-Auszug:

```
ERROR ... FoodCsvImportUsecase : CSV-Import-Job 280669b2-... unerwartet fehlgeschlagen
java.io.UncheckedIOException: java.io.IOException: Invalid char between
encapsulated token and delimiter at line: 71.371, position: 191.079.710
	at org.apache.commons.csv.Lexer.parseEncapsulatedToken(Lexer.java:363)
	...
	at de.birk.calory.usecase.food.importer.FoodCsvImportUsecase.processRecords(...)
```

`UncheckedIOException` ist eine `RuntimeException` und wurde vom zuvor
eingebauten `catch (RuntimeException e)` in `importFromFile()` korrekt
abgefangen, sauber geloggt (mit Job-ID und Stacktrace) und hat den Job auf
`FAILED` gesetzt statt ihn – wie vor dem letzten Fix – auf ewig in `RUNNING`
hängen zu lassen. Dieser Teil funktioniert also wie gewünscht. Es geht hier
um ein eigenständiges, vorgelagertes Problem: Der CSV-Parser selbst kommt an
einer bestimmten Stelle der Datei (Zeile ≈ 71.371, Zeichenposition ≈ 191 Mio.)
nicht klar.

## 2. Fehleranalyse: falsche Quote-Konfiguration für eine rohe TSV-Datei

`FoodCsvImportUsecase.importFromFile()` baut den Parser aktuell so auf:

```java
CSVFormat.DEFAULT
    .withDelimiter('\t')
    .withFirstRecordAsHeader()
    .withIgnoreHeaderCase()
    .withTrim()
    .parse(reader)
```

`CSVFormat.DEFAULT` aktiviert RFC4180-Anführungszeichen-Semantik (Quote-
Zeichen `"`): ein `"` leitet ein "eingekapseltes" Feld ein, in dem der
Delimiter enthalten sein darf, und ein weiteres `"` beendet es wieder. Der
OpenFoodFacts-Export ist aber eine **rohe TSV-Datei ohne CSV-Escaping** –
Anführungszeichen, die in Produktnamen, Zutatenlisten o. Ä. vorkommen (z. B.
Zoll-Angaben wie `12"`, wörtliche Zitate, French Guillemets, die falsch
kodiert wurden), sind dort reiner Text und nicht als CSV-Feldbegrenzer
gemeint. Sobald der Parser auf ein einzelnes, "unpassend" platziertes `"`
trifft, interpretiert er es fälschlich als Beginn/Ende eines eingekapselten
Felds – bei der nächsten Unstimmigkeit (ein Zeichen zwischen vermeintlichem
Zitat-Ende und dem nächsten Tab) bricht der Lexer mit genau diesem Fehler ab.

Das erklärt auch, warum sich das nicht wie die anderen Datenqualitätsfälle
(fehlender Name/fehlende Kalorien/fehlender Hersteller) einfach pro Zeile
überspringen lässt: Der Fehler passiert bereits beim **Tokenisieren** (in
`Lexer.parseEncapsulatedToken`, aufgerufen aus `CSVParser$CSVRecordIterator
.hasNext()`), also BEVOR überhaupt ein `CSVRecord`-Objekt existiert, um das
sich pro Zeile try/catchen ließe. Der interne Zustand des Parsers ist nach
diesem Fehler nicht mehr sauber weiterlesbar – die einzig robuste Lösung ist,
die Quote-Interpretation für diese Datei von vornherein zu deaktivieren,
statt zu versuchen, den Parser-Fehler nachträglich abzufangen.

## 3. Fix: Quote-Zeichen deaktivieren

```java
CSVFormat.DEFAULT
    .withDelimiter('\t')
    .withQuote(null)
    .withFirstRecordAsHeader()
    .withIgnoreHeaderCase()
    .withTrim()
    .parse(reader)
```

`withQuote(null)` schaltet die Encapsulation komplett ab – `"` wird dann wie
jedes andere Zeichen als Teil des Feldinhalts behandelt, genau wie es für
eine TSV-Datei ohne Escaping korrekt ist.

**Abzuwägender Kompromiss (bewusst in Kauf genommen, bitte im Code kurz
kommentieren):** Ohne Quote-Handling kann ein Feld, das einen echten
eingebetteten Zeilenumbruch enthält (z. B. in `ingredients_text`), das
Parsen einer Zeile vorzeitig beenden und den Rest fälschlich als neue Zeile
interpretieren. Das ist ein deutlich selteneres, weniger schwerwiegendes
Problem als der aktuelle Totalabsturz (führt bestenfalls zu ein paar
zusätzlich übersprungenen/fehlerhaften Zeilen statt zum Abbruch des
gesamten Imports) und für einen TSV-Export, bei dem Tabs im Feldinhalt laut
gängiger Praxis beim Export bereits entfernt werden, die pragmatisch
richtige Wahl.

## 4. Verifikation

- Lokal gegen die tatsächliche, große Originaldatei erneut importieren und
  bestätigen, dass der Import jetzt vollständig durchläuft (kein weiterer
  `IOException`/`UncheckedIOException` aus dem Lexer mehr).
- Neuer Testfall in `FoodCsvRowMapperUnitTest` bzw. einem parser-nahen Test:
  eine CSV-Zeile mit einem eingebetteten `"` mitten im `product_name` (z. B.
  `12" Pizza Margherita`), verifizieren, dass die Zeile jetzt **korrekt
  geparst** wird (nicht übersprungen – das Zeichen ist ja gültiger Text,
  kein Datenqualitätsproblem).
- Falls vorhanden, bestehenden `FoodCsvImportUsecaseUnitTest`-Fixtures um
  eine entsprechende Zeile ergänzen, damit dieser Fall dauerhaft
  regressionsgetestet ist.

## 5. QA-Vorgaben (bestehender Rahmen)

- Checkstyle (Google-Style, Javadoc, `@author`) und ArchUnit unverändert
  einhalten – reine Verhaltensänderung innerhalb von
  `usecase.food.importer.FoodCsvImportUsecase`, keine neuen Klassen nötig.
- JaCoCo-Coverage-Schwellen (≥90 %/90 %/80 %) weiterhin einhalten, neuer
  Testfall aus Abschnitt 4 zählt mit ein.
- `mvn clean verify` muss fehlerfrei durchlaufen.

## 6. Definition of Done

- Ein erneuter Import der vollständigen OpenFoodFacts-Datei läuft ohne
  Parser-Absturz bis `COMPLETED` durch (oder scheitert nur noch an einem
  tatsächlich neuen, andersartigen Problem, das gesondert zu melden wäre).
- Zeilen mit eingebetteten Anführungszeichen im Text werden korrekt
  importiert statt den gesamten Job zum Absturz zu bringen.
- Neuer Regressionstest für diesen Fall ist vorhanden und grün.
```
