package com.offread.app.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

/* JADX INFO: loaded from: classes.dex */
public class ReadabilityEngine {
    private static final Set<String> UNWANTED_TAGS = new HashSet(Arrays.asList("script", "style", "noscript", "template", "svg", "canvas", "applet", "object", "embed", "form", "input", "button", "select", "textarea", "nav", "footer", "aside", "dialog", "iframe"));

    public static class ExtractionResult {
        public String markdown = "";
        public String plainText = "";
    }

    public static ExtractionResult extractContent(HtmlNode htmlNode, String str, String str2) {
        ExtractionResult extractionResult = new ExtractionResult();
        if (htmlNode == null) {
            return extractionResult;
        }
        AdFilter.removeAdElements(htmlNode);
        cleanUnwantedElements(htmlNode);
        scoreCandidates(htmlNode);
        HtmlNode htmlNodeSelectBestCandidate = selectBestCandidate(htmlNode);
        if (htmlNodeSelectBestCandidate != null) {
            htmlNode = htmlNodeSelectBestCandidate;
        }
        HtmlNode htmlNodeCloneTree = htmlNode.cloneTree();
        pruneWinnerTree(htmlNodeCloneTree, str2);
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        formatNode(htmlNodeCloneTree, sb, sb2, str);
        extractionResult.markdown = TextNormalizer.normalizeArticle(sb.toString());
        extractionResult.plainText = TextNormalizer.normalizeArticle(sb2.toString());
        return extractionResult;
    }

