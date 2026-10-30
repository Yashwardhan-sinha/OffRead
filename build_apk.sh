#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"
SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}"
API="${ANDROID_API:-35}"
BT="${ANDROID_BUILD_TOOLS:-35.0.0}"
ANDROID_JAR="$SDK_ROOT/platforms/android-$API/android.jar"
TOOLS="$SDK_ROOT/build-tools/$BT"
AAPT2="$(command -v aapt2)"
D8="$TOOLS/d8"
ZIPALIGN="$(command -v zipalign || true)"
APKSIGNER="$(command -v apksigner)"
KEYSTORE="$ROOT/offread-dev.keystore"
BUILD_DIR="$ROOT/build_cli"
ACTION="${1:-all}"

for f in "$ANDROID_JAR" "$AAPT2" "$D8" "$APKSIGNER"; do
  [ -e "$f" ] || { echo "Missing required build tool: $f" >&2; exit 2; }
done
command -v javac >/dev/null || { echo 'Missing javac' >&2; exit 2; }
command -v java >/dev/null || { echo 'Missing java' >&2; exit 2; }
command -v python3 >/dev/null || { echo 'Missing python3' >&2; exit 2; }

compile_main() {
  rm -rf "$BUILD_DIR/gen" "$BUILD_DIR/classes" "$BUILD_DIR/dex"
  rm -f "$BUILD_DIR/unaligned.apk" "$BUILD_DIR/aligned.apk" "$BUILD_DIR/compiled_res.zip"
  mkdir -p "$BUILD_DIR/gen" "$BUILD_DIR/classes" "$BUILD_DIR/dex"
  "$AAPT2" compile --dir app/src/main/res -o "$BUILD_DIR/compiled_res.zip"
  "$AAPT2" link -I "$ANDROID_JAR" \
    --manifest app/src/main/AndroidManifest.xml \
    --min-sdk-version 24 --target-sdk-version 33 \
    --version-code 2 --version-name '1.0.0-rot1' \
    -o "$BUILD_DIR/unaligned.apk" --java "$BUILD_DIR/gen" \
    "$BUILD_DIR/compiled_res.zip" --auto-add-overlay
  mapfile -t JAVA_FILES < <(find app/src/main/java "$BUILD_DIR/gen" -name '*.java' -type f | sort)
  javac -encoding UTF-8 --release 17 -Xlint:deprecation \
    -cp "$ANDROID_JAR" -d "$BUILD_DIR/classes" "${JAVA_FILES[@]}"
}

run_tests() {
  compile_main
  mkdir -p "$BUILD_DIR/test_classes"
  mapfile -t TEST_FILES < <(find app/src/test/java -name '*.java' -type f | sort)
  if ((${#TEST_FILES[@]})); then
    javac -encoding UTF-8 --release 17 \
      -cp "$ANDROID_JAR:$BUILD_DIR/classes" \
      -d "$BUILD_DIR/test_classes" "${TEST_FILES[@]}"
    java -cp "$ANDROID_JAR:$BUILD_DIR/classes:$BUILD_DIR/test_classes" \
      com.offread.app.UnitTestSuite
  else
    echo 'No JVM unit tests found.'
  fi
}

build_apk() {
  TYPE="${1:-debug}"
  compile_main
  mapfile -t CLASS_FILES < <(find "$BUILD_DIR/classes" -name '*.class' -type f | sort)
  D8_FLAGS=(--release)
  if [ "$TYPE" = debug ]; then D8_FLAGS=(--debug); fi
  bash "$D8" -JXmx768M --lib "$ANDROID_JAR" --min-api 24 "${D8_FLAGS[@]}" \
    --output "$BUILD_DIR/dex" "${CLASS_FILES[@]}"
  python3 - "$BUILD_DIR/unaligned.apk" "$BUILD_DIR/dex/classes.dex" <<'PY'
import sys, zipfile
apk, dex = sys.argv[1:]
with zipfile.ZipFile(apk, 'a', compression=zipfile.ZIP_DEFLATED) as z:
    z.write(dex, 'classes.dex')
PY
  if [ -n "$ZIPALIGN" ]; then
    "$ZIPALIGN" -f 4 "$BUILD_DIR/unaligned.apk" "$BUILD_DIR/aligned.apk"
  else
    echo "zipalign unavailable for this Termux architecture; preserving valid unaligned APK for now."
    cp "$BUILD_DIR/unaligned.apk" "$BUILD_DIR/aligned.apk"
  fi
  if [ ! -f "$KEYSTORE" ]; then
    echo 'Creating a separate development signing key (not the original app key).'
    KEY_PASS="$(od -An -N24 -tx1 /dev/urandom | tr -d ' \n')"
    printf '%s\n' "$KEY_PASS" > "$ROOT/.offread-dev-keypass"
    chmod 600 "$ROOT/.offread-dev-keypass"
    keytool -genkeypair -noprompt -keystore "$KEYSTORE" \
      -storepass "$KEY_PASS" -keypass "$KEY_PASS" -alias offread-dev \
      -keyalg RSA -keysize 2048 -validity 10000 \
      -dname 'CN=OffRead Development, OU=Development, O=OffRead, C=IN'
  fi
  KEY_PASS="$(cat "$ROOT/.offread-dev-keypass")"
  OUT="$BUILD_DIR/OffRead-$TYPE.apk"
  "$APKSIGNER" sign --ks "$KEYSTORE" --ks-pass "pass:$KEY_PASS" \
    --ks-key-alias offread-dev --key-pass "pass:$KEY_PASS" \
    --out "$OUT" "$BUILD_DIR/aligned.apk"
  "$APKSIGNER" verify --verbose "$OUT"
  sha256sum "$OUT" > "$OUT.sha256"
  echo "Built: $OUT"
  ls -lh "$OUT" "$OUT.sha256"
}

case "$ACTION" in
  clean) rm -rf "$BUILD_DIR" ;;
  test) run_tests ;;
  debug) build_apk debug ;;
  release) build_apk release ;;
  all) run_tests; build_apk debug; build_apk release ;;
  *) echo "Usage: $0 {clean|test|debug|release|all}" >&2; exit 2 ;;
esac
