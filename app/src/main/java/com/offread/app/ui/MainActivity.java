package com.offread.app.ui;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
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
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* JADX INFO: loaded from: classes.dex */
public class MainActivity extends ThemedActivity implements ArticleAdapter.OnArticleActionListener {
    private ArticleAdapter adapter;
    private Button btnFetch;
    private EditText editUrl;
    private boolean fetchInFlight;
    private View layoutEmpty;
    private ListView listArticles;
    private ProgressBar progressArticle;
    private ArticleRepository repository;
    private TextView tvStatusNotice;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override // com.offread.app.ui.ThemedActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        applySystemBars(themeColor(R.attr.colorSurface));
        this.repository = ArticleRepository.getInstance(this);
        this.editUrl = (EditText) findViewById(R.id.edit_url);
        this.btnFetch = (Button) findViewById(R.id.btn_open_article);
        this.progressArticle = (ProgressBar) findViewById(R.id.progress_article);
        this.tvStatusNotice = (TextView) findViewById(R.id.tv_status_notice);
        this.listArticles = (ListView) findViewById(R.id.list_articles);
        this.layoutEmpty = findViewById(R.id.layout_empty);
        ArticleAdapter articleAdapter = new ArticleAdapter(this, this);
        this.adapter = articleAdapter;
        this.listArticles.setAdapter((ListAdapter) articleAdapter);
        this.listArticles.setEmptyView(null);
        ImageButton imageButton = (ImageButton) findViewById(R.id.btn_add_article);
        ImageButton imageButton2 = (ImageButton) findViewById(R.id.btn_stats);
        imageButton.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.MainActivity.1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$onCreate$0(view);
            }
        });
        imageButton2.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.MainActivity.2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$onCreate$1(view);
            }
        });
        this.btnFetch.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.MainActivity.3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$onCreate$2(view);
            }
        });
        this.editUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() { // from class: com.offread.app.ui.MainActivity.4
            @Override // android.widget.TextView.OnEditorActionListener
            public final boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return MainActivity.this.lambda$onCreate$3(textView, i, keyEvent);
            }
        });
        BottomNavBar bottomNavBar = (BottomNavBar) findViewById(R.id.nav_bar);
        bottomNavBar.setSelected(0);
        bottomNavBar.setOnTabSelectedListener(new BottomNavBar.OnTabSelectedListener() { // from class: com.offread.app.ui.MainActivity.5
            @Override // com.offread.app.ui.BottomNavBar.OnTabSelectedListener
            public final void onTabSelected(int i) {
                MainActivity.this.lambda$onCreate$4(i);
            }
        });
        this.listArticles.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.offread.app.ui.MainActivity.6
            @Override // android.widget.AdapterView.OnItemClickListener
            public final void onItemClick(AdapterView adapterView, View view, int i, long j) {
                MainActivity.this.lambda$onCreate$5(adapterView, view, i, j);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$0(View view) {
        startActivity(new Intent(this, (Class<?>) AddArticleActivity.class));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$1(View view) {
        startActivity(new Intent(this, (Class<?>) StatisticsActivity.class));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$2(View view) {
        handleOpenArticle();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$onCreate$3(TextView textView, int i, KeyEvent keyEvent) {
        if (i != 2 && i != 6) {
            return false;
        }
        handleOpenArticle();
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$4(int i) {
        if (i != 0) {
            goToSection(i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$5(AdapterView adapterView, View view, int i, long j) {
        openArticle(this.adapter.getItem(i));
    }

    @Override // com.offread.app.ui.ThemedActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        loadLibrary();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        this.executor.shutdownNow();
    }

    private void loadLibrary() {
        this.executor.execute(new Runnable() { // from class: com.offread.app.ui.MainActivity.8
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$loadLibrary$7();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$loadLibrary$7() {
        final List<Article> articles = this.repository.getArticles(ArticleRepository.FilterType.ALL, null, null, ArticleRepository.SortOrder.NEWEST);
        this.mainHandler.post(new Runnable() { // from class: com.offread.app.ui.MainActivity.7
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$loadLibrary$6(articles);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$loadLibrary$6(List list) {
        this.adapter.setArticles(list);
        this.layoutEmpty.setVisibility(list.isEmpty() ? 0 : 8);
        this.listArticles.setVisibility(list.isEmpty() ? 8 : 0);
    }

    private void openArticle(Article article) {
        if (article == null) {
            return;
        }
        Intent intent = new Intent(this, (Class<?>) ReaderActivity.class);
        intent.putExtra("extra_article_id", article.getId());
        startActivity(intent);
    }

    @Override // com.offread.app.ui.ArticleAdapter.OnArticleActionListener
    public void onArticleClick(Article article) {
        openArticle(article);
    }

    @Override // com.offread.app.ui.ArticleAdapter.OnArticleActionListener
    public void onBookmarkToggle(Article article) {
        boolean zIsFavorite = article.isFavorite();
        boolean z = !zIsFavorite;
        this.repository.setFavorite(article.getId(), z);
        article.setFavorite(z);
        this.adapter.notifyDataSetChanged();
        Toast.makeText(this, !zIsFavorite ? R.string.reader_saved : R.string.reader_removed, 0).show();
    }

    @Override // com.offread.app.ui.ArticleAdapter.OnArticleActionListener
    public void onArchiveToggle(Article article) {
        this.repository.toggleArchive(article.getId());
        article.setArchived(!article.isArchived());
        this.adapter.notifyDataSetChanged();
    }

    private void handleOpenArticle() {
        if (this.fetchInFlight) {
            return;
        }
        String strTrim = this.editUrl.getText().toString().trim();
        if (strTrim.isEmpty()) {
            showStatus(getString(R.string.error_empty_url));
            return;
        }
        if (!strTrim.startsWith("http://") && !strTrim.startsWith("https://")) {
            strTrim = "https://" + strTrim;
            this.editUrl.setText(strTrim);
        }
        if (!isValidHttpUrl(strTrim)) {
            showStatus(getString(R.string.error_invalid_url));
            return;
        }
        if (!isNetworkAvailable()) {
            showStatus(getString(R.string.error_no_network));
            return;
        }
        final String urlToFetch = strTrim;
        this.fetchInFlight = true;
        this.btnFetch.setEnabled(false);
        this.progressArticle.setVisibility(0);
        showStatus(getString(R.string.fetching_article));
        this.executor.execute(new Runnable() { // from class: com.offread.app.ui.MainActivity.0
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$handleOpenArticle$9(urlToFetch);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleOpenArticle$9(String str) {
        String strResolveErrorMessage;
        Article articlePersist = null;
        try {
            ReadabilityResult readabilityResultFetchFromUrl = ArticleExtractor.fetchFromUrl(str);
            if (readabilityResultFetchFromUrl == null || readabilityResultFetchFromUrl.getPlainText().trim().isEmpty() || readabilityResultFetchFromUrl.getWordCount() < 5) {
                strResolveErrorMessage = getString(R.string.error_no_article);
            } else {
                articlePersist = persist(readabilityResultFetchFromUrl, str);
                strResolveErrorMessage = null;
            }
        } catch (Throwable th) {
            strResolveErrorMessage = resolveErrorMessage(th);
        }
        final Article resultArticle = articlePersist;
        final String resultError = strResolveErrorMessage;
        this.mainHandler.post(new Runnable() { // from class: com.offread.app.ui.MainActivity.9
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$handleOpenArticle$8(resultArticle, resultError);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleOpenArticle$8(Article article, String str) {
        this.fetchInFlight = false;
        this.btnFetch.setEnabled(true);
        this.progressArticle.setVisibility(8);
        if (article != null) {
            this.editUrl.setText("");
            loadLibrary();
            openArticle(article);
        } else {
            showStatus(str);
            Toast.makeText(this, str, 1).show();
        }
    }

    private Article persist(ReadabilityResult readabilityResult, String str) {
        String strIdentityUrl = Article.identityUrl(str, readabilityResult.getCanonicalUrl());
        Article articleFindArticleByUrl = this.repository.findArticleByUrl(strIdentityUrl);
        Article article = articleFindArticleByUrl != null ? articleFindArticleByUrl : new Article();
        String siteName = !readabilityResult.getSiteName().isEmpty() ? readabilityResult.getSiteName() : TextAnalysisUtil.extractDomain(str);
        if (!readabilityResult.getCanonicalUrl().isEmpty()) {
            str = readabilityResult.getCanonicalUrl();
        }
        article.setTitle(readabilityResult.getTitle());
        article.setAuthor(readabilityResult.getAuthor());
        article.setUrl(strIdentityUrl);
        article.setCanonicalUrl(str);
        article.setDomain(siteName);
        article.setContent(readabilityResult.getContent());
        article.setPlainText(readabilityResult.getPlainText());
        article.setExcerpt(readabilityResult.getExcerpt());
        article.setWordCount(readabilityResult.getWordCount());
        article.setReadingTimeMinutes(readabilityResult.getReadingTimeMinutes());
        article.setPublishedDate(readabilityResult.getPublishedDate());
        article.setImageUrl(readabilityResult.getLeadImageUrl());
        article.setLanguage(readabilityResult.getLanguage());
        article.setCategory(siteName);
        if (article.getTags() == null || article.getTags().isEmpty()) {
            article.setTags("");
        }
        ArticleRepository articleRepository = this.repository;
        if (articleFindArticleByUrl != null) {
            articleRepository.updateArticle(article);
            return article;
        }
        article.setId(articleRepository.insertArticle(article));
        return article;
    }

    private void showStatus(String str) {
        this.tvStatusNotice.setText(str);
        this.tvStatusNotice.setVisibility(0);
    }

    private boolean isValidHttpUrl(String str) {
        return SafeUrl.isWellFormedHttpUrl(str);
    }

    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService("connectivity");
            if (connectivityManager == null) {
                return true;
            }
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } catch (Exception unused) {
            return true;
        }
    }

    private String resolveErrorMessage(Throwable th) {
        if (th instanceof NetworkFetcher.BlockedTargetException) {
            return getString(R.string.error_blocked_url);
        }
        if ((th instanceof UnknownHostException) || (th instanceof ConnectException)) {
            return getString(R.string.error_no_network);
        }
        if (th instanceof SocketTimeoutException) {
            return getString(R.string.error_timeout);
        }
        String message = th.getMessage();
        if (message != null) {
            if (message.startsWith("HTTP error ")) {
                return getString(R.string.error_http_status, new Object[]{httpStatusOf(message)});
            }
            if (message.startsWith("Cleartext HTTP traffic")) {
                return getString(R.string.error_cleartext_blocked);
            }
            if (message.contains("No meaningful article content")) {
                return getString(R.string.error_no_article);
            }
            if (message.contains("timed out")) {
                return getString(R.string.error_timeout);
            }
        }
        return getString(R.string.error_parse_failed);
    }

    private String httpStatusOf(String str) {
        String strTrim = str.substring(11).trim();
        int iIndexOf = strTrim.indexOf(32);
        return iIndexOf > 0 ? strTrim.substring(0, iIndexOf) : strTrim;
    }
}
