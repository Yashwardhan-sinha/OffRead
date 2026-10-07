package com.offread.app.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.offread.app.R;
import com.offread.app.data.Article;
import com.offread.app.data.ArticleRepository;
import com.offread.app.parser.ArticleExtractor;
import com.offread.app.parser.ReadabilityResult;
import com.offread.app.parser.TextAnalysisUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddArticleActivity extends Activity {

    private ArticleRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private TextView tabUrl;
    private TextView tabText;
    private LinearLayout layoutFormUrl;
    private LinearLayout layoutFormText;

    // URL Form
    private EditText editUrlInput;
    private EditText editUrlTags;
    private ProgressBar progressUrlFetch;
    private TextView tvUrlFetchStatus;
    private Button btnSubmitUrl;

    // Manual Form
    private EditText editManualTitle;
    private EditText editManualAuthor;
    private EditText editManualTags;
    private EditText editManualContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_article);

        repository = ArticleRepository.getInstance(this);

        initViews();
        setupTabs();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btn_add_back);
        tabUrl = findViewById(R.id.tab_mode_url);
        tabText = findViewById(R.id.tab_mode_text);
        layoutFormUrl = findViewById(R.id.layout_form_url);
        layoutFormText = findViewById(R.id.layout_form_text);

        editUrlInput = findViewById(R.id.edit_url_input);
        editUrlTags = findViewById(R.id.edit_url_tags);
        Button btnPasteUrl = findViewById(R.id.btn_paste_url);
        progressUrlFetch = findViewById(R.id.progress_url_fetch);
        tvUrlFetchStatus = findViewById(R.id.tv_url_fetch_status);
        btnSubmitUrl = findViewById(R.id.btn_submit_url);

        editManualTitle = findViewById(R.id.edit_manual_title);
        editManualAuthor = findViewById(R.id.edit_manual_author);
        editManualTags = findViewById(R.id.edit_manual_tags);
        editManualContent = findViewById(R.id.edit_manual_content);
        Button btnSubmitManual = findViewById(R.id.btn_submit_manual);

        btnBack.setOnClickListener(v -> finish());

        btnPasteUrl.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null && cm.hasPrimaryClip()) {
                ClipData.Item item = cm.getPrimaryClip().getItemAt(0);
                if (item != null && item.getText() != null) {
                    editUrlInput.setText(item.getText().toString().trim());
                }
            }
        });

        btnSubmitUrl.setOnClickListener(v -> handleUrlSubmit());
        btnSubmitManual.setOnClickListener(v -> handleManualSubmit());
    }

    private void setupTabs() {
        tabUrl.setOnClickListener(v -> {
            tabUrl.setBackgroundResource(R.drawable.bg_chip_active);
            tabUrl.setTextColor(getResources().getColor(R.color.text_primary));
            tabText.setBackgroundResource(R.drawable.bg_chip);
            tabText.setTextColor(getResources().getColor(R.color.text_secondary));

            layoutFormUrl.setVisibility(View.VISIBLE);
            layoutFormText.setVisibility(View.GONE);
        });

        tabText.setOnClickListener(v -> {
            tabText.setBackgroundResource(R.drawable.bg_chip_active);
            tabText.setTextColor(getResources().getColor(R.color.text_primary));
            tabUrl.setBackgroundResource(R.drawable.bg_chip);
            tabUrl.setTextColor(getResources().getColor(R.color.text_secondary));

            layoutFormUrl.setVisibility(View.GONE);
            layoutFormText.setVisibility(View.VISIBLE);
        });
    }

    private void handleUrlSubmit() {
        String url = editUrlInput.getText().toString().trim();
        if (url.isEmpty()) {
            Toast.makeText(this, "Please enter a valid URL", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        final String finalUrl = url;
        final String tags = editUrlTags.getText().toString().trim();

        // Show loading state
        btnSubmitUrl.setEnabled(false);
        progressUrlFetch.setVisibility(View.VISIBLE);
        tvUrlFetchStatus.setVisibility(View.VISIBLE);
        tvUrlFetchStatus.setText(R.string.fetching_article);

        executor.execute(() -> {
            try {
                ReadabilityResult result = ArticleExtractor.fetchFromUrl(finalUrl);

                String domain = (result.getSiteName() != null && !result.getSiteName().isEmpty()) ?
                        result.getSiteName() : TextAnalysisUtil.extractDomain(finalUrl);
                String articleUrl = (result.getCanonicalUrl() != null && !result.getCanonicalUrl().isEmpty()) ?
                        result.getCanonicalUrl() : finalUrl;

                Article article = new Article();
                article.setTitle(result.getTitle());
                article.setAuthor(result.getAuthor());
                article.setUrl(articleUrl);
                article.setDomain(domain);
                article.setContent(result.getContent());
                article.setPlainText(result.getPlainText());
                article.setExcerpt(result.getExcerpt());
                article.setWordCount(result.getWordCount());
                article.setReadingTimeMinutes(result.getReadingTimeMinutes());
                article.setCategory(domain);
                article.setTags(tags);

                long id = repository.insertArticle(article);

                runOnUiThread(() -> {
                    Toast.makeText(AddArticleActivity.this, "Article saved offline!", Toast.LENGTH_SHORT).show();
                    Intent readerIntent = new Intent(AddArticleActivity.this, ReaderActivity.class);
                    readerIntent.putExtra(ReaderActivity.EXTRA_ARTICLE_ID, id);
                    startActivity(readerIntent);
                    finish();
                });
            } catch (Exception e) {
                // If offline or network fails, create offline placeholder so URL is not lost
                runOnUiThread(() -> {
                    Article fallback = new Article();
                    fallback.setTitle(TextAnalysisUtil.extractDomain(finalUrl));
                    fallback.setUrl(finalUrl);
                    fallback.setDomain(TextAnalysisUtil.extractDomain(finalUrl));
                    fallback.setContent("Could not download article immediately (" + e.getMessage() + ").\n\nOriginal URL: " + finalUrl);
                    fallback.setPlainText("Offline queued: " + finalUrl);
                    fallback.setExcerpt(finalUrl);
                    fallback.setTags(tags);
                    repository.insertArticle(fallback);

                    Toast.makeText(AddArticleActivity.this, "Saved as offline draft (network unavailable)", Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        });
    }

    private void handleManualSubmit() {
        String title = editManualTitle.getText().toString().trim();
        String author = editManualAuthor.getText().toString().trim();
        String tags = editManualTags.getText().toString().trim();
        String content = editManualContent.getText().toString().trim();

        if (content.isEmpty()) {
            Toast.makeText(this, "Please enter article content", Toast.LENGTH_SHORT).show();
            return;
        }

        ReadabilityResult result = ArticleExtractor.processUserText(title, author, content);

        Article article = new Article();
        article.setTitle(result.getTitle());
        article.setAuthor(result.getAuthor());
        article.setUrl("");
        article.setDomain("Note");
        article.setContent(result.getContent());
        article.setPlainText(result.getPlainText());
        article.setExcerpt(result.getExcerpt());
        article.setWordCount(result.getWordCount());
        article.setReadingTimeMinutes(result.getReadingTimeMinutes());
        article.setCategory("Note");
        article.setTags(tags);

        long id = repository.insertArticle(article);

        Toast.makeText(this, "Saved to library!", Toast.LENGTH_SHORT).show();
        Intent readerIntent = new Intent(AddArticleActivity.this, ReaderActivity.class);
        readerIntent.putExtra(ReaderActivity.EXTRA_ARTICLE_ID, id);
        startActivity(readerIntent);
        finish();
    }
}
