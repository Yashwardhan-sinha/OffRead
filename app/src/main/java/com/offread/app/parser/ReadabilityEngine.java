package com.offread.app.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class ReadabilityEngine {

    private static final Pattern NEGATIVE_PATTERN = Pattern.compile(
            "(?i)(ad|ads|advert|advertisement|banner|combx|comment|comments|community|disqus|extra|" +
                    "foot|footer|menu|nav|remark|rss|shoutbox|sidebar|sponsor|sponsored|ad-break|" +
                    "pagination|pager|popup|modal|newsletter|subscribe|subscription|social|share|" +
                    "share-bar|sharing|cookie|consent|toast|tracker|outbrain|taboola|widget|related|" +
                    "related-posts|recommendations|trending|promo)"
    );

    private static final Pattern POSITIVE_PATTERN = Pattern.compile(
            "(?i)(article|body|content|entry|main|post|story|prose|blog-post|markdown|text|page)"
    );

    private static final Set<String> UNWANTED_TAGS = new HashSet<>(Arrays.asList(
            "script", "style", "noscript", "template", "svg", "canvas", "applet",
            "object", "embed", "form", "input", "button", "select", "textarea",
            "nav", "footer", "aside", "dialog", "iframe"
    ));

    public static class ExtractionResult {
        public String markdown = "";
        public String plainText = "";
    }

    public static ExtractionResult extractContent(HtmlNode root, String sourceUrl, String articleTitle) {
        ExtractionResult result = new ExtractionResult();
        if (root == null) return result;

        // 1. Pre-cleaning unwanted elements
        cleanUnwantedElements(root);

        // 2. Score candidates
        scoreCandidates(root);

        // 3. Select winning container
        HtmlNode winner = selectBestCandidate(root);
        if (winner == null) {
            winner = root;
        }

        // 4. Clone and prune winner
        HtmlNode cleanTree = winner.cloneTree();
        pruneWinnerTree(cleanTree, articleTitle);

        // 5. Generate Markdown and Plain Text
        StringBuilder mdBuilder = new StringBuilder();
        StringBuilder ptBuilder = new StringBuilder();

        formatNode(cleanTree, mdBuilder, ptBuilder, sourceUrl);

        result.markdown = cleanFormatting(mdBuilder.toString());
        result.plainText = cleanFormatting(ptBuilder.toString());

        return result;
    }

    private static void cleanUnwantedElements(HtmlNode node) {
        List<HtmlNode> toRemove = new ArrayList<>();
        for (HtmlNode child : node.getChildren()) {
            if (UNWANTED_TAGS.contains(child.getTagName())) {
                toRemove.add(child);
                continue;
            }

            // Check negative class / ID pattern
            String clsAndId = child.getClassName() + " " + child.getId();
            if (NEGATIVE_PATTERN.matcher(clsAndId).find() && !POSITIVE_PATTERN.matcher(clsAndId).find()) {
                toRemove.add(child);
                continue;
            }

            cleanUnwantedElements(child);
        }

        for (HtmlNode dead : toRemove) {
            node.removeChild(dead);
        }
    }

    private static void scoreCandidates(HtmlNode root) {
        List<HtmlNode> candidates = root.findAll(n -> {
            String tag = n.getTagName();
            return "p".equals(tag) || "blockquote".equals(tag) || "pre".equals(tag) ||
                    "code".equals(tag) || "div".equals(tag) || "section".equals(tag) ||
                    "article".equals(tag) || "li".equals(tag);
        });

        for (HtmlNode node : candidates) {
            int textLen = node.getTextLength();
            if (textLen < 20) continue;

            double score = 0.0;
            String tag = node.getTagName();

            switch (tag) {
                case "p":
                    score += 10.0;
                    break;
                case "blockquote":
                case "pre":
                case "code":
                    score += 15.0;
                    break;
                case "li":
                    score += 3.0;
                    break;
                default:
                    score += 5.0;
                    break;
            }

            // Length score
            score += Math.min(50.0, textLen / 25.0);

            // Punctuation score (prose clauses)
            score += (node.countPunctuation() * 3.0);

            // Class / ID weights
            String clsAndId = node.getClassName() + " " + node.getId();
            if (POSITIVE_PATTERN.matcher(clsAndId).find()) {
                score += 25.0;
            }
            if (NEGATIVE_PATTERN.matcher(clsAndId).find()) {
                score -= 25.0;
            }

            // Link density penalty
            double linkDensity = node.calculateLinkDensity();
            if (linkDensity > 0.5) {
                score *= 0.2;
            } else if (linkDensity > 0.3) {
                score *= 0.5;
            } else {
                score *= (1.0 - linkDensity);
            }

            node.addReadabilityScore(score);

            // Propagate up to parent and grandparent
            HtmlNode parent = node.getParent();
            if (parent != null) {
                parent.addReadabilityScore(score);
                HtmlNode grandParent = parent.getParent();
                if (grandParent != null) {
                    grandParent.addReadabilityScore(score * 0.5);
                }
            }
        }
    }

    private static HtmlNode selectBestCandidate(HtmlNode root) {
        HtmlNode best = null;
        double maxScore = 0.0;

        List<HtmlNode> allNodes = root.findAll(n -> n.getReadabilityScore() > 0);
        for (HtmlNode node : allNodes) {
            if (node.getReadabilityScore() > maxScore) {
                maxScore = node.getReadabilityScore();
                best = node;
            }
        }

        if (best != null && maxScore >= 20.0) {
            return best;
        }

        // Semantic fallbacks
        HtmlNode article = root.findFirst(n -> "article".equals(n.getTagName()));
        if (article != null && article.getTextLength() > 50) {
            return article;
        }

        HtmlNode main = root.findFirst(n -> "main".equals(n.getTagName()));
        if (main != null && main.getTextLength() > 50) {
            return main;
        }

        HtmlNode classMatch = root.findFirst(n -> {
            String cls = n.getClassName().toLowerCase(Locale.ROOT);
            return cls.contains("article-body") || cls.contains("post-content") ||
                    cls.contains("entry-content") || cls.contains("story-body") ||
                    cls.contains("content-body");
        });
        if (classMatch != null && classMatch.getTextLength() > 50) {
            return classMatch;
        }

        HtmlNode body = root.findFirst(n -> "body".equals(n.getTagName()));
        if (body != null && body.getTextLength() > 50) {
            return body;
        }

        return root;
    }

    private static void pruneWinnerTree(HtmlNode node, String articleTitle) {
        List<HtmlNode> toRemove = new ArrayList<>();
        for (HtmlNode child : node.getChildren()) {
            // Remove high link-density blocks inside the article (e.g. inline ads, read next links)
            double linkDensity = child.calculateLinkDensity();
            if (linkDensity > 0.4 && child.getTextLength() > 40 && !"blockquote".equals(child.getTagName())) {
                toRemove.add(child);
                continue;
            }

            // Remove headers with high link density
            String tag = child.getTagName();
            if (tag.startsWith("h") && tag.length() == 2 && linkDensity > 0.25) {
                toRemove.add(child);
                continue;
            }

            // Remove redundant title H1 if matches extracted article title
            if ("h1".equals(tag) && articleTitle != null && !articleTitle.isEmpty()) {
                String h1Text = child.getInnerText().trim();
                if (h1Text.equalsIgnoreCase(articleTitle) || articleTitle.toLowerCase(Locale.ROOT).contains(h1Text.toLowerCase(Locale.ROOT))) {
                    toRemove.add(child);
                    continue;
                }
            }

            // Recursively prune
            pruneWinnerTree(child, articleTitle);
        }

        for (HtmlNode dead : toRemove) {
            node.removeChild(dead);
        }
    }

    private static void formatNode(HtmlNode node, StringBuilder md, StringBuilder pt, String sourceUrl) {
        if (node.isTextNode()) {
            String text = node.getText();
            if (text != null && !text.isEmpty()) {
                md.append(text);
                pt.append(text);
            }
            return;
        }

        String tag = node.getTagName();

        // Line breaks & block starts
        switch (tag) {
            case "h1":
                md.append("\n\n# ");
                pt.append("\n\n");
                break;
            case "h2":
                md.append("\n\n## ");
                pt.append("\n\n");
                break;
            case "h3":
                md.append("\n\n### ");
                pt.append("\n\n");
                break;
            case "h4":
            case "h5":
            case "h6":
                md.append("\n\n#### ");
                pt.append("\n\n");
                break;

            case "blockquote": {
                StringBuilder innerMd = new StringBuilder();
                StringBuilder innerPt = new StringBuilder();
                for (HtmlNode child : node.getChildren()) {
                    formatNode(child, innerMd, innerPt, sourceUrl);
                }
                String quoteText = innerMd.toString().trim();
                if (!quoteText.isEmpty()) {
                    md.append("\n\n");
                    String[] lines = quoteText.split("\n");
                    for (String line : lines) {
                        String trimmed = line.trim();
                        if (!trimmed.isEmpty()) {
                            md.append("> ").append(trimmed).append("\n");
                        }
                    }
                    md.append("\n");
                }
                String ptText = innerPt.toString().trim();
                if (!ptText.isEmpty()) {
                    pt.append("\n\n").append(ptText).append("\n\n");
                }
                return;
            }
            case "pre": {
                StringBuilder codeMd = new StringBuilder();
                for (HtmlNode child : node.getChildren()) {
                    formatNode(child, codeMd, pt, sourceUrl);
                }
                String codeContent = codeMd.toString().trim();
                md.append("\n\n```\n").append(codeContent).append("\n```\n\n");
                pt.append("\n\n").append(node.getInnerText().trim()).append("\n\n");
                return;
            }
            case "p":
                if (!"li".equals(getParentTag(node))) {
                    md.append("\n\n");
                    pt.append("\n\n");
                }
                break;
            case "div":
            case "section":
            case "article":
                md.append("\n\n");
                pt.append("\n\n");
                break;
            case "li":
                md.append("\n• ");
                pt.append("\n• ");
                break;
            case "br":
                md.append("\n");
                pt.append("\n");
                return;
            case "hr":
                md.append("\n\n---\n\n");
                pt.append("\n\n");
                return;
            case "strong":
            case "b":
                md.append("**");
                break;
            case "em":
            case "i":
                md.append("*");
                break;
            case "code":
                if (!"pre".equals(getParentTag(node))) {
                    md.append("`");
                }
                break;
            case "img":
                formatImage(node, md, sourceUrl);
                return;
            case "a":
                formatLink(node, md, pt, sourceUrl);
                return;
        }

        // Process children
        for (HtmlNode child : node.getChildren()) {
            formatNode(child, md, pt, sourceUrl);
        }

        // Block closures
        switch (tag) {
            case "h1":
            case "h2":
            case "h3":
            case "h4":
            case "h5":
            case "h6":
                md.append("\n\n");
                pt.append("\n\n");
                break;
            case "p":
                if (!"li".equals(getParentTag(node))) {
                    md.append("\n\n");
                    pt.append("\n\n");
                }
                break;
            case "pre":
                md.append("\n```\n\n");
                pt.append("\n\n");
                break;
            case "strong":
            case "b":
                md.append("**");
                break;
            case "em":
            case "i":
                md.append("*");
                break;
            case "code":
                if (!"pre".equals(getParentTag(node))) {
                    md.append("`");
                }
                break;
        }
    }

    private static void formatImage(HtmlNode img, StringBuilder md, String sourceUrl) {
        String src = img.getAttribute("src");
        if (src.isEmpty()) {
            src = img.getAttribute("data-src");
        }
        if (src.isEmpty()) return;

        String resolved = TextAnalysisUtil.resolveUrl(sourceUrl, src);
        if (resolved.isEmpty()) return;

        String alt = img.getAttribute("alt");
        if (alt.isEmpty()) {
            alt = "Image";
        }

        md.append("\n\n![").append(alt).append("](").append(resolved).append(")\n\n");
    }

    private static void formatLink(HtmlNode a, StringBuilder md, StringBuilder pt, String sourceUrl) {
        String href = a.getAttribute("href");
        String text = a.getInnerText().trim();
        if (text.isEmpty()) return;

        String resolved = TextAnalysisUtil.resolveUrl(sourceUrl, href);
        if (!resolved.isEmpty() && !resolved.startsWith("javascript:")) {
            md.append("[").append(text).append("](").append(resolved).append(")");
        } else {
            md.append(text);
        }
        pt.append(text);
    }

    private static String getParentTag(HtmlNode node) {
        HtmlNode p = node.getParent();
        return p != null ? p.getTagName() : "";
    }

    private static String cleanFormatting(String text) {
        if (text == null) return "";
        // Normalize space within lines
        String s = text.replaceAll("[ \t]+", " ");
        // Normalize multiple line breaks
        s = s.replaceAll("\n[ \t]+", "\n");
        s = s.replaceAll("\n{3,}", "\n\n");
        return s.trim();
    }
}
