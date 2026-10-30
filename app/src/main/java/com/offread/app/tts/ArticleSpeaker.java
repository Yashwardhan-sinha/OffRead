package com.offread.app.tts;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import com.offread.app.parser.TextAnalysisUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/* JADX INFO: loaded from: classes.dex */
public class ArticleSpeaker {
    public static final int LANGUAGE_MISSING_DATA = 1;
    public static final int LANGUAGE_NO_ENGINE = 3;
    public static final int LANGUAGE_OK = 0;
    public static final int LANGUAGE_UNSUPPORTED = 2;
    private Locale appliedLanguage;
    private Locale articleLanguage;
    private final Context context;
    private SpeakerListener listener;
    private Locale preferredLanguage;
    private TextToSpeech tts;
    private boolean isInitialized = false;
    private volatile boolean released = false;
    private List<String> sentences = new ArrayList();
    private int currentIndex = 0;
    private boolean isPlaying = false;
    private float speechRate = 1.0f;

    public interface SpeakerListener {
        void onInitialized(boolean z);

        void onLanguageResult(int i, Locale locale);

        void onPlaybackCompleted();

        void onPlaybackStateChanged(boolean z);

        void onSentenceChanged(int i, int i2, String str);
    }

    public ArticleSpeaker(Context context, SpeakerListener speakerListener) {
        this.context = context.getApplicationContext();
        this.listener = speakerListener;
        initTts();
    }

