# Tipspromenad-app-for-Android
This Android application brings the traditional Swedish tipspromenad (quiz walk) into the digital age. 

## Språkstöd – fas 1

Den befintliga Kotlin-appen har separata inställningar för **appspråk** och **frågespråk**: svenska (`sv`), engelska (`en`), spanska (`es`), danska (`da`), norska bokmål (`no`) och finska (`fi`). `se`/`dk` accepteras som alias; norska `nb`/`nn` normaliseras till `no`. Engelska är UI-fallback och första frågespråk. Välj språk via menyn Inställningar.

Android behåller AppCompat, XML, View Binding, fragment och WebView. Den skickar `?ui=sv&quizLang=fi` till samma GitHub Pages-adress som tidigare. **Ladda upp den uppdaterade TipspromenadQuizWebPage innan du förväntar dig det nya frågespråksstödet i Android.** Ingen webbplats har publicerats automatiskt.

Webbquizet sparar svar och rättningsstatus lokalt i webbläsaren; Android behåller sidan vid Info/Tillbaka. Detta är inte ett garanterat offlinepaket: nät krävs fortfarande för att ladda webbplats/frågor efter omstart. Inga GPS- eller QR-funktioner är aktiverade i fas 1.

## Separata webbprojekt

- `related-projects/TipspromenadQuizWebPage/`: befintlig statisk webbplats, sex UI-språk och originalfrågor på en/es/sv plus sex nya frågor på alla sex språk. Kör `node tools/serve.cjs` i den mappen.
- `related-projects/Tipspromenad/`: befintligt webpackprojekt, rättad byggkonfiguration och samma språkstöd, med de tolv ursprungliga svenska frågorna bevarade i JSON. Kör `npm ci`, `npm test`, `npm run build` i den mappen.

Mapparna är självständiga Git-kopior och ignoreras av Android-repot. Öppna respektive mapp när du vill granska, committa och pusha dess ändringar. De medföljer även som fristående ZIP-filer under `deliverables/` efter paketering. Se respektive README för GitHub Pages-instruktioner.

Gemensam webblogik, UI-ordbok och startfrågebank har sin kanoniska källa i QuizWebPage. Synka distributionskopian i det andra projektet med `powershell -File tools/sync-web-assets.ps1`; `-Check` jämför SHA-256 och upptäcker avvikelser. Inga syskonmappar behövs när ett enskilt webbprojekt körs eller laddas upp.

## Bygga och testa Android

Använd JDK 21 och Android SDK 36. Projektets Gradle-wrapper är 8.13. Android Studio är lokalt konfigurerat för `C:\Users\ander\.jdks\jbr-21.0.11`; dess nyare inbyggda Java 25 fungerade inte med denna verktygskedja.

```powershell
$env:JAVA_HOME = 'C:\Users\ander\.jdks\jbr-21.0.11'
$env:GRADLE_USER_HOME = 'C:\Users\ander\.gradle'
.\gradlew.bat build assembleDebugAndroidTest --console=plain
```

Enhetstester kontrollerar språkalias och att alla sex Android-resursuppsättningar är kompletta. Instrumenterade tester kompileras av kommandot ovan; kör `connectedDebugAndroidTest` med ansluten enhet/emulator för att faktiskt köra dem. Debug-APK: `app/build/outputs/apk/debug/app-debug.apk`. Release-APK är osignerad.

Webbtester: `node --test related-projects/TipspromenadQuizWebPage/tests/core.test.cjs related-projects/Tipspromenad/tests/core.test.cjs`. Kontraktexempel: `node tools/validate-contracts.cjs` efter installation av webpackprojektets beroenden.

## Dokumentation och återstående etapper

[Arkitektur och prioriterad plan](docs/ARCHITECTURE.md) beskriver alla tre projekt, dubbelarbete, buggar och filplan. [Frågeformat och migrering](docs/contracts/README.md) innehåller JSON Schema och exempel på gemensamma frågor och frysta quizinstanser.

Detta är fas 1 med kontraktdesign för nästa steg. Hela den äldre frågebanken är inte översatt eller faktagranskad. Kategorifilter, native offlinequiz, GPS, delnings-/rättningskoder, QR och arrangörsverktyg återstår enligt planen. [Verifiering](docs/VERIFICATION.md) anger körda kontroller och begränsningar.
