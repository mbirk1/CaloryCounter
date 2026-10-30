# Ticket: Datenqualität – einmaliges Bereinigungswerkzeug für Duplikate im Lebensmittelkatalog

## Ausgangslage / Kontext

Der Lebensmittelkatalog speist sich mittlerweile aus drei Quellen (`FoodSource`): manuell angelegte Lebensmittel, der OpenFoodFacts-Import (potenziell hunderttausende bis Millionen Zeilen, siehe Kommentar in `DeleteFoodUsecase`: *"a table with real, bulk-imported data (hundreds of thousands to millions of rows)"*) und der BLS-4.0-Import (7.140 Zeilen). Innerhalb einzelner Quellen entstehen dabei faktische Duplikate: OpenFoodFacts enthält z. B. dieselbe Ware mehrfach unter leicht abweichenden Produktnamen oder in unterschiedlichen Gebindegrößen mit eigenem Barcode.

**Wichtige Einschränkung, die bei der Umsetzung beachtet werden muss:** Der BLS-Datensatz enthält bewusst viele fein abgestufte Varianten desselben Grundlebensmittels (z. B. "Apfel roh" vs. "Apfel, geschält, roh" vs. "Apfel gedünstet") – diese sind **keine Duplikate**, sondern eigenständige, nährwertmäßig unterschiedliche Einträge. Eine reine Namens-Ähnlichkeit reicht als Duplikat-Kriterium also nicht aus; sie muss um eine Nährwert-Nähe-Prüfung ergänzt werden, sonst würden echte, bewusst getrennte BLS-Varianten fälschlich als Duplikate vorgeschlagen.

**Klärungen aus Rücksprache mit dem Fachbereich (bereits entschieden, nicht mehr offen):**

1. **Quellübergreifend vs. quellintern**: Duplikate werden ausschließlich **innerhalb derselben Quelle** gesucht (BLS gegen BLS, OpenFoodFacts gegen OpenFoodFacts, Manuell gegen Manuell). Ein generisches BLS-Referenzlebensmittel und ein konkretes OpenFoodFacts-Markenprodukt gelten nie als Duplikat zueinander.
2. **Kein dauerhaftes Produkt-Feature**: Das ist **kein** Feature, das dauerhaft in der Anwendung sichtbar bleibt – kein Menüpunkt, kein wiederverwendbarer "Analyse starten"-Button, nichts, was permanent läuft oder beliebig oft angestoßen werden kann. Es handelt sich um ein **einmalig auszuführendes Bereinigungswerkzeug**, vergleichbar mit einem Migrations-/Wartungsskript: einmal laufen lassen, Ergebnis sichten, bestätigte Duplikate zusammenführen, fertig.
3. **Rein Backend, kein Frontend**: Die Erkennung und das Zusammenführen laufen ausschließlich über gezielt aufgerufene, interne Backend-Endpunkte (z. B. per REST-Client durch einen Entwickler/Admin) – es wird keine Oberfläche dafür gebaut. Die Bestätigung/Ablehnung einzelner Cluster geschieht durch gezielte einzelne API-Aufrufe anhand der zurückgelieferten Cluster-Liste, nicht über eine gebaute Review-Seite.

## Ziel

Ein einmalig auszuführendes Backend-Werkzeug erkennt ähnliche/duplizierte Lebensmittel innerhalb derselben Quelle, gruppiert sie zu Clustern und liefert sie über einen Lese-Endpunkt zur Sichtung. Über gezielte Einzel-Aufrufe können bestätigte Cluster zusammengeführt oder abgelehnt werden. Nach einmaliger Durchführung besteht keine Notwendigkeit, das Werkzeug weiter im laufenden Betrieb vorzuhalten oder zu bewerben.

## Funktionale Anforderungen

### FR-1: Datenbankschema für Duplikat-Cluster

