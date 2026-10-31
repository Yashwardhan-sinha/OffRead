package com.offread.app.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.offread.app.R;
import com.offread.app.data.AppPreferences;
import com.offread.app.data.Article;
import com.offread.app.data.ArticleRepository;
import com.offread.app.parser.ArticleContentBlocks;
import com.offread.app.parser.SafeUrl;
import com.offread.app.tts.ArticleSpeaker;
import com.offread.app.tts.SpeechLanguages;
import com.offread.app.util.ReaderMetaFormatter;
import com.offread.app.util.UIHelper;
import java.util.List;
import java.util.Locale;

/* JADX INFO: loaded from: classes.dex */
public class ReaderActivity extends ThemedActivity implements ArticleSpeaker.SpeakerListener {
    public static final String EXTRA_ARTICLE_ID = "extra_article_id";
    private Article article;
    private LinearLayout blockContainer;
    private List<ArticleContentBlocks.Block> blocks;
    private ImageButton btnBookmark;
    private ImageButton btnTtsPlayPause;
    private TextView btnTtsSpeed;
    private ImageView imgLead;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private AppPreferences preferences;
    private ProgressBar progressScroll;
    private LinearLayout readerColumn;
    private LinearLayout readerMeasure;
    private ArticleRepository repository;
    private View root;
    private ScrollView scroll;
    private int sessionSeconds;
    private long sessionStart;
    private ArticleSpeaker speaker;
    private LinearLayout toolbar;
    private LinearLayout ttsBar;
    private TextView tvEmpty;
    private TextView tvMeta;
    private TextView tvTags;
    private TextView tvTitle;
    private TextView tvToolbarTitle;
    private TextView tvTtsStatus;
    private View viewRule;
    private static final float SPEED_STEP_DOWN = 0.75f;
    private static final float[] SPEED_STEPS = {SPEED_STEP_DOWN, 1.0f, 1.25f, 1.5f};

