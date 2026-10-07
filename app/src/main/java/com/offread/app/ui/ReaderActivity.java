package com.offread.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.offread.app.R;
import com.offread.app.data.AppPreferences;
import com.offread.app.data.Article;
import com.offread.app.data.ArticleRepository;
import com.offread.app.tts.ArticleSpeaker;
import com.offread.app.util.UIHelper;

public class ReaderActivity extends Activity {

    public static final String EXTRA_ARTICLE_ID = "extra_article_id";

    private ArticleRepository repository;
    private AppPreferences preferences;
    private Article article;
    private ArticleSpeaker speaker;

    private View rootLayout;
    private LinearLayout toolbarReader;
    private ScrollView scrollReader;
    private ProgressBar progressTop;
    private TextView tvProgressHeader;
    private TextView tvTitle;
    private TextView tvMeta;
    private TextView tvBody;
    private View viewDivider;
    private ImageButton btnFavorite;
    private ImageButton btnArchive;

    // TTS bar views
    private LinearLayout layoutTtsBar;
    private ImageButton btnTtsPlayPause;
    private ImageButton btnTtsStop;
    private TextView tvTtsStatus;
    private TextView tvTtsSpeed;
    private ImageButton btnTtsClose;

    // Bottom action buttons
    private Button btnMarkCompleted;
    private Button btnOpenSource;

    private long sessionStartTime = 0;
    private int sessionTotalSeconds = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        repository = ArticleRepository.getInstance(this);
        preferences = new AppPreferences(this);

        long articleId = getIntent().getLongExtra(EXTRA_ARTICLE_ID, -1);
        article = repository.getArticle(articleId);

        if (article == null) {
            Toast.makeText(this, "Article not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        bindArticleData();
        applyPreferences();
        setupScrollListener();
        initTts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sessionStartTime = System.currentTimeMillis();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sessionStartTime > 0) {
            long durationSec = (System.currentTimeMillis() - sessionStartTime) / 1000;
            sessionTotalSeconds += (int) durationSec;
            if (sessionTotalSeconds > 5 && article != null) {
                int wordsRead = Math.min(article.getWordCount(), (int) (sessionTotalSeconds * (3.5)));
                repository.recordReadingSession(article.getId(), sessionTotalSeconds, wordsRead);
                sessionTotalSeconds = 0;
            }
        }
        saveCurrentProgress();
        if (speaker != null) {
            speaker.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speaker != null) {
            speaker.shutdown();
        }
    }

