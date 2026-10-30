package com.offread.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.offread.app.R;
import com.offread.app.data.ArticleRepository;
import com.offread.app.data.ReadingStats;
import java.text.NumberFormat;
import java.util.Locale;

/* JADX INFO: loaded from: classes.dex */
public class StatisticsActivity extends ThemedActivity {
    @Override // com.offread.app.ui.ThemedActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_statistics);
        applySystemBars();
        ((ImageButton) findViewById(R.id.btn_stats_back)).setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.StatisticsActivity.0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                StatisticsActivity.this.lambda$onCreate$0(view);
            }
        });
        loadStatistics();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$0(View view) {
        finish();
    }

    private void loadStatistics() {
        ReadingStats readingStats = ArticleRepository.getInstance(this).getReadingStats();
        TextView textView = (TextView) findViewById(R.id.tv_stat_streak_val);
        TextView textView2 = (TextView) findViewById(R.id.tv_stat_saved_val);
        TextView textView3 = (TextView) findViewById(R.id.tv_stat_finished_val);
        TextView textView4 = (TextView) findViewById(R.id.tv_stat_words_val);
        TextView textView5 = (TextView) findViewById(R.id.tv_stat_time_val);
        int streakDays = readingStats.getStreakDays();
        textView.setText(streakDays + (streakDays == 1 ? " Day" : " Days"));
        textView2.setText(String.valueOf(readingStats.getTotalArticles()));
        textView3.setText(String.valueOf(readingStats.getFinishedArticles()));
        textView4.setText(NumberFormat.getNumberInstance(Locale.getDefault()).format((long) readingStats.getTotalWordsRead()));
        textView5.setText(readingStats.getFormattedReadingTime());
    }
}
