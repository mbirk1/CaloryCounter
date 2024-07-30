# Calory Count Application API

## Einrichtung des Projektes

### Technische Vorraussetzungen
Vor der Entwicklung und Nutzung der API müssen technische Vorraussetzungen sichergestellt werden. Installiere hierzu 
folgende Drittanwendungen: 
 * Docker
 * Java 21

### Lokales Starten der Anwendung

Um die Anwendung lokal zu starten sind wenige Schritte nötig. 

1. Starte den, im docker-compose File befindlichen, Datenbankservice 'calory-db'.
2. Ist dieser hochgefahren, kannst du die vordefinierte Run-Konfiguration ausführen. 
3. Die Datenbankskripte werden beim Hochfahren der Anwendung migriert. 
4. Die Anwendung ist bereit Http-Request zu erhalten.

## Deployment
0. Hast du die Anwendung bereits versucht zu bauen, führe zunächst folgenden Befehl aus:
```shell
mvn clean
```
1. Baue zunächst die Anwendung als jar: 
```shell
mvn install
```
2. Nun kann die Jar-Datei auf einem Server deiner Wahl deployed werden. Alternativ sollte ein Workflow genutzt werden,
um diese zu auszuliefern.

## Bedienung

## Endpunkte
Eine Liste der verfügbaren Endpunkte ist mithilfe der SwaggerUI lesbar. Dazu muss lediglich das [Backend](#lokales-starten-der-anwendung)
gestartet werden. 
Die SwaggerUI ist dann verfügbar unter: 
```shell
http://localhost:8080/swagger-ui/index.html
```