    @Override // com.offread.app.ui.ThemedActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_reader);
        this.repository = ArticleRepository.getInstance(this);
        this.preferences = new AppPreferences(this);
        long longExtra = getIntent().getLongExtra("extra_article_id", -1L);
        Article article = longExtra > 0 ? this.repository.getArticle(longExtra) : null;
        this.article = article;
        if (article == null) {
            Toast.makeText(this, R.string.error_article_missing, 0).show();
            finish();
            return;
        }
        bindViews();
        wireControls();
        this.blocks = ArticleContentBlocks.parse(this.article.getContent());
        applyPreferences();
        renderArticle();
        ArticleSpeaker articleSpeaker = new ArticleSpeaker(this, this);
        this.speaker = articleSpeaker;
        articleSpeaker.setSpeechRate(this.preferences.getTtsSpeed());
        this.speaker.setPreferredLanguage(SpeechLanguages.localeForTag(this.preferences.getTtsLanguage()));
        this.speaker.setArticleLanguage(SpeechLanguages.localeForTag(this.article.getLanguage()));
        this.speaker.loadText(speechText());
    }

    private void bindViews() {
        this.root = findViewById(R.id.reader_root);
        this.toolbar = (LinearLayout) findViewById(R.id.toolbar_reader);
        this.scroll = (ScrollView) findViewById(R.id.scroll_reader);
        this.progressScroll = (ProgressBar) findViewById(R.id.progress_reader_scroll);
        this.tvToolbarTitle = (TextView) findViewById(R.id.tv_reader_toolbar_title);
        this.tvTitle = (TextView) findViewById(R.id.tv_reader_title);
        this.tvMeta = (TextView) findViewById(R.id.tv_reader_meta);
        this.tvTags = (TextView) findViewById(R.id.tv_reader_tags);
        this.imgLead = (ImageView) findViewById(R.id.img_reader_lead);
        this.viewRule = findViewById(R.id.view_reader_rule);
        this.readerMeasure = (LinearLayout) findViewById(R.id.reader_measure);
        this.readerColumn = (LinearLayout) findViewById(R.id.reader_column);
        this.blockContainer = (LinearLayout) findViewById(R.id.reader_blocks);
        this.tvEmpty = (TextView) findViewById(R.id.tv_reader_empty);
        this.ttsBar = (LinearLayout) findViewById(R.id.layout_tts_bar);
        this.tvTtsStatus = (TextView) findViewById(R.id.tv_tts_status);
        this.btnTtsSpeed = (TextView) findViewById(R.id.btn_tts_speed);
        this.btnTtsPlayPause = (ImageButton) findViewById(R.id.btn_tts_play_pause);
        this.btnBookmark = (ImageButton) findViewById(R.id.btn_reader_bookmark);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$wireControls$0(View view) {
        finish();
    }

    private void wireControls() {
        findViewById(R.id.btn_reader_back).setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.ReaderActivity.3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ReaderActivity.this.lambda$wireControls$0(view);
            }
        });
        this.btnBookmark.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.ReaderActivity.4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ReaderActivity.this.lambda$wireControls$1(view);
            }
        });
        findViewById(R.id.btn_reader_tts).setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.ReaderActivity.5
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ReaderActivity.this.lambda$wireControls$2(view);
            }
        });
        findViewById(R.id.btn_reader_appearance).setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.ReaderActivity.6
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ReaderActivity.this.lambda$wireControls$3(view);
            }
        });
        findViewById(R.id.btn_reader_more).setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.ReaderActivity.7
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ReaderActivity.this.showMoreMenu(view);
            }
        });
        this.btnTtsPlayPause.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.ReaderActivity.8
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ReaderActivity.this.lambda$wireControls$4(view);
            }
        });
        findViewById(R.id.btn_tts_stop).setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.ReaderActivity.9
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ReaderActivity.this.lambda$wireControls$5(view);
            }
        });
        findViewById(R.id.btn_tts_prev).setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.ReaderActivity.10
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ReaderActivity.this.lambda$wireControls$6(view);
            }
        });
        this.btnTtsSpeed.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.ReaderActivity.11
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ReaderActivity.this.lambda$wireControls$7(view);
            }
        });
        this.scroll.setOnScrollChangeListener(new View.OnScrollChangeListener() { // from class: com.offread.app.ui.ReaderActivity.12
            @Override // android.view.View.OnScrollChangeListener
            public final void onScrollChange(View view, int i, int i2, int i3, int i4) {
                ReaderActivity.this.lambda$wireControls$8(view, i, i2, i3, i4);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$wireControls$1(View view) {
        toggleBookmark();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$wireControls$2(View view) {
        toggleTtsBar();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$wireControls$3(View view) {
        ViewAppearanceDialog.show(this, this.preferences, new ViewAppearanceDialog.AppearanceChangeListener() { // from class: com.offread.app.ui.ReaderActivity.18
            @Override // com.offread.app.ui.ViewAppearanceDialog.AppearanceChangeListener
            public final void onAppearanceChanged() {
                ReaderActivity.this.applyPreferences();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$wireControls$4(View view) {
        ArticleSpeaker articleSpeaker = this.speaker;
        if (articleSpeaker == null) {
            return;
        }
        if (articleSpeaker.isPlaying()) {
            this.speaker.pause();
        } else {
            startReading();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$wireControls$5(View view) {
        stopReading();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$wireControls$6(View view) {
        ArticleSpeaker articleSpeaker = this.speaker;
        if (articleSpeaker != null) {
            articleSpeaker.previous();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$wireControls$7(View view) {
        cycleSpeed();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$wireControls$8(View view, int i, int i2, int i3, int i4) {
        int height = this.scroll.getChildAt(0).getHeight() - this.scroll.getHeight();
        this.progressScroll.setProgress(Math.max(0, Math.min(100, height > 0 ? (int) ((i2 / height) * 100.0f) : 0)));
    }

    private void renderArticle() {
        this.tvTitle.setText(this.article.getTitle());
        this.tvToolbarTitle.setText(this.article.getTitle());
        this.tvMeta.setText(buildMetaLine());
        String strJoin = ReaderMetaFormatter.join((String[]) ReaderMetaFormatter.splitTags(this.article.getTags()).toArray(new String[0]));
        this.tvTags.setText(strJoin);
        this.tvTags.setVisibility(strJoin.isEmpty() ? 8 : 0);
        String strResolveImageUrl = ArticleContentBlocks.resolveImageUrl(this.article.getImageUrl(), this.article.getUrl());
        boolean zIsEmpty = strResolveImageUrl.isEmpty();
        ImageView imageView = this.imgLead;
        if (zIsEmpty) {
            imageView.setVisibility(8);
        } else {
            RemoteImageLoader.into(imageView, strResolveImageUrl);
        }
        int iRender = ReaderViewBuilder.render(this.blockContainer, this.blocks, buildStyle());
        this.blockContainer.setVisibility(iRender == 0 ? 8 : 0);
        this.tvEmpty.setVisibility(iRender != 0 ? 8 : 0);
        if (this.article.getScrollPosition() > 0) {
            final int scrollPosition = this.article.getScrollPosition();
            this.blockContainer.post(new Runnable() { // from class: com.offread.app.ui.ReaderActivity.19
                @Override // java.lang.Runnable
                public final void run() {
                    ReaderActivity.this.lambda$renderArticle$9(scrollPosition);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$renderArticle$9(int i) {
        this.scroll.scrollTo(0, i);
    }

    private String buildMetaLine() {
        String publishedDate;
        boolean zIsEmpty = this.article.getDomain().isEmpty();
        Article article = this.article;
        String domain = !zIsEmpty ? article.getDomain() : article.getCategory();
        boolean zIsEmpty2 = this.article.getPublishedDate().isEmpty();
        Article article2 = this.article;
        if (!zIsEmpty2) {
            publishedDate = article2.getPublishedDate();
        } else {
            publishedDate = ReadingDateFormatter.format(this, article2.getCreatedAt());
        }
        return ReaderMetaFormatter.join(this.article.getAuthor(), domain, publishedDate, getString(R.string.minutes_read, new Object[]{Integer.valueOf(this.article.getReadingTimeMinutes())}));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void applyPreferences() {
        if (this.article == null) {
            return;
        }
        UIHelper.ReaderThemeColors themeColors = UIHelper.getThemeColors(this.preferences.getTheme(), this);
        this.root.setBackgroundColor(themeColors.backgroundColor);
        this.toolbar.setBackgroundColor(themeColors.surfaceColor);
        this.ttsBar.setBackgroundColor(themeColors.surfaceColor);
        this.viewRule.setBackgroundColor(themeColor(R.attr.colorDivider));
        this.tvTitle.setTextColor(themeColors.textColor);
        this.tvMeta.setTextColor(themeColors.secondaryTextColor);
        this.tvTags.setTextColor(themeColors.secondaryTextColor);
        this.tvToolbarTitle.setTextColor(themeColors.secondaryTextColor);
        this.tvEmpty.setTextColor(themeColors.secondaryTextColor);
        this.tvTtsStatus.setTextColor(themeColors.secondaryTextColor);
        int i = themeColors.secondaryTextColor;
        int[] iArr = {R.id.btn_reader_back, R.id.btn_reader_appearance, R.id.btn_reader_more, R.id.btn_reader_tts, R.id.btn_tts_prev, R.id.btn_tts_play_pause, R.id.btn_tts_stop};
        for (int i2 = 0; i2 < 7; i2++) {
            ((ImageButton) findViewById(iArr[i2])).setColorFilter(i);
        }
        this.btnTtsSpeed.setTextColor(themeColors.secondaryTextColor);
        paintBookmarkIcon();
        applyReadingWidth();
        applySystemBarsFor(themeColors);
        if (this.blockContainer.getChildCount() > 0) {
            int scrollY = this.scroll.getScrollY();
            ReaderViewBuilder.render(this.blockContainer, this.blocks, buildStyle());
            this.scroll.scrollTo(0, scrollY);
        }
        boolean zIsFullscreen = this.preferences.isFullscreen();
        LinearLayout linearLayout = this.toolbar;
        if (zIsFullscreen) {
            linearLayout.setVisibility(8);
            this.progressScroll.setVisibility(8);
        } else {
            linearLayout.setVisibility(0);
            this.progressScroll.setVisibility(0);
        }
        if (this.preferences.isKeepScreenOn()) {
            getWindow().addFlags(128);
        } else {
            getWindow().clearFlags(128);
        }
    }

    private void applyReadingWidth() {
        boolean zEquals = AppPreferences.WIDTH_WIDE.equals(this.preferences.getReadingWidth());
        int dimensionPixelSize = getResources().getDimensionPixelSize(zEquals ? R.dimen.reader_padding_horizontal_wide : R.dimen.reader_padding_horizontal);
        int dimensionPixelSize2 = getResources().getDimensionPixelSize(zEquals ? R.dimen.reader_max_width_wide : R.dimen.reader_max_width_normal);
        this.readerMeasure.setPadding(dimensionPixelSize, 0, dimensionPixelSize, 0);
        int i = getResources().getDisplayMetrics().widthPixels - (dimensionPixelSize * 2);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.readerColumn.getLayoutParams();
        layoutParams.width = Math.min(i, dimensionPixelSize2);
        this.readerColumn.setLayoutParams(layoutParams);
        this.readerColumn.setPadding(0, getResources().getDimensionPixelSize(R.dimen.reader_padding_top), 0, getResources().getDimensionPixelSize(R.dimen.reader_padding_bottom));
    }

    private ReaderViewBuilder.Style buildStyle() {
        UIHelper.ReaderThemeColors themeColors = UIHelper.getThemeColors(this.preferences.getTheme(), this);
        float lineSpacing = this.preferences.getLineSpacing();
        ReaderViewBuilder.Style style = new ReaderViewBuilder.Style();
        style.bodySp = this.preferences.getFontSize();
        style.typeface = UIHelper.getTypeface(this.preferences.getFontFamily());
        style.inkColor = themeColors.textColor;
        style.mutedColor = themeColors.secondaryTextColor;
        style.accentColor = themeColor(R.attr.colorAccent);
        style.quoteTextColor = themeColors.secondaryTextColor;
        style.dividerColor = themeColor(R.attr.colorDivider);
        style.paragraphGap = dimen(R.dimen.block_paragraph_gap);
        style.headingGapBefore = dimen(R.dimen.block_heading_gap_before);
        style.headingGapAfter = dimen(R.dimen.block_heading_gap_after);
        style.subheadingGapBefore = dimen(R.dimen.block_subheading_gap_before);
        style.subheadingGapAfter = dimen(R.dimen.block_subheading_gap_after);
        style.miniHeadingGapBefore = dimen(R.dimen.block_mini_heading_gap_before);
        style.miniHeadingGapAfter = dimen(R.dimen.block_mini_heading_gap_after);
        style.listGap = dimen(R.dimen.block_list_gap);
        style.quoteGap = dimen(R.dimen.block_quote_gap);
        style.imageGap = dimen(R.dimen.block_image_gap);
        style.ruleGap = dimen(R.dimen.block_rule_gap);
        style.lineHeight = lineSpacing;
        style.headingLineHeight = Math.min(lineSpacing, 1.25f);
        style.imageMaxHeight = dimen(R.dimen.cover_image_max_height);
        return style;
    }

    private void toggleBookmark() {
        boolean zIsFavorite = this.article.isFavorite();
        boolean z = !zIsFavorite;
        this.repository.setFavorite(this.article.getId(), z);
        this.article.setFavorite(z);
        paintBookmarkIcon();
        Toast.makeText(this, !zIsFavorite ? R.string.reader_saved : R.string.reader_removed, 0).show();
    }

    private void paintBookmarkIcon() {
        int iThemeColor = themeColor(R.attr.colorAccent);
        int i = UIHelper.getThemeColors(this.preferences.getTheme(), this).secondaryTextColor;
        boolean zIsFavorite = this.article.isFavorite();
        ImageButton imageButton = this.btnBookmark;
        if (zIsFavorite) {
            imageButton.setImageResource(R.drawable.ic_bookmark_filled);
            this.btnBookmark.setColorFilter(iThemeColor);
            this.btnBookmark.setContentDescription(getString(R.string.action_unbookmark));
        } else {
            imageButton.setImageResource(R.drawable.ic_bookmark);
            this.btnBookmark.setColorFilter(i);
            this.btnBookmark.setContentDescription(getString(R.string.action_bookmark));
        }
    }

    private String speechText() {
        String plainText = ArticleContentBlocks.toPlainText(this.blocks);
        return plainText.trim().isEmpty() ? this.article.getPlainText() : plainText;
    }

    private void toggleTtsBar() {
        if (this.ttsBar.getVisibility() == 0) {
            stopReading();
            this.ttsBar.setVisibility(8);
        } else {
            this.ttsBar.setVisibility(0);
            this.btnTtsSpeed.setText(speedLabel(this.preferences.getTtsSpeed()));
            ArticleSpeaker articleSpeaker = this.speaker;
            if (articleSpeaker == null || !articleSpeaker.isEngineAvailable()) {
                this.tvTtsStatus.setText(R.string.tts_none_installed);
            } else {
                this.tvTtsStatus.setText(R.string.action_tts);
            }
        }
        applySystemBarsFor(UIHelper.getThemeColors(this.preferences.getTheme(), this));
    }

    private void startReading() {
        ArticleSpeaker articleSpeaker = this.speaker;
        if (articleSpeaker == null) {
            return;
        }
        if (!articleSpeaker.isEngineAvailable()) {
            Toast.makeText(this, R.string.tts_none_installed, 1).show();
            return;
        }
        if (this.speaker.getSentences().isEmpty()) {
            Toast.makeText(this, R.string.reader_no_text, 0).show();
            return;
        }
        if (this.ttsBar.getVisibility() != 0) {
            this.ttsBar.setVisibility(0);
        }
        applySystemBarsFor(UIHelper.getThemeColors(this.preferences.getTheme(), this));
        this.speaker.play();
    }

    private void stopReading() {
        ArticleSpeaker articleSpeaker = this.speaker;
        if (articleSpeaker != null) {
            articleSpeaker.stop();
        }
        this.btnTtsPlayPause.setImageResource(R.drawable.ic_play);
        this.tvTtsStatus.setText(R.string.tts_paused);
    }

    private void cycleSpeed() {
        ArticleSpeaker articleSpeaker = this.speaker;
        float speechRate = articleSpeaker != null ? articleSpeaker.getSpeechRate() : this.preferences.getTtsSpeed();
        int i = 0;
        float f = SPEED_STEPS[0];
        while (true) {
            float[] fArr = SPEED_STEPS;
            if (i >= fArr.length) {
                break;
            }
            if (Math.abs(speechRate - fArr[i]) < 0.06f) {
                f = fArr[(i + 1) % fArr.length];
                break;
            }
            float f2 = fArr[i];
            if (speechRate < f2) {
                f = f2;
                break;
            }
            i++;
        }
        ArticleSpeaker articleSpeaker2 = this.speaker;
        if (articleSpeaker2 != null) {
            articleSpeaker2.setSpeechRate(f);
        }
        this.preferences.setTtsSpeed(f);
        this.btnTtsSpeed.setText(speedLabel(f));
    }

    private String speedLabel(float f) {
        if (Math.abs(f - SPEED_STEP_DOWN) < 0.06f) {
            return getString(R.string.speed_75);
        }
        if (Math.abs(f - 1.25f) < 0.06f) {
            return getString(R.string.speed_125);
        }
        return Math.abs(f - 1.5f) < 0.06f ? getString(R.string.speed_150) : getString(R.string.speed_100);
    }

    @Override // com.offread.app.tts.ArticleSpeaker.SpeakerListener
    public void onInitialized(final boolean z) {
        post(new Runnable() { // from class: com.offread.app.ui.ReaderActivity.17
            @Override // java.lang.Runnable
            public final void run() {
                ReaderActivity.this.lambda$onInitialized$10(z);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onInitialized$10(boolean z) {
        if (z) {
            return;
        }
        this.tvTtsStatus.setText(R.string.tts_none_installed);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onSentenceChanged$11(int i, int i2) {
        this.tvTtsStatus.setText(getString(R.string.speech_sentence, new Object[]{Integer.valueOf(i + 1), Integer.valueOf(i2)}));
    }

    @Override // com.offread.app.tts.ArticleSpeaker.SpeakerListener
    public void onSentenceChanged(final int i, final int i2, String str) {
        post(new Runnable() { // from class: com.offread.app.ui.ReaderActivity.2
            @Override // java.lang.Runnable
            public final void run() {
                ReaderActivity.this.lambda$onSentenceChanged$11(i, i2);
            }
        });
    }

    @Override // com.offread.app.tts.ArticleSpeaker.SpeakerListener
    public void onPlaybackStateChanged(final boolean z) {
        post(new Runnable() { // from class: com.offread.app.ui.ReaderActivity.13
            @Override // java.lang.Runnable
            public final void run() {
                ReaderActivity.this.lambda$onPlaybackStateChanged$12(z);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onPlaybackStateChanged$12(boolean z) {
        this.btnTtsPlayPause.setImageResource(z ? R.drawable.ic_pause : R.drawable.ic_play);
        this.btnTtsPlayPause.setContentDescription(getString(z ? R.string.tts_playing : R.string.tts_paused));
        if (z || this.ttsBar.getVisibility() != 0) {
            return;
        }
        this.tvTtsStatus.setText(R.string.tts_paused);
    }

    @Override // com.offread.app.tts.ArticleSpeaker.SpeakerListener
    public void onPlaybackCompleted() {
        post(new Runnable() { // from class: com.offread.app.ui.ReaderActivity.14
            @Override // java.lang.Runnable
            public final void run() {
                ReaderActivity.this.lambda$onPlaybackCompleted$13();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onPlaybackCompleted$13() {
        this.btnTtsPlayPause.setImageResource(R.drawable.ic_play);
        this.tvTtsStatus.setText(R.string.speech_paused);
    }

    @Override // com.offread.app.tts.ArticleSpeaker.SpeakerListener
    public void onLanguageResult(final int i, final Locale locale) {
        post(new Runnable() { // from class: com.offread.app.ui.ReaderActivity.0
            @Override // java.lang.Runnable
            public final void run() {
                ReaderActivity.this.lambda$onLanguageResult$14(i, locale);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onLanguageResult$14(int i, Locale locale) {
        if (this.ttsBar.getVisibility() != 0) {
            return;
        }
        if (i == 0 && locale != null) {
            this.tvTtsStatus.setText(SpeechLanguages.displayLabel(locale));
            return;
        }
        TextView textView = this.tvTtsStatus;
        if (i == 3) {
            textView.setText(R.string.tts_none_installed);
        } else {
            textView.setText(R.string.tts_language_unavailable);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showMoreMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenu().add(0, 1, 0, R.string.action_share);
        popupMenu.getMenu().add(0, 2, 1, R.string.action_speed_read);
        popupMenu.getMenu().add(0, 3, 2, R.string.action_mark_finished);
        popupMenu.getMenu().add(0, 4, 3, this.article.isArchived() ? R.string.action_unarchive : R.string.action_archive);
        if (!this.article.getUrl().isEmpty()) {
            popupMenu.getMenu().add(0, 5, 4, R.string.action_open_source);
        }
        popupMenu.getMenu().add(0, 6, 5, R.string.action_delete);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() { // from class: com.offread.app.ui.ReaderActivity.1
            @Override // android.widget.PopupMenu.OnMenuItemClickListener
            public final boolean onMenuItemClick(MenuItem menuItem) {
                return ReaderActivity.this.lambda$showMoreMenu$15(menuItem);
            }
        });
        popupMenu.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$showMoreMenu$15(MenuItem menuItem) {
        handleMoreAction(menuItem.getItemId());
        return true;
    }

    private void handleMoreAction(int i) {
        switch (i) {
            case 1:
                shareArticle();
                break;
            case 2:
                stopReading();
                Intent intent = new Intent(this, (Class<?>) SpeedReaderActivity.class);
                intent.putExtra("extra_article_id", this.article.getId());
                startActivity(intent);
                break;
            case 3:
                saveCurrentProgress();
                this.repository.markAsCompleted(this.article.getId());
                Toast.makeText(this, R.string.article_finished, 0).show();
                finish();
                break;
            case ArticleContentBlocks.TYPE_LIST_ITEM /* 4 */:
                boolean z = this.repository.toggleArchive(this.article.getId());
                this.article.setArchived(z);
                Toast.makeText(this, z ? R.string.moved_to_archive : R.string.restored_to_library, 0).show();
                break;
            case ArticleContentBlocks.TYPE_QUOTE /* 5 */:
                openSource();
                break;
            case ArticleContentBlocks.TYPE_CODE /* 6 */:
                confirmDelete();
                break;
        }
    }

    private void shareArticle() {
        String str;
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SEND");
        String url = this.article.getUrl();
        String title = this.article.getTitle();
        if (url.isEmpty()) {
            str = "";
        } else {
            str = url + "\n\n";
        }
        intent.putExtra("android.intent.extra.TEXT", title + "\n\n" + str + this.article.getExcerpt());
        intent.setType("text/plain");
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.action_share)));
        } catch (Exception unused) {
            Toast.makeText(this, R.string.error_share_failed, 0).show();
        }
    }

    private void openSource() {
        String url = this.article.getUrl();
        if (url.isEmpty() || !SafeUrl.isWellFormedHttpUrl(url)) {
            Toast.makeText(this, R.string.no_original_url, 0).show();
            return;
        }
        try {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
        } catch (Exception unused) {
            Toast.makeText(this, R.string.browser_unavailable, 0).show();
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this).setTitle(R.string.confirm_delete_title).setMessage(R.string.confirm_delete_message).setNegativeButton(R.string.action_cancel, (DialogInterface.OnClickListener) null).setPositiveButton(R.string.action_delete_confirm, new DialogInterface.OnClickListener() { // from class: com.offread.app.ui.ReaderActivity.16
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                ReaderActivity.this.lambda$confirmDelete$16(dialogInterface, i);
            }
        }).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$confirmDelete$16(DialogInterface dialogInterface, int i) {
        this.repository.deleteArticle(this.article.getId());
        Toast.makeText(this, R.string.article_deleted, 0).show();
        finish();
    }

    @Override // com.offread.app.ui.ThemedActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        if (this.article == null) {
            return;
        }
        this.sessionStart = System.currentTimeMillis();
    }

    @Override // android.app.Activity
    protected void onPause() {
        super.onPause();
        if (this.article == null) {
            return;
        }
        logReadingSession();
        saveCurrentProgress();
        ArticleSpeaker articleSpeaker = this.speaker;
        if (articleSpeaker != null) {
            articleSpeaker.pause();
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        ArticleSpeaker articleSpeaker = this.speaker;
        if (articleSpeaker != null) {
            articleSpeaker.shutdown();
            this.speaker = null;
        }
    }

    private void logReadingSession() {
        if (this.sessionStart == 0) {
            return;
        }
        int iCurrentTimeMillis = this.sessionSeconds + ((int) ((System.currentTimeMillis() - this.sessionStart) / 1000));
        this.sessionSeconds = iCurrentTimeMillis;
        this.sessionStart = 0L;
        if (iCurrentTimeMillis <= 5) {
            return;
        }
        this.repository.recordReadingSession(this.article.getId(), this.sessionSeconds, Math.min(this.article.getWordCount(), (int) (this.sessionSeconds * 3.5f)));
        this.sessionSeconds = 0;
    }

    private void saveCurrentProgress() {
        int scrollY = this.scroll.getScrollY();
        int height = this.scroll.getChildAt(0).getHeight() - this.scroll.getHeight();
        int iMax = height > 0 ? Math.max(0, Math.min(100, (int) ((scrollY / height) * 100.0f))) : 0;
        this.article.setScrollPosition(scrollY);
        this.article.setProgressPercent(iMax);
        this.repository.updateReadingProgress(this.article.getId(), scrollY, iMax);
    }

    private void post(final Runnable runnable) {
        this.mainHandler.post(new Runnable() { // from class: com.offread.app.ui.ReaderActivity.15
            @Override // java.lang.Runnable
            public final void run() {
                ReaderActivity.this.lambda$post$17(runnable);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$post$17(Runnable runnable) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        runnable.run();
    }

    private void applySystemBarsFor(UIHelper.ReaderThemeColors readerThemeColors) {
        UIHelper.applySystemBars(this, readerThemeColors.backgroundColor, this.ttsBar.getVisibility() == 0 ? readerThemeColors.surfaceColor : readerThemeColors.backgroundColor, readerThemeColors.isLight);
    }
}
