package com.offread.app.tts;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.offread.app.parser.TextAnalysisUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ArticleSpeaker {

    public interface SpeakerListener {
        void onInitialized(boolean success);
        void onSentenceChanged(int index, int total, String sentence);
        void onPlaybackStateChanged(boolean isPlaying);
        void onPlaybackCompleted();
    }

    private final Context context;
    private TextToSpeech tts;
    private SpeakerListener listener;
    private boolean isInitialized = false;

    private List<String> sentences = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isPlaying = false;
    private float speechRate = 1.0f;

    public ArticleSpeaker(Context context, SpeakerListener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        initTts();
    }

    private void initTts() {
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int res = tts.setLanguage(Locale.getDefault());
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(Locale.US);
                }
                tts.setSpeechRate(speechRate);
                isInitialized = true;
                setupUtteranceListener();
                if (listener != null) {
                    listener.onInitialized(true);
                }
            } else {
                isInitialized = false;
                if (listener != null) {
                    listener.onInitialized(false);
                }
            }
        });
    }

    private void setupUtteranceListener() {
        if (tts == null) return;
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                isPlaying = true;
                if (listener != null) {
                    listener.onPlaybackStateChanged(true);
                }
            }

            @Override
            public void onDone(String utteranceId) {
                if (isPlaying) {
                    currentIndex++;
                    if (currentIndex < sentences.size()) {
                        speakCurrentSentence();
                    } else {
                        isPlaying = false;
                        currentIndex = 0;
                        if (listener != null) {
                            listener.onPlaybackStateChanged(false);
                            listener.onPlaybackCompleted();
                        }
                    }
                }
            }

            @Override
            public void onError(String utteranceId) {
                isPlaying = false;
                if (listener != null) {
                    listener.onPlaybackStateChanged(false);
                }
            }
        });
    }

    public void loadText(String fullText) {
        stop();
        sentences = TextAnalysisUtil.splitSentences(fullText);
        currentIndex = 0;
    }

    public void play() {
        if (!isInitialized || sentences.isEmpty()) return;
        if (currentIndex >= sentences.size()) {
            currentIndex = 0;
        }
        isPlaying = true;
        speakCurrentSentence();
    }

    public void pause() {
        isPlaying = false;
        if (tts != null) {
            tts.stop();
        }
        if (listener != null) {
            listener.onPlaybackStateChanged(false);
        }
    }

    public void stop() {
        isPlaying = false;
        currentIndex = 0;
        if (tts != null) {
            tts.stop();
        }
        if (listener != null) {
            listener.onPlaybackStateChanged(false);
        }
    }

    public void next() {
        if (currentIndex < sentences.size() - 1) {
            currentIndex++;
            if (isPlaying) {
                speakCurrentSentence();
            } else if (listener != null) {
                listener.onSentenceChanged(currentIndex, sentences.size(), sentences.get(currentIndex));
            }
        }
    }

    public void previous() {
        if (currentIndex > 0) {
            currentIndex--;
            if (isPlaying) {
                speakCurrentSentence();
            } else if (listener != null) {
                listener.onSentenceChanged(currentIndex, sentences.size(), sentences.get(currentIndex));
            }
        }
    }

    public void setSpeechRate(float rate) {
        this.speechRate = Math.max(0.5f, Math.min(2.5f, rate));
        if (tts != null) {
            tts.setSpeechRate(this.speechRate);
        }
    }

    public float getSpeechRate() {
        return speechRate;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int getTotalSentences() {
        return sentences.size();
    }

    private void speakCurrentSentence() {
        if (tts == null || !isInitialized || currentIndex >= sentences.size()) return;
        String sentence = sentences.get(currentIndex);
        if (listener != null) {
            listener.onSentenceChanged(currentIndex, sentences.size(), sentence);
        }
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "sentence_" + currentIndex);
        tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, params, "sentence_" + currentIndex);
    }

    public void shutdown() {
        stop();
        if (tts != null) {
            tts.shutdown();
            tts = null;
        }
    }
}
