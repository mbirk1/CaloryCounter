# Ticket: Logging und Nachverfolgbarkeit nachziehen (Rezept-, BLS-Import- und Duplikat-Funktionen)

## Ausgangslage / Kontext

Bei der Durchsicht der zuletzt gebauten Features (Rezept-Feature, BLS-Import) wurden folgende konkrete Lücken gefunden:

- **`FindRecipeUsecase` hat überhaupt kein Logging** (kein `Logger`-Feld, keine einzige `LOG`-Anweisung) – im Gegensatz zu praktisch jedem anderen Usecase der Anwendung (`CreateRecipeUsecase`, `DeleteRecipeUsecase`, `CreateFoodUsecase`, `UpdateFoodUsecase`, `FindFoodUsecase.findAllFoods()` etc. loggen alle konsequent auf Debug-/Info-/Warn-Ebene).
- **`FindFoodUsecase.findFoodById()` hat kein Logging**, obwohl die Schwestermethode `findAllFoods()` in derselben Klasse vorbildlich mit `LOG.debug(...)` bei Anfrage und Ergebnis protokolliert – eine Inkonsistenz innerhalb derselben Klasse.
- **Keine Anfragen-übergreifende Nachverfolgbarkeit**: Jede Log-Zeile steht für sich allein. Es gibt keine Korrelations-ID, mit der sich mehrere Log-Zeilen, die zu ein und derselben HTTP-Anfrage gehören, im Log wiederfinden und zusammenhängend lesen lassen – außer beim CSV-/BLS-Import, wo eine Job-ID manuell durch jede Log-Zeile durchgereicht wird. Für alle anderen (synchronen) Endpunkte fehlt dieses Muster komplett.
- **Keine Nutzer-Zuordnung in Log-Zeilen**: Die Anwendung hat einen JWT-basierten Auth-Layer (`JwtAuthenticationFilter` setzt einen `TokenPrincipal` mit `id`/`email` in den `SecurityContextHolder`), aber keine einzige Log-Zeile in der gesamten Anwendung nutzt diese Information. Eine Zeile wie `"Rezept 'X' erfolgreich angelegt"` sagt nicht, welcher Nutzer das Rezept angelegt hat – bei einem Diebstahl-/Missbrauchs- oder Support-Fall lässt sich das aktuell aus den Logs nicht rekonstruieren.
- Es existiert weder eine `application.yml`/`logback-spring.xml` noch sonst eine angepasste Logging-Konfiguration – die Anwendung läuft komplett mit Spring Boots Standard-Konsolen-Pattern, das keine MDC-Werte (siehe unten) anzeigen würde, selbst wenn sie gesetzt würden.

## Ziel

Jede Log-Zeile der Anwendung lässt sich automatisch einer HTTP-Anfrage und – sofern authentifiziert – einem Nutzer zuordnen, ohne dass jeder einzelne Usecase das manuell in seinen Log-Text schreiben muss. Bestehende Lücken in Rezept- und Lebensmittel-Usecases werden geschlossen. Es gibt eine dokumentierte, konsistente Konvention für Log-Level, an die sich neue Features halten.

## Funktionale Anforderungen

### FR-1: Anfragen-Korrelation und Nutzer-Kontext über MDC

**Gegeben** es gibt keine Möglichkeit, Log-Zeilen einer HTTP-Anfrage oder einem Nutzer zuzuordnen, außer beim Import (manuelle Job-ID).
**Wenn** eine HTTP-Anfrage eingeht.
**Dann** setzt ein neuer `RequestTracingFilter` (`OncePerRequestFilter`, in der Filterkette **nach** `JwtAuthenticationFilter` registriert, damit der authentifizierte Principal zu diesem Zeitpunkt schon bekannt ist) für die Dauer der Anfrage zwei SLF4J-MDC-Werte:
- `requestId`: aus dem `X-Request-Id`-Header übernommen, falls vom Aufrufer mitgeschickt, sonst neu generiert (`UUID.randomUUID()`).
- `user`: die E-Mail-Adresse des authentifizierten `TokenPrincipal` (`SecurityContextHolder.getContext().getAuthentication()`), oder `"anonymous"`, falls die Anfrage nicht authentifiziert ist.

Beide Werte werden in einem `finally`-Block nach Abschluss der Anfrage per `MDC.clear()` wieder entfernt (Thread-Pool-Wiederverwendung sonst Gefahr von Kontext-Leck zwischen unabhängigen Anfragen).

**Vorteil dieses Ansatzes gegenüber manuellem Ergänzen jeder einzelnen Log-Zeile:** Jede bereits bestehende und jede künftige `LOG.debug/info/warn/error(...)`-Anweisung in der gesamten Anwendung bekommt Anfrage- und Nutzer-Kontext automatisch mitgeliefert, ohne dass an den eigentlichen Log-Aufrufen im Code etwas geändert werden muss.

### FR-2: Logging-Pattern um MDC-Werte erweitern

**Gegeben** es existiert keine eigene Logging-Konfiguration, Spring Boots Standard-Pattern zeigt keine MDC-Werte an.
**Wenn** eine Log-Zeile ausgegeben wird.
**Dann** enthält das Konsolen-Pattern (`logging.pattern.console` in einer neuen `application.yml`, oder alternativ eine `logback-spring.xml`) zusätzlich `%X{requestId}` und `%X{user}`, sodass Request-ID und Nutzer in jeder Zeile sichtbar sind, unabhängig davon, welcher Usecase geloggt hat.

