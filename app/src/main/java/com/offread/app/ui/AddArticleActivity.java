package com.offread.app.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
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
import com.offread.app.parser.NetworkFetcher;
import com.offread.app.parser.ReadabilityResult;
import com.offread.app.parser.SafeUrl;
import com.offread.app.parser.TextAnalysisUtil;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* JADX INFO: loaded from: classes.dex */
public class AddArticleActivity extends ThemedActivity {
    private Button btnSubmitUrl;
    private EditText editManualAuthor;
    private EditText editManualContent;
    private EditText editManualTags;
    private EditText editManualTitle;
    private EditText editUrlInput;
    private EditText editUrlTags;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private LinearLayout layoutFormText;
    private LinearLayout layoutFormUrl;
    private ProgressBar progressUrlFetch;
    private ArticleRepository repository;
    private TextView tabText;
    private TextView tabUrl;
    private TextView tvUrlFetchStatus;

    @Override // com.offread.app.ui.ThemedActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_add_article);
        applySystemBars();
        this.repository = ArticleRepository.getInstance(this);
        initViews();
        setupTabs();
        selectUrlTab();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        this.executor.shutdown();
    }

    private void initViews() {
        ImageButton imageButton = (ImageButton) findViewById(R.id.btn_add_back);
        this.tabUrl = (TextView) findViewById(R.id.tab_mode_url);
        this.tabText = (TextView) findViewById(R.id.tab_mode_text);
        this.layoutFormUrl = (LinearLayout) findViewById(R.id.layout_form_url);
        this.layoutFormText = (LinearLayout) findViewById(R.id.layout_form_text);
        this.editUrlInput = (EditText) findViewById(R.id.edit_url_input);
        this.editUrlTags = (EditText) findViewById(R.id.edit_url_tags);
        Button button = (Button) findViewById(R.id.btn_paste_url);
        this.progressUrlFetch = (ProgressBar) findViewById(R.id.progress_url_fetch);
        this.tvUrlFetchStatus = (TextView) findViewById(R.id.tv_url_fetch_status);
        this.btnSubmitUrl = (Button) findViewById(R.id.btn_submit_url);
        this.editManualTitle = (EditText) findViewById(R.id.edit_manual_title);
        this.editManualAuthor = (EditText) findViewById(R.id.edit_manual_author);
        this.editManualTags = (EditText) findViewById(R.id.edit_manual_tags);
        this.editManualContent = (EditText) findViewById(R.id.edit_manual_content);
        Button button2 = (Button) findViewById(R.id.btn_submit_manual);
        imageButton.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.AddArticleActivity.0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                AddArticleActivity.this.lambda$initViews$0(view);
            }
        });
        button.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.AddArticleActivity.1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                AddArticleActivity.this.lambda$initViews$1(view);
            }
        });
        this.btnSubmitUrl.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.AddArticleActivity.2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                AddArticleActivity.this.lambda$initViews$2(view);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.AddArticleActivity.3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                AddArticleActivity.this.lambda$initViews$3(view);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$initViews$0(View view) {
        finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$initViews$1(View view) {
        ClipData.Item itemAt;
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService("clipboard");
        if (clipboardManager == null || !clipboardManager.hasPrimaryClip() || (itemAt = clipboardManager.getPrimaryClip().getItemAt(0)) == null || itemAt.getText() == null) {
            return;
        }
        this.editUrlInput.setText(itemAt.getText().toString().trim());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$initViews$2(View view) {
        handleUrlSubmit();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$initViews$3(View view) {
        handleManualSubmit();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setupTabs$4(View view) {
        selectUrlTab();
    }

    private void setupTabs() {
        this.tabUrl.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.AddArticleActivity.5
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                AddArticleActivity.this.lambda$setupTabs$4(view);
            }
        });
        this.tabText.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.AddArticleActivity.6
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                AddArticleActivity.this.lambda$setupTabs$5(view);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setupTabs$5(View view) {
        selectTextTab();
    }

    private void selectUrlTab() {
        this.tabUrl.setBackgroundResource(R.drawable.bg_chip_active);
        this.tabUrl.setTextColor(themeColor(R.attr.colorTextPrimary));
        this.tabText.setBackgroundResource(R.drawable.bg_chip);
        this.tabText.setTextColor(themeColor(R.attr.colorTextSecondary));
        this.layoutFormUrl.setVisibility(0);
        this.layoutFormText.setVisibility(8);
    }

    private void selectTextTab() {
        this.tabText.setBackgroundResource(R.drawable.bg_chip_active);
        this.tabText.setTextColor(themeColor(R.attr.colorTextPrimary));
        this.tabUrl.setBackgroundResource(R.drawable.bg_chip);
        this.tabUrl.setTextColor(themeColor(R.attr.colorTextSecondary));
        this.layoutFormUrl.setVisibility(8);
        this.layoutFormText.setVisibility(0);
    }

    private void handleUrlSubmit() {
        String strTrim = this.editUrlInput.getText().toString().trim();
        if (strTrim.isEmpty()) {
            Toast.makeText(this, R.string.error_invalid_url, 0).show();
            return;
        }
        if (!strTrim.startsWith("http://") && !strTrim.startsWith("https://")) {
            strTrim = "https://" + strTrim;
            this.editUrlInput.setText(strTrim);
        }
        if (!SafeUrl.isWellFormedHttpUrl(strTrim)) {
            Toast.makeText(this, R.string.error_invalid_url, 0).show();
            return;
        }
        final String urlToFetch = strTrim;
        final String strTrim2 = this.editUrlTags.getText().toString().trim();
        this.btnSubmitUrl.setEnabled(false);
        this.progressUrlFetch.setVisibility(0);
        this.tvUrlFetchStatus.setVisibility(0);
        this.tvUrlFetchStatus.setText(R.string.fetching_article);
        this.executor.execute(new Runnable() { // from class: com.offread.app.ui.AddArticleActivity.4
            @Override // java.lang.Runnable
            public final void run() {
                AddArticleActivity.this.lambda$handleUrlSubmit$9(urlToFetch, strTrim2);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleUrlSubmit$9(String str, String str2) {
        ReadabilityResult readabilityResultFetchFromUrl = null;
        Throwable failure = null;
        try {
            readabilityResultFetchFromUrl = ArticleExtractor.fetchFromUrl(str);
        } catch (Throwable error) {
            failure = error;
        }
        if (readabilityResultFetchFromUrl != null) {
            final long jSaveFetched = saveFetched(readabilityResultFetchFromUrl, str, str2);
            runOnUiThread(new Runnable() { // from class: com.offread.app.ui.AddArticleActivity.7
                @Override // java.lang.Runnable
                public final void run() {
                    AddArticleActivity.this.lambda$handleUrlSubmit$6(jSaveFetched);
                }
            });
        } else if (failure instanceof NetworkFetcher.BlockedTargetException) {
            runOnUiThread(new Runnable() { // from class: com.offread.app.ui.AddArticleActivity.8
                @Override // java.lang.Runnable
                public final void run() {
                    AddArticleActivity.this.lambda$handleUrlSubmit$7();
                }
            });
        } else {
            saveOfflineDraft(str, str2);
            runOnUiThread(new Runnable() { // from class: com.offread.app.ui.AddArticleActivity.9
                @Override // java.lang.Runnable
                public final void run() {
                    AddArticleActivity.this.lambda$handleUrlSubmit$8();
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleUrlSubmit$6(long j) {
        Toast.makeText(this, R.string.article_saved_offline, 0).show();
        Intent intent = new Intent(this, (Class<?>) ReaderActivity.class);
        intent.putExtra("extra_article_id", j);
        startActivity(intent);
        finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleUrlSubmit$7() {
        resetForm();
        Toast.makeText(this, R.string.error_blocked_url, 1).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleUrlSubmit$8() {
        Toast.makeText(this, R.string.offline_draft_saved, 1).show();
        finish();
    }

    private long saveFetched(ReadabilityResult readabilityResult, String str, String str2) {
        String strExtractDomain = (readabilityResult.getSiteName() == null || readabilityResult.getSiteName().isEmpty()) ? TextAnalysisUtil.extractDomain(str) : readabilityResult.getSiteName();
        String strIdentityUrl = Article.identityUrl(str, readabilityResult.getCanonicalUrl());
        Article articleFindArticleByUrl = this.repository.findArticleByUrl(strIdentityUrl);
        Article article = articleFindArticleByUrl != null ? articleFindArticleByUrl : new Article();
        article.setTitle(readabilityResult.getTitle());
        article.setAuthor(readabilityResult.getAuthor());
        article.setUrl(strIdentityUrl);
        article.setCanonicalUrl(readabilityResult.getCanonicalUrl());
        article.setDomain(strExtractDomain);
        article.setContent(readabilityResult.getContent());
        article.setPlainText(readabilityResult.getPlainText());
        article.setExcerpt(readabilityResult.getExcerpt());
        article.setWordCount(readabilityResult.getWordCount());
        article.setReadingTimeMinutes(readabilityResult.getReadingTimeMinutes());
        article.setPublishedDate(readabilityResult.getPublishedDate());
        article.setImageUrl(readabilityResult.getLeadImageUrl());
        article.setLanguage(readabilityResult.getLanguage());
        article.setCategory(strExtractDomain);
        article.setTags(str2);
        ArticleRepository articleRepository = this.repository;
        if (articleFindArticleByUrl != null) {
            articleRepository.updateArticle(article);
            return article.getId();
        }
        return articleRepository.insertArticle(article);
    }

    private void saveOfflineDraft(String str, String str2) {
        String strExtractDomain = TextAnalysisUtil.extractDomain(str);
        Article article = new Article();
        article.setTitle(strExtractDomain);
        article.setUrl(str);
        article.setDomain(strExtractDomain);
        article.setContent(getString(R.string.offline_download_failed));
        article.setPlainText(getString(R.string.offline_queued_prefix, new Object[]{str}));
        article.setExcerpt(strExtractDomain);
        article.setTags(str2);
        this.repository.insertArticle(article);
    }

    private void resetForm() {
        this.btnSubmitUrl.setEnabled(true);
        this.progressUrlFetch.setVisibility(8);
        this.tvUrlFetchStatus.setVisibility(8);
    }

    private void handleManualSubmit() {
        String strTrim = this.editManualTitle.getText().toString().trim();
        String strTrim2 = this.editManualAuthor.getText().toString().trim();
        String strTrim3 = this.editManualTags.getText().toString().trim();
        String strTrim4 = this.editManualContent.getText().toString().trim();
        if (strTrim4.isEmpty()) {
            Toast.makeText(this, R.string.content_required, 0).show();
            return;
        }
        ReadabilityResult readabilityResultProcessUserText = ArticleExtractor.processUserText(strTrim, strTrim2, strTrim4);
        Article article = new Article();
        article.setTitle(readabilityResultProcessUserText.getTitle());
        article.setAuthor(readabilityResultProcessUserText.getAuthor());
        article.setUrl("");
        article.setDomain(getString(R.string.note_category));
        article.setContent(readabilityResultProcessUserText.getContent());
        article.setPlainText(readabilityResultProcessUserText.getPlainText());
        article.setExcerpt(readabilityResultProcessUserText.getExcerpt());
        article.setWordCount(readabilityResultProcessUserText.getWordCount());
        article.setReadingTimeMinutes(readabilityResultProcessUserText.getReadingTimeMinutes());
        article.setCategory(getString(R.string.note_category));
        article.setTags(strTrim3);
        long jInsertArticle = this.repository.insertArticle(article);
        Toast.makeText(this, R.string.article_saved_offline, 0).show();
        Intent intent = new Intent(this, (Class<?>) ReaderActivity.class);
        intent.putExtra("extra_article_id", jInsertArticle);
        startActivity(intent);
        finish();
    }
}
