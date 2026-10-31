# Known limitations and remaining validation

These are explicit follow-up items, not hidden assumptions.

## 1. Original signing key is unavailable

The installed APK and the recovered workspace use different signing certificates. The original private key was not found in the inspected Termux home directory.

- Keep the original installed app.
- Do not uninstall it to test the recovered build.
- Do not assume the new APK can update the installed one.
- A separate test package/authority or a carefully backed-up migration plan is required before installation.

## 2. APK alignment

The SDK zipalign binary available through the copied Android SDK is x86-64 and cannot execute on this ARM64 Termux device. The current script skips alignment and clearly reports that fact. The signed APKs are valid, but alignment should be solved before production distribution.

## 3. Reconstructed source is not original source

JADX reconstructed Java-like source from DEX. One large method was restored from the GitHub source because the decompiler failed on it. Several other decompiler artifacts were repaired and covered by tests. This is a high-confidence development baseline, not a perfect recovery of lost local files.

## 4. Manual testing still required

The current test suite has 28 passing JVM tests. The following still need real-device verification on a separate test install:

- Home, article list, search, filters, bookmarks and archive.
- URL import and Share-menu import.
- Image, quotation, link, code-block and long-article rendering.
- Theme, font, line spacing and fullscreen settings.
- TTS language selection and speed reader.
- JSON export/import round trip.
- Statistics and reading progress persistence.
- Database migration from older populated data.

## 5. Backup scope

The installed APK and original Git history are backed up. The app's private database was not extracted. Keep the existing installation and use OffRead's own export function before any uninstall or data migration.

## 6. Build-system scope

The recovered project has a working custom Termux CLI build script and a minimal local SDK. A Gradle wrapper is not currently included; the CLI script is the supported build path. Add a Gradle wrapper only if Android Studio/Gradle workflows become a real requirement.

## 7. Release signing

The current release task signs with a local development key. It is not a production-store signing configuration. Keep the key/password out of Git and establish a deliberate production signing and backup policy before distribution.
