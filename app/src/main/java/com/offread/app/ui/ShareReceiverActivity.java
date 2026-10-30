package com.offread.app.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.offread.app.R;
import com.offread.app.data.AppPreferences;
import com.offread.app.data.Article;
import com.offread.app.data.ArticleRepository;
import com.offread.app.parser.ArticleExtractor;
import com.offread.app.parser.NetworkFetcher;
import com.offread.app.parser.ReadabilityResult;
import com.offread.app.parser.SafeUrl;
import com.offread.app.parser.TextAnalysisUtil;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* JADX INFO: loaded from: classes.dex */
public class ShareReceiverActivity extends ThemedActivity {
    private static final int MAX_SHARED_TEXT_CHARS = 200000;
    private static final Pattern SHARED_URL = Pattern.compile("https?://[^\\s<>\"'`\\[\\]{}|^\\\\]+");
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ArticleRepository repository;
    private volatile boolean saveStarted;

    @Override // com.offread.app.ui.ThemedActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        String stringExtra;
        super.onCreate(bundle);
        this.repository = ArticleRepository.getInstance(this);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if ("android.intent.action.SEND".equals(action) && "text/plain".equals(type) && (stringExtra = intent.getStringExtra("android.intent.extra.TEXT")) != null && !stringExtra.trim().isEmpty()) {
            String strCap = cap(stringExtra.trim());
            confirmBeforeSaving(extractSafeUrl(strCap), strCap);
        } else {
            Toast.makeText(this, R.string.nothing_to_save, 0).show();
            finish();
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        this.executor.shutdown();
    }

