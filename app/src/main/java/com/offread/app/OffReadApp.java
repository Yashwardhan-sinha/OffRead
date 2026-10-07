package com.offread.app;

import android.app.Application;

import com.offread.app.data.AppPreferences;
import com.offread.app.data.ArticleRepository;
import com.offread.app.util.SampleContentHelper;

public class OffReadApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AppPreferences preferences = new AppPreferences(this);
        if (preferences.isFirstRun()) {
            ArticleRepository repository = ArticleRepository.getInstance(this);
            SampleContentHelper.populateSamplesIfEmpty(repository);
            preferences.setFirstRunCompleted();
        }
    }
}
