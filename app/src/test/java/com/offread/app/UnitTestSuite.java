package com.offread.app;

import com.offread.app.data.Article;
import com.offread.app.data.ReadingStats;
import com.offread.app.parser.ArticleExtractor;
import com.offread.app.parser.HtmlNode;
import com.offread.app.parser.HtmlParser;
import com.offread.app.parser.JsonLdExtractor;
import com.offread.app.parser.MiniJson;
import com.offread.app.parser.ReadabilityResult;
import com.offread.app.parser.TextAnalysisUtil;
import com.offread.app.util.SampleContentHelper;
import com.offread.app.util.UIHelper;

import java.util.List;
import java.util.Map;

public class UnitTestSuite {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Running OffRead Comprehensive Tests  ");
        System.out.println("========================================");

        int passed = 0;
        int failed = 0;

        String[] testNames = {
                "testWordCount",
                "testReadingTime",
                "testExcerptCreation",
                "testOrpCalculation",
                "testSentenceSplitting",
                "testDomainExtraction",
                "testHtmlExtraction",
                "testUserTextProcessing",
                "testArticleModelLifecycle",
                "testReadingStatsFormatting",
                "testSampleContentHelper",
                "testUIThemeResolution",
                "testHtmlParserAndDomTree",
                "testLinkDensityFiltering",
                "testBoilerplateAndAdRemoval",
                "testJsonLdMetadataExtraction",
                "testRelativeUrlResolution",
                "testCleanTitleAndAuthor",
                "testComplexNestedArticleExtraction",
                "testMalformedHtmlGracefulHandling",
                "testMiniJsonParsing",
                "testValidArticleHtmlExtractsTitleAndParagraphs",
                "testNavigationScriptStyleExcluded",
                "testHtmlEntitiesDecoded",
                "testInvalidAndEmptyInputRejected",
                "testShortNonArticleContentHandledSafely",
                "testCanonicalUrlExtraction",
                "testMissingOptionalMetadataDoesNotCrash"
        };

        Runnable[] tests = {
                UnitTestSuite::testWordCount,
                UnitTestSuite::testReadingTime,
                UnitTestSuite::testExcerptCreation,
                UnitTestSuite::testOrpCalculation,
                UnitTestSuite::testSentenceSplitting,
                UnitTestSuite::testDomainExtraction,
                UnitTestSuite::testHtmlExtraction,
                UnitTestSuite::testUserTextProcessing,
                UnitTestSuite::testArticleModelLifecycle,
                UnitTestSuite::testReadingStatsFormatting,
                UnitTestSuite::testSampleContentHelper,
                UnitTestSuite::testUIThemeResolution,
                UnitTestSuite::testHtmlParserAndDomTree,
                UnitTestSuite::testLinkDensityFiltering,
                UnitTestSuite::testBoilerplateAndAdRemoval,
                UnitTestSuite::testJsonLdMetadataExtraction,
                UnitTestSuite::testRelativeUrlResolution,
                UnitTestSuite::testCleanTitleAndAuthor,
                UnitTestSuite::testComplexNestedArticleExtraction,
                UnitTestSuite::testMalformedHtmlGracefulHandling,
                UnitTestSuite::testMiniJsonParsing,
                UnitTestSuite::testValidArticleHtmlExtractsTitleAndParagraphs,
                UnitTestSuite::testNavigationScriptStyleExcluded,
                UnitTestSuite::testHtmlEntitiesDecoded,
                UnitTestSuite::testInvalidAndEmptyInputRejected,
                UnitTestSuite::testShortNonArticleContentHandledSafely,
                UnitTestSuite::testCanonicalUrlExtraction,
                UnitTestSuite::testMissingOptionalMetadataDoesNotCrash
        };

