package com.offread.app.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.offread.app.R;
import com.offread.app.data.AppPreferences;
import com.offread.app.data.Article;
import com.offread.app.data.ArticleRepository;
import com.offread.app.parser.TextAnalysisUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class SpeedReaderActivity extends ThemedActivity {
    public static final String EXTRA_ARTICLE_ID = "extra_article_id";
    private Article article;
    private ImageButton btnPlayPause;
    private TextView btnWpm250;
    private TextView btnWpm350;
    private TextView btnWpm450;
    private TextView btnWpm600;
    private AppPreferences preferences;
    private ArticleRepository repository;
    private SeekBar seekProgress;
    private Runnable tickerRunnable;
    private TextView tvContext;
    private TextView tvCurrentWpm;
    private TextView tvProgressLabel;
    private TextView tvWord;
    private List<String> words = new ArrayList();
    private int currentWordIndex = 0;
    private boolean isPlaying = false;
    private int currentWpm = 350;
    private final Handler tickerHandler = new Handler(Looper.getMainLooper());
    private int wordsReadInSession = 0;

    @Override // com.offread.app.ui.ThemedActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_speed_reader);
        applySystemBars();
        this.repository = ArticleRepository.getInstance(this);
        AppPreferences appPreferences = new AppPreferences(this);
        this.preferences = appPreferences;
        this.currentWpm = appPreferences.getSpeedWpm();
        Article article = this.repository.getArticle(getIntent().getLongExtra("extra_article_id", -1L));
        this.article = article;
        if (article == null) {
            Toast.makeText(this, R.string.error_article_missing, 0).show();
            finish();
        } else {
            prepareWords();
            initViews();
            setupTicker();
            updateDisplay();
        }
    }

    @Override // android.app.Activity
    protected void onPause() {
        Article article;
        super.onPause();
        pauseTicker();
        int i = this.wordsReadInSession;
        if (i <= 0 || (article = this.article) == null) {
            return;
        }
        this.repository.recordReadingSession(article.getId(), Math.max(1, (int) ((i / this.currentWpm) * 60.0f)), this.wordsReadInSession);
        this.wordsReadInSession = 0;
    }

    private void prepareWords() {
        String plainText = this.article.getPlainText();
        if (plainText != null && !plainText.trim().isEmpty()) {
            this.words.addAll(Arrays.asList(plainText.trim().split("\\s+")));
        }
        if (this.words.isEmpty()) {
            this.words.add("No");
            this.words.add("content");
            this.words.add("found.");
        }
    }

    private void initViews() {
        ImageButton imageButton = (ImageButton) findViewById(R.id.btn_speed_back);
        TextView textView = (TextView) findViewById(R.id.tv_speed_title);
        this.tvWord = (TextView) findViewById(R.id.tv_rsvp_word);
        this.tvContext = (TextView) findViewById(R.id.tv_rsvp_context);
        this.tvCurrentWpm = (TextView) findViewById(R.id.tv_speed_current_wpm);
        this.tvProgressLabel = (TextView) findViewById(R.id.tv_speed_progress_label);
        this.seekProgress = (SeekBar) findViewById(R.id.seek_speed_progress);
        this.btnPlayPause = (ImageButton) findViewById(R.id.btn_speed_play_pause);
        Button button = (Button) findViewById(R.id.btn_speed_rewind);
        Button button2 = (Button) findViewById(R.id.btn_speed_forward);
        this.btnWpm250 = (TextView) findViewById(R.id.btn_wpm_250);
        this.btnWpm350 = (TextView) findViewById(R.id.btn_wpm_350);
        this.btnWpm450 = (TextView) findViewById(R.id.btn_wpm_450);
        this.btnWpm600 = (TextView) findViewById(R.id.btn_wpm_600);
        textView.setText(this.article.getTitle());
        this.tvCurrentWpm.setText(this.currentWpm + " WPM");
        this.seekProgress.setMax(this.words.size() + (-1));
        imageButton.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.SpeedReaderActivity.0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SpeedReaderActivity.this.lambda$initViews$0(view);
            }
        });
        this.btnPlayPause.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.SpeedReaderActivity.3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SpeedReaderActivity.this.lambda$initViews$1(view);
            }
        });
        button.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.SpeedReaderActivity.4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SpeedReaderActivity.this.lambda$initViews$2(view);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.SpeedReaderActivity.5
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SpeedReaderActivity.this.lambda$initViews$3(view);
            }
        });
        this.seekProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { // from class: com.offread.app.ui.SpeedReaderActivity.1
            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                if (z) {
                    SpeedReaderActivity.this.currentWordIndex = i;
                    SpeedReaderActivity.this.updateDisplay();
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar seekBar) {
                SpeedReaderActivity.this.pauseTicker();
            }
        });
        this.btnWpm250.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.SpeedReaderActivity.6
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SpeedReaderActivity.this.lambda$initViews$4(view);
            }
        });
        this.btnWpm350.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.SpeedReaderActivity.7
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SpeedReaderActivity.this.lambda$initViews$5(view);
            }
        });
        this.btnWpm450.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.SpeedReaderActivity.8
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SpeedReaderActivity.this.lambda$initViews$6(view);
            }
        });
        this.btnWpm600.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.SpeedReaderActivity.9
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SpeedReaderActivity.this.lambda$initViews$7(view);
            }
        });
        updateWpmChips();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$initViews$0(View view) {
        finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$initViews$1(View view) {
        if (this.isPlaying) {
            pauseTicker();
        } else {
            startTicker();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$initViews$2(View view) {
        this.currentWordIndex = Math.max(0, this.currentWordIndex - 10);
        updateDisplay();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$initViews$3(View view) {
        this.currentWordIndex = Math.min(this.words.size() - 1, this.currentWordIndex + 10);
        updateDisplay();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$initViews$4(View view) {
        setWpm(250);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$initViews$5(View view) {
        setWpm(350);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$initViews$6(View view) {
        setWpm(450);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$initViews$7(View view) {
        setWpm(600);
    }

    private void setWpm(int i) {
        this.currentWpm = i;
        this.preferences.setSpeedWpm(i);
        this.tvCurrentWpm.setText(i + " WPM");
        updateWpmChips();
    }

    private void updateWpmChips() {
        TextView textView = this.btnWpm250;
        int i = this.currentWpm;
        int i2 = R.drawable.bg_chip_active;
        textView.setBackgroundResource(i == 250 ? R.drawable.bg_chip_active : R.drawable.bg_chip);
        this.btnWpm350.setBackgroundResource(this.currentWpm == 350 ? R.drawable.bg_chip_active : R.drawable.bg_chip);
        this.btnWpm450.setBackgroundResource(this.currentWpm == 450 ? R.drawable.bg_chip_active : R.drawable.bg_chip);
        TextView textView2 = this.btnWpm600;
        if (this.currentWpm != 600) {
            i2 = R.drawable.bg_chip;
        }
        textView2.setBackgroundResource(i2);
    }

    private void setupTicker() {
        this.tickerRunnable = new Runnable() { // from class: com.offread.app.ui.SpeedReaderActivity.2
            @Override // java.lang.Runnable
            public void run() {
                double d;
                double d2;
                if (SpeedReaderActivity.this.isPlaying) {
                    int i = SpeedReaderActivity.this.currentWordIndex;
                    int size = SpeedReaderActivity.this.words.size() - 1;
                    SpeedReaderActivity speedReaderActivity = SpeedReaderActivity.this;
                    if (i < size) {
                        speedReaderActivity.currentWordIndex++;
                        SpeedReaderActivity.this.wordsReadInSession++;
                        SpeedReaderActivity.this.updateDisplay();
                        String str = (String) SpeedReaderActivity.this.words.get(SpeedReaderActivity.this.currentWordIndex);
                        long j = 60000 / SpeedReaderActivity.this.currentWpm;
                        if (!str.endsWith(".") && !str.endsWith("!") && !str.endsWith("?")) {
                            if (str.endsWith(",") || str.endsWith(";") || str.endsWith(":")) {
                                d = j;
                                d2 = 1.4d;
                            }
                            SpeedReaderActivity.this.tickerHandler.postDelayed(this, j);
                            return;
                        }
                        d = j;
                        d2 = 1.8d;
                        j = (long) (d * d2);
                        SpeedReaderActivity.this.tickerHandler.postDelayed(this, j);
                        return;
                    }
                    speedReaderActivity.pauseTicker();
                    Toast.makeText(SpeedReaderActivity.this, R.string.speed_completed, 0).show();
                }
            }
        };
    }

    private void startTicker() {
        if (this.currentWordIndex >= this.words.size() - 1) {
            this.currentWordIndex = 0;
        }
        this.isPlaying = true;
        this.btnPlayPause.setImageResource(R.drawable.ic_pause);
        this.tickerHandler.post(this.tickerRunnable);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void pauseTicker() {
        this.isPlaying = false;
        this.btnPlayPause.setImageResource(R.drawable.ic_play);
        this.tickerHandler.removeCallbacks(this.tickerRunnable);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateDisplay() {
        int i = this.currentWordIndex;
        if (i < 0 || i >= this.words.size()) {
            return;
        }
        String str = this.words.get(this.currentWordIndex);
        int iCalculateOrpIndex = TextAnalysisUtil.calculateOrpIndex(str);
        SpannableString spannableString = new SpannableString(str);
        if (iCalculateOrpIndex < str.length()) {
            spannableString.setSpan(new ForegroundColorSpan(themeColor(R.attr.colorDanger)), iCalculateOrpIndex, iCalculateOrpIndex + 1, 33);
        }
        this.tvWord.setText(spannableString);
        int iMin = Math.min(this.words.size(), this.currentWordIndex + 4);
        StringBuilder sb = new StringBuilder();
        for (int iMax = Math.max(0, this.currentWordIndex - 3); iMax < iMin; iMax++) {
            if (iMax == this.currentWordIndex) {
                sb.append("[");
                sb.append(this.words.get(iMax));
                sb.append("] ");
            } else {
                sb.append(this.words.get(iMax));
                sb.append(" ");
            }
        }
        this.tvContext.setText(sb.toString().trim());
        this.seekProgress.setProgress(this.currentWordIndex);
        int size = (int) ((this.currentWordIndex / this.words.size()) * 100.0f);
        this.tvProgressLabel.setText((this.currentWordIndex + 1) + " / " + this.words.size() + " words (" + size + "%)");
    }
}
