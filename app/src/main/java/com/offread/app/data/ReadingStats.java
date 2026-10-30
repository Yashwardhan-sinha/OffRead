package com.offread.app.data;

/* JADX INFO: loaded from: classes.dex */
public class ReadingStats {
    private int finishedArticles;
    private int streakDays;
    private int totalArticles;
    private int totalSecondsRead;
    private int totalWordsRead;

    public ReadingStats() {
    }

    public ReadingStats(int i, int i2, int i3, int i4, int i5) {
        this.totalArticles = i;
        this.finishedArticles = i2;
        this.totalWordsRead = i3;
        this.totalSecondsRead = i4;
        this.streakDays = i5;
    }

    public int getTotalArticles() {
        return this.totalArticles;
    }

    public void setTotalArticles(int i) {
        this.totalArticles = i;
    }

    public int getFinishedArticles() {
        return this.finishedArticles;
    }

    public void setFinishedArticles(int i) {
        this.finishedArticles = i;
    }

    public int getTotalWordsRead() {
        return this.totalWordsRead;
    }

    public void setTotalWordsRead(int i) {
        this.totalWordsRead = i;
    }

    public int getTotalSecondsRead() {
        return this.totalSecondsRead;
    }

    public void setTotalSecondsRead(int i) {
        this.totalSecondsRead = i;
    }

    public int getStreakDays() {
        return this.streakDays;
    }

    public void setStreakDays(int i) {
        this.streakDays = i;
    }

    public String getFormattedReadingTime() {
        int i = this.totalSecondsRead / 60;
        if (i < 60) {
            return i + "m";
        }
        int i2 = i / 60;
        int i3 = i % 60;
        if (i3 == 0) {
            return i2 + "h";
        }
        return i2 + "h " + i3 + "m";
    }
}
