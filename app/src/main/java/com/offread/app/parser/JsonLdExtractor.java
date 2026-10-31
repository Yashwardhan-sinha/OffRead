package com.offread.app.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class JsonLdExtractor {

    public static class JsonLdData {
        public String headline = "";
        public String author = "";
        public String datePublished = "";
        public String description = "";
        public String imageUrl = "";
        public String publisherName = "";
        public String articleBody = "";
    }

    public static JsonLdData extract(HtmlNode htmlNode) {
        String innerText;
        JsonLdData jsonLdData = new JsonLdData();
        if (htmlNode != null) {
            loop0: for (HtmlNode htmlNode2 : htmlNode.getElementsByTag("script")) {
                if (htmlNode2.getAttribute("type").toLowerCase(Locale.ROOT).contains("ld+json") && (innerText = htmlNode2.getInnerText()) != null && !innerText.trim().isEmpty()) {
                    try {
                        Object obj = MiniJson.parse(innerText);
                        if (obj != null) {
                            ArrayList arrayList = new ArrayList();
                            collectArticleCandidates(obj, arrayList);
                            Iterator it = arrayList.iterator();
                            while (it.hasNext()) {
                                populateData((Map) it.next(), jsonLdData);
                                if (!jsonLdData.headline.isEmpty() || !jsonLdData.articleBody.isEmpty()) {
                                    break loop0;
                                }
                            }
                        } else {
                            continue;
                        }
                    } catch (Throwable unused) {
                        continue;
                    }
                }
            }
        }
        return jsonLdData;
    }

    private static void collectArticleCandidates(Object obj, List<Map<String, Object>> list) {
        if (obj instanceof Map) {
            Map<String, Object> map = (Map) obj;
            if (isArticleType(map.get("@type"))) {
                list.add(map);
            }
            Object obj2 = map.get("@graph");
            if (obj2 != null) {
                collectArticleCandidates(obj2, list);
                return;
            }
            return;
        }
        if (obj instanceof List) {
            Iterator it = ((List) obj).iterator();
            while (it.hasNext()) {
                collectArticleCandidates(it.next(), list);
            }
        }
    }

    private static boolean isArticleType(Object obj) {
        if (obj == null) {
            return false;
        }
        String lowerCase = obj.toString().toLowerCase(Locale.ROOT);
        return lowerCase.contains("article") || lowerCase.contains("blogposting") || lowerCase.contains("newsarticle") || lowerCase.contains("techarticle") || lowerCase.contains("report") || lowerCase.contains("webpage");
    }

    private static void populateData(Map<String, Object> map, JsonLdData jsonLdData) {
        if (jsonLdData.headline.isEmpty()) {
            if (map.containsKey("headline")) {
                jsonLdData.headline = String.valueOf(map.get("headline"));
            } else if (map.containsKey("name")) {
                jsonLdData.headline = String.valueOf(map.get("name"));
            }
        }
        if (jsonLdData.author.isEmpty() && map.containsKey("author")) {
            jsonLdData.author = extractAuthor(map.get("author"));
        }
        if (jsonLdData.datePublished.isEmpty()) {
            if (map.containsKey("datePublished")) {
                jsonLdData.datePublished = String.valueOf(map.get("datePublished"));
            } else if (map.containsKey("dateCreated")) {
                jsonLdData.datePublished = String.valueOf(map.get("dateCreated"));
            } else if (map.containsKey("dateModified")) {
                jsonLdData.datePublished = String.valueOf(map.get("dateModified"));
            }
        }
        if (jsonLdData.description.isEmpty() && map.containsKey("description")) {
            jsonLdData.description = String.valueOf(map.get("description"));
        }
        if (jsonLdData.imageUrl.isEmpty() && map.containsKey("image")) {
            jsonLdData.imageUrl = extractImageUrl(map.get("image"));
        }
        if (jsonLdData.publisherName.isEmpty() && map.containsKey("publisher")) {
            Object obj = map.get("publisher");
            if (obj instanceof Map) {
                Object obj2 = ((Map) obj).get("name");
                if (obj2 != null) {
                    jsonLdData.publisherName = String.valueOf(obj2);
                }
            } else if (obj instanceof String) {
                jsonLdData.publisherName = (String) obj;
            }
        }
        if (jsonLdData.articleBody.isEmpty() && map.containsKey("articleBody")) {
            jsonLdData.articleBody = String.valueOf(map.get("articleBody"));
        }
    }

    private static String extractAuthor(Object obj) {
        if (obj == null) {
            return "";
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        if (obj instanceof Map) {
            Object obj2 = ((Map) obj).get("name");
            if (obj2 == null) {
                return "";
            }
            return String.valueOf(obj2);
        }
        if (!(obj instanceof List)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Iterator it = ((List) obj).iterator();
        while (it.hasNext()) {
            String strExtractAuthor = extractAuthor(it.next());
            if (!strExtractAuthor.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(strExtractAuthor);
            }
        }
        return sb.toString();
    }

    private static String extractImageUrl(Object obj) {
        if (obj == null) {
            return "";
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        if (obj instanceof Map) {
            Object obj2 = ((Map) obj).get("url");
            if (obj2 == null) {
                return "";
            }
            return String.valueOf(obj2);
        }
        if (obj instanceof List) {
            Iterator it = ((List) obj).iterator();
            while (it.hasNext()) {
                String strExtractImageUrl = extractImageUrl(it.next());
                if (!strExtractImageUrl.isEmpty()) {
                    return strExtractImageUrl;
                }
            }
        }
        return "";
    }
}
