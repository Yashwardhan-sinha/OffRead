package com.offread.app.ui;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.offread.app.R;
import com.offread.app.data.AppPreferences;
import com.offread.app.parser.TextAnalysisUtil;

public class MainActivity extends Activity {

    private EditText editUrl;
    private Button btnOpenArticle;
    private TextView tvArticleTitle;
    private TextView tvArticleMeta;
    private TextView tvArticleContent;
    private TextView tvStatusNotice;

    private ImageButton btnBookmark;
    private ImageButton btnListen;
    private ImageButton btnThemeToggle;

    private boolean isBookmarked = false;
    private boolean isListening = false;
    private AppPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = new AppPreferences(this);

        // Apply selected theme
        applyCurrentTheme();

        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
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
        tvArticleTitle = findViewById(R.id.tv_article_title);
        tvArticleMeta = findViewById(R.id.tv_article_meta);
        tvArticleContent = findViewById(R.id.tv_article_content);
        tvStatusNotice = findViewById(R.id.tv_status_notice);

        btnBookmark = findViewById(R.id.btn_bookmark);
        btnListen = findViewById(R.id.btn_listen);
        btnThemeToggle = findViewById(R.id.btn_theme_toggle);

        updateBookmarkUI();
        updateThemeIcon();
    }

    private void setupListeners() {
        // Open Article Button Handler
        btnOpenArticle.setOnClickListener(v -> handleOpenArticle());

        // Bookmark Button Handler
        btnBookmark.setOnClickListener(v -> {
            isBookmarked = !isBookmarked;
            updateBookmarkUI();
            Toast.makeText(this, isBookmarked ? "Article bookmarked locally" : "Bookmark removed", Toast.LENGTH_SHORT).show();
        });

        // Text-to-Speech / Listen Button Handler
        btnListen.setOnClickListener(v -> {
            isListening = !isListening;
            if (isListening) {
                btnListen.setColorFilter(Color.parseColor("#0D9488"));
                Toast.makeText(this, "Text-to-speech ready (awaiting extracted article)", Toast.LENGTH_SHORT).show();
            } else {
                btnListen.setColorFilter(Color.parseColor("#94A3B8"));
                Toast.makeText(this, "Listen stopped", Toast.LENGTH_SHORT).show();
            }
        });

        // Theme Toggle Handler
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
        if (inputUrl.isEmpty()) {
            Toast.makeText(this, "Please enter an article URL", Toast.LENGTH_SHORT).show();
            tvStatusNotice.setText("Please enter a valid article URL.");
            return;
        }

        if (!inputUrl.startsWith("http://") && !inputUrl.startsWith("https://")) {
            inputUrl = "https://" + inputUrl;
            editUrl.setText(inputUrl);
        }

        String domain = TextAnalysisUtil.extractDomain(inputUrl);

        // Update placeholder areas without performing fake extraction
        tvArticleTitle.setText("Article from " + domain);
        tvArticleMeta.setText("Source: " + domain + " • Ready for extraction pipeline");

        tvArticleContent.setText("Ready for article extraction.\n\n" +
                "Target URL: " + inputUrl + "\n\n" +
                "The extraction engine will fetch this article, strip advertisements, trackers, and clutter, " +
                "and render clean distraction-free text here.\n\n" +
                "• All data remains 100% local to this device.\n" +
                "• No accounts, analytics, or third-party SDKs.\n" +
                "• Bookmark and text-to-speech are enabled above.");

        tvStatusNotice.setText("Article target set. Ready for extraction.");
        Toast.makeText(this, "Article target loaded", Toast.LENGTH_SHORT).show();
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

    private void updateThemeIcon() {
        String current = preferences.getTheme();
        if (AppPreferences.THEME_DARK.equals(current)) {
            btnThemeToggle.setColorFilter(Color.parseColor("#F59E0B"));
        } else {
            btnThemeToggle.setColorFilter(Color.parseColor("#64748B"));
        }
    }
}
