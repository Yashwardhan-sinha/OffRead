package com.offread.app.ui;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
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

public class SpeedReaderActivity extends Activity {

    public static final String EXTRA_ARTICLE_ID = "extra_article_id";

    private ArticleRepository repository;
    private AppPreferences preferences;
    private Article article;

    private List<String> words = new ArrayList<>();
    private int currentWordIndex = 0;
    private boolean isPlaying = false;
    private int currentWpm = 350;

    private final Handler tickerHandler = new Handler(Looper.getMainLooper());
    private Runnable tickerRunnable;

    private TextView tvWord;
    private TextView tvContext;
    private TextView tvCurrentWpm;
    private TextView tvProgressLabel;
    private SeekBar seekProgress;
    private ImageButton btnPlayPause;

    private TextView btnWpm250;
    private TextView btnWpm350;
    private TextView btnWpm450;
    private TextView btnWpm600;

    private int wordsReadInSession = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_reader);

        repository = ArticleRepository.getInstance(this);
        preferences = new AppPreferences(this);
        currentWpm = preferences.getSpeedWpm();

        long articleId = getIntent().getLongExtra(EXTRA_ARTICLE_ID, -1);
        article = repository.getArticle(articleId);

        if (article == null) {
            Toast.makeText(this, "Article not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        prepareWords();
        initViews();
        setupTicker();
        updateDisplay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseTicker();
        if (wordsReadInSession > 0 && article != null) {
            int durationSec = (int) ((wordsReadInSession / (float) currentWpm) * 60);
            repository.recordReadingSession(article.getId(), Math.max(1, durationSec), wordsReadInSession);
            wordsReadInSession = 0;
        }
    }

    private void prepareWords() {
        String plain = article.getPlainText();
        if (plain != null && !plain.trim().isEmpty()) {
            String[] tokens = plain.trim().split("\\s+");
            words.addAll(Arrays.asList(tokens));
        }
        if (words.isEmpty()) {
            words.add("No");
            words.add("content");
            words.add("found.");
        }
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btn_speed_back);
        TextView tvTitle = findViewById(R.id.tv_speed_title);
        tvWord = findViewById(R.id.tv_rsvp_word);
        tvContext = findViewById(R.id.tv_rsvp_context);
        tvCurrentWpm = findViewById(R.id.tv_speed_current_wpm);
        tvProgressLabel = findViewById(R.id.tv_speed_progress_label);
        seekProgress = findViewById(R.id.seek_speed_progress);

        btnPlayPause = findViewById(R.id.btn_speed_play_pause);
        Button btnRewind = findViewById(R.id.btn_speed_rewind);
        Button btnForward = findViewById(R.id.btn_speed_forward);

        btnWpm250 = findViewById(R.id.btn_wpm_250);
        btnWpm350 = findViewById(R.id.btn_wpm_350);
        btnWpm450 = findViewById(R.id.btn_wpm_450);
        btnWpm600 = findViewById(R.id.btn_wpm_600);

        tvTitle.setText(article.getTitle());
        tvCurrentWpm.setText(currentWpm + " WPM");
        seekProgress.setMax(words.size() - 1);

        btnBack.setOnClickListener(v -> finish());

        btnPlayPause.setOnClickListener(v -> {
            if (isPlaying) {
                pauseTicker();
            } else {
                startTicker();
            }
        });

        btnRewind.setOnClickListener(v -> {
            currentWordIndex = Math.max(0, currentWordIndex - 10);
            updateDisplay();
        });

        btnForward.setOnClickListener(v -> {
            currentWordIndex = Math.min(words.size() - 1, currentWordIndex + 10);
            updateDisplay();
        });

        seekProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    currentWordIndex = progress;
                    updateDisplay();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pauseTicker();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnWpm250.setOnClickListener(v -> setWpm(250));
        btnWpm350.setOnClickListener(v -> setWpm(350));
        btnWpm450.setOnClickListener(v -> setWpm(450));
        btnWpm600.setOnClickListener(v -> setWpm(600));

        updateWpmChips();
    }

    private void setWpm(int wpm) {
        this.currentWpm = wpm;
        preferences.setSpeedWpm(wpm);
        tvCurrentWpm.setText(wpm + " WPM");
        updateWpmChips();
    }

    private void updateWpmChips() {
        btnWpm250.setBackgroundResource(currentWpm == 250 ? R.drawable.bg_chip_active : R.drawable.bg_chip);
        btnWpm350.setBackgroundResource(currentWpm == 350 ? R.drawable.bg_chip_active : R.drawable.bg_chip);
        btnWpm450.setBackgroundResource(currentWpm == 450 ? R.drawable.bg_chip_active : R.drawable.bg_chip);
        btnWpm600.setBackgroundResource(currentWpm == 600 ? R.drawable.bg_chip_active : R.drawable.bg_chip);
    }

    private void setupTicker() {
        tickerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPlaying) return;

                if (currentWordIndex < words.size() - 1) {
                    currentWordIndex++;
                    wordsReadInSession++;
                    updateDisplay();

                    String word = words.get(currentWordIndex);
                    long delay = 60000 / currentWpm;

                    // Pause longer on sentence endings
                    if (word.endsWith(".") || word.endsWith("!") || word.endsWith("?")) {
                        delay = (long) (delay * 1.8);
                    } else if (word.endsWith(",") || word.endsWith(";") || word.endsWith(":")) {
                        delay = (long) (delay * 1.4);
                    }

                    tickerHandler.postDelayed(this, delay);
                } else {
                    pauseTicker();
                    Toast.makeText(SpeedReaderActivity.this, "Completed Speed Reading!", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void startTicker() {
        if (currentWordIndex >= words.size() - 1) {
            currentWordIndex = 0;
        }
        isPlaying = true;
        btnPlayPause.setImageResource(R.drawable.ic_pause);
        tickerHandler.post(tickerRunnable);
    }

    private void pauseTicker() {
        isPlaying = false;
        btnPlayPause.setImageResource(R.drawable.ic_play);
        tickerHandler.removeCallbacks(tickerRunnable);
    }

    private void updateDisplay() {
        if (currentWordIndex < 0 || currentWordIndex >= words.size()) return;

        String rawWord = words.get(currentWordIndex);
        int orpIndex = TextAnalysisUtil.calculateOrpIndex(rawWord);

        SpannableString span = new SpannableString(rawWord);
        if (orpIndex < rawWord.length()) {
            span.setSpan(new ForegroundColorSpan(Color.parseColor("#EF4444")),
                    orpIndex, orpIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tvWord.setText(span);

        // Surrounding context sentence snippet
        int start = Math.max(0, currentWordIndex - 3);
        int end = Math.min(words.size(), currentWordIndex + 4);
        StringBuilder contextBuilder = new StringBuilder();
        for (int i = start; i < end; i++) {
            if (i == currentWordIndex) {
                contextBuilder.append("[").append(words.get(i)).append("] ");
            } else {
                contextBuilder.append(words.get(i)).append(" ");
            }
        }
        tvContext.setText(contextBuilder.toString().trim());

        seekProgress.setProgress(currentWordIndex);

        int percent = (int) (((float) currentWordIndex / words.size()) * 100);
        tvProgressLabel.setText((currentWordIndex + 1) + " / " + words.size() + " words (" + percent + "%)");
    }
}
