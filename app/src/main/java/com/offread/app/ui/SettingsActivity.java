package com.offread.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.offread.app.R;
import com.offread.app.data.Article;
import com.offread.app.data.ArticleRepository;
import com.offread.app.util.BackupHelper;
import com.offread.app.util.SampleContentHelper;

import java.io.File;
import java.util.List;

public class SettingsActivity extends Activity {

    private ArticleRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        repository = ArticleRepository.getInstance(this);

        ImageButton btnBack = findViewById(R.id.btn_settings_back);
        btnBack.setOnClickListener(v -> finish());

        View cardExport = findViewById(R.id.card_setting_export);
        View cardImport = findViewById(R.id.card_setting_import);
        View cardSamples = findViewById(R.id.card_setting_samples);
        View cardClear = findViewById(R.id.card_setting_clear);

        cardExport.setOnClickListener(v -> handleExport());
        cardImport.setOnClickListener(v -> handleImport());
        cardSamples.setOnClickListener(v -> handleRestoreSamples());
        cardClear.setOnClickListener(v -> handleClearAll());
    }

    private void handleExport() {
        try {
            List<Article> articles = repository.getAllArticlesForExport();
            String json = BackupHelper.exportToJson(articles);

            File targetDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (targetDir == null) {
                targetDir = getFilesDir();
            }

            File file = BackupHelper.writeBackupToFile(targetDir, json);

            // Share backup JSON file
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, json);
            sendIntent.setType("application/json");
            startActivity(Intent.createChooser(sendIntent, "Export OffRead Library"));

            Toast.makeText(this, "Library exported! (" + articles.size() + " articles)", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handleImport() {
        Toast.makeText(this, "Import backup files from Downloads or Documents", Toast.LENGTH_LONG).show();
    }

    private void handleRestoreSamples() {
        List<Article> samples = SampleContentHelper.getSampleArticles();
        for (Article a : samples) {
            repository.insertArticle(a);
        }
        Toast.makeText(this, "Sample articles restored!", Toast.LENGTH_SHORT).show();
    }

    private void handleClearAll() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Library?")
                .setMessage("Are you sure you want to permanently delete all saved articles and reading logs? This cannot be undone.")
                .setPositiveButton("Clear All", (dialog, which) -> {
                    repository.clearAll();
                    Toast.makeText(SettingsActivity.this, "Library cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
