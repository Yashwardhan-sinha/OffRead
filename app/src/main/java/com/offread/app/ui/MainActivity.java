package com.offread.app.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.offread.app.R;
import com.offread.app.data.AppPreferences;
import com.offread.app.data.Article;
import com.offread.app.data.ArticleRepository;
import com.offread.app.parser.ArticleExtractor;
import com.offread.app.parser.ReadabilityResult;
import com.offread.app.parser.TextAnalysisUtil;
import com.offread.app.tts.ArticleSpeaker;

import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private EditText editUrl;
    private Button btnOpenArticle;
    private ProgressBar progressArticle;
    private TextView tvArticleTitle;
    private TextView tvArticleMeta;
    private ImageView imgArticleCover;
    private TextView tvArticleContent;
    private TextView tvStatusNotice;

    private ImageButton btnBookmark;
    private ImageButton btnListen;
    private ImageButton btnThemeToggle;

    private boolean isBookmarked = false;
    private boolean isListening = false;
    private AppPreferences preferences;
    private ArticleRepository repository;
    private Article currentArticle = null;
    private ArticleSpeaker speaker = null;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = new AppPreferences(this);
        repository = ArticleRepository.getInstance(this);

        applyCurrentTheme();
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
        initTts();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (speaker != null && isListening) {
            speaker.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speaker != null) {
            speaker.shutdown();
        }
        executor.shutdown();
    }

    private void applyCurrentTheme() {
        String theme = preferences.getTheme();
        if (AppPreferences.THEME_DARK.equals(theme) || AppPreferences.THEME_BLACK.equals(theme)) {
            setTheme(R.style.Theme_OffRead);
        } else {
            setTheme(R.style.Theme_OffRead);
        }
    }

    private void initViews() {
        editUrl = findViewById(R.id.edit_url);
        btnOpenArticle = findViewById(R.id.btn_open_article);
        progressArticle = findViewById(R.id.progress_article);
        tvArticleTitle = findViewById(R.id.tv_article_title);
        tvArticleMeta = findViewById(R.id.tv_article_meta);
        imgArticleCover = findViewById(R.id.img_article_cover);
        tvArticleContent = findViewById(R.id.tv_article_content);
        tvStatusNotice = findViewById(R.id.tv_status_notice);

        btnBookmark = findViewById(R.id.btn_bookmark);
        btnListen = findViewById(R.id.btn_listen);
        btnThemeToggle = findViewById(R.id.btn_theme_toggle);

        updateBookmarkUI();
        updateListenUI();
        updateThemeIcon();
    }

    private void initTts() {
        speaker = new ArticleSpeaker(this, new ArticleSpeaker.SpeakerListener() {
            @Override
            public void onInitialized(boolean success) {
            }

            @Override
            public void onSentenceChanged(int index, int total, String sentence) {
            }

            @Override
            public void onPlaybackStateChanged(boolean isPlayingState) {
                runOnUiThread(() -> {
                    isListening = isPlayingState;
                    updateListenUI();
                });
            }

            @Override
            public void onPlaybackCompleted() {
                runOnUiThread(() -> {
                    isListening = false;
                    updateListenUI();
                });
            }
        });
    }

    private void setupListeners() {
        btnOpenArticle.setOnClickListener(v -> handleOpenArticle());

        btnBookmark.setOnClickListener(v -> {
            if (currentArticle != null && currentArticle.getId() > 0) {
                boolean fav = repository.toggleFavorite(currentArticle.getId());
                currentArticle.setFavorite(fav);
                isBookmarked = fav;
                updateBookmarkUI();
                Toast.makeText(this, isBookmarked ? "Saved to Bookmarks" : "Removed from Bookmarks", Toast.LENGTH_SHORT).show();
            } else if (currentArticle != null) {
                isBookmarked = !isBookmarked;
                currentArticle.setFavorite(isBookmarked);
                long id = repository.insertArticle(currentArticle);
                currentArticle.setId(id);
                updateBookmarkUI();
                Toast.makeText(this, isBookmarked ? "Article bookmarked locally" : "Bookmark removed", Toast.LENGTH_SHORT).show();
            } else {
                isBookmarked = !isBookmarked;
                updateBookmarkUI();
                Toast.makeText(this, isBookmarked ? "Article bookmarked locally" : "Bookmark removed", Toast.LENGTH_SHORT).show();
            }
        });

        btnListen.setOnClickListener(v -> {
            if (currentArticle == null || currentArticle.getPlainText().trim().isEmpty()) {
                Toast.makeText(this, "Please open an article first to listen", Toast.LENGTH_SHORT).show();
                return;
            }

            if (speaker == null) {
                Toast.makeText(this, "Speech engine initializing, please wait…", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isListening) {
                speaker.pause();
            } else {
                speaker.play();
            }
        });

        btnThemeToggle.setOnClickListener(v -> {
            String current = preferences.getTheme();
            if (AppPreferences.THEME_DARK.equals(current)) {
                preferences.setTheme(AppPreferences.THEME_LIGHT);
                Toast.makeText(this, "Switched to Light Theme", Toast.LENGTH_SHORT).show();
            } else {
                preferences.setTheme(AppPreferences.THEME_DARK);
                Toast.makeText(this, "Switched to Dark Theme", Toast.LENGTH_SHORT).show();
            }
            recreate();
        });
    }

    private void handleOpenArticle() {
        String inputUrl = editUrl.getText().toString().trim();

        // 1. Check for empty URL
        if (inputUrl.isEmpty()) {
            String errorMsg = "Please enter an article URL";
            tvStatusNotice.setText(errorMsg);
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            return;
        }

        // Auto-prepend https if missing scheme
        if (!inputUrl.startsWith("http://") && !inputUrl.startsWith("https://")) {
            inputUrl = "https://" + inputUrl;
            editUrl.setText(inputUrl);
        }

        // 2. Validate URL structure
        if (!isValidHttpUrl(inputUrl)) {
            String errorMsg = "Invalid URL. Please enter a valid HTTP or HTTPS address.";
            tvStatusNotice.setText(errorMsg);
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Check network state if available
        if (!isNetworkAvailable()) {
            String errorMsg = "No internet connection. Please check your network and try again.";
            tvStatusNotice.setText(errorMsg);
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Set Loading State
        final String finalTargetUrl = inputUrl;
        btnOpenArticle.setEnabled(false);
        progressArticle.setVisibility(View.VISIBLE);
        tvStatusNotice.setText("Fetching and extracting article…");

        if (speaker != null) {
            speaker.stop();
        }

        // 5. Asynchronous Fetch & Extraction
        executor.execute(() -> {
            try {
                ReadabilityResult result = ArticleExtractor.fetchFromUrl(finalTargetUrl);

                if (result == null || result.getPlainText().trim().isEmpty() || result.getWordCount() < 5) {
                    throw new Exception("No meaningful article content found at this address.");
                }

                // Lead image download
                Bitmap heroBitmap = null;
                if (!result.getLeadImageUrl().isEmpty()) {
                    heroBitmap = downloadBitmap(result.getLeadImageUrl());
                }

                // Check if existing article in DB
                Article existing = repository.getArticleByUrl(finalTargetUrl);
                Article articleToSave = existing != null ? existing : new Article();

                String domain = !result.getSiteName().isEmpty() ?
                        result.getSiteName() : TextAnalysisUtil.extractDomain(finalTargetUrl);
                String canonical = !result.getCanonicalUrl().isEmpty() ?
                        result.getCanonicalUrl() : finalTargetUrl;

                articleToSave.setTitle(result.getTitle());
                articleToSave.setAuthor(result.getAuthor());
                articleToSave.setUrl(finalTargetUrl);
                articleToSave.setCanonicalUrl(canonical);
                articleToSave.setDomain(domain);
                articleToSave.setContent(result.getContent());
                articleToSave.setPlainText(result.getPlainText());
                articleToSave.setExcerpt(result.getExcerpt());
                articleToSave.setWordCount(result.getWordCount());
                articleToSave.setReadingTimeMinutes(result.getReadingTimeMinutes());
                articleToSave.setPublishedDate(result.getPublishedDate());
                articleToSave.setImageUrl(result.getLeadImageUrl());
                articleToSave.setCategory(domain);

                if (existing != null) {
                    repository.updateArticle(articleToSave);
                } else {
                    long newId = repository.insertArticle(articleToSave);
                    articleToSave.setId(newId);
                }

                final Bitmap finalHeroBitmap = heroBitmap;
                final Article finalArticle = articleToSave;

                runOnUiThread(() -> {
                    btnOpenArticle.setEnabled(true);
                    progressArticle.setVisibility(View.GONE);

                    currentArticle = finalArticle;
                    isBookmarked = finalArticle.isFavorite();
                    updateBookmarkUI();

                    // Title
                    tvArticleTitle.setText(finalArticle.getTitle());

                    // Metadata byline: Author • Date • Domain • Reading Time
                    StringBuilder metaBuilder = new StringBuilder();
                    if (!finalArticle.getAuthor().isEmpty()) {
                        metaBuilder.append(finalArticle.getAuthor()).append(" • ");
                    }
                    if (!finalArticle.getPublishedDate().isEmpty()) {
                        metaBuilder.append(finalArticle.getPublishedDate()).append(" • ");
                    }
                    metaBuilder.append(finalArticle.getDomain()).append(" • ");
                    metaBuilder.append(finalArticle.getReadingTimeMinutes()).append(" min read");
                    tvArticleMeta.setText(metaBuilder.toString());

                    // Hero Image
                    if (finalHeroBitmap != null) {
                        imgArticleCover.setImageBitmap(finalHeroBitmap);
                        imgArticleCover.setVisibility(View.VISIBLE);
                    } else {
                        imgArticleCover.setVisibility(View.GONE);
                    }

                    // Content Body
                    tvArticleContent.setText(finalArticle.getContent());

                    // Load TTS text
                    if (speaker != null) {
                        speaker.loadText(finalArticle.getPlainText());
                    }

                    String successMsg = "Article loaded successfully (" + finalArticle.getWordCount() + " words)";
                    tvStatusNotice.setText(successMsg);
                    Toast.makeText(MainActivity.this, "Article extracted cleanly!", Toast.LENGTH_SHORT).show();
                });

            } catch (Throwable t) {
                final String friendlyMessage = resolveErrorMessage(t);
                runOnUiThread(() -> {
                    btnOpenArticle.setEnabled(true);
                    progressArticle.setVisibility(View.GONE);
                    tvStatusNotice.setText(friendlyMessage);
                    Toast.makeText(MainActivity.this, friendlyMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private boolean isValidHttpUrl(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) return false;
        try {
            URI uri = new URI(urlString.trim());
            String scheme = uri.getScheme();
            if (scheme == null) return false;
            String lower = scheme.toLowerCase(Locale.ROOT);
            if (!"http".equals(lower) && !"https".equals(lower)) return false;
            String host = uri.getHost();
            return host != null && host.contains(".");
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if (cm == null) return true;
            NetworkInfo active = cm.getActiveNetworkInfo();
            return active != null && active.isConnected();
        } catch (Exception e) {
            return true;
        }
    }

    private String resolveErrorMessage(Throwable t) {
        if (t instanceof UnknownHostException || t instanceof ConnectException) {
            return "No internet connection. Please check your network and try again.";
        }
        if (t instanceof SocketTimeoutException) {
            return "Connection timed out. The server took too long to respond.";
        }
        String msg = t.getMessage();
        if (msg != null) {
            if (msg.contains("HTTP error")) {
                return "HTTP error: " + msg;
            }
            if (msg.contains("No meaningful article content")) {
                return "No meaningful article content found at this address.";
            }
            if (msg.contains("timed out")) {
                return "Connection timed out. Please try again later.";
            }
        }
        return "Unable to parse this webpage into readable text.";
    }

    private Bitmap downloadBitmap(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) return null;
        try {
            URL url = new URL(imageUrl.trim());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 13)");
            int code = conn.getResponseCode();
            if (code >= 200 && code < 300) {
                try (InputStream in = conn.getInputStream()) {
                    return BitmapFactory.decodeStream(in);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void updateBookmarkUI() {
        if (isBookmarked) {
            btnBookmark.setImageResource(R.drawable.ic_bookmark_filled);
            btnBookmark.setColorFilter(Color.parseColor("#F59E0B"));
        } else {
            btnBookmark.setImageResource(R.drawable.ic_bookmark);
            btnBookmark.setColorFilter(Color.parseColor("#94A3B8"));
        }
    }

    private void updateListenUI() {
        if (isListening) {
            btnListen.setColorFilter(Color.parseColor("#0D9488"));
        } else {
            btnListen.setColorFilter(Color.parseColor("#94A3B8"));
        }
    }

    private void updateThemeIcon() {
        String current = preferences.getTheme();
        if (AppPreferences.THEME_DARK.equals(current)) {
            btnThemeToggle.setColorFilter(Color.parseColor("#F59E0B"));
        } else {
            btnThemeToggle.setColorFilter(Color.parseColor("#64748B"));
        }
    }
}
