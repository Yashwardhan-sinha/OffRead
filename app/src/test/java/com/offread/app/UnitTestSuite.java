package com.offread.app;

import com.offread.app.data.Article;
import com.offread.app.data.ReadingStats;
import com.offread.app.parser.ArticleExtractor;
import com.offread.app.parser.ReadabilityResult;
import com.offread.app.parser.TextAnalysisUtil;
import com.offread.app.util.SampleContentHelper;
import com.offread.app.util.UIHelper;

import java.util.List;

public class UnitTestSuite {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Running OffRead Comprehensive Tests  ");
        System.out.println("========================================");

        int passed = 0;
        int failed = 0;

        try {
            testWordCount();
            passed++;
            System.out.println("✓ testWordCount passed");
        } catch (Throwable t) {
            failed++;
            System.err.println("✗ testWordCount failed: " + t.getMessage());
        }

        try {
            testReadingTime();
            passed++;
            System.out.println("✓ testReadingTime passed");
        } catch (Throwable t) {
            failed++;
            System.err.println("✗ testReadingTime failed: " + t.getMessage());
        }

        try {
            testExcerptCreation();
            passed++;
            System.out.println("✓ testExcerptCreation passed");
        } catch (Throwable t) {
            failed++;
            System.err.println("✗ testExcerptCreation failed: " + t.getMessage());
        }

        try {
            testOrpCalculation();
            passed++;
            System.out.println("✓ testOrpCalculation passed");
        } catch (Throwable t) {
            failed++;
            System.err.println("✗ testOrpCalculation failed: " + t.getMessage());
        }

        try {
            testSentenceSplitting();
            passed++;
            System.out.println("✓ testSentenceSplitting passed");
        } catch (Throwable t) {
            failed++;
            System.err.println("✗ testSentenceSplitting failed: " + t.getMessage());
        }

        try {
            testDomainExtraction();
            passed++;
            System.out.println("✓ testDomainExtraction passed");
        } catch (Throwable t) {
            failed++;
            System.err.println("✗ testDomainExtraction failed: " + t.getMessage());
        }

        try {
            testHtmlExtraction();
            passed++;
            System.out.println("✓ testHtmlExtraction passed");
        } catch (Throwable t) {
            failed++;
            System.err.println("✗ testHtmlExtraction failed: " + t.getMessage());
        }

        try {
            testUserTextProcessing();
            passed++;
            System.out.println("✓ testUserTextProcessing passed");
        } catch (Throwable t) {
            failed++;
            System.err.println("✗ testUserTextProcessing failed: " + t.getMessage());
        }

        try {
            testArticleModelLifecycle();
            passed++;
            System.out.println("✓ testArticleModelLifecycle passed");
        } catch (Throwable t) {
            failed++;
            System.err.println("✗ testArticleModelLifecycle failed: " + t.getMessage());
        }

        try {
            testReadingStatsFormatting();
            passed++;
            System.out.println("✓ testReadingStatsFormatting passed");
        } catch (Throwable t) {
            failed++;
            System.err.println("✗ testReadingStatsFormatting failed: " + t.getMessage());
        }

        try {
            testSampleContentHelper();
            passed++;
            System.out.println("✓ testSampleContentHelper passed");
        } catch (Throwable t) {
            failed++;
            System.err.println("✗ testSampleContentHelper failed: " + t.getMessage());
        }

        try {
            testUIThemeResolution();
            passed++;
            System.out.println("✓ testUIThemeResolution passed");
        } catch (Throwable t) {
            failed++;
            System.err.println("✗ testUIThemeResolution failed: " + t.getMessage());
        }

        System.out.println("========================================");
        System.out.println("Summary: " + passed + " passed, " + failed + " failed");
        System.out.println("========================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String msg, boolean condition) {
        if (!condition) {
            throw new AssertionError("Assertion failed: " + msg);
        }
    }

