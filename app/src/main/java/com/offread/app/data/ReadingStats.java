package com.offread.app.data;

public class ReadingStats {
    private int totalArticles;
    private int finishedArticles;
    private int totalWordsRead;
    private int totalSecondsRead;
    private int streakDays;

    public ReadingStats() {
    }

    public ReadingStats(int totalArticles, int finishedArticles, int totalWordsRead, int totalSecondsRead, int streakDays) {
        this.totalArticles = totalArticles;
        this.finishedArticles = finishedArticles;
        this.totalWordsRead = totalWordsRead;
        this.totalSecondsRead = totalSecondsRead;
        this.streakDays = streakDays;
    }

    public int getTotalArticles() { return totalArticles; }
    public void setTotalArticles(int totalArticles) { this.totalArticles = totalArticles; }

    public int getFinishedArticles() { return finishedArticles; }
    public void setFinishedArticles(int finishedArticles) { this.finishedArticles = finishedArticles; }

    public int getTotalWordsRead() { return totalWordsRead; }
    public void setTotalWordsRead(int totalWordsRead) { this.totalWordsRead = totalWordsRead; }

    public int getTotalSecondsRead() { return totalSecondsRead; }
    public void setTotalSecondsRead(int totalSecondsRead) { this.totalSecondsRead = totalSecondsRead; }

    public int getStreakDays() { return streakDays; }
    public void setStreakDays(int streakDays) { this.streakDays = streakDays; }

    public String getFormattedReadingTime() {
        int minutes = totalSecondsRead / 60;
        if (minutes < 60) {
            return minutes + "m";
        }
        int hours = minutes / 60;
        int remMin = minutes % 60;
        if (remMin == 0) {
            return hours + "h";
        }
        return hours + "h " + remMin + "m";
    }
}
