package com.offread.app.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import com.offread.app.R;
import com.offread.app.data.ArticleRepository;
import com.offread.app.data.ReadingStats;

import java.text.NumberFormat;
import java.util.Locale;

public class StatisticsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        ImageButton btnBack = findViewById(R.id.btn_stats_back);
        btnBack.setOnClickListener(v -> finish());

        loadStatistics();
    }

    private void loadStatistics() {
        ArticleRepository repository = ArticleRepository.getInstance(this);
        ReadingStats stats = repository.getReadingStats();

        TextView tvStreak = findViewById(R.id.tv_stat_streak_val);
        TextView tvSaved = findViewById(R.id.tv_stat_saved_val);
        TextView tvFinished = findViewById(R.id.tv_stat_finished_val);
        TextView tvWords = findViewById(R.id.tv_stat_words_val);
        TextView tvTime = findViewById(R.id.tv_stat_time_val);

        int streak = stats.getStreakDays();
        tvStreak.setText(streak + (streak == 1 ? " Day" : " Days"));

        tvSaved.setText(String.valueOf(stats.getTotalArticles()));
        tvFinished.setText(String.valueOf(stats.getFinishedArticles()));

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
        tvWords.setText(nf.format(stats.getTotalWordsRead()));

        tvTime.setText(stats.getFormattedReadingTime());
    }
}
