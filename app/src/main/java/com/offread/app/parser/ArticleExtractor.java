package com.offread.app.parser;

/* JADX INFO: loaded from: classes.dex */
public class ArticleExtractor {
    public static ReadabilityResult fetchFromUrl(String str) throws Exception {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        NetworkFetcher.FetchResult fetchResultFetch = NetworkFetcher.fetch(str.trim());
        return extractFromHtml(fetchResultFetch.html, fetchResultFetch.finalUrl);
    }

    public static ReadabilityResult extractFromHtml(String str, String str2) {
        String str3 = str2;
        if (str == null || str.trim().isEmpty()) {
            String strExtractDomain = TextAnalysisUtil.extractDomain(str3);
            if (str3 == null) {
                str3 = "";
            }
            return new ReadabilityResult("Untitled", "", "", "", "", 0, 1, "", "", strExtractDomain, str3, "");
        }
        HtmlNode htmlNode = HtmlParser.parse(str);
        MetadataExtractor.ArticleMetadata articleMetadataExtract = MetadataExtractor.extract(htmlNode, str3);
        ReadabilityEngine.ExtractionResult extractionResultExtractContent = ReadabilityEngine.extractContent(htmlNode, str3, articleMetadataExtract.title);
        String strNormalizeArticle = extractionResultExtractContent.markdown;
        String strNormalizeArticle2 = extractionResultExtractContent.plainText;
        if (strNormalizeArticle2.length() < 50 && articleMetadataExtract.jsonLdBody != null && !articleMetadataExtract.jsonLdBody.trim().isEmpty()) {
            strNormalizeArticle = TextNormalizer.normalizeArticle(articleMetadataExtract.jsonLdBody);
            strNormalizeArticle2 = TextNormalizer.normalizeArticle(articleMetadataExtract.jsonLdBody);
        }
        String str4 = strNormalizeArticle2;
        String str5 = strNormalizeArticle;
        int iCountWords = TextAnalysisUtil.countWords(str4);
        int iCalculateReadingTimeMinutes = TextAnalysisUtil.calculateReadingTimeMinutes(iCountWords);
        String strNormalizeInline = TextNormalizer.normalizeInline(articleMetadataExtract.description);
        if (strNormalizeInline.isEmpty()) {
            strNormalizeInline = TextAnalysisUtil.createExcerpt(str4, 160);
        }
        String str6 = strNormalizeInline;
        String strNormalizeInline2 = TextNormalizer.normalizeInline(articleMetadataExtract.title);
        if (strNormalizeInline2.isEmpty()) {
            strNormalizeInline2 = TextAnalysisUtil.extractDomain(str3);
        }
        return new ReadabilityResult(strNormalizeInline2, TextNormalizer.normalizeInline(articleMetadataExtract.author), str5, str4, str6, iCountWords, iCalculateReadingTimeMinutes, articleMetadataExtract.publishedDate, articleMetadataExtract.leadImageUrl, articleMetadataExtract.siteName, articleMetadataExtract.canonicalUrl, articleMetadataExtract.language);
    }

    public static ReadabilityResult processUserText(String str, String str2, String str3) {
        String strTrim = (str == null || str.trim().isEmpty()) ? "Untitled Note" : str.trim();
        String strTrim2 = "";
        String strTrim3 = str2 != null ? str2.trim() : "";
        if (str3 != null) {
            strTrim2 = str3.trim();
        }
        String str4 = strTrim2;
        for (String str5 : str4.split("\n\\s*\n")) {
            str5.trim().isEmpty();
        }
        int iCountWords = TextAnalysisUtil.countWords(str4);
        return new ReadabilityResult(strTrim, strTrim3, str4, str4, TextAnalysisUtil.createExcerpt(str4, 160), iCountWords, TextAnalysisUtil.calculateReadingTimeMinutes(iCountWords));
    }
}
