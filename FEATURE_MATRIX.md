# OffRead feature matrix

This matrix records features visible in the recovered source/resources. A code path being present does not mean every device-specific behavior has passed manual testing.

| Area | Evidence in recovered source | Status |
|---|---|---|
| URL article import | NetworkFetcher, ArticleExtractor, SafeUrl | Implemented in source; manual device test pending |
| Android Share menu | ShareReceiverActivity and manifest SEND intent | Implemented in source; manual test pending |
| Manual notes / offline drafts | AddArticleActivity, ArticleRepository | Implemented in source; manual test pending |
| Local article library | SQLite schema v3, ArticleRepository | Implemented; 28-test suite covers core model behavior |
| Search / filters / sorting | MainActivity, ArticleRepository | Present; manual UI test pending |
| Favorites and archive | Article, ArticleAdapter, repository | Present; manual UI test pending |
| Bookmarks | BookmarksActivity, BookmarkAdapter | Present; manual UI test pending |
| Reading progress | Article fields and repository | Present; manual UI test pending |
| Reader themes and typography | ThemeManager, ThemedActivity, appearance resources | Present; manual UI test pending |
| Article images | RemoteImageLoader, ArticleContentBlocks | Present; requires network |
| Rich content | ReaderViewBuilder, ArticleContentBlocks | Present; manual content fixtures needed |
| Text-to-speech | ArticleSpeaker, SpeechLanguages, SpeechLanguageDialog | Present; depends on installed Android TTS voices |
| RSVP speed reader | SpeedReaderActivity | Present; manual UI test pending |
| Reading statistics | ReadingStats, StatisticsActivity, reading_logs | Present; manual UI test pending |
| JSON export/import | BackupHelper, SettingsActivity, BackupFileProvider | Present; backup round-trip test pending |
| HTML parsing/readability | HtmlParser, ReadabilityEngine, MetadataExtractor, JsonLdExtractor | 28 JVM tests pass |
| Boilerplate/ad cleanup | AdFilter | 28 JVM tests pass for relevant parser cases |
| URL safety | SafeUrl and NetworkFetcher redirect checks | Present; dedicated security tests still needed |

## Database

Current schema version: 3.

Tables:
- articles
- highlights
- reading_logs

The app has migration logic for article metadata columns introduced in schema versions 2 and 3. A real-device upgrade test from an older populated database remains pending.

## Permissions and network

The decoded manifest requests only:
- INTERNET
- ACCESS_NETWORK_STATE

Network access is used for fetching public web pages and remote article images. Saved content is stored locally for offline reading.
