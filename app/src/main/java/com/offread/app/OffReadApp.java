package com.offread.app;

import android.app.Application;
import com.offread.app.data.AppPreferences;
import com.offread.app.data.ArticleRepository;
import com.offread.app.util.SampleContentHelper;

/* JADX INFO: loaded from: classes.dex */
public class OffReadApp extends Application {
    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        AppPreferences appPreferences = new AppPreferences(this);
        if (appPreferences.isFirstRun()) {
            SampleContentHelper.populateSamplesIfEmpty(ArticleRepository.getInstance(this));
            appPreferences.setFirstRunCompleted();
        }
    }
}
