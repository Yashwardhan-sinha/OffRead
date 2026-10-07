# OffRead 📖

> **A distraction-free, 100% offline-first article and reading sanctuary for Android.**

OffRead transforms online articles, essays, and notes into clean, quiet reading experiences. Free yourself from invasive cookie banners, intrusive autoplay ads, and noisy algorithmic feeds. Read offline anywhere—on flights, subways, retreats, or whenever you want peace and focus.

---

## ✨ Features

- **🌐 Offline-First Library**:
  - Save articles via Android's native system **Share** menu (`ACTION_SEND`) directly from browsers (Chrome, Firefox, Brave) or apps.
  - Direct URL extractor that strips clutter, scripts, trackers, ads, and navigation boilerplate using intelligent readability heuristics.
  - Manual quick-note & article creator for saving drafts, markdown clips, or study notes.
  - Fast, full-text SQLite search across titles, contents, authors, domains, and tags.
  - Filter by **All**, **Unread**, **Favorites**, and **Archive**, plus dynamic tag chips.

- **👓 Distraction-Free Typography & Reader**:
  - **4 Handcrafted Themes**:
    - *Slate Dark* (modern dark UI)
    - *Warm Sepia* (comfort book paper)
    - *Paper White* (clean editorial)
    - *AMOLED Black* (true #000000 black for OLED battery saving)
  - **Typography Controls**:
    - Choose between Serif, Sans-Serif, and Monospace typefaces.
    - Fine-grained font sizing (12sp to 32sp).
    - Adjustable line spacing (1.2x, 1.5x, 1.8x).
  - Immersive fullscreen reading mode (hides system bars on tap).
  - Keep-screen-on wake-lock toggle.
  - Auto-saving reading position and top progress bar.

- **⚡ RSVP Speed Reader (Rapid Serial Visual Presentation)**:
  - Spritz-style visual speed reader displaying words one at a time.
  - Optimal Recognition Point (ORP) highlighting in focal red to eliminate eye saccades.
  - Configurable reading speeds: **250**, **350**, **450**, and **600 WPM**.
  - Punctuation-aware pacing with interactive scrubbing slider.

- **🎧 Text-to-Speech (TTS) Voice Player**:
  - Offline voice reader powered by native Android `TextToSpeech`.
  - Sentence-by-sentence reading with auto-advancing audio player controls.
  - Variable playback speeds (0.75x, 1.0x, 1.25x, 1.5x, 2.0x).

- **📊 Reading Insights & Streaks**:
  - Track articles saved, articles finished, total words read, and total reading time.
  - Active day reading streaks to build a consistent reading habit.

- **🔒 100% Private & Open**:
  - No user accounts required.
  - Zero external tracking, zero analytics, zero network telemetry.
  - JSON Backup & Restore: export your entire library to a local file anytime.

---

## 🛠️ Architecture & Tech Stack

OffRead is engineered with a clean, high-performance architecture built directly against native Android APIs:

- **Language**: Java 17 / OpenJDK 21
- **Target SDK**: Android API 33 (Android 13)
- **Minimum SDK**: Android API 24 (Android 7.0 Nougat)
- **Database**: SQLite (`OffReadDbHelper` & `ArticleRepository`)
- **Parser**: Native HTTP stream reader with heuristic readability extractor (`ArticleExtractor`)
- **Speech**: Android `TextToSpeech` engine (`ArticleSpeaker`)
- **Zero Bloat**: 0 external heavy frameworks or proprietary tracking libraries.

---

## 📦 Building & Installing

### Building via Termux / CLI

OffRead includes a standalone build automation script (`build_apk.sh`) that compiles resources, bytecode, and signs APKs right on device or in headless CI:

```bash
# Run unit tests
./build_apk.sh test

# Build debug APK
./build_apk.sh debug

# Build release APK
./build_apk.sh release

# Run tests and build both APKs
./build_apk.sh all
```

Output APKs:
- `OffRead.apk` (Signed Release APK)
- `app-debug.apk` (Signed Debug APK)

### Building via Gradle / Android Studio

OffRead also adheres to the standard Android Gradle project layout:

```bash
./gradlew assembleRelease
```

---

## 📄 License

Open-source under the MIT License.
