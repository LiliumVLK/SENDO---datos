#!/bin/sh
# Compila SENDO.apk sin Gradle. Uso: KS_PASS=... ./build.sh <versionCode> <versionName>
set -e
SDK=${ANDROID_HOME:-/home/claude/android-sdk}; BT=$SDK/build-tools/35.0.0; JAR=$SDK/platforms/android-34/android.jar
VC=${1:-1}; VN=${2:-1.0}
rm -rf build && mkdir -p build/gen build/classes build/dex
$BT/aapt2 compile --dir res -o build/res.zip
$BT/aapt2 link -o build/base.apk -I $JAR --manifest AndroidManifest.xml -A assets --min-sdk-version 24 --target-sdk-version 34 \
  --version-code $VC --version-name $VN --java build/gen build/res.zip
javac -nowarn --release 8 -classpath $JAR -d build/classes $(find src build/gen -name '*.java') 2>&1 | grep -v "JAVA_TOOL_OPTIONS" || true
$BT/d8 --min-api 24 --lib $JAR --output build/dex $(find build/classes -name '*.class') 2>&1 | grep -v JAVA_TOOL_OPTIONS || true
cp build/base.apk build/unsigned.apk && (cd build/dex && zip -q -j ../unsigned.apk classes.dex)
$BT/zipalign -f -p 4 build/unsigned.apk build/aligned.apk
$BT/apksigner sign --ks sendo.keystore --ks-pass env:KS_PASS --key-pass env:KS_PASS --out build/SENDO.apk build/aligned.apk 2>&1 | grep -v JAVA_TOOL_OPTIONS || true
$BT/apksigner verify --print-certs build/SENDO.apk 2>&1 | grep -v JAVA_TOOL_OPTIONS | head -3