### FR-3: Fehlendes Logging in `FindRecipeUsecase` nachziehen

**Gegeben** `FindRecipeUsecase` hat aktuell kein Logging.
**Wenn** ein Rezept oder alle Rezepte abgefragt werden.
**Dann** wird analog zu `FindFoodUsecase.findAllFoods()` protokolliert: `LOG.debug(...)` bei Anfrage-Eingang (inkl. angefragter ID bzw. "alle Rezepte"), `LOG.warn(...)` falls `findRecipeById` eine nicht existierende ID anfragt (aktuell wirft `orElseThrow()` ohne jede Protokollierung), `LOG.debug(...)` mit Trefferanzahl bei `findAllRecipes()`.

### FR-4: Fehlendes Logging in `FindFoodUsecase.findFoodById()` nachziehen

**Gegeben** `findFoodById()` hat kein Logging, obwohl `findAllFoods()` in derselben Klasse konsequent loggt.
**Wenn** ein einzelnes Lebensmittel abgefragt wird.
**Dann** wird analog `LOG.debug(...)` bei Anfrage und `LOG.warn(...)` bei nicht gefundener ID ergänzt (aktuell wirft auch hier `orElseThrow()` ohne Argument und ohne Protokollierung).

### FR-5: Dokumentierte Log-Level-Konvention

**Gegeben** die bestehenden Usecases folgen bereits implizit einem konsistenten Muster (siehe `CreateRecipeUsecase`, `DeleteFoodUsecase`, `FindFoodUsecase`), das aber nirgends schriftlich festgehalten ist.
**Wenn** neue Usecases oder Endpunkte entstehen.
**Dann** gilt eine im Projekt dokumentierte Konvention (z. B. als Abschnitt in einer `CONTRIBUTING.md` oder im Projekt-Wiki, damit sie zukünftigen Tickets/Implementierungen als Referenz dient):
- `DEBUG`: Eingang einer Anfrage mit ihren Parametern, sowie unauffällige Zwischenschritte.
- `INFO`: erfolgreicher, zustandsverändernder Abschluss (angelegt, aktualisiert, gelöscht, importiert, zusammengeführt).
- `WARN`: abgelehnte/ungültige Eingabe, nicht gefundene Ressource, übersprungene Zeile bei einem Import, aufgelöster Konfliktfall (z. B. der Merge-Konfliktfall im Duplikat-Ticket).
- `ERROR`: unerwarteter, nicht regulär behandelter Fehler (z. B. Job-Abbruch, E/A-Fehler).

## Nicht-funktionale Anforderungen

- Neue/geänderte Klassen mit Unit-Tests abgedeckt (`RequestTracingFilterUnitTest` – MDC wird gesetzt und nach der Anfrage wieder geleert, auch im Fehlerfall; ergänzte Logging-Fälle in `FindRecipeUsecaseUnitTest`/`FindFoodUsecaseUnitTest` können, wo sinnvoll, über einen Test-Log-Appender verifiziert werden, statt reines Vertrauen auf Code-Review).
- JaCoCo-Schwellen (≥90 % Line/Method, ≥80 % Branch) weiterhin eingehalten.
- Checkstyle bleibt grün.
- Keine sensiblen Daten (Passwörter, Tokens) landen im MDC oder in Log-Zeilen – nur die E-Mail-Adresse als Nutzerkennung, wie bereits an anderer Stelle (z. B. Exception-Handling) unkritisch verwendet.

## Datenmodell-Implikationen

Keine. Reine Querschnitts-/Infrastruktur-Änderung (Filter, Logging-Konfiguration) plus punktuelle Ergänzungen in bestehenden Usecase-Klassen.

## Abgrenzung / Out-of-Scope

- **Persistente Audit-Log-Tabelle** (z. B. eine durchsuchbare "Wer hat wann was geändert"-Historie in der Datenbank) – dieses Ticket verbessert ausschließlich die klassischen SLF4J-Logs, kein neues Datenbank-Audit-Trail-Feature.
- **Log-Aggregation/-Versand an ein externes System** (ELK, Grafana Loki o. Ä.) – nicht Teil dieses Tickets, reine Anwendungs-interne Verbesserung.
- **Frontend-seitiges Logging/Tracing** (z. B. Fehler-Tracking im Browser) – nicht Teil dieses Tickets.

## Definition of Done

- FR-1 bis FR-5 umgesetzt und mit Tests abgedeckt.
- `mvn verify` (Checkstyle, JaCoCo-Schwellen) grün.
- Manueller Test: Eine authentifizierte Anfrage auslösen (z. B. Rezept anlegen) → Log-Zeilen zeigen dieselbe `requestId` über den gesamten Anfrage-Verlauf sowie die E-Mail-Adresse des anfragenden Nutzers. Eine nicht existierende Rezept-/Lebensmittel-ID abfragen → `WARN`-Zeile mit der angefragten ID erscheint.
- Log-Level-Konvention ist an einer für zukünftige Tickets auffindbaren Stelle dokumentiert.
- Code-Review bestätigt, dass `MDC.clear()` auch bei einer während der Anfrage geworfenen Exception zuverlässig ausgeführt wird (kein Kontext-Leck in den Thread-Pool).
