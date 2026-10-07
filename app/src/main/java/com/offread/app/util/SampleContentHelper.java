package com.offread.app.util;

import com.offread.app.data.Article;
import com.offread.app.data.ArticleRepository;
import com.offread.app.parser.TextAnalysisUtil;

import java.util.ArrayList;
import java.util.List;

public class SampleContentHelper {

    public static List<Article> getSampleArticles() {
        List<Article> samples = new ArrayList<>();

        // Article 1: Guide to OffRead
        Article guide = new Article();
        guide.setTitle("Welcome to OffRead: Your Distraction-Free Offline Haven");
        guide.setAuthor("OffRead Team");
        guide.setUrl("https://offread.app/welcome");
        guide.setDomain("offread.app");
        guide.setCategory("Guide");
        guide.setTags("Welcome,Guide,Productivity");
        String guideContent =
                "Welcome to OffRead!\n\n" +
                "In an era filled with endless algorithmic feeds, popups, and clickbait, OffRead is designed to return reading to its purest form: focused, calm, and distraction-free.\n\n" +
                "### Core Superpowers of OffRead\n\n" +
                "1. Save From Anywhere:\n" +
                "Whenever you come across an interesting article in Chrome, Firefox, or any app, simply tap 'Share' and choose OffRead. The article will be stripped of ads, cookie banners, and trackers, then saved securely to your device for offline reading.\n\n" +
                "2. Distraction-Free Reader:\n" +
                "Customize your reading environment exactly how you want. Switch between Paper White, Warm Sepia, Slate Dark, and pure AMOLED Black. Tailor font families (Serif, Sans, Monospace), font size, and line spacing to your personal preference.\n\n" +
                "3. RSVP Speed Reader:\n" +
                "Power through articles at 350 to 600 words per minute using the built-in Rapid Serial Visual Presentation reader. By focusing your eye on the red Optimal Recognition Point (ORP), you eliminate eye saccades and dramatically increase reading pace.\n\n" +
                "4. Offline Voice Reader (TTS):\n" +
                "Transform any saved article into an audiobook with native Text-to-Speech. Listen while commuting, exercising, or relaxing with eyes closed.\n\n" +
                "5. 100% Offline & Private:\n" +
                "All your articles, reading stats, and notes are stored strictly on your device in a local SQLite database. No external tracking, no accounts required, no telemetry.\n\n" +
                "Enjoy your reading journey!";
        guide.setContent(guideContent);
        guide.setPlainText(guideContent);
        guide.setWordCount(TextAnalysisUtil.countWords(guideContent));
        guide.setReadingTimeMinutes(TextAnalysisUtil.calculateReadingTimeMinutes(guide.getWordCount()));
        guide.setExcerpt(TextAnalysisUtil.createExcerpt(guideContent, 160));
        guide.setFavorite(true);
        samples.add(guide);

        // Article 2: Seneca - On the Shortness of Life
        Article seneca = new Article();
        seneca.setTitle("On the Shortness of Life (Selected Excerpts)");
        seneca.setAuthor("Lucius Annaeus Seneca");
        seneca.setUrl("https://standardebooks.org/seneca/on-the-shortness-of-life");
        seneca.setDomain("standardebooks.org");
        seneca.setCategory("Philosophy");
        seneca.setTags("Philosophy,Stoicism,Classic");
        String senecaContent =
                "Most mortals, Paulinus, complain of the malice of Nature, because we are born for a brief span of life, and because even this space allotted to us rushes by so speedily and so rapidly that with very few exceptions life closes while we are still preparing to live.\n\n" +
                "### Life is Long If You Know How to Use It\n\n" +
                "It is not that we have a short time to live, but that we waste a lot of it. Life is long enough, and a sufficiently generous estimate has been given to us for the highest achievements, if it were all well invested.\n\n" +
                "But when it is squandered in luxury and heedlessness, when it is spent for no good end, we are driven at last by extreme necessity to realize that it has passed before we realized that it was passing.\n\n" +
                "So it is: the life we receive is not short, but we make it so; nor do we have any lack of it, but are lavish with it.\n\n" +
                "### Guarding Your Attention\n\n" +
                "Men do not suffer their estates to be invaded, and if there is any dispute over their boundaries, they rush to stones and arms; yet they allow others to encroach upon their life—nay, they themselves actually invite in those who will take possession of it.\n\n" +
                "No one will be found who is willing to distribute his money; yet among how many each one distributes his life! Men are thrifty in guarding their private property, but the moment it comes to spending time, they are utter spendthrifts of the one thing wherein it is honorable to be frugal.\n\n" +
                "You are living as if destined to live forever; your own frailty never occurs to you; you do not notice how much time has already passed, but squander it as if you had a full and overflowing supply.";
        seneca.setContent(senecaContent);
        seneca.setPlainText(senecaContent);
        seneca.setWordCount(TextAnalysisUtil.countWords(senecaContent));
        seneca.setReadingTimeMinutes(TextAnalysisUtil.calculateReadingTimeMinutes(seneca.getWordCount()));
        seneca.setExcerpt(TextAnalysisUtil.createExcerpt(senecaContent, 160));
        samples.add(seneca);

        // Article 3: Deep Work vs Distraction
        Article deepWork = new Article();
        deepWork.setTitle("The Lost Art of Deep Reading in the Digital Age");
        deepWork.setAuthor("Elena Rostova");
        deepWork.setUrl("https://readingrenaissance.org/deep-reading");
        deepWork.setDomain("readingrenaissance.org");
        deepWork.setCategory("Science");
        deepWork.setTags("Science,Focus,Mindset");
        String deepWorkContent =
                "When was the last time you sat down with a long piece of writing for forty-five uninterrupted minutes, without glancing at notifications, tab-switching, or skimming ahead?\n\n" +
                "Cognitive neuroscientists have documented that the medium through which we consume information shapes our neurological pathways. The modern web encourages skimming, fast scanning for keywords, and rapid context switching.\n\n" +
                "### Linear Reading vs Hyperlink Surfing\n\n" +
                "When we read linear, long-form prose, our brain engages in deep semantic processing. We build mental models of the author's argument, compare it against our own lived experiences, and form novel syntheses.\n\n" +
                "By contrast, when text is broken up with animated advertisements, related story carousels, and hyperlinks, our executive brain must constantly answer subtle subconscious questions: 'Should I click this link? Is this banner relevant? What does this popup say?'\n\n" +
                "This perpetual micro-decision fatigue degrades comprehension and strips away the contemplative pleasure of reading.\n\n" +
                "### The Offline Antidote\n\n" +
                "Taking content offline is not just about connectivity; it is a ritual of cognitive protection. By disconnecting from the live network, you establish a quiet digital sanctuary where the words on the screen have your complete, undivided presence.";
        deepWork.setContent(deepWorkContent);
        deepWork.setPlainText(deepWorkContent);
        deepWork.setWordCount(TextAnalysisUtil.countWords(deepWorkContent));
        deepWork.setReadingTimeMinutes(TextAnalysisUtil.calculateReadingTimeMinutes(deepWork.getWordCount()));
        deepWork.setExcerpt(TextAnalysisUtil.createExcerpt(deepWorkContent, 160));
        samples.add(deepWork);

        return samples;
    }

    public static void populateSamplesIfEmpty(ArticleRepository repository) {
        List<Article> existing = repository.getAllArticlesForExport();
        if (existing.isEmpty()) {
            for (Article a : getSampleArticles()) {
                repository.insertArticle(a);
            }
        }
    }
}