    private void initTts() {
        try {
            this.tts = new TextToSpeech(this.context, new TextToSpeech.OnInitListener() { // from class: com.offread.app.tts.ArticleSpeaker.0
                @Override // android.speech.tts.TextToSpeech.OnInitListener
                public final void onInit(int i) {
                    ArticleSpeaker.this.lambda$initTts$0(i);
                }
            });
        } catch (Throwable unused) {
            this.isInitialized = false;
            this.tts = null;
            SpeakerListener speakerListener = this.listener;
            if (speakerListener != null) {
                speakerListener.onInitialized(false);
                speakerListener.onLanguageResult(3, null);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$initTts$0(int i) {
        TextToSpeech textToSpeech = this.tts;
        SpeakerListener speakerListener = this.listener;
        if (this.released) {
            return;
        }
        if (i != 0 || textToSpeech == null) {
            this.isInitialized = false;
            if (speakerListener != null) {
                speakerListener.onInitialized(false);
                return;
            }
            return;
        }
        textToSpeech.setSpeechRate(this.speechRate);
        applyLanguage(resolveTargetLanguage());
        this.isInitialized = true;
        setupUtteranceListener();
        if (speakerListener != null) {
            speakerListener.onInitialized(true);
        }
    }

    private Locale resolveTargetLanguage() {
        Locale locale = this.preferredLanguage;
        if (locale != null) {
            return locale;
        }
        Locale locale2 = this.articleLanguage;
        return locale2 != null ? locale2 : Locale.getDefault();
    }

    private int applyLanguage(Locale locale) {
        int language;
        TextToSpeech textToSpeech = this.tts;
        if (textToSpeech != null && locale != null) {
            try {
                int language2 = textToSpeech.setLanguage(locale);
                if (language2 == -1) {
                    report(1, null);
                    return 1;
                }
                if (language2 == -2) {
                    Locale locale2 = Locale.getDefault();
                    try {
                        language = textToSpeech.setLanguage(locale2);
                    } catch (Throwable unused) {
                        language = 2;
                    }
                    if (language == -1 || language == -2) {
                        report(2, null);
                        return 2;
                    }
                    this.appliedLanguage = locale2;
                    report(2, locale2);
                    return 2;
                }
                this.appliedLanguage = locale;
                report(0, locale);
                return 0;
            } catch (Throwable unused2) {
            }
        }
        return 3;
    }

    private void report(int i, Locale locale) {
        SpeakerListener speakerListener = this.listener;
        if (this.released || speakerListener == null) {
            return;
        }
        speakerListener.onLanguageResult(i, locale);
    }

    public void setPreferredLanguage(Locale locale) {
        this.preferredLanguage = locale;
        if (this.isInitialized) {
            applyLanguage(resolveTargetLanguage());
        }
    }

    public void setArticleLanguage(Locale locale) {
        this.articleLanguage = locale;
        if (locale != null && this.preferredLanguage == null && this.isInitialized) {
            applyLanguage(locale);
        }
    }

    public Locale getAppliedLanguage() {
        return this.appliedLanguage;
    }

    public boolean isEngineAvailable() {
        return this.tts != null;
    }

    public boolean isLanguageSupported(Locale locale) {
        TextToSpeech textToSpeech = this.tts;
        if (textToSpeech != null && locale != null) {
            try {
                int iIsLanguageAvailable = textToSpeech.isLanguageAvailable(locale);
                if (iIsLanguageAvailable != -2 && iIsLanguageAvailable != -1) {
                    return true;
                }
            } catch (Throwable unused) {
            }
        }
        return false;
    }

    private void setupUtteranceListener() {
        TextToSpeech textToSpeech = this.tts;
        if (textToSpeech == null) {
            return;
        }
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() { // from class: com.offread.app.tts.ArticleSpeaker.1
            @Override // android.speech.tts.UtteranceProgressListener
            public void onStart(String str) {
                SpeakerListener speakerListener = ArticleSpeaker.this.listener;
                if (ArticleSpeaker.this.released || speakerListener == null) {
                    return;
                }
                ArticleSpeaker.this.isPlaying = true;
                speakerListener.onPlaybackStateChanged(true);
            }

            @Override // android.speech.tts.UtteranceProgressListener
            public void onDone(String str) {
                SpeakerListener speakerListener = ArticleSpeaker.this.listener;
                if (!ArticleSpeaker.this.released && ArticleSpeaker.this.isPlaying) {
                    ArticleSpeaker.this.currentIndex++;
                    int i = ArticleSpeaker.this.currentIndex;
                    int size = ArticleSpeaker.this.sentences.size();
                    ArticleSpeaker articleSpeaker = ArticleSpeaker.this;
                    if (i < size) {
                        articleSpeaker.speakCurrentSentence();
                        return;
                    }
                    articleSpeaker.isPlaying = false;
                    ArticleSpeaker.this.currentIndex = 0;
                    if (speakerListener != null) {
                        speakerListener.onPlaybackStateChanged(false);
                        speakerListener.onPlaybackCompleted();
                    }
                }
            }

            @Override // android.speech.tts.UtteranceProgressListener
            public void onError(String str) {
                SpeakerListener speakerListener = ArticleSpeaker.this.listener;
                if (ArticleSpeaker.this.released || speakerListener == null) {
                    return;
                }
                ArticleSpeaker.this.isPlaying = false;
                speakerListener.onPlaybackStateChanged(false);
            }
        });
    }

    public void loadText(String str) {
        stop();
        this.sentences = TextAnalysisUtil.splitSentences(str);
        this.currentIndex = 0;
    }

    public List<String> getSentences() {
        return this.sentences;
    }

    public void play() {
        if (!this.isInitialized || this.sentences.isEmpty()) {
            return;
        }
        if (this.currentIndex >= this.sentences.size()) {
            this.currentIndex = 0;
        }
        this.isPlaying = true;
        speakCurrentSentence();
    }

    public void pause() {
        SpeakerListener speakerListener;
        this.isPlaying = false;
        TextToSpeech textToSpeech = this.tts;
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        if (this.released || (speakerListener = this.listener) == null) {
            return;
        }
        speakerListener.onPlaybackStateChanged(false);
    }

    public void stop() {
        SpeakerListener speakerListener;
        this.isPlaying = false;
        this.currentIndex = 0;
        TextToSpeech textToSpeech = this.tts;
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        if (this.released || (speakerListener = this.listener) == null) {
            return;
        }
        speakerListener.onPlaybackStateChanged(false);
    }

    public void next() {
        if (this.currentIndex < this.sentences.size() - 1) {
            int i = this.currentIndex + 1;
            this.currentIndex = i;
            if (this.isPlaying) {
                speakCurrentSentence();
                return;
            }
            SpeakerListener speakerListener = this.listener;
            if (speakerListener != null) {
                speakerListener.onSentenceChanged(i, this.sentences.size(), this.sentences.get(this.currentIndex));
            }
        }
    }

    public void previous() {
        int i = this.currentIndex;
        if (i > 0) {
            int i2 = i - 1;
            this.currentIndex = i2;
            if (this.isPlaying) {
                speakCurrentSentence();
                return;
            }
            SpeakerListener speakerListener = this.listener;
            if (speakerListener != null) {
                speakerListener.onSentenceChanged(i2, this.sentences.size(), this.sentences.get(this.currentIndex));
            }
        }
    }

    public void setSpeechRate(float f) {
        float fMax = Math.max(0.5f, Math.min(2.5f, f));
        this.speechRate = fMax;
        TextToSpeech textToSpeech = this.tts;
        if (textToSpeech != null) {
            textToSpeech.setSpeechRate(fMax);
        }
    }

    public float getSpeechRate() {
        return this.speechRate;
    }

    public boolean isPlaying() {
        return this.isPlaying;
    }

    public int getCurrentIndex() {
        return this.currentIndex;
    }

    public int getTotalSentences() {
        return this.sentences.size();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void speakCurrentSentence() {
        TextToSpeech textToSpeech = this.tts;
        SpeakerListener speakerListener = this.listener;
        List<String> list = this.sentences;
        if (this.released || textToSpeech == null || !this.isInitialized || this.currentIndex >= list.size()) {
            return;
        }
        String str = list.get(this.currentIndex);
        if (speakerListener != null) {
            speakerListener.onSentenceChanged(this.currentIndex, list.size(), str);
        }
        Bundle bundle = new Bundle();
        bundle.putString("utteranceId", "sentence_" + this.currentIndex);
        textToSpeech.speak(str, 0, bundle, "sentence_" + this.currentIndex);
    }

    public void shutdown() {
        this.released = true;
        this.isInitialized = false;
        stop();
        TextToSpeech textToSpeech = this.tts;
        if (textToSpeech != null) {
            textToSpeech.shutdown();
            this.tts = null;
        }
        this.listener = null;
    }
}
