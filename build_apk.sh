#!/data/data/com.termux/files/usr/bin/bash
set -e

ACTION="${1:-all}"
ANDROID_JAR="/data/data/com.termux/files/home/android-sdk/platforms/android-33/android.jar"
BUILD_DIR="build_cli"
MIN_SDK="24"
TARGET_SDK="33"
VERSION_CODE="1"
VERSION_NAME="1.0.0"

clean_build() {
    echo "=== Cleaning Build Artifacts ==="
    rm -rf "$BUILD_DIR" compiled_res.zip unaligned.apk *.apk *.idsig
    echo "Clean complete."
}

compile_resources_and_code() {
    mkdir -p "$BUILD_DIR/gen" "$BUILD_DIR/classes" "$BUILD_DIR/dex"

    echo "[1/4] Compiling Android Resources with AAPT2..."
    aapt2 compile --dir app/src/main/res -o compiled_res.zip

    echo "[2/4] Linking Resources & Generating R.java..."
    aapt2 link -I "$ANDROID_JAR" \
        --manifest app/src/main/AndroidManifest.xml \
        --min-sdk-version "$MIN_SDK" \
        --target-sdk-version "$TARGET_SDK" \
        --version-code "$VERSION_CODE" \
        --version-name "$VERSION_NAME" \
        -o unaligned.apk \
        --java "$BUILD_DIR/gen" \
        compiled_res.zip \
        --auto-add-overlay

    echo "[3/4] Compiling Java Source Files with javac..."
    JAVA_FILES=$(find app/src/main/java "$BUILD_DIR/gen" -name "*.java")
    javac -encoding UTF-8 -source 17 -target 17 \
        -cp "$ANDROID_JAR" \
        -d "$BUILD_DIR/classes" \
        $JAVA_FILES
}

run_tests() {
    echo "=== Running OffRead Unit Tests ==="
    compile_resources_and_code

    mkdir -p "$BUILD_DIR/test_classes"
    javac -encoding UTF-8 -source 17 -target 17 \
        -cp "$ANDROID_JAR:$BUILD_DIR/classes" \
        -d "$BUILD_DIR/test_classes" \
        app/src/test/java/com/offread/app/UnitTestSuite.java

    java -cp "$ANDROID_JAR:$BUILD_DIR/classes:$BUILD_DIR/test_classes" \
        com.offread.app.UnitTestSuite
}

build_apk() {
    BUILD_TYPE="${1:-release}"
    echo "=== Building OffRead ($BUILD_TYPE) APK ==="

    if [ "$BUILD_TYPE" = "debug" ]; then
        OUT_APK="app-debug.apk"
    else
        OUT_APK="OffRead-release.apk"
    fi

    compile_resources_and_code

    echo "[4/5] Compiling DEX bytecode with D8..."
    CLASS_FILES=$(find "$BUILD_DIR/classes" -name "*.class")
    D8_FLAG="--release"
    if [ "$BUILD_TYPE" = "debug" ]; then
        D8_FLAG="--debug"
    fi
    d8 --lib "$ANDROID_JAR" \
       --min-api "$MIN_SDK" \
       $D8_FLAG \
       --output "$BUILD_DIR/dex" \
       $CLASS_FILES

    echo "[5/5] Packaging and Signing APK..."
    python3 -c "
import zipfile, os
with zipfile.ZipFile('unaligned.apk', 'a', compression=zipfile.ZIP_DEFLATED) as apk:
    dex_path = os.path.join('$BUILD_DIR/dex', 'classes.dex')
    apk.write(dex_path, 'classes.dex')
print('  classes.dex added successfully')
"

    KEYSTORE="offread.keystore"
    if [ ! -f "$KEYSTORE" ]; then
        keytool -genkeypair -v -keystore "$KEYSTORE" \
            -storepass offreadapp -alias offread -keypass offreadapp \
            -keyalg RSA -keysize 2048 -validity 10000 \
            -dname "CN=OffRead, OU=App, O=OffRead, L=Global, S=Earth, C=US"
    fi

    apksigner sign --ks "$KEYSTORE" \
        --ks-pass pass:offreadapp \
        --ks-key-alias offread \
        --key-pass pass:offreadapp \
        --out "$OUT_APK" \
        unaligned.apk

    apksigner verify --verbose "$OUT_APK"

    if [ "$BUILD_TYPE" = "release" ]; then
        cp -f "$OUT_APK" "OffRead.apk"
        [ -f "${OUT_APK}.idsig" ] && cp -f "${OUT_APK}.idsig" "OffRead.apk.idsig"
    fi

    echo "=== BUILD SUCCESSFUL: $OUT_APK ==="
    ls -lh "$OUT_APK"
}

case "$ACTION" in
    clean)
        clean_build
        ;;
    test)
        run_tests
        ;;
    debug)
        build_apk "debug"
        ;;
    release)
        build_apk "release"
        ;;
    all|*)
        run_tests
        build_apk "debug"
        build_apk "release"
        ;;
esac