**Gegeben** es gibt aktuell keine Struktur, um vorgeschlagene Duplikate zu verwalten.
**Wenn** die neue Migration (`V20260915__add_food_duplicate_clusters.sql`) ausgeführt wird.
**Dann** entstehen zwei neue Tabellen:
- `tab_calory_food_duplicate_cluster` (`id uuid PK`, `source varchar` – die `FoodSource`-Quelle, für die der Cluster gilt, `status varchar` – `PENDING`/`CONFIRMED`/`REJECTED`, `created_at timestamp`, `resolved_at timestamp null`).
- `tab_calory_food_duplicate_cluster_member` (`cluster_id uuid FK → tab_calory_food_duplicate_cluster(id) ON DELETE CASCADE`, `food_id uuid FK → tab_calory_food(id)`, `similarity_score numeric(4,3)`, `suggested_canonical boolean`, `PRIMARY KEY (cluster_id, food_id)`).

Zusätzlich wird die PostgreSQL-Erweiterung `pg_trgm` aktiviert (`CREATE EXTENSION IF NOT EXISTS pg_trgm;` – dieselbe Vorgehensweise wie bereits bei `uuid-ossp` in der initialen Migration) sowie ein GIN-Trigram-Index auf `lower(tab_calory_food.name)` angelegt, um die Kandidatensuche in FR-2 auch bei sehr großen Tabellen (Millionen Zeilen bei OpenFoodFacts) performant zu halten, statt eines naiven paarweisen Vergleichs aller Zeilen (O(n²)).

Diese Tabellen dienen ausschließlich als Zwischenspeicher für den einmaligen Lauf dieses Werkzeugs – kein fortlaufend genutztes Feature-Datenmodell.

### FR-2: Kandidaten-Erkennung (Ähnlichkeits-Analyse)

**Gegeben** es gibt noch keinen Mechanismus, um ähnliche Lebensmittel zu finden.
**Wenn** die Analyse für eine Quelle (`MANUAL`, `OPENFOODFACTS` oder `BLS`) ausgeführt wird.
**Dann** werden Kandidatenpaare innerhalb dieser Quelle ermittelt, bei denen **beide** Bedingungen erfüllt sind:
- **Namens-Ähnlichkeit**: Trigram-Ähnlichkeit (`similarity(lower(a.name), lower(b.name))`, PostgreSQL `pg_trgm`) oberhalb eines konfigurierbaren Schwellwerts (Startwert: `0.5`).
- **Nährwert-Nähe**: Kalorien und alle vorhandenen Makros weichen relativ um nicht mehr als einen konfigurierbaren Prozentsatz voneinander ab (Startwert: `10 %`). Da alle Lebensmittel innerhalb einer Quelle einheitlich auf 100 g normiert sind, können die Werte direkt verglichen werden, ohne vorher auf eine gemeinsame Menge umzurechnen.

Beide Schwellwerte sind über `application.yml` konfigurierbar, nicht hart codiert – sie lassen sich beim einmaligen Testlauf vor der eigentlichen Ausführung noch nachjustieren, ohne Code zu ändern.

Aus den gefundenen Paaren werden per Union-Find zusammenhängende Cluster gebildet (transitive Gruppierung: A↔B und B↔C ähnlich ⇒ ein Cluster {A, B, C}), nicht nur isolierte Paare. Innerhalb jedes Clusters wird ein Mitglied als `suggested_canonical` markiert (Heuristik: das Lebensmittel mit den meisten befüllten optionalen Feldern – Makros, Marke, Kategorie, Bild-URL – gilt als vollständigster und damit am besten geeigneter Kandidat).

### FR-3: Einmaliger, intern aufgerufener Analyse-Lauf

**Gegeben** eine vollständige Ähnlichkeits-Analyse kann bei Millionen Zeilen (OpenFoodFacts) mehrere Minuten dauern und darf daher keine HTTP-Anfrage blockieren.
**Wenn** die Analyse einmalig gestartet wird (`POST /api/internal/food-duplicates/analyze?source=OPENFOODFACTS`).
**Dann** läuft sie asynchron im Hintergrund (`@Async`, gleiches Dispatcher-Muster wie beim bestehenden Import), der Endpunkt liefert sofort einen pollbaren Job-Status zurück (`GET /api/internal/food-duplicates/analyze/{jobId}`).

**Wichtig – Abgrenzung zur bestehenden Import-Infrastruktur**: Das `/api/internal/...`-Präfix (statt `/api/food/...`) markiert diese Endpunkte bewusst als nicht Teil der regulären, im Frontend beworbenen API-Oberfläche. Es gibt dafür keinen Button, keinen Menüpunkt, keinen Verweis aus dem Frontend – die Endpunkte werden gezielt und einmalig direkt aufgerufen (z. B. per REST-Client durch einen Entwickler/Admin).