    private static void assertEquals(String msg, Object expected, Object actual) {
        if (expected == null && actual == null) return;
        if (expected != null && !expected.equals(actual)) {
            throw new AssertionError(msg + " -> Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void testWordCount() {
        assertEquals("Empty string word count", 0, TextAnalysisUtil.countWords(""));
        assertEquals("Whitespace word count", 0, TextAnalysisUtil.countWords("   \n\t  "));
        assertEquals("Three words", 3, TextAnalysisUtil.countWords("One two three"));
        assertEquals("Padded words", 4, TextAnalysisUtil.countWords("  The   quick brown fox   "));
    }

    private static void testReadingTime() {
        assertEquals("0 words reading time", 1, TextAnalysisUtil.calculateReadingTimeMinutes(0));
        assertEquals("150 words reading time", 1, TextAnalysisUtil.calculateReadingTimeMinutes(150));
        assertEquals("200 words reading time", 1, TextAnalysisUtil.calculateReadingTimeMinutes(200));
        assertEquals("201 words reading time", 2, TextAnalysisUtil.calculateReadingTimeMinutes(201));
        assertEquals("600 words reading time", 3, TextAnalysisUtil.calculateReadingTimeMinutes(600));
    }

    private static void testExcerptCreation() {
        String longText = "Focus and deep attention are among the rarest commodities in modern society. When you disconnect, you reclaim sovereignty over your mind.";
        String excerpt = TextAnalysisUtil.createExcerpt(longText, 40);
        assertTrue("Excerpt not null", excerpt != null);
        assertTrue("Excerpt truncated with ellipsis", excerpt.endsWith("…"));
        assertTrue("Excerpt within bounds", excerpt.length() <= 45);
    }

    private static void testOrpCalculation() {
        assertEquals("Short word 'a' ORP", 0, TextAnalysisUtil.calculateOrpIndex("a"));
        assertEquals("Word 'read' ORP", 1, TextAnalysisUtil.calculateOrpIndex("read"));
        assertEquals("Word 'attention' ORP", 2, TextAnalysisUtil.calculateOrpIndex("attention"));
        assertEquals("Word 'understanding' ORP", 3, TextAnalysisUtil.calculateOrpIndex("understanding"));
    }

    private static void testSentenceSplitting() {
        String text = "First sentence! Second sentence? Third sentence. Fourth sentence.";
        List<String> sentences = TextAnalysisUtil.splitSentences(text);
        assertEquals("Sentence count", 4, sentences.size());
        assertTrue("First sentence content", sentences.get(0).startsWith("First"));
    }

    private static void testDomainExtraction() {
        assertEquals("Standard URL domain", "standardebooks.org",
                TextAnalysisUtil.extractDomain("https://standardebooks.org/ebooks/seneca"));
        assertEquals("WWW URL domain", "paulgraham.com",
                TextAnalysisUtil.extractDomain("https://www.paulgraham.com/greatwork.html"));
        assertEquals("Empty URL domain", "Note",
                TextAnalysisUtil.extractDomain(""));
    }

    private static void testHtmlExtraction() {
        String html = "<!DOCTYPE html><html><head><title>The Art of Focus - OffRead Blog</title>" +
                "<meta property=\"og:title\" content=\"The Art of Focus\">" +
                "<meta name=\"author\" content=\"Marcus Aurelius\">" +
                "<script>alert('track');</script>" +
                "<style>body { color: red; }</style>" +
                "</head><body>" +
                "<nav><a href=\"/\">Home</a></nav>" +
                "<article>" +
                "<h1>The Art of Focus</h1>" +
                "<p>True focus is the elimination of unnecessary clutter and distraction.</p>" +
                "<p>When reading offline, the mind rests in quiet contemplation.</p>" +
                "</article>" +
                "<footer>Copyright 2026</footer>" +
                "</body></html>";

        ReadabilityResult result = ArticleExtractor.extractFromHtml(html, "https://offread.app/blog/focus");
        assertEquals("Extracted title", "The Art of Focus", result.getTitle());
        assertEquals("Extracted author", "Marcus Aurelius", result.getAuthor());
        assertTrue("Content contains paragraph", result.getContent().contains("True focus"));
        assertTrue("Content excludes script", !result.getContent().contains("alert"));
        assertTrue("Content excludes style", !result.getContent().contains("body { color: red; }"));
        assertTrue("Word count greater than 10", result.getWordCount() > 10);
    }

    private static void testUserTextProcessing() {
        String raw = "Header Line\n\nFirst paragraph here with some text.\n\nSecond paragraph here with more text.";
        ReadabilityResult result = ArticleExtractor.processUserText("My Notes", "Self", raw);
        assertEquals("Title", "My Notes", result.getTitle());
        assertEquals("Author", "Self", result.getAuthor());
        assertEquals("Word count", 14, result.getWordCount());
    }

    private static void testArticleModelLifecycle() {
        Article a = new Article();
        a.setId(101);
        a.setTitle("Mindfulness and Reading");
        a.setAuthor("Seneca");
        a.setUrl("https://example.com/essay");
        a.setDomain("example.com");
        a.setContent("Deep long form content");
        a.setPlainText("Deep long form content");
        a.setWordCount(4);
        a.setReadingTimeMinutes(1);
        a.setProgressPercent(45);
        a.setFavorite(true);
        a.setArchived(false);
        a.setCategory("Philosophy");
        a.setTags("Stoicism,Focus");

        assertEquals("Article ID", 101L, a.getId());
        assertEquals("Article Title", "Mindfulness and Reading", a.getTitle());
        assertEquals("Article Author", "Seneca", a.getAuthor());
        assertEquals("Article Progress", 45, a.getProgressPercent());
        assertTrue("Article is favorite", a.isFavorite());
        assertTrue("Article not archived", !a.isArchived());
    }

    private static void testReadingStatsFormatting() {
        ReadingStats s1 = new ReadingStats(5, 3, 2500, 1500, 2);
        // 1500 seconds = 25 minutes
        assertEquals("Formatted 25m", "25m", s1.getFormattedReadingTime());

        ReadingStats s2 = new ReadingStats(10, 8, 12000, 7500, 5);
        // 7500 seconds = 125 minutes = 2h 5m
        assertEquals("Formatted 2h 5m", "2h 5m", s2.getFormattedReadingTime());

        assertEquals("Streak days", 5, s2.getStreakDays());
        assertEquals("Total words", 12000, s2.getTotalWordsRead());
    }

    private static void testSampleContentHelper() {
        List<Article> samples = SampleContentHelper.getSampleArticles();
        assertTrue("Samples not empty", !samples.isEmpty());
        assertTrue("At least 3 starter essays", samples.size() >= 3);

        for (Article a : samples) {
            assertTrue("Sample has title", a.getTitle() != null && !a.getTitle().isEmpty());
            assertTrue("Sample has non-empty content", a.getContent() != null && a.getContent().length() > 50);
            assertTrue("Sample has valid word count", a.getWordCount() > 20);
            assertTrue("Sample has read time", a.getReadingTimeMinutes() >= 1);
        }
    }

    private static void testUIThemeResolution() {
        UIHelper.ReaderThemeColors dark = UIHelper.getThemeColors("dark");
        assertTrue("Dark theme is not light", !dark.isLight);

        UIHelper.ReaderThemeColors sepia = UIHelper.getThemeColors("sepia");
        assertTrue("Sepia theme is light", sepia.isLight);

        UIHelper.ReaderThemeColors light = UIHelper.getThemeColors("light");
        assertTrue("Light theme is light", light.isLight);

        UIHelper.ReaderThemeColors black = UIHelper.getThemeColors("black");
        assertTrue("Black theme is not light", !black.isLight);
    }
}
