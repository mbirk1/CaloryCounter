# Calory Count Application API

## Einrichtung des Projektes

### Technische Vorraussetzungen
Vor der Entwicklung und Nutzung der API müssen technische Vorraussetzungen sichergestellt werden. Installiere hierzu 
folgende Drittanwendungen: 
 * Docker
 * Java 21

### Lokales Starten der Anwendung

Um die Anwendung lokal zu starten sind wenige Schritte nötig. 

1. Starte den, im docker-compose File befindlichen, Datenbankservice.
2. Ist dieser hochgefahren, kannst du die vordefinierte Run-Konfiguration ausführen. 
3. Die Datenbankskripte werden beim Hochfahren der Anwendung migriert. 
4. Die Anwendung ist bereit Http-Request zu erhalten.

## Deployment
0. Hast du die Anwendung bereits versucht zu bauen, führe zunächst folgenden Befehl aus:
```
mvn clean
```
1. Baue zunächst die Anwendung als jar: 
```
mvn install
```
2. Nun kann die Jar-Datei auf einem Server deiner Wahl deployed werden. Alternativ sollte ein Workflow genutzt werden,
um diese zu auszuliefern.

## Bedienung

## Endpunkte
Die Liste der verfügbaren Endpunkte, sowie deren Verhalten wird über ein Maven Plugin bereitgestellt. Dieses nennt sich 
AsciiDoctor und ein lokaler Server kann über Maven gestartet werden:
```
mvn asciidoctor:http
```
Neben dem Ausführen des lokalen Servers kann auch die [index.adoc](src/main/asciidoc/index.adoc) in einem Browser der Wahl
geöffnet werden.

[Liste der verfügbaren Endpunkte](target/generated-docs/index.html)