    private static void cleanUnwantedElements(HtmlNode htmlNode) {
        ArrayList arrayList = new ArrayList();
        for (HtmlNode htmlNode2 : htmlNode.getChildren()) {
            if (UNWANTED_TAGS.contains(htmlNode2.getTagName())) {
                arrayList.add(htmlNode2);
            } else if (AdFilter.looksLikeBoilerplate(htmlNode2) && !AdFilter.looksLikeArticleContent(htmlNode2)) {
                arrayList.add(htmlNode2);
            } else {
                cleanUnwantedElements(htmlNode2);
            }
        }
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            htmlNode.removeChild((HtmlNode) it.next());
        }
    }

    private static void scoreCandidates(HtmlNode htmlNode) {
        double d;
        for (HtmlNode htmlNode2 : htmlNode.findAll(new Predicate<HtmlNode>() {
            @Override
            public boolean test(HtmlNode obj) {
                return ReadabilityEngine.lambda$scoreCandidates$0(obj);
            }
        })) {
            int textLength = htmlNode2.getTextLength();
            if (textLength >= 20) {
                String tagName = htmlNode2.getTagName();
                tagName.hashCode();
                switch (tagName) {
                    case "p":
                        d = 10.0d;
                        break;
                    case "li":
                        d = 3.0d;
                        break;
                    case "pre":
                    case "code":
                    case "blockquote":
                        d = 15.0d;
                        break;
                    default:
                        d = 5.0d;
                        break;
                }
                double dMin = d + Math.min(50.0d, ((double) textLength) / 25.0d) + (((double) htmlNode2.countPunctuation()) * 3.0d);
                if (AdFilter.looksLikeArticleContent(htmlNode2)) {
                    dMin += 25.0d;
                }
                if (AdFilter.looksLikeBoilerplate(htmlNode2)) {
                    dMin -= 25.0d;
                }
                double dCalculateLinkDensity = htmlNode2.calculateLinkDensity();
                double d2 = dCalculateLinkDensity > 0.5d ? dMin * 0.2d : dCalculateLinkDensity > 0.3d ? dMin * 0.5d : dMin * (1.0d - dCalculateLinkDensity);
                htmlNode2.addReadabilityScore(d2);
                HtmlNode parent = htmlNode2.getParent();
                if (parent != null) {
                    parent.addReadabilityScore(d2);
                    HtmlNode parent2 = parent.getParent();
                    if (parent2 != null) {
                        parent2.addReadabilityScore(d2 * 0.5d);
                    }
                }
            }
        }
    }

    static /* synthetic */ boolean lambda$scoreCandidates$0(HtmlNode htmlNode) {
        String tagName = htmlNode.getTagName();
        return "p".equals(tagName) || "blockquote".equals(tagName) || "pre".equals(tagName) || "code".equals(tagName) || "div".equals(tagName) || "section".equals(tagName) || "article".equals(tagName) || "li".equals(tagName);
    }

    static /* synthetic */ boolean lambda$selectBestCandidate$1(HtmlNode htmlNode) {
        return htmlNode.getReadabilityScore() > 0.0d;
    }

    private static HtmlNode selectBestCandidate(HtmlNode htmlNode) {
        HtmlNode htmlNode2 = null;
        double readabilityScore = 0.0d;
        for (HtmlNode htmlNode3 : htmlNode.findAll(new Predicate<HtmlNode>() {
            @Override
            public boolean test(HtmlNode obj) {
                return ReadabilityEngine.lambda$selectBestCandidate$1(obj);
            }
        })) {
            if (htmlNode3.getReadabilityScore() > readabilityScore) {
                readabilityScore = htmlNode3.getReadabilityScore();
                htmlNode2 = htmlNode3;
            }
        }
        if (htmlNode2 != null && readabilityScore >= 20.0d) {
            return htmlNode2;
        }
        HtmlNode htmlNodeFindFirst = htmlNode.findFirst(new Predicate() { // from class: com.offread.app.parser.ReadabilityEngine.1
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return ReadabilityEngine.lambda$selectBestCandidate$2((HtmlNode) obj);
            }
        });
        if (htmlNodeFindFirst != null && htmlNodeFindFirst.getTextLength() > 50) {
            return htmlNodeFindFirst;
        }
        HtmlNode htmlNodeFindFirst2 = htmlNode.findFirst(new Predicate() { // from class: com.offread.app.parser.ReadabilityEngine.2
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return ReadabilityEngine.lambda$selectBestCandidate$3((HtmlNode) obj);
            }
        });
        if (htmlNodeFindFirst2 != null && htmlNodeFindFirst2.getTextLength() > 50) {
            return htmlNodeFindFirst2;
        }
        HtmlNode htmlNodeFindFirst3 = htmlNode.findFirst(new Predicate() { // from class: com.offread.app.parser.ReadabilityEngine.3
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return ReadabilityEngine.lambda$selectBestCandidate$4((HtmlNode) obj);
            }
        });
        if (htmlNodeFindFirst3 != null && htmlNodeFindFirst3.getTextLength() > 50) {
            return htmlNodeFindFirst3;
        }
        HtmlNode htmlNodeFindFirst4 = htmlNode.findFirst(new Predicate() { // from class: com.offread.app.parser.ReadabilityEngine.4
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return ReadabilityEngine.lambda$selectBestCandidate$5((HtmlNode) obj);
            }
        });
        return (htmlNodeFindFirst4 == null || htmlNodeFindFirst4.getTextLength() <= 50) ? htmlNode : htmlNodeFindFirst4;
    }

    static /* synthetic */ boolean lambda$selectBestCandidate$2(HtmlNode htmlNode) {
        return "article".equals(htmlNode.getTagName());
    }

    static /* synthetic */ boolean lambda$selectBestCandidate$3(HtmlNode htmlNode) {
        return "main".equals(htmlNode.getTagName());
    }

    static /* synthetic */ boolean lambda$selectBestCandidate$4(HtmlNode htmlNode) {
        String lowerCase = htmlNode.getClassName().toLowerCase(Locale.ROOT);
        return lowerCase.contains("article-body") || lowerCase.contains("post-content") || lowerCase.contains("entry-content") || lowerCase.contains("story-body") || lowerCase.contains("content-body");
    }

    static /* synthetic */ boolean lambda$selectBestCandidate$5(HtmlNode htmlNode) {
        return "body".equals(htmlNode.getTagName());
    }

    private static void pruneWinnerTree(HtmlNode htmlNode, String str) {
        ArrayList arrayList = new ArrayList();
        for (HtmlNode htmlNode2 : htmlNode.getChildren()) {
            String innerText = htmlNode2.getInnerText();
            if ((AdFilter.isAdPlaceholderText(innerText) || TextNormalizer.scrubInvisible(innerText).trim().isEmpty()) && !"img".equals(htmlNode2.getTagName()) && !"picture".equals(htmlNode2.getTagName())) {
                arrayList.add(htmlNode2);
            } else {
                double dCalculateLinkDensity = htmlNode2.calculateLinkDensity();
                if (dCalculateLinkDensity > 0.4d && htmlNode2.getTextLength() > 40 && !"blockquote".equals(htmlNode2.getTagName())) {
                    arrayList.add(htmlNode2);
                } else {
                    String tagName = htmlNode2.getTagName();
                    if (tagName.startsWith("h") && tagName.length() == 2 && dCalculateLinkDensity > 0.25d) {
                        arrayList.add(htmlNode2);
                    } else {
                        if ("h1".equals(tagName) && str != null && !str.isEmpty()) {
                            String strTrim = htmlNode2.getInnerText().trim();
                            if (strTrim.equalsIgnoreCase(str) || str.toLowerCase(Locale.ROOT).contains(strTrim.toLowerCase(Locale.ROOT))) {
                                arrayList.add(htmlNode2);
                            }
                        }
                        pruneWinnerTree(htmlNode2, str);
                    }
                }
            }
        }
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            htmlNode.removeChild((HtmlNode) it.next());
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code duplicated, block: B:152:0x02b7 A[LOOP:0: B:150:0x02b1->B:152:0x02b7, LOOP_END] */
    /* JADX WARN: Code duplicated, block: B:155:0x02cf  */
    /* JADX WARN: Code duplicated, block: B:156:0x02d3  */
    /* JADX WARN: Code duplicated, block: B:159:0x02dc  */
    /* JADX WARN: Code duplicated, block: B:160:0x02e0  */
    /* JADX WARN: Code duplicated, block: B:163:0x02e7  */
    /* JADX WARN: Code duplicated, block: B:164:0x02eb  */
    /* JADX WARN: Code duplicated, block: B:167:0x02f4  */
    /* JADX WARN: Code duplicated, block: B:168:0x02f8  */
    /* JADX WARN: Code duplicated, block: B:171:0x0301  */
    /* JADX WARN: Code duplicated, block: B:172:0x0305  */
    /* JADX WARN: Code duplicated, block: B:175:0x030e  */
    /* JADX WARN: Code duplicated, block: B:176:0x0311  */
    /* JADX WARN: Code duplicated, block: B:179:0x031a  */
    /* JADX WARN: Code duplicated, block: B:180:0x031c  */
    /* JADX WARN: Code duplicated, block: B:183:0x0323  */
    /* JADX WARN: Code duplicated, block: B:184:0x0325  */
    /* JADX WARN: Code duplicated, block: B:187:0x032c  */
    /* JADX WARN: Code duplicated, block: B:188:0x032e  */
    /* JADX WARN: Code duplicated, block: B:191:0x0335  */
    /* JADX WARN: Code duplicated, block: B:192:0x0337  */
    /* JADX WARN: Code duplicated, block: B:195:0x033e  */
    /* JADX WARN: Code duplicated, block: B:196:0x0341  */
    /* JADX WARN: Code duplicated, block: B:199:0x0348  */
    /* JADX WARN: Code duplicated, block: B:200:0x034b  */
    /* JADX WARN: Code duplicated, block: B:203:0x0353  */
    /* JADX WARN: Code duplicated, block: B:204:0x0356  */
    /* JADX WARN: Code duplicated, block: B:207:0x035e  */
    /* JADX WARN: Code duplicated, block: B:210:0x0364  */
    /* JADX WARN: Code duplicated, block: B:212:0x036e  */
    /* JADX WARN: Code duplicated, block: B:214:0x0374  */
    /* JADX WARN: Code duplicated, block: B:216:0x037d  */
    /* JADX WARN: Code duplicated, block: B:218:0x0384  */
    /* JADX WARN: Code duplicated, block: B:220:0x038e  */
    /* JADX WARN: Code duplicated, block: B:222:0x0395  */
    /* JADX WARN: Code duplicated, block: B:224:0x039b  */
    /* JADX WARN: Code duplicated, block: B:254:? A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:255:? A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:256:? A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:257:? A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:258:? A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:259:? A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:260:? A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:261:? A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:262:? A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:263:? A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:264:? A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:265:? A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:266:? A[SYNTHETIC] */
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

    private static void formatImage(HtmlNode htmlNode, StringBuilder sb, String str) {
                String attribute = htmlNode.getAttribute("src");
                if (attribute.isEmpty()) {
                    attribute = htmlNode.getAttribute("data-src");
                }
                if (attribute.isEmpty()) {
                    return;
                }
                String strResolveImageUrl = ArticleContentBlocks.resolveImageUrl(attribute, str);
                if (strResolveImageUrl.isEmpty()) {
                    return;
                }
                String attribute2 = htmlNode.getAttribute("alt");
                if (attribute2.isEmpty()) {
                    attribute2 = "Image";
                }
                sb.append("\n\n![");
                sb.append(attribute2);
                sb.append("](");
                sb.append(strResolveImageUrl);
                sb.append(")\n\n");
            }

            private static void formatLink(HtmlNode htmlNode, StringBuilder sb, StringBuilder sb2, String str) {
                String attribute = htmlNode.getAttribute("href");
                String strTrim = htmlNode.getInnerText().trim();
                if (strTrim.isEmpty()) {
                    return;
                }
                String strResolveUrl = TextAnalysisUtil.resolveUrl(str, attribute);
                if (!strResolveUrl.isEmpty() && !strResolveUrl.startsWith("javascript:")) {
                    sb.append("[");
                    sb.append(strTrim);
                    sb.append("](");
                    sb.append(strResolveUrl);
                    sb.append(")");
                } else {
                    sb.append(strTrim);
                }
                sb2.append(strTrim);
            }

            private static String getParentTag(HtmlNode htmlNode) {
                HtmlNode parent = htmlNode.getParent();
                return parent != null ? parent.getTagName() : "";
            }
        }