### FR-4: Sichtung vorgeschlagener Cluster

**Gegeben** vorgeschlagene Cluster müssen vor dem Zusammenführen gesichtet werden.
**Wenn** `GET /api/internal/food-duplicates?status=PENDING&source=...` aufgerufen wird.
**Dann** werden alle Cluster mit Status `PENDING` inkl. ihrer Mitglieder (Name, Kalorien, Makros, Ähnlichkeits-Score, vorgeschlagenes kanonisches Lebensmittel) als JSON zurückgegeben – zur Sichtung direkt über die Antwort bzw. einen REST-Client, ohne eigene Oberfläche.

### FR-5: Zusammenführen eines bestätigten Clusters

**Gegeben** ein Cluster soll nach Sichtung zusammengeführt werden.
**Wenn** `POST /api/internal/food-duplicates/{clusterId}/merge` mit Body `{ "canonicalFoodId": ... }` aufgerufen wird.
**Dann** läuft in einer Transaktion:
1. Für jedes nicht-kanonische Cluster-Mitglied werden alle `tab_calory_recipe_food`-Zeilen auf das kanonische Lebensmittel umgehängt (`food_id` aktualisiert).
2. **Konfliktfall**: Referenziert ein Rezept bereits sowohl das kanonische als auch ein nicht-kanonisches Mitglied, würde das Umhängen die zusammengesetzte Primärschlüssel-Eindeutigkeit `(food_id, recipe_id)` verletzen. In diesem Fall wird die Zeile des nicht-kanonischen Mitglieds ersatzlos entfernt (nicht die Mengen addiert – das würde die Rezept-Nährwerte unerwartet verändern) und der Konflikt mit `LOG.warn(...)` protokolliert (siehe FR-7).
3. Die nicht-kanonischen Lebensmittel werden gelöscht.
4. Der Cluster wird auf `CONFIRMED` gesetzt, `resolved_at` gefüllt.

Schlägt einer der Schritte fehl, wird die gesamte Transaktion zurückgerollt.

### FR-6: Ablehnen eines Clusters

**Gegeben** ein vorgeschlagener Cluster ist bei genauerem Hinsehen kein echtes Duplikat (z. B. zwei unterschiedliche BLS-Zubereitungsvarianten).
**Wenn** `POST /api/internal/food-duplicates/{clusterId}/reject` aufgerufen wird.
**Dann** wird der Cluster auf `REJECTED` gesetzt, `resolved_at` gefüllt, keine Daten werden verändert.

### FR-7: Lückenlose Protokollierung des gesamten Werkzeugs

**Gegeben** dieses Werkzeug löscht Lebensmittel-Zeilen unwiderruflich – anders als reguläre Produkt-Features muss sich im Nachhinein aus den Logs exakt rekonstruieren lassen, was passiert ist, da es keine Oberfläche gibt, über die sich der Vorgang noch einmal nachvollziehen ließe.
**Wenn** die Analyse läuft, ein Cluster zusammengeführt oder abgelehnt wird.
**Dann** wird jeweils protokolliert (Level gemäß der im separaten Logging-Ticket dokumentierten Konvention):
- `LOG.info` bei Start und Abschluss der Analyse, inkl. Quelle, Anzahl untersuchter Lebensmittel und Anzahl gefundener Cluster.
- `LOG.info` bei jedem erfolgreichen Merge, inkl. Cluster-ID, kanonischer Food-ID/-Name und der Liste der gelöschten Food-IDs/-Namen.
- `LOG.warn` bei jedem Konfliktfall aus FR-5 Schritt 2, inkl. betroffener Rezept-ID.
- `LOG.info` bei jeder Ablehnung, inkl. Cluster-ID.

Sofern das separate Logging-Ticket (MDC-basierte Anfrage-/Nutzer-Korrelation) zum Zeitpunkt der Umsetzung bereits verfügbar ist, wird dieselbe Infrastruktur genutzt, sodass auch hier nachvollziehbar ist, wer den jeweiligen Aufruf ausgeführt hat.