        for (int i = 0; i < tests.length; i++) {
            try {
                tests[i].run();
                passed++;
                System.out.println("✓ " + testNames[i] + " passed");
            } catch (Throwable t) {
                failed++;
                System.err.println("✗ " + testNames[i] + " failed: " + t.getMessage());
                t.printStackTrace(System.err);
            }
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
        assertEquals("Formatted 25m", "25m", s1.getFormattedReadingTime());

        ReadingStats s2 = new ReadingStats(10, 8, 12000, 7500, 5);
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

    private static void testHtmlParserAndDomTree() {
        String html = "<div id=\"main\" class=\"container\"><p>Paragraph 1 with <a href=\"/link\">anchor</a>.</p><img src=\"pic.png\" alt=\"Photo\"><br><p>Paragraph 2.</p></div>";
        HtmlNode root = HtmlParser.parse(html);
        assertTrue("Root not null", root != null);

        HtmlNode div = root.findFirst(n -> "div".equals(n.getTagName()));
        assertTrue("Found div", div != null);
        assertEquals("Div ID", "main", div.getId());
        assertEquals("Div Class", "container", div.getClassName());

        List<HtmlNode> ps = div.getElementsByTag("p");
        assertEquals("Found 2 paragraphs", 2, ps.size());

        HtmlNode img = div.findFirst(n -> "img".equals(n.getTagName()));
        assertTrue("Found self-closing img", img != null);
        assertEquals("Img src", "pic.png", img.getAttribute("src"));
        assertEquals("Img alt", "Photo", img.getAttribute("alt"));

        assertTrue("Text contains Paragraph 1", div.getInnerText().contains("Paragraph 1"));
        assertTrue("Text contains Paragraph 2", div.getInnerText().contains("Paragraph 2"));
    }

    private static void testLinkDensityFiltering() {
        String navHtml = "<div class=\"nav-menu\"><a href=\"/1\">One</a> <a href=\"/2\">Two</a> <a href=\"/3\">Three</a> <a href=\"/4\">Four</a></div>";
        HtmlNode nav = HtmlParser.parse(navHtml).findFirst(n -> "div".equals(n.getTagName()));
        assertTrue("Nav link density high", nav.calculateLinkDensity() > 0.6);

        String articleHtml = "<div><p>This is a long and insightful paragraph discussing the virtues of offline focus and sovereignty over attention. Here is one <a href=\"/ref\">reference</a> but the rest is pure thought and prose.</p></div>";
        HtmlNode prose = HtmlParser.parse(articleHtml).findFirst(n -> "div".equals(n.getTagName()));
        assertTrue("Article link density low", prose.calculateLinkDensity() < 0.2);
    }

    private static void testBoilerplateAndAdRemoval() {
        String html = "<html><body>" +
                "<header><div class=\"ad-banner\">Buy our stuff!</div></header>" +
                "<div class=\"sidebar-widget\"><p>Trending gossip and clickbait headlines.</p></div>" +
                "<div class=\"article-body\">" +
                "<p>Deep work is the ability to focus without distraction on a cognitively demanding task.</p>" +
                "<p>It allows you to quickly master complicated information and produce better results in less time.</p>" +
                "</div>" +
                "<div class=\"disqus-comments\"><p>Comments from internet users.</p></div>" +
                "</body></html>";

        ReadabilityResult res = ArticleExtractor.extractFromHtml(html, "https://example.com/deepwork");
        assertTrue("Content has deep work", res.getContent().contains("Deep work is the ability to focus"));
        assertTrue("Content excludes ads", !res.getContent().contains("Buy our stuff"));
        assertTrue("Content excludes sidebar", !res.getContent().contains("Trending gossip"));
        assertTrue("Content excludes comments", !res.getContent().contains("Comments from internet users"));
    }

    private static void testJsonLdMetadataExtraction() {
        String html = "<html><head>" +
                "<script type=\"application/ld+json\">" +
                "{" +
                "  \"@context\": \"https://schema.org\"," +
                "  \"@type\": \"NewsArticle\"," +
                "  \"headline\": \"The Architecture of Silence\"," +
                "  \"author\": {\"@type\": \"Person\", \"name\": \"Elena Rostova\"}," +
                "  \"datePublished\": \"2026-09-18T10:00:00Z\"," +
                "  \"image\": \"https://example.com/hero.jpg\"," +
                "  \"description\": \"An inquiry into solitude and clarity in the modern age.\"" +
                "}" +
                "</script></head><body>" +
                "<article><p>Silence is not the absence of sound, but the presence of stillness.</p></article>" +
                "</body></html>";

        HtmlNode root = HtmlParser.parse(html);
        JsonLdExtractor.JsonLdData data = JsonLdExtractor.extract(root);
        assertEquals("JSON-LD Headline", "The Architecture of Silence", data.headline);
        assertEquals("JSON-LD Author", "Elena Rostova", data.author);
        assertEquals("JSON-LD Date", "2026-09-18T10:00:00Z", data.datePublished);
        assertEquals("JSON-LD Image", "https://example.com/hero.jpg", data.imageUrl);
        assertEquals("JSON-LD Description", "An inquiry into solitude and clarity in the modern age.", data.description);

        ReadabilityResult res = ArticleExtractor.extractFromHtml(html, "https://example.com/silence");
        assertEquals("Extracted title", "The Architecture of Silence", res.getTitle());
        assertEquals("Extracted author", "Elena Rostova", res.getAuthor());
        assertEquals("Extracted published date", "2026-09-18T10:00:00Z", res.getPublishedDate());
        assertEquals("Extracted lead image", "https://example.com/hero.jpg", res.getLeadImageUrl());
        assertEquals("Extracted excerpt", "An inquiry into solitude and clarity in the modern age.", res.getExcerpt());
    }

    private static void testRelativeUrlResolution() {
        String base = "https://journal.org/essays/focus.html";
        assertEquals("Root relative URL", "https://journal.org/images/cover.jpg",
                TextAnalysisUtil.resolveUrl(base, "/images/cover.jpg"));
        assertEquals("Document relative URL", "https://journal.org/essays/diagram.png",
                TextAnalysisUtil.resolveUrl(base, "diagram.png"));
        assertEquals("Parent relative URL", "https://journal.org/assets/style.css",
                TextAnalysisUtil.resolveUrl(base, "../assets/style.css"));
        assertEquals("Protocol relative URL", "https://cdn.journal.org/lib.js",
                TextAnalysisUtil.resolveUrl(base, "//cdn.journal.org/lib.js"));
        assertEquals("Already absolute URL", "https://other.com/photo.jpg",
                TextAnalysisUtil.resolveUrl(base, "https://other.com/photo.jpg"));
    }

    private static void testCleanTitleAndAuthor() {
        assertEquals("Clean title with pipe", "Deep Work Principles",
                TextAnalysisUtil.cleanTitle("Deep Work Principles | Cal Newport Blog", "Cal Newport Blog"));
        assertEquals("Clean title with dash", "Meditation and Presence",
                TextAnalysisUtil.cleanTitle("Meditation and Presence - The Zen Journal", "The Zen Journal"));
        assertEquals("Clean title with em-dash", "Writing Well",
                TextAnalysisUtil.cleanTitle("Writing Well — William Zinsser", "William Zinsser"));

        assertEquals("Clean author 'By ' prefix", "George Orwell",
                TextAnalysisUtil.cleanAuthor("By George Orwell"));
        assertEquals("Clean author 'Written by ' prefix", "Hannah Arendt",
                TextAnalysisUtil.cleanAuthor("Written by Hannah Arendt"));
        assertEquals("Clean author with entities", "Jean-Paul Sartre",
                TextAnalysisUtil.cleanAuthor("Author: Jean-Paul Sartre"));
    }

    private static void testComplexNestedArticleExtraction() {
        String complexHtml = "<!DOCTYPE html><html><head>" +
                "<meta property=\"og:title\" content=\"Mastering Cognitive Endurance\">" +
                "<meta property=\"og:site_name\" content=\"Cognition Digest\">" +
                "<meta name=\"author\" content=\"Dr. Sarah Lin\">" +
                "<meta property=\"og:image\" content=\"/media/lead.jpg\">" +
                "</head><body>" +
                "<div class=\"top-nav\"><a href=\"/\">Home</a><a href=\"/topics\">Topics</a></div>" +
                "<div class=\"social-sharing-buttons\"><button>Tweet</button><button>Share</button></div>" +
                "<main>" +
                "<div class=\"post-content\">" +
                "<h1>Mastering Cognitive Endurance</h1>" +
                "<p>Cognitive endurance is the capacity to sustain prolonged intellectual effort without mental fatigue.</p>" +
                "<h2>The Biological Substrate</h2>" +
                "<p>When neurons fire in concert, metabolic substrates are consumed at an elevated pace.</p>" +
                "<blockquote><p>Rest is not idleness, but the necessary condition for intellectual synthesis.</p></blockquote>" +
                "<h3>Core Protocols</h3>" +
                "<ul>" +
                "<li>Unbroken 90-minute ultradian cycles.</li>" +
                "<li>Total sensory isolation during recovery periods.</li>" +
                "</ul>" +
                "<pre><code>// Sleep protocol cycle\nsleep(8 * 3600);</code></pre>" +
                "<img src=\"/media/graph.png\" alt=\"Endurance Graph\">" +
                "</div>" +
                "</main>" +
                "<div class=\"sidebar\"><div class=\"ad-box\">Sponsored: Smart vitamins</div></div>" +
                "<footer><p>&copy; 2026 Cognition Digest</p></footer>" +
                "</body></html>";

        ReadabilityResult res = ArticleExtractor.extractFromHtml(complexHtml, "https://cognition.org/articles/endurance");
        assertEquals("Title", "Mastering Cognitive Endurance", res.getTitle());
        assertEquals("Author", "Dr. Sarah Lin", res.getAuthor());
        assertEquals("Site Name", "Cognition Digest", res.getSiteName());
        assertEquals("Lead Image", "https://cognition.org/media/lead.jpg", res.getLeadImageUrl());

        String md = res.getContent();
        assertTrue("Has h2 heading", md.contains("## The Biological Substrate"));
        assertTrue("Has blockquote", md.contains("> Rest is not idleness"));
        assertTrue("Has list item", md.contains("• Unbroken 90-minute ultradian cycles."));
        assertTrue("Has code block", md.contains("sleep(8 * 3600);"));
        assertTrue("Has resolved image", md.contains("![Endurance Graph](https://cognition.org/media/graph.png)"));

        // Noise excluded
        assertTrue("Excludes top nav", !md.contains("Home") && !md.contains("Topics"));
        assertTrue("Excludes social buttons", !md.contains("Tweet") && !md.contains("Share"));
        assertTrue("Excludes sponsored ad", !md.contains("Sponsored: Smart vitamins"));
        assertTrue("Excludes footer copyright", !md.contains("Cognition Digest"));

        // Plain text contains sentences without markdown markers
        String pt = res.getPlainText();
        assertTrue("Plain text has paragraph", pt.contains("Cognitive endurance is the capacity"));
        assertTrue("Plain text has no markdown ##", !pt.contains("##"));
        assertTrue("Plain text has no markdown >", !pt.contains(">"));
    }

    private static void testMalformedHtmlGracefulHandling() {
        String malformed = "<div class=\"content\"><p>Unclosed paragraph 1<p>Unclosed paragraph 2<div><span>Nested text without end tags";
        ReadabilityResult res = ArticleExtractor.extractFromHtml(malformed, "https://broken.com");
        assertTrue("Extracted text from malformed HTML", res.getContent().contains("Unclosed paragraph"));

        ReadabilityResult emptyRes = ArticleExtractor.extractFromHtml("", "https://empty.com");
        assertEquals("Empty HTML title", "Untitled", emptyRes.getTitle());
        assertEquals("Empty HTML word count", 0, emptyRes.getWordCount());
    }

    @SuppressWarnings("unchecked")
    private static void testMiniJsonParsing() {
        String json = "{\"name\": \"OffRead\", \"version\": 1.0, \"active\": true, \"tags\": [\"privacy\", \"offline\"], \"nested\": {\"key\": \"value\\nwith\\tescape\"}}";
        Map<String, Object> map = MiniJson.parseObject(json);
        assertTrue("Map not null", map != null);
        assertEquals("Name field", "OffRead", map.get("name"));
        assertEquals("Active field", Boolean.TRUE, map.get("active"));

        List<Object> tags = (List<Object>) map.get("tags");
        assertEquals("Tags size", 2, tags.size());
        assertEquals("Tag 0", "privacy", tags.get(0));

        Map<String, Object> nested = (Map<String, Object>) map.get("nested");
        assertEquals("Nested key", "value\nwith\tescape", nested.get("key"));
    }

    private static void testValidArticleHtmlExtractsTitleAndParagraphs() {
        String html = "<!DOCTYPE html><html><head><title>Deep Focus Guide</title></head><body>" +
                "<article>" +
                "<h1>Deep Focus Guide</h1>" +
                "<p>Deep focus enables substantial cognitive breakthroughs.</p>" +
                "<p>By dedicating uninterrupted time, one cultivates mastery.</p>" +
                "</article>" +
                "</body></html>";
        ReadabilityResult res = ArticleExtractor.extractFromHtml(html, "https://sample.org/focus");
        assertEquals("Title", "Deep Focus Guide", res.getTitle());
        assertTrue("Paragraph 1 extracted", res.getContent().contains("Deep focus enables substantial cognitive breakthroughs."));
        assertTrue("Paragraph 2 extracted", res.getContent().contains("By dedicating uninterrupted time, one cultivates mastery."));
        assertTrue("Word count > 10", res.getWordCount() > 10);
    }

    private static void testNavigationScriptStyleExcluded() {
        String html = "<html><head><style>.ad{color:red;}</style><script>console.log('secret');</script></head>" +
                "<body>" +
                "<nav><a href=\"/\">Home</a><a href=\"/blog\">Blog</a></nav>" +
                "<header><div class=\"banner\">Header Banner</div></header>" +
                "<article><p>This is the legitimate article content that should be retained.</p></article>" +
                "<footer><p>Copyright 2026</p></footer>" +
                "</body></html>";
        ReadabilityResult res = ArticleExtractor.extractFromHtml(html, "https://sample.org/clean");
        assertTrue("Legitimate content present", res.getContent().contains("This is the legitimate article content"));
        assertTrue("Style excluded", !res.getContent().contains(".ad{color:red;}"));
        assertTrue("Script excluded", !res.getContent().contains("console.log"));
        assertTrue("Nav links excluded", !res.getContent().contains("Home") && !res.getContent().contains("Blog"));
        assertTrue("Footer excluded", !res.getContent().contains("Copyright 2026"));
    }

    private static void testHtmlEntitiesDecoded() {
        String html = "<article><p>OffRead&apos;s &ldquo;offline&rdquo; mode &amp; quiet sanctuary &mdash; pure &#39;focus&#39;.</p></article>";
        ReadabilityResult res = ArticleExtractor.extractFromHtml(html, "https://sample.org/entities");
        String content = res.getContent();
        assertTrue("Decoded &apos; or &#39;", content.contains("OffRead's") || content.contains("OffRead’s"));
        assertTrue("Decoded &amp;", content.contains("&") && !content.contains("&amp;"));
        assertTrue("Decoded &mdash;", content.contains("—") && !content.contains("&mdash;"));
        assertTrue("Decoded quotes", content.contains("\"offline\"") || content.contains("“offline”"));
    }

    private static void testInvalidAndEmptyInputRejected() {
        ReadabilityResult nullRes = ArticleExtractor.extractFromHtml(null, "https://sample.org");
        assertEquals("Null HTML handled safely", "Untitled", nullRes.getTitle());
        assertEquals("Null HTML word count 0", 0, nullRes.getWordCount());

        ReadabilityResult emptyRes = ArticleExtractor.extractFromHtml("   \n\t  ", "https://sample.org");
        assertEquals("Empty HTML handled safely", "Untitled", emptyRes.getTitle());
        assertEquals("Empty HTML word count 0", 0, emptyRes.getWordCount());
    }

    private static void testShortNonArticleContentHandledSafely() {
        String html = "<html><body><div>404 Not Found</div></body></html>";
        ReadabilityResult res = ArticleExtractor.extractFromHtml(html, "https://sample.org/404");
        assertTrue("Handled without crash", res != null);
        assertTrue("Short content detected", res.getWordCount() <= 5);
    }

    private static void testCanonicalUrlExtraction() {
        String html = "<html><head>" +
                "<link rel=\"canonical\" href=\"https://canonical.example.com/true-article.html\" />" +
                "</head><body><article><p>Some long form prose describing an interesting idea in depth.</p></article></body></html>";
        ReadabilityResult res = ArticleExtractor.extractFromHtml(html, "https://redirected.example.com/item?id=123");
        assertEquals("Canonical URL extracted", "https://canonical.example.com/true-article.html", res.getCanonicalUrl());
    }

    private static void testMissingOptionalMetadataDoesNotCrash() {
        String html = "<html><body><p>An article with absolutely no title tag, no metadata, no authors, and no figures whatsoever, but pure readable text.</p></body></html>";
        ReadabilityResult res = ArticleExtractor.extractFromHtml(html, "https://minimalist.com/essay");
        assertTrue("Result not null", res != null);
        assertEquals("Author is empty", "", res.getAuthor());
        assertEquals("Lead image is empty", "", res.getLeadImageUrl());
        assertEquals("Published date is empty", "", res.getPublishedDate());
        assertEquals("Fallback title to domain", "minimalist.com", res.getTitle());
        assertTrue("Content extracted", res.getContent().contains("An article with absolutely no title"));
    }
}
