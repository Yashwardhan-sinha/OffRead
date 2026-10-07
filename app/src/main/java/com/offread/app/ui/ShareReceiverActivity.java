package com.offread.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.offread.app.data.Article;
import com.offread.app.data.ArticleRepository;
import com.offread.app.parser.ArticleExtractor;
import com.offread.app.parser.ReadabilityResult;
import com.offread.app.parser.TextAnalysisUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShareReceiverActivity extends Activity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText != null && !sharedText.trim().isEmpty()) {
                handleIncomingContent(sharedText.trim());
                return;
            }
        }

        Toast.makeText(this, "Nothing to save", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void handleIncomingContent(String text) {
        String url = extractUrl(text);
        ArticleRepository repo = ArticleRepository.getInstance(this);

        if (url != null) {
            Toast.makeText(this, "Saving article offline with OffRead…", Toast.LENGTH_SHORT).show();
            final String finalUrl = url;

            executor.execute(() -> {
                try {
                    ReadabilityResult res = ArticleExtractor.fetchFromUrl(finalUrl);
                    Article a = new Article();
                    a.setTitle(res.getTitle());
                    a.setAuthor(res.getAuthor());
                    a.setUrl(finalUrl);
                    a.setDomain(TextAnalysisUtil.extractDomain(finalUrl));
                    a.setContent(res.getContent());
                    a.setPlainText(res.getPlainText());
                    a.setExcerpt(res.getExcerpt());
                    a.setWordCount(res.getWordCount());
                    a.setReadingTimeMinutes(res.getReadingTimeMinutes());
                    a.setCategory(TextAnalysisUtil.extractDomain(finalUrl));
                    repo.insertArticle(a);

                    runOnUiThread(() -> {
                        Toast.makeText(ShareReceiverActivity.this, "Saved: " + a.getTitle(), Toast.LENGTH_LONG).show();
                        finish();
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Article fallback = new Article();
                        fallback.setTitle(TextAnalysisUtil.extractDomain(finalUrl));
                        fallback.setUrl(finalUrl);
                        fallback.setDomain(TextAnalysisUtil.extractDomain(finalUrl));
                        fallback.setContent("Saved offline link:\n\n" + finalUrl);
                        fallback.setPlainText(finalUrl);
                        fallback.setExcerpt(finalUrl);
                        repo.insertArticle(fallback);

                        Toast.makeText(ShareReceiverActivity.this, "Saved link offline to OffRead", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            });
        } else {
            // Save as text note
            ReadabilityResult res = ArticleExtractor.processUserText("Shared Note", "", text);
            Article a = new Article();
            a.setTitle(res.getTitle());
            a.setAuthor("");
            a.setUrl("");
            a.setDomain("Note");
            a.setContent(res.getContent());
            a.setPlainText(res.getPlainText());
            a.setExcerpt(res.getExcerpt());
            a.setWordCount(res.getWordCount());
            a.setReadingTimeMinutes(res.getReadingTimeMinutes());
            a.setCategory("Note");
            repo.insertArticle(a);

            Toast.makeText(this, "Saved note to OffRead!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private String extractUrl(String text) {
        Pattern pattern = Pattern.compile("https?://[\\w\\d:#@%/;$()~_?\\+-=\\\\.&]+");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}