    private void initViews() {
        rootLayout = findViewById(R.id.root_reader);
        toolbarReader = findViewById(R.id.toolbar_reader);
        scrollReader = findViewById(R.id.scroll_reader);
        progressTop = findViewById(R.id.progress_reading_top);
        tvProgressHeader = findViewById(R.id.tv_reader_progress_header);
        tvTitle = findViewById(R.id.tv_reader_title);
        tvMeta = findViewById(R.id.tv_reader_meta);
        tvBody = findViewById(R.id.tv_reader_body);
        viewDivider = findViewById(R.id.view_reader_divider);

        ImageButton btnBack = findViewById(R.id.btn_reader_back);
        ImageButton btnTts = findViewById(R.id.btn_reader_tts);
        ImageButton btnSpeed = findViewById(R.id.btn_reader_speed);
        ImageButton btnAppearance = findViewById(R.id.btn_reader_appearance);
        btnFavorite = findViewById(R.id.btn_reader_favorite);
        btnArchive = findViewById(R.id.btn_reader_archive);
        ImageButton btnShare = findViewById(R.id.btn_reader_share);

        layoutTtsBar = findViewById(R.id.layout_tts_bar);
        btnTtsPlayPause = findViewById(R.id.btn_tts_play_pause);
        btnTtsStop = findViewById(R.id.btn_tts_stop);
        tvTtsStatus = findViewById(R.id.tv_tts_status);
        tvTtsSpeed = findViewById(R.id.tv_tts_speed);
        btnTtsClose = findViewById(R.id.btn_tts_close);

        btnMarkCompleted = findViewById(R.id.btn_reader_mark_completed);
        btnOpenSource = findViewById(R.id.btn_reader_open_source);

        btnBack.setOnClickListener(v -> finish());

        btnFavorite.setOnClickListener(v -> {
            boolean fav = repository.toggleFavorite(article.getId());
            article.setFavorite(fav);
            updateFavoriteIcon();
        });

        btnArchive.setOnClickListener(v -> {
            boolean arch = repository.toggleArchive(article.getId());
            article.setArchived(arch);
            updateArchiveIcon();
            Toast.makeText(this, arch ? "Moved to Archive" : "Restored to Library", Toast.LENGTH_SHORT).show();
        });

        btnSpeed.setOnClickListener(v -> {
            Intent intent = new Intent(ReaderActivity.this, SpeedReaderActivity.class);
            intent.putExtra(SpeedReaderActivity.EXTRA_ARTICLE_ID, article.getId());
            startActivity(intent);
        });

        btnTts.setOnClickListener(v -> {
            if (layoutTtsBar.getVisibility() == View.VISIBLE) {
                layoutTtsBar.setVisibility(View.GONE);
                if (speaker != null) speaker.stop();
            } else {
                layoutTtsBar.setVisibility(View.VISIBLE);
                if (speaker != null) speaker.loadText(article.getPlainText());
            }
        });

        btnAppearance.setOnClickListener(v -> ViewAppearanceDialog.show(ReaderActivity.this, preferences, this::applyPreferences));

        btnShare.setOnClickListener(v -> shareArticle());

        btnMarkCompleted.setOnClickListener(v -> {
            repository.markAsCompleted(article.getId());
            article.setProgressPercent(100);
            article.setArchived(true);
            Toast.makeText(this, "Marked as Finished!", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnOpenSource.setOnClickListener(v -> {
            String url = article.getUrl();
            if (url != null && !url.isEmpty()) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    Toast.makeText(this, "Could not open browser", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No original URL available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindArticleData() {
        tvTitle.setText(article.getTitle());

        String author = article.getAuthor();
        String domain = article.getDomain();
        String time = article.getReadingTimeMinutes() + " min read";
        String metaText = (author != null && !author.isEmpty() ? author + " • " : "") +
                (domain != null && !domain.isEmpty() ? domain + " • " : "") + time;
        tvMeta.setText(metaText);

        tvBody.setText(article.getContent());

        if (article.getUrl() == null || article.getUrl().isEmpty()) {
            btnOpenSource.setVisibility(View.GONE);
        } else {
            btnOpenSource.setVisibility(View.VISIBLE);
        }

        updateFavoriteIcon();
        updateArchiveIcon();

        // Restore scroll position once laid out
        scrollReader.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                scrollReader.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (article.getScrollPosition() > 0) {
                    scrollReader.scrollTo(0, article.getScrollPosition());
                }
            }
        });
    }

    private void setupScrollListener() {
        scrollReader.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY = scrollReader.getScrollY();
            int totalHeight = scrollReader.getChildAt(0).getHeight() - scrollReader.getHeight();
            if (totalHeight > 0) {
                int percent = (int) (((float) scrollY / totalHeight) * 100);
                percent = Math.max(0, Math.min(100, percent));
                progressTop.setProgress(percent);
                tvProgressHeader.setText(percent + "% read • " + article.getDomain());
            }
        });
    }

    private void saveCurrentProgress() {
        if (article == null) return;
        int scrollY = scrollReader.getScrollY();
        int totalHeight = scrollReader.getChildAt(0).getHeight() - scrollReader.getHeight();
        int percent = 0;
        if (totalHeight > 0) {
            percent = (int) (((float) scrollY / totalHeight) * 100);
            percent = Math.max(0, Math.min(100, percent));
        }
        article.setScrollPosition(scrollY);
        article.setProgressPercent(percent);
        repository.updateReadingProgress(article.getId(), scrollY, percent);
    }

    private void applyPreferences() {
        // Theme colors
        String theme = preferences.getTheme();
        UIHelper.ReaderThemeColors colors = UIHelper.getThemeColors(theme);

        rootLayout.setBackgroundColor(colors.backgroundColor);
        toolbarReader.setBackgroundColor(colors.surfaceColor);
        layoutTtsBar.setBackgroundColor(colors.surfaceColor);
        viewDivider.setBackgroundColor(colors.secondaryTextColor);

        tvTitle.setTextColor(colors.textColor);
        tvMeta.setTextColor(colors.secondaryTextColor);
        tvBody.setTextColor(colors.textColor);
        tvProgressHeader.setTextColor(colors.secondaryTextColor);
        tvTtsStatus.setTextColor(colors.secondaryTextColor);

        UIHelper.applySystemBars(this, colors.backgroundColor, colors.isLight);

        // Typography
        tvBody.setTypeface(UIHelper.getTypeface(preferences.getFontFamily()));
        tvBody.setTextSize(preferences.getFontSize());
        tvBody.setLineSpacing(0, preferences.getLineSpacing());

        // Keep screen on
        if (preferences.isKeepScreenOn()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        // Fullscreen
        if (preferences.isFullscreen()) {
            toolbarReader.setVisibility(View.GONE);
        } else {
            toolbarReader.setVisibility(View.VISIBLE);
        }
    }

    private void initTts() {
        speaker = new ArticleSpeaker(this, new ArticleSpeaker.SpeakerListener() {
            @Override
            public void onInitialized(boolean success) {
                if (success) {
                    speaker.setSpeechRate(preferences.getTtsSpeed());
                }
            }

            @Override
            public void onSentenceChanged(int index, int total, String sentence) {
                runOnUiThread(() -> tvTtsStatus.setText("Reading (" + (index + 1) + "/" + total + ")"));
            }

            @Override
            public void onPlaybackStateChanged(boolean isPlaying) {
                runOnUiThread(() -> {
                    btnTtsPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
                    if (!isPlaying) tvTtsStatus.setText("Audio Reader Paused");
                });
            }

            @Override
            public void onPlaybackCompleted() {
                runOnUiThread(() -> {
                    tvTtsStatus.setText("Finished Reading");
                    btnTtsPlayPause.setImageResource(R.drawable.ic_play);
                });
            }
        });

        btnTtsPlayPause.setOnClickListener(v -> {
            if (speaker.isPlaying()) {
                speaker.pause();
            } else {
                speaker.play();
            }
        });

        btnTtsStop.setOnClickListener(v -> speaker.stop());

        tvTtsSpeed.setOnClickListener(v -> {
            float cur = speaker.getSpeechRate();
            float next = 1.0f;
            if (Math.abs(cur - 1.0f) < 0.1f) next = 1.25f;
            else if (Math.abs(cur - 1.25f) < 0.1f) next = 1.5f;
            else if (Math.abs(cur - 1.5f) < 0.1f) next = 2.0f;
            else if (Math.abs(cur - 2.0f) < 0.1f) next = 0.75f;
            else next = 1.0f;

            speaker.setSpeechRate(next);
            preferences.setTtsSpeed(next);
            tvTtsSpeed.setText(next + "x");
        });

        btnTtsClose.setOnClickListener(v -> {
            speaker.stop();
            layoutTtsBar.setVisibility(View.GONE);
        });
    }

    private void updateFavoriteIcon() {
        if (article.isFavorite()) {
            btnFavorite.setImageResource(R.drawable.ic_star_filled);
            btnFavorite.setColorFilter(Color.parseColor("#F59E0B"));
        } else {
            btnFavorite.setImageResource(R.drawable.ic_star);
            btnFavorite.setColorFilter(Color.parseColor("#94A3B8"));
        }
    }

    private void updateArchiveIcon() {
        if (article.isArchived()) {
            btnArchive.setImageResource(R.drawable.ic_unarchive);
            btnArchive.setColorFilter(Color.parseColor("#0D9488"));
        } else {
            btnArchive.setImageResource(R.drawable.ic_archive);
            btnArchive.setColorFilter(Color.parseColor("#94A3B8"));
        }
    }

    private void shareArticle() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        String shareBody = article.getTitle() + "\n\n" +
                (article.getUrl() != null && !article.getUrl().isEmpty() ? article.getUrl() + "\n\n" : "") +
                article.getExcerpt();
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Share Article"));
    }
}
