# Guida rapida per Android Studio

1. Apri Android Studio e crea un nuovo progetto: **Empty Views Activity** (linguaggio **Kotlin**, Package name: `com.example.messengertracker`, Minimum SDK: **API 26**).
2. Sostituisci `app/src/main/AndroidManifest.xml` con il file `AndroidManifest.xml` scaricato.
3. Crea la cartella `app/src/main/res/xml/` e inserisci al suo interno `accessibility_service_config.xml`.
4. Copia `MessengerAccessibilityService.kt` nella cartella `app/src/main/java/com/example/messengertracker/`.
5. Sostituisci `MainActivity.kt` in `app/src/main/java/com/example/messengertracker/` con il file scaricato.
6. Vai su **Build > Build Bundle(s) / APK(s) > Build APK(s)** per generare il file `app-debug.apk` da trasferire e installare sul tuo Samsung S22 Ultra.