    private void confirmBeforeSaving(final String str, final String str2) {
        String string;
        String strAbbreviate = str != null ? str : abbreviate(str2);
        if (str != null) {
            string = getString(R.string.share_confirm_message_link, new Object[]{strAbbreviate});
        } else {
            string = getString(R.string.share_confirm_message_note, new Object[]{strAbbreviate});
        }
        new AlertDialog.Builder(this).setTitle(R.string.share_confirm_title).setMessage(string).setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() { // from class: com.offread.app.ui.ShareReceiverActivity.3
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                ShareReceiverActivity.this.lambda$confirmBeforeSaving$0(dialogInterface, i);
            }
        }).setPositiveButton(R.string.share_confirm_save, new DialogInterface.OnClickListener() { // from class: com.offread.app.ui.ShareReceiverActivity.4
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                ShareReceiverActivity.this.lambda$confirmBeforeSaving$1(str, str2, dialogInterface, i);
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: com.offread.app.ui.ShareReceiverActivity.5
            @Override // android.content.DialogInterface.OnCancelListener
            public final void onCancel(DialogInterface dialogInterface) {
                ShareReceiverActivity.this.lambda$confirmBeforeSaving$2(dialogInterface);
            }
        }).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$confirmBeforeSaving$0(DialogInterface dialogInterface, int i) {
        finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$confirmBeforeSaving$1(String str, String str2, DialogInterface dialogInterface, int i) {
        if (str != null) {
            saveLink(str);
        } else {
            saveNote(str2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$confirmBeforeSaving$2(DialogInterface dialogInterface) {
        finish();
    }

    private void saveLink(final String str) {
        if (this.saveStarted) {
            return;
        }
        this.saveStarted = true;
        Toast.makeText(this, R.string.saving_article, 0).show();
        this.executor.execute(new Runnable() { // from class: com.offread.app.ui.ShareReceiverActivity.0
            @Override // java.lang.Runnable
            public final void run() {
                ShareReceiverActivity.this.lambda$saveLink$6(str);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$saveLink$6(String str) {
        ReadabilityResult readabilityResultFetchFromUrl = null;
        Throwable failure = null;
        try {
            readabilityResultFetchFromUrl = ArticleExtractor.fetchFromUrl(str);
        } catch (Throwable error) {
            failure = error;
        }
        if (readabilityResultFetchFromUrl != null) {
            final String strSaveFetched = saveFetched(readabilityResultFetchFromUrl, str);
            runOnUiThread(new Runnable() { // from class: com.offread.app.ui.ShareReceiverActivity.6
                @Override // java.lang.Runnable
                public final void run() {
                    ShareReceiverActivity.this.lambda$saveLink$3(strSaveFetched);
                }
            });
        } else if (failure instanceof NetworkFetcher.BlockedTargetException) {
            runOnUiThread(new Runnable() { // from class: com.offread.app.ui.ShareReceiverActivity.7
                @Override // java.lang.Runnable
                public final void run() {
                    ShareReceiverActivity.this.lambda$saveLink$4();
                }
            });
        } else {
            saveDraft(str);
            runOnUiThread(new Runnable() { // from class: com.offread.app.ui.ShareReceiverActivity.8
                @Override // java.lang.Runnable
                public final void run() {
                    ShareReceiverActivity.this.lambda$saveLink$5();
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$saveLink$3(String str) {
        Toast.makeText(this, getString(R.string.saved_article, new Object[]{str}), 1).show();
        finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$saveLink$4() {
        Toast.makeText(this, R.string.error_blocked_url, 1).show();
        finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$saveLink$5() {
        Toast.makeText(this, R.string.saved_link_offline, 0).show();
        finish();
    }

    private String saveFetched(ReadabilityResult readabilityResult, String str) {
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
        ArticleRepository articleRepository = this.repository;
        if (articleFindArticleByUrl != null) {
            articleRepository.updateArticle(article);
        } else {
            articleRepository.insertArticle(article);
        }
        return article.getTitle();
    }

    private void saveDraft(String str) {
        String strExtractDomain = TextAnalysisUtil.extractDomain(str);
        Article article = new Article();
        article.setTitle(strExtractDomain);
        article.setUrl(str);
        article.setDomain(strExtractDomain);
        article.setContent(getString(R.string.offline_queued_prefix, new Object[]{str}));
        article.setPlainText(getString(R.string.offline_queued_prefix, new Object[]{str}));
        article.setExcerpt(strExtractDomain);
        this.repository.insertArticle(article);
    }

    private void saveNote(final String str) {
        if (this.saveStarted) {
            return;
        }
        this.saveStarted = true;
        this.executor.execute(new Runnable() { // from class: com.offread.app.ui.ShareReceiverActivity.1
            @Override // java.lang.Runnable
            public final void run() {
                ShareReceiverActivity.this.lambda$saveNote$8(str);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$saveNote$8(String str) {
        ReadabilityResult readabilityResultProcessUserText = ArticleExtractor.processUserText(getString(R.string.shared_note_title), AppPreferences.TTS_LANGUAGE_AUTO, str);
        Article article = new Article();
        article.setTitle(readabilityResultProcessUserText.getTitle());
        article.setAuthor(AppPreferences.TTS_LANGUAGE_AUTO);
        article.setUrl(AppPreferences.TTS_LANGUAGE_AUTO);
        article.setDomain(getString(R.string.note_category));
        article.setContent(readabilityResultProcessUserText.getContent());
        article.setPlainText(readabilityResultProcessUserText.getPlainText());
        article.setExcerpt(readabilityResultProcessUserText.getExcerpt());
        article.setWordCount(readabilityResultProcessUserText.getWordCount());
        article.setReadingTimeMinutes(readabilityResultProcessUserText.getReadingTimeMinutes());
        article.setLanguage(readabilityResultProcessUserText.getLanguage());
        article.setCategory(getString(R.string.note_category));
        this.repository.insertArticle(article);
        runOnUiThread(new Runnable() { // from class: com.offread.app.ui.ShareReceiverActivity.2
            @Override // java.lang.Runnable
            public final void run() {
                ShareReceiverActivity.this.lambda$saveNote$7();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$saveNote$7() {
        Toast.makeText(this, R.string.saved_note, 0).show();
        finish();
    }

    private static String extractSafeUrl(String str) {
        Matcher matcher = SHARED_URL.matcher(str);
        while (matcher.find()) {
            String strTrimTrailingPunctuation = trimTrailingPunctuation(matcher.group());
            if (SafeUrl.isWellFormedHttpUrl(strTrimTrailingPunctuation)) {
                return strTrimTrailingPunctuation;
            }
        }
        return null;
    }

    private static String trimTrailingPunctuation(String str) {
        while (!str.isEmpty() && ".,;:!?'\"".indexOf(str.charAt(str.length() - 1)) >= 0) {
            str = str.substring(0, str.length() - 1);
        }
        while (str.endsWith(")") && count(str, ')') > count(str, '(')) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    private static int count(String str, char c) {
        int i = 0;
        for (int i2 = 0; i2 < str.length(); i2++) {
            if (str.charAt(i2) == c) {
                i++;
            }
        }
        return i;
    }

    private static String cap(String str) {
        return str.length() <= MAX_SHARED_TEXT_CHARS ? str : str.substring(0, MAX_SHARED_TEXT_CHARS);
    }

    private static String abbreviate(String str) {
        String strTrim = str.replaceAll("\\s+", " ").trim();
        if (strTrim.length() <= 400) {
            return strTrim;
        }
        return strTrim.substring(0, 400) + "…";
    }
}
