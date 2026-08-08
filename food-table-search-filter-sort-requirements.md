# Anforderungsdokument: Suche, Diät-Filter & Sortierung für die Lebensmittel-Tabelle

> Zum direkten Copy-Paste für einen Coding-Agenten. Bezieht sich auf die
> bestehende, bereits paginierte Lebensmittel-Tabelle
> (`FoodTableComponent`/`FoodStore`/`FoodRestController.getAllFoods`).

---

```
Du arbeitest an "Calory Counter" (Spring Boot / hexagonale Architektur
Backend, Angular 19 Frontend). Die folgenden Anforderungen betreffen
ausschließlich die Lebensmittel-Übersichtsseite (`/food`), auf der bereits
eine servergepaginierte Tabelle existiert. Setze sie unter Einhaltung der
bestehenden QA-Vorgaben um (Checkstyle, ArchUnit-Layer-Regeln, JaCoCo
≥90 %/90 %/80 %, bestehende Test- und Codestil-Konventionen).

## 1. Ausgangslage

- `FoodRestController.getAllFoods(page, size)` liefert aktuell eine feste,
  serverseitig nach `name` sortierte Seite (`FindFoodUsecase.findAllFoods`,
  hartkodiert `Sort.by("name")`). Es gibt weder Such- noch Filter- noch
  wählbare Sortier-Parameter.
- `FoodTableComponent`/`food-table.component.html` zeigt eine statische
  Spaltenüberschrift (Name, Kalorien, Gramm, Aktionen) ohne Klick-Interaktion.
- `tab_calory_food` hat aktuell u. a. die Spalten `name, calory_count, grams,
  brand, category, ...`. `category` enthält bei importierten Lebensmitteln
  freien OpenFoodFacts-Kategorietext (z. B. "Plant-based foods and
  beverages") – **keine** verlässliche Vegan-/Vegetarisch-Kennzeichnung.
  Manuell angelegte Lebensmittel (`AddFoodDialogComponent`) haben aktuell gar
  keine Kategorie-/Diät-Angabe.
- OpenFoodFacts stellt eine eigene Spalte `ingredients_analysis_tags` mit
  Werten wie `en:vegan`, `en:non-vegan`, `en:vegetarian`,
  `en:non-vegetarian`, `en:maybe-vegan`, `en:vegan-status-unknown` bereit,
  die beim bisherigen Import NICHT übernommen wurde.

## 2. Ziel

Auf der Lebensmittel-Seite sollen Nutzer:innen

1. per Suchleiste nach Lebensmitteln suchen können,
2. nach Diät-Eigenschaft (vegan/vegetarisch/nicht vegetarisch) filtern
   können,
3. die Tabelle durch Klick auf die Spaltenüberschriften sortieren können,

jeweils serverseitig umgesetzt (die Tabelle ist paginiert – Suche/Filter/
Sortierung dürfen sich nur auf die jeweils aktuell geladene Seite auswirken,
nicht auf im Client bereits geladene, aber unvollständige Daten).

## 3. Funktionale Anforderungen

### FR-1: Suchleiste

Oberhalb der Tabelle (auf `/food`, neben den bestehenden Buttons
"Lebensmittel hinzufügen"/"CSV importieren") wird ein Textfeld ergänzt.

- Die Eingabe filtert serverseitig auf den Lebensmittelnamen
  (Case-insensitive, Teilstring-Suche, z. B. "milch" findet "Vollmilch").
- Eingabe wird debounced (empfohlen: 300 ms), es wird nicht bei jedem
  Tastendruck ein Request abgesetzt.
- Eine neue Sucheingabe setzt die aktuelle Seite auf Seite 1 zurück (sonst
  könnte der Nutzer auf einer nun leeren/ungültigen Seite landen).
- Leeres Suchfeld zeigt wieder alle Lebensmittel (unter Berücksichtigung
  eines evtl. aktiven Diät-Filters, siehe FR-2).

**Akzeptanzkriterien**

- Gegeben die Tabelle zeigt Lebensmittel, wenn ich "Apfel" in die Suchleiste
  eingebe, dann zeigt die Tabelle nur noch Lebensmittel, deren Name "Apfel"
  enthält (unabhängig von Groß-/Kleinschreibung), inkl. korrekt
  aktualisierter Gesamtseitenzahl.
- Gegeben ich habe eine Suche mit mehreren Ergebnisseiten, wenn ich den
  Suchbegriff ändere, dann springt die Ansicht zurück auf Seite 1.
- Gegeben ich leere die Suchleiste, wenn das Feld leer ist, dann werden
  wieder alle (ggf. durch den Diät-Filter eingeschränkten) Lebensmittel
  angezeigt.

### FR-2: Diät-Filter (vegan/vegetarisch/nicht vegetarisch)

**Datenmodell-Erweiterung (Voraussetzung):**

- Neue Spalte `tab_calory_food.diet` (String/Enum-artig:
  `VEGAN, VEGETARIAN, NON_VEGETARIAN, UNKNOWN`), Default `UNKNOWN`.
- Herleitung beim (künftigen) CSV-Import aus `ingredients_analysis_tags`:
  - enthält `en:vegan` → `VEGAN`
  - sonst enthält `en:vegetarian` → `VEGETARIAN`
  - sonst enthält `en:non-vegetarian` → `NON_VEGETARIAN`
  - alles andere (fehlend, `en:maybe-vegan`, `en:maybe-vegetarian`,
    `en:vegan-status-unknown` etc.) → `UNKNOWN` (bewusst konservativ: eine
    Unsicherheit wird nicht als "vegan" ausgewiesen).
- **Bestandsdaten-Backfill nötig:** Die bereits importierten Lebensmittel
  wurden vor dieser Erweiterung importiert und haben keinen `diet`-Wert. Der
  bestehende Re-Import-Mechanismus überspringt Datensätze mit bereits
  vorhandener `external_id` (Duplikat-Handling), aktualisiert sie aber
  NICHT – ein einfacher erneuter Import derselben Datei würde die
  Bestandsdaten also nicht nachträglich mit `diet` anreichern. Kläre und
  implementiere einen der folgenden Wege:
  a) Import-Logik von "skip on duplicate" auf "upsert on duplicate"
     umstellen (aktualisiert bestehende Zeilen bei erneutem Import), oder
  b) einen separaten, einmaligen Backfill-Mechanismus (Skript/Endpoint), der
     nur das `diet`-Feld für bereits vorhandene `external_id`-Datensätze
     nachträgt.
  Dokumentiere die getroffene Wahl im Code (Javadoc/Commit-Message).
- Formular "Lebensmittel hinzufügen" (`AddFoodDialogComponent`) bekommt ein
  neues, optionales Auswahlfeld für die Diät-Eigenschaft (Default
  `UNKNOWN`), damit auch manuell angelegte Lebensmittel korrekt gefiltert
  werden können.

**Filter-UI:**

- Eine Filterzeile oberhalb der Tabelle (z. B. Button-Gruppe oder Auswahl)
  mit den Optionen "Alle", "Vegan", "Vegetarisch", "Nicht vegetarisch".
- Der Filter ist mit der Suche aus FR-1 kombinierbar (UND-verknüpft, nicht
  exklusiv).
- Auswahl eines Filters setzt die aktuelle Seite auf Seite 1 zurück.

**Akzeptanzkriterien**

- Gegeben Lebensmittel mit unterschiedlichem `diet`-Wert existieren, wenn
  ich den Filter "Vegan" auswähle, dann zeigt die Tabelle ausschließlich
  Lebensmittel mit `diet = VEGAN`.
- Gegeben ich habe "Vegan" gefiltert, wenn ich zusätzlich einen Suchbegriff
  eingebe, dann werden beide Bedingungen kombiniert (UND).
- Gegeben ich wähle "Alle", wenn kein Filter aktiv sein soll, dann werden
  wieder alle Lebensmittel unabhängig vom `diet`-Wert angezeigt (inkl.
  `UNKNOWN`).

### FR-3: Sortierung über Tabellenspalten

- Die Spaltenüberschriften "Name", "Kalorien" und "Gramm" werden klickbar.
- Erster Klick auf eine Spalte sortiert aufsteigend danach, zweiter Klick
  auf dieselbe Spalte kehrt auf absteigend um, ein dritter Klick kehrt
  wieder zu aufsteigend zurück (kein "unsortiert"-Zustand nötig – es ist
  immer irgendeine Sortierung aktiv, initial `name` aufsteigend wie bisher).
- Die aktuell aktive Sortierspalte und -richtung wird visuell erkennbar
  markiert (z. B. Pfeil-Icon neben dem Spaltennamen).
- Serverseitig wird der Sortierparameter gegen eine feste Positivliste
  erlaubter Felder geprüft (`name`, `calory_count`, `grams`) – ein
  unbekannter/ungültiger Wert führt zu einer klaren 400-Antwort, nicht zu
  einem 500er (aktuell wird `Sort.by(String)` mit potenziell unvalidiertem
  Client-Input aufgerufen müssen; das darf nicht unkontrolliert
  durchgereicht werden).

**Akzeptanzkriterien**

- Gegeben die Tabelle ist nach Name aufsteigend sortiert (Standard), wenn
  ich auf die Spalte "Kalorien" klicke, dann ist die Tabelle nach Kalorien
  aufsteigend sortiert und ein entsprechendes Icon zeigt dies an.
- Gegeben die Tabelle ist nach "Kalorien" aufsteigend sortiert, wenn ich
  erneut auf "Kalorien" klicke, dann wird auf absteigend umgeschaltet.
- Gegeben Suche und/oder Diät-Filter sind aktiv, wenn ich die Sortierung
  ändere, dann bleiben Suche und Filter unverändert bestehen (alle drei
  Zustände sind unabhängig kombinierbar).

## 4. Nicht-funktionale Anforderungen

- Suche, Filter und Sortierung laufen serverseitig (DB-Query), nicht als
  Client-seitiges Filtern der aktuell geladenen Seite – sonst wären
  Ergebnisse über Seitenwechsel hinweg inkonsistent.
- Suchfeld-Eingabe debounced, um die Backend-Last nicht unnötig zu erhöhen.
- Sortierfeld serverseitig validiert (siehe FR-3), keine unvalidierte
  Weitergabe von Client-Eingaben an `Sort.by(...)`.
- Bedienbarkeit: Sortierbare Spaltenüberschriften und Filter-Buttons sind
  per Tastatur bedienbar und mit `aria-label`/`aria-sort` versehen.
- Bestehendes Design-System (Tailwind-Farbtokens, Font-Awesome-Icons)
  konsistent weiterverwenden, keine neuen Styling-Ansätze einführen.

## 5. Auswirkungen auf das Datenmodell

- Neue Flyway-Migration (nächste freie Versionsnummer) für die Spalte
  `diet` inkl. sinnvollem Index, falls danach gefiltert wird
  (`CREATE INDEX idx_tab_calory_food_diet ON tab_calory_food (diet);`).
- `Food`-Domain, `FoodPersistence`, `FoodDto`/`FoodDetailsDto` sowie die
  zugehörigen Converter um das `diet`-Feld erweitern.
- `FoodRepository`/`FindFoodUsecase`/`FoodRestController.getAllFoods` um
  Parameter `search` (String, optional), `diet` (Enum, optional) und `sort`
  + `direction` (bzw. kombiniert, z. B. `sort=calory,desc`) erweitern.

## 6. Abgrenzung (Out of Scope)

- Volltextsuche über Zutatenlisten/Marken (Suche bleibt auf den Namen
  beschränkt, sofern nicht anders entschieden).
- Weitere Diät-/Ernährungsformen (glutenfrei, halal, laktosefrei etc.) –
  das `diet`-Feld ist bewusst nur auf die vegan/vegetarisch-Achse
  ausgelegt, eine spätere Erweiterung soll aber nicht grundsätzlich
  erschwert werden.
- Persistieren der Filter-/Sortier-/Sucheinstellungen über
  Seiten-Reloads/Sessions hinweg.

## 7. Definition of Done

- `mvn clean verify` und `npm run lint`/`npm run format` laufen fehlerfrei.
- Bestehende Tests für `FindFoodUsecase`/`FoodRestController` sind an die
  neuen Parameter angepasst, neue Tests für Suche, Diät-Filter, Sortierung
  (inkl. Kombination aller drei) sowie für ungültige Sortierfelder
  (400-Antwort) sind vorhanden.
- Backfill-Entscheidung aus FR-2 ist umgesetzt und gegen die real
  importierten Bestandsdaten verifiziert (Stichprobe: ein bekanntermaßen
  veganes Produkt hat nach dem Backfill `diet = VEGAN`).
```
