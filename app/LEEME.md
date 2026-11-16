# App SENDO (Android, solo lectura)

WebView que muestra `assets/www/index.html` (la misma página que la web SENDO, en modo cliente: `window.SENDO_APP`)
y lee los planes cifrados de `c/` en este repositorio.

Compilar (sin Gradle): Android SDK con `platforms;android-34` y `build-tools;35.0.0`, y la firma `sendo.keystore`
(no está en el repositorio; se guarda aparte). `KS_PASS=<contraseña> ./build.sh <versionCode> <versionName>` → `build/SENDO.apk`.
Para que el móvil acepte una actualización hay que subir el versionCode y firmar con la misma clave.
