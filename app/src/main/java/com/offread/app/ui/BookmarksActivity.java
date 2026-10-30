package com.offread.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.offread.app.R;
import com.offread.app.data.Article;
import com.offread.app.data.ArticleRepository;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* JADX INFO: loaded from: classes.dex */
public class BookmarksActivity extends ThemedActivity implements BookmarkAdapter.OnBookmarkActionListener {
    private BookmarkAdapter adapter;
    private View layoutEmpty;
    private ListView listBookmarks;
    private ArticleRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override // com.offread.app.ui.ThemedActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_bookmarks);
        applySystemBars(themeColor(R.attr.colorSurface));
        this.repository = ArticleRepository.getInstance(this);
        this.listBookmarks = (ListView) findViewById(R.id.list_bookmarks);
        this.layoutEmpty = findViewById(R.id.layout_bookmarks_empty);
        BookmarkAdapter bookmarkAdapter = new BookmarkAdapter(this, this);
        this.adapter = bookmarkAdapter;
        this.listBookmarks.setAdapter((ListAdapter) bookmarkAdapter);
        this.listBookmarks.setEmptyView(null);
        BottomNavBar bottomNavBar = (BottomNavBar) findViewById(R.id.nav_bar);
        bottomNavBar.setSelected(1);
        bottomNavBar.setOnTabSelectedListener(new BottomNavBar.OnTabSelectedListener() { // from class: com.offread.app.ui.BookmarksActivity.0
            @Override // com.offread.app.ui.BottomNavBar.OnTabSelectedListener
            public final void onTabSelected(int i) {
                BookmarksActivity.this.lambda$onCreate$0(i);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$0(int i) {
        if (i != 1) {
            goToSection(i);
        }
    }

    @Override // com.offread.app.ui.ThemedActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        loadBookmarks();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        this.executor.shutdownNow();
    }

    private void loadBookmarks() {
        this.executor.execute(new Runnable() { // from class: com.offread.app.ui.BookmarksActivity.1
            @Override // java.lang.Runnable
            public final void run() {
                BookmarksActivity.this.lambda$loadBookmarks$2();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$loadBookmarks$2() {
        final List<Article> bookmarks = this.repository.getBookmarks();
        this.mainHandler.post(new Runnable() { // from class: com.offread.app.ui.BookmarksActivity.2
            @Override // java.lang.Runnable
            public final void run() {
                BookmarksActivity.this.lambda$loadBookmarks$1(bookmarks);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: show, reason: merged with bridge method [inline-methods] */
    public void lambda$loadBookmarks$1(List<Article> list) {
        if (isFinishing()) {
            return;
        }
        this.adapter.setItems(list);
        boolean zIsEmpty = list.isEmpty();
        this.layoutEmpty.setVisibility(zIsEmpty ? 0 : 8);
        this.listBookmarks.setVisibility(zIsEmpty ? 8 : 0);
    }

    @Override // com.offread.app.ui.BookmarkAdapter.OnBookmarkActionListener
    public void onBookmarkClick(Article article) {
        if (article == null || article.getId() <= 0) {
            return;
        }
        if (this.repository.getArticle(article.getId()) == null) {
            Toast.makeText(this, R.string.error_article_missing, 0).show();
            loadBookmarks();
        } else {
            Intent intent = new Intent(this, (Class<?>) ReaderActivity.class);
            intent.putExtra("extra_article_id", article.getId());
            startActivity(intent);
        }
    }

    @Override // com.offread.app.ui.BookmarkAdapter.OnBookmarkActionListener
    public void onBookmarkRemove(Article article) {
        this.repository.setFavorite(article.getId(), false);
        Toast.makeText(this, R.string.reader_removed, 0).show();
        loadBookmarks();
    }
}