## Nicht-funktionale Anforderungen

- Neue Backend-Klassen mit Unit-Tests abgedeckt (Union-Find-Clustering-Logik, Merge-Konfliktbehandlung bei bereits doppelt verknüpften Rezepten, Reject-Verhalten), JaCoCo-Schwellen (≥90 % Line/Method, ≥80 % Branch) weiterhin eingehalten.
- Checkstyle bleibt grün.
- Die Ähnlichkeits-Schwellwerte (Name, Nährwert-Toleranz) sind über Konfiguration einstellbar, nicht hart codiert.
- Kein Frontend-Bestandteil, kein dauerhafter Navigationspunkt, keine Verlinkung aus der bestehenden Oberfläche.
- Wie beim bestehenden Import-Endpunkt gibt es noch kein Rollenmodell – die Endpunkte sind technisch für jeden authentifizierten Nutzer erreichbar, werden aber durch das `/api/internal/...`-Präfix und das Fehlen jeder Frontend-Verlinkung bewusst nicht beworben (bestehender, bereits an anderer Stelle dokumentierter TODO-Zustand, kein neu eingeführtes Sicherheitsproblem).
- Der Analyse-Lauf für die OpenFoodFacts-Quelle muss gegen eine realistisch große Testdatenmenge (mehrere hunderttausend Zeilen) auf Laufzeit geprüft werden, bevor er einmalig produktiv ausgeführt wird.

## Datenmodell-Implikationen

- Zwei neue Tabellen (FR-1), keine Änderung an bestehenden `tab_calory_food`-Spalten.
- Neuer Index (`pg_trgm`-GIN-Index auf `lower(name)`) – erhöht den Speicherbedarf und die Schreiblast bei Inserts/Updates auf `tab_calory_food` leicht, ist aber Voraussetzung für performante Kandidatensuche bei großen Tabellen.
- Merge löscht tatsächlich Zeilen aus `tab_calory_food` (nicht nur eine Markierung) – konsistent mit dem bestehenden `DeleteFoodUsecase`-Verhalten.

## Abgrenzung / Out-of-Scope

- **Jede Form von dauerhafter Produktoberfläche für Datenqualität** (Menüpunkt, wiederverwendbarer Button, permanent laufender Dienst) – bewusst nicht Teil dieses Tickets, siehe Klärung 2/3.
- **Quellübergreifende Duplikat-Erkennung** (BLS vs. OpenFoodFacts vs. Manuell) – bewusst ausgeklammert, siehe Klärung 1.
- **Automatisches, unbeaufsichtigtes Zusammenführen** ohne vorherige Sichtung – nicht Teil dieses Tickets.
- **Rückgängig machen eines bereits bestätigten Merges** – sobald zusammengeführt und gelöscht, ist der Vorgang nicht über die Anwendung rückholbar (nur über ein Datenbank-Backup).
- **Automatische, wiederkehrende Analyse-Läufe** (z. B. nach jedem Import automatisch anstoßen) – ausdrücklich nicht gewünscht, siehe Klärung 2.
- **Rollenbasierte Einschränkung** der Endpunkte auf Admin-Nutzer – wie beim bestehenden Import-Endpunkt vorerst nicht umgesetzt, da kein Rollenmodell existiert.

## Definition of Done

- Alle FR-1 bis FR-7 umgesetzt und mit Tests abgedeckt.
- `mvn verify` (Checkstyle, JaCoCo-Schwellen) grün.
- Manueller Test (einmalige Durchführung): Analyse für die BLS-Quelle anstoßen → keine falsch-positiven Cluster zwischen bewusst unterschiedlichen Zubereitungsvarianten. Cluster mit einem Rezept-Bezug zusammenführen → Rezept referenziert danach das kanonische Lebensmittel, Rezept-Nährwerte bleiben korrekt, Merge ist vollständig aus den Logs rekonstruierbar (FR-7). Cluster ablehnen → Status wechselt zu `REJECTED`, keine Datenänderung.
- Laufzeittest der Analyse gegen eine große OpenFoodFacts-Testmenge dokumentiert.
- Code-Review bestätigt korrekte Transaktionsgrenzen beim Merge sowie dass keinerlei Frontend-/Navigations-Anbindung entstanden ist.
