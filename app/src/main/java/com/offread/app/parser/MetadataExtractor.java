package com.offread.app.parser;

import com.offread.app.data.AppPreferences;
import com.offread.app.data.OffReadDbHelper;
import java.util.Locale;
import java.util.function.Predicate;

/* JADX INFO: loaded from: classes.dex */
public class MetadataExtractor {

    public static class ArticleMetadata {
        public String title = AppPreferences.TTS_LANGUAGE_AUTO;
        public String author = AppPreferences.TTS_LANGUAGE_AUTO;
        public String publishedDate = AppPreferences.TTS_LANGUAGE_AUTO;
        public String leadImageUrl = AppPreferences.TTS_LANGUAGE_AUTO;
        public String siteName = AppPreferences.TTS_LANGUAGE_AUTO;
        public String canonicalUrl = AppPreferences.TTS_LANGUAGE_AUTO;
        public String language = AppPreferences.TTS_LANGUAGE_AUTO;
        public String description = AppPreferences.TTS_LANGUAGE_AUTO;
        public String jsonLdBody = AppPreferences.TTS_LANGUAGE_AUTO;
    }

    public static ArticleMetadata extract(HtmlNode htmlNode, String str) {
        HtmlNode htmlNodeFindFirst;
        HtmlNode htmlNodeFindFirst2;
        HtmlNode htmlNodeFindFirst3;
        HtmlNode htmlNodeFindFirst4;
        HtmlNode htmlNodeFindFirst5;
        ArticleMetadata articleMetadata = new ArticleMetadata();
        if (htmlNode != null) {
            JsonLdExtractor.JsonLdData jsonLdDataExtract = JsonLdExtractor.extract(htmlNode);
            if (jsonLdDataExtract != null) {
                articleMetadata.title = jsonLdDataExtract.headline;
                articleMetadata.author = jsonLdDataExtract.author;
                articleMetadata.publishedDate = jsonLdDataExtract.datePublished;
                articleMetadata.leadImageUrl = jsonLdDataExtract.imageUrl;
                articleMetadata.siteName = jsonLdDataExtract.publisherName;
                articleMetadata.description = jsonLdDataExtract.description;
                articleMetadata.jsonLdBody = jsonLdDataExtract.articleBody;
            }
            for (HtmlNode htmlNode2 : htmlNode.getElementsByTag("meta")) {
                String lowerCase = htmlNode2.getAttribute("property").toLowerCase(Locale.ROOT);
                String lowerCase2 = htmlNode2.getAttribute("name").toLowerCase(Locale.ROOT);
                String attribute = htmlNode2.getAttribute(OffReadDbHelper.COL_CONTENT);
                if (!attribute.isEmpty()) {
                    if (articleMetadata.siteName.isEmpty() && ("og:site_name".equals(lowerCase) || "og:site_name".equals(lowerCase2))) {
                        articleMetadata.siteName = attribute;
                    }
                    if (articleMetadata.title.isEmpty() && ("og:title".equals(lowerCase) || "og:title".equals(lowerCase2) || "twitter:title".equals(lowerCase2) || "twitter:title".equals(lowerCase) || OffReadDbHelper.COL_TITLE.equals(lowerCase2))) {
                        articleMetadata.title = attribute;
                    }
                    if (articleMetadata.author.isEmpty() && (OffReadDbHelper.COL_AUTHOR.equals(lowerCase2) || "article:author".equals(lowerCase) || "article:author".equals(lowerCase2) || "twitter:creator".equals(lowerCase2) || "dc.creator".equals(lowerCase2) || "sailthru.author".equals(lowerCase2) || "byl".equals(lowerCase2))) {
                        articleMetadata.author = attribute;
                    }
                    if (articleMetadata.publishedDate.isEmpty() && ("article:published_time".equals(lowerCase) || "article:published_time".equals(lowerCase2) || "og:published_time".equals(lowerCase) || "dc.date.issued".equals(lowerCase2) || "pubdate".equals(lowerCase2) || "sailthru.date".equals(lowerCase2))) {
                        articleMetadata.publishedDate = attribute;
                    }
                    if (articleMetadata.leadImageUrl.isEmpty() && ("og:image".equals(lowerCase) || "og:image:url".equals(lowerCase) || "og:image:secure_url".equals(lowerCase) || "twitter:image".equals(lowerCase2) || "twitter:image:src".equals(lowerCase2))) {
                        articleMetadata.leadImageUrl = attribute;
                    }
                    if (articleMetadata.canonicalUrl.isEmpty() && ("og:url".equals(lowerCase) || "og:url".equals(lowerCase2))) {
                        articleMetadata.canonicalUrl = attribute;
                    }
                    if (articleMetadata.description.isEmpty() && ("og:description".equals(lowerCase) || "description".equals(lowerCase2) || "twitter:description".equals(lowerCase2))) {
                        articleMetadata.description = attribute;
                    }
                    if (articleMetadata.language.isEmpty() && ("content-language".equals(htmlNode2.getAttribute("http-equiv").toLowerCase(Locale.ROOT)) || "og:locale".equals(lowerCase))) {
                        articleMetadata.language = attribute;
                    }
                }
            }
            for (HtmlNode htmlNode3 : htmlNode.getElementsByTag("link")) {
                String lowerCase3 = htmlNode3.getAttribute("rel").toLowerCase(Locale.ROOT);
                String attribute2 = htmlNode3.getAttribute("href");
                if (!attribute2.isEmpty()) {
                    if (articleMetadata.canonicalUrl.isEmpty() && "canonical".equals(lowerCase3)) {
                        articleMetadata.canonicalUrl = attribute2;
                    }
                    if (articleMetadata.leadImageUrl.isEmpty() && ("image_src".equals(lowerCase3) || "apple-touch-icon".equals(lowerCase3))) {
                        articleMetadata.leadImageUrl = attribute2;
                    }
                }
            }
            if (articleMetadata.title.isEmpty() && (htmlNodeFindFirst5 = htmlNode.findFirst(new Predicate() { // from class: com.offread.app.parser.MetadataExtractor.0
                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return MetadataExtractor.lambda$extract$0((HtmlNode) obj);
                }
            })) != null) {
                articleMetadata.title = htmlNodeFindFirst5.getInnerText();
            }
            if (articleMetadata.title.isEmpty() && (htmlNodeFindFirst4 = htmlNode.findFirst(new Predicate() { // from class: com.offread.app.parser.MetadataExtractor.1
                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return MetadataExtractor.lambda$extract$1((HtmlNode) obj);
                }
            })) != null) {
                articleMetadata.title = htmlNodeFindFirst4.getInnerText();
            }
            if (articleMetadata.title.isEmpty() && str != null) {
                articleMetadata.title = TextAnalysisUtil.extractDomain(str);
            }
            if (articleMetadata.author.isEmpty() && (htmlNodeFindFirst3 = htmlNode.findFirst(new Predicate() { // from class: com.offread.app.parser.MetadataExtractor.2
                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return MetadataExtractor.lambda$extract$2((HtmlNode) obj);
                }
            })) != null) {
                articleMetadata.author = htmlNodeFindFirst3.getInnerText();
            }
            if (articleMetadata.publishedDate.isEmpty() && (htmlNodeFindFirst2 = htmlNode.findFirst(new Predicate() { // from class: com.offread.app.parser.MetadataExtractor.3
                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return MetadataExtractor.lambda$extract$3((HtmlNode) obj);
                }
            })) != null) {
                String attribute3 = htmlNodeFindFirst2.getAttribute("datetime");
                if (attribute3.isEmpty()) {
                    attribute3 = htmlNodeFindFirst2.getInnerText();
                }
                articleMetadata.publishedDate = attribute3;
            }
            if (articleMetadata.language.isEmpty() && (htmlNodeFindFirst = htmlNode.findFirst(new Predicate() { // from class: com.offread.app.parser.MetadataExtractor.4
                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return MetadataExtractor.lambda$extract$4((HtmlNode) obj);
                }
            })) != null) {
                articleMetadata.language = htmlNodeFindFirst.getAttribute("lang");
            }
            if (articleMetadata.siteName.isEmpty() && str != null) {
                articleMetadata.siteName = TextAnalysisUtil.extractDomain(str);
            }
            if (articleMetadata.canonicalUrl.isEmpty()) {
                articleMetadata.canonicalUrl = str != null ? str : AppPreferences.TTS_LANGUAGE_AUTO;
            }
            articleMetadata.title = TextAnalysisUtil.cleanTitle(articleMetadata.title, articleMetadata.siteName);
            articleMetadata.author = TextAnalysisUtil.cleanAuthor(articleMetadata.author);
            if (!articleMetadata.leadImageUrl.isEmpty()) {
                articleMetadata.leadImageUrl = ArticleContentBlocks.resolveImageUrl(articleMetadata.leadImageUrl, str);
            }
            if (!articleMetadata.canonicalUrl.isEmpty() && str != null) {
                articleMetadata.canonicalUrl = TextAnalysisUtil.resolveUrl(str, articleMetadata.canonicalUrl);
            }
        }
        return articleMetadata;
    }

    static /* synthetic */ boolean lambda$extract$0(HtmlNode htmlNode) {
        return OffReadDbHelper.COL_TITLE.equals(htmlNode.getTagName());
    }

    static /* synthetic */ boolean lambda$extract$1(HtmlNode htmlNode) {
        return "h1".equals(htmlNode.getTagName());
    }

    static /* synthetic */ boolean lambda$extract$2(HtmlNode htmlNode) {
        String lowerCase = htmlNode.getAttribute("rel").toLowerCase(Locale.ROOT);
        String lowerCase2 = htmlNode.getAttribute("itemprop").toLowerCase(Locale.ROOT);
        String lowerCase3 = htmlNode.getClassName().toLowerCase(Locale.ROOT);
        if (OffReadDbHelper.COL_AUTHOR.equals(lowerCase) || OffReadDbHelper.COL_AUTHOR.equals(lowerCase2) || lowerCase3.contains("byline") || lowerCase3.contains("author-name")) {
            return true;
        }
        return lowerCase3.contains(OffReadDbHelper.COL_AUTHOR) && !lowerCase3.contains("authors-list");
    }

    static /* synthetic */ boolean lambda$extract$3(HtmlNode htmlNode) {
        return "time".equals(htmlNode.getTagName());
    }

    static /* synthetic */ boolean lambda$extract$4(HtmlNode htmlNode) {
        return "html".equals(htmlNode.getTagName());
    }
}
