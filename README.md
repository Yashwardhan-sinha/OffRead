# OffRead

OffRead is a native Android reading app that saves web articles and notes into a local library for distraction-free reading.

**Offline behavior:** saved articles and notes are stored locally and can be read offline. Fetching a new web page and loading remote article images require an internet connection. OffRead is offline-first, not an app that never uses the network.

## Features present in the recovered APK source

- Save public HTTP/HTTPS articles from a URL or Android Share menu.
- Create manual notes and offline drafts.
- Local SQLite library with search, categories/tags, favorites, archive, reading progress and history.
- Article extraction with metadata/JSON-LD handling, readability scoring, boilerplate/ad cleanup and URL safety checks.
- Reader themes, font controls, reading width, line spacing, fullscreen and keep-screen-on options.
- Article images, links, quotations, code blocks and structured content rendering.
- Bookmarks, text-to-speech with language selection, and RSVP speed reading.
- Reading statistics and local JSON export/import.

See FEATURE_MATRIX.md for implementation evidence and remaining manual checks.

## Architecture

- Java source targeting Java 17; built with the Termux OpenJDK toolchain.
- Native Android framework APIs; no external runtime libraries are required by the recovered source.
- SQLite through SQLiteOpenHelper; current database schema version is 3.
- Custom HTML/metadata/readability parser and bounded network fetcher.
- Android TextToSpeech for read-aloud support.

## Build in Termux

The supported build path is the self-contained CLI script. It does not require Gradle or an internet download during a normal build.

~~~bash
./build_apk.sh test
./build_apk.sh debug
./build_apk.sh release
./build_apk.sh all
~~~

Artifacts are written to build_cli/:

- OffRead-debug.apk
- OffRead-release.apk
- Matching SHA-256 files

The local minimal Android SDK is expected at $HOME/Android/Sdk. Set ANDROID_SDK_ROOT to override it. The script uses Termux aapt2 and apksigner, plus the local Android API 35 platform jar and D8 compiler.

**Signing note:** the script creates a local development key on first build. Its key is not the original key that signed the installed OffRead APK. Do not uninstall or overwrite your working installation to test these builds. The release task is a locally signed development release, not a production-store release.

**Alignment note:** this ARM64 Termux environment currently has no compatible zipalign executable. The script reports this and produces a signed, valid but unaligned APK. See KNOWN_LIMITATIONS.md.

## Recovery baseline

- GitHub baseline commit: 8a6ff2885929ad162e8a3b78775e226673e65106
- Installed APK version: 1.0.0-rot1, version code 2
- Package: com.offread.app
- Recovery branch: recovery/apk-baseline

The original GitHub source and the exact installed APK are preserved in the recovery archive. See RECOVERY_REPORT.md.

## Privacy and network behavior

The recovered manifest requests INTERNET and ACCESS_NETWORK_STATE because URL extraction and remote image loading need network access. Saved article content is stored locally. No third-party analytics SDK was identified in the APK inspection, but this is not a formal network-traffic audit.

## Development

Read DEVELOPMENT.md before changing the recovered baseline. Keep source changes on a branch, run the test suite before building, and keep the original installed app untouched until a separately signed test build has been manually verified.
