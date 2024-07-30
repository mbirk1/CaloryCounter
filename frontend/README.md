# Calory Count Application Frontend

## Tailwind Stylegenerierung

### Installation

Das Plugin kann mit folgenden Befehl installiert werden:

```shell
    npm install -D tailwindcss postcss autoprefixer
```

### Stylegenerierung

Die benötigten Styles können direkt in die className Tags der benötigten HTML Elemente genutzt werden. Ist die Komponente geschrieben, dann kann über folgenden Befehl die Generierung des Stylings beginnen:

```shell
    npx tailwindcss -i ./src/index.css -o ./src/output.css --watch
```

Dies generiert aus allen Komponenten alle benötigten CSS Attribute und legt sie in der output.css Datei ab. Die output.css Datei wiederum ist in der index.tsx eingebunden und wird als Standart für das Styling verwendet. Mit dem Attribut "--watch" wird angegeben, dass Tailwind dauerhaft darauf schaut, ob es Änderungen an den CSS Klassen gegeben hat und ggf diese neu baut.

## Lokale Entwicklung

Das Frontend kann innerhalb des `\frontend` Verzeichnises gestartet werden.  
Hierzu ein Terminal starten, mit dem `cd` Befehl in das Verzeichnis navigieren und `npm run start` ausführen. Dies startet
ein internes Script innerhalb der `package.json`. Dabei gibt `npm run` an, dass ein Script ausgeführt werden soll und `start`
bezeichnet dieses Script.

