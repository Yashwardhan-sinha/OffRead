package com.offread.app.parser;

import java.util.List;
import java.util.Locale;

public class MetadataExtractor {

    public static class ArticleMetadata {
        public String title = "";
        public String author = "";
        public String publishedDate = "";
        public String leadImageUrl = "";
        public String siteName = "";
        public String canonicalUrl = "";
        public String language = "";
        public String description = "";
        public String jsonLdBody = "";
    }

    public static ArticleMetadata extract(HtmlNode root, String sourceUrl) {
        ArticleMetadata meta = new ArticleMetadata();
        if (root == null) return meta;

        // 1. JSON-LD Extraction
        JsonLdExtractor.JsonLdData jsonLd = JsonLdExtractor.extract(root);
        if (jsonLd != null) {
            meta.title = jsonLd.headline;
            meta.author = jsonLd.author;
            meta.publishedDate = jsonLd.datePublished;
            meta.leadImageUrl = jsonLd.imageUrl;
            meta.siteName = jsonLd.publisherName;
            meta.description = jsonLd.description;
            meta.jsonLdBody = jsonLd.articleBody;
        }

        // 2. Scan meta and link tags
        List<HtmlNode> metaTags = root.getElementsByTag("meta");
        for (HtmlNode m : metaTags) {
            String property = m.getAttribute("property").toLowerCase(Locale.ROOT);
            String name = m.getAttribute("name").toLowerCase(Locale.ROOT);
            String content = m.getAttribute("content");
            if (content.isEmpty()) continue;

            // Site name
            if (meta.siteName.isEmpty() && ("og:site_name".equals(property) || "og:site_name".equals(name))) {
                meta.siteName = content;
            }

            // Title
            if (meta.title.isEmpty()) {
                if ("og:title".equals(property) || "og:title".equals(name) ||
                        "twitter:title".equals(name) || "twitter:title".equals(property) ||
                        "title".equals(name)) {
                    meta.title = content;
                }
            }

            // Author
            if (meta.author.isEmpty()) {
                if ("author".equals(name) || "article:author".equals(property) ||
                        "article:author".equals(name) || "twitter:creator".equals(name) ||
                        "dc.creator".equals(name) || "sailthru.author".equals(name) ||
                        "byl".equals(name)) {
                    meta.author = content;
                }
            }

            // Date
            if (meta.publishedDate.isEmpty()) {
                if ("article:published_time".equals(property) || "article:published_time".equals(name) ||
                        "og:published_time".equals(property) || "dc.date.issued".equals(name) ||
                        "pubdate".equals(name) || "sailthru.date".equals(name)) {
                    meta.publishedDate = content;
                }
            }

            // Lead image
            if (meta.leadImageUrl.isEmpty()) {
                if ("og:image".equals(property) || "og:image:url".equals(property) ||
                        "og:image:secure_url".equals(property) || "twitter:image".equals(name) ||
                        "twitter:image:src".equals(name)) {
                    meta.leadImageUrl = content;
                }
            }

            // Canonical URL
            if (meta.canonicalUrl.isEmpty()) {
                if ("og:url".equals(property) || "og:url".equals(name)) {
                    meta.canonicalUrl = content;
                }
            }

            // Description
            if (meta.description.isEmpty()) {
                if ("og:description".equals(property) || "description".equals(name) ||
                        "twitter:description".equals(name)) {
                    meta.description = content;
                }
            }

            // Language
            if (meta.language.isEmpty()) {
                String httpEquiv = m.getAttribute("http-equiv").toLowerCase(Locale.ROOT);
                if ("content-language".equals(httpEquiv) || "og:locale".equals(property)) {
                    meta.language = content;
                }
            }
        }

        // 3. Scan <link> tags (canonical & image_src)
        List<HtmlNode> linkTags = root.getElementsByTag("link");
        for (HtmlNode link : linkTags) {
            String rel = link.getAttribute("rel").toLowerCase(Locale.ROOT);
            String href = link.getAttribute("href");
            if (href.isEmpty()) continue;

            if (meta.canonicalUrl.isEmpty() && "canonical".equals(rel)) {
                meta.canonicalUrl = href;
            }
            if (meta.leadImageUrl.isEmpty() && ("image_src".equals(rel) || "apple-touch-icon".equals(rel))) {
                meta.leadImageUrl = href;
            }
        }

        // 4. Fallback Title (<title>, <h1>)
        if (meta.title.isEmpty()) {
            HtmlNode titleElem = root.findFirst(n -> "title".equals(n.getTagName()));
            if (titleElem != null) {
                meta.title = titleElem.getInnerText();
            }
        }
        if (meta.title.isEmpty()) {
            HtmlNode h1Elem = root.findFirst(n -> "h1".equals(n.getTagName()));
            if (h1Elem != null) {
                meta.title = h1Elem.getInnerText();
            }
        }
        if (meta.title.isEmpty() && sourceUrl != null) {
            meta.title = TextAnalysisUtil.extractDomain(sourceUrl);
        }

        // 5. Fallback Author (rel="author", class="author" / "byline")
        if (meta.author.isEmpty()) {
            HtmlNode authorElem = root.findFirst(n -> {
                String rel = n.getAttribute("rel").toLowerCase(Locale.ROOT);
                String itemprop = n.getAttribute("itemprop").toLowerCase(Locale.ROOT);
                String cls = n.getClassName().toLowerCase(Locale.ROOT);
                return "author".equals(rel) || "author".equals(itemprop) ||
                        cls.contains("byline") || cls.contains("author-name") ||
                        (cls.contains("author") && !cls.contains("authors-list"));
            });
            if (authorElem != null) {
                meta.author = authorElem.getInnerText();
            }
        }

        // 6. Fallback Date (<time datetime="...">)
        if (meta.publishedDate.isEmpty()) {
            HtmlNode timeElem = root.findFirst(n -> "time".equals(n.getTagName()));
            if (timeElem != null) {
                String dt = timeElem.getAttribute("datetime");
                meta.publishedDate = !dt.isEmpty() ? dt : timeElem.getInnerText();
            }
        }

        // 7. Fallback Language (<html lang="...">)
        if (meta.language.isEmpty()) {
            HtmlNode htmlElem = root.findFirst(n -> "html".equals(n.getTagName()));
            if (htmlElem != null) {
                meta.language = htmlElem.getAttribute("lang");
            }
        }

        // 8. Fallback Site Name & Canonical URL
        if (meta.siteName.isEmpty() && sourceUrl != null) {
            meta.siteName = TextAnalysisUtil.extractDomain(sourceUrl);
        }
        if (meta.canonicalUrl.isEmpty()) {
            meta.canonicalUrl = sourceUrl != null ? sourceUrl : "";
        }

        // 9. Clean Title & Author
        meta.title = TextAnalysisUtil.cleanTitle(meta.title, meta.siteName);
        meta.author = TextAnalysisUtil.cleanAuthor(meta.author);

        // 10. Resolve URLs
        if (!meta.leadImageUrl.isEmpty() && sourceUrl != null) {
            meta.leadImageUrl = TextAnalysisUtil.resolveUrl(sourceUrl, meta.leadImageUrl);
        }
        if (!meta.canonicalUrl.isEmpty() && sourceUrl != null) {
            meta.canonicalUrl = TextAnalysisUtil.resolveUrl(sourceUrl, meta.canonicalUrl);
        }

        return meta;
    }
}
