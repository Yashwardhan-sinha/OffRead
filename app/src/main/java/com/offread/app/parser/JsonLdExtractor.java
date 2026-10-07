package com.offread.app.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    public static JsonLdData extract(HtmlNode root) {
        JsonLdData result = new JsonLdData();
        if (root == null) return result;

        List<HtmlNode> scripts = root.getElementsByTag("script");
        for (HtmlNode script : scripts) {
            String type = script.getAttribute("type").toLowerCase(Locale.ROOT);
            if (type.contains("ld+json")) {
                String text = script.getInnerText();
                if (text == null || text.trim().isEmpty()) continue;

                try {
                    Object parsed = MiniJson.parse(text);
                    if (parsed != null) {
                        List<Map<String, Object>> candidateObjects = new ArrayList<>();
                        collectArticleCandidates(parsed, candidateObjects);

                        for (Map<String, Object> obj : candidateObjects) {
                            populateData(obj, result);
                            if (!result.headline.isEmpty() || !result.articleBody.isEmpty()) {
                                return result;
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static void collectArticleCandidates(Object jsonNode, List<Map<String, Object>> out) {
        if (jsonNode instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) jsonNode;
            Object type = map.get("@type");
            if (isArticleType(type)) {
                out.add(map);
            }

            Object graph = map.get("@graph");
            if (graph != null) {
                collectArticleCandidates(graph, out);
            }
        } else if (jsonNode instanceof List) {
            List<?> list = (List<?>) jsonNode;
            for (Object item : list) {
                collectArticleCandidates(item, out);
            }
        }
    }

    private static boolean isArticleType(Object typeObj) {
        if (typeObj == null) return false;
        String typeStr = typeObj.toString().toLowerCase(Locale.ROOT);
        return typeStr.contains("article") ||
                typeStr.contains("blogposting") ||
                typeStr.contains("newsarticle") ||
                typeStr.contains("techarticle") ||
                typeStr.contains("report") ||
                typeStr.contains("webpage");
    }

    @SuppressWarnings("unchecked")
    private static void populateData(Map<String, Object> map, JsonLdData data) {
        // Headline
        if (data.headline.isEmpty()) {
            if (map.containsKey("headline")) {
                data.headline = String.valueOf(map.get("headline"));
            } else if (map.containsKey("name")) {
                data.headline = String.valueOf(map.get("name"));
            }
        }

        // Author
        if (data.author.isEmpty() && map.containsKey("author")) {
            data.author = extractAuthor(map.get("author"));
        }

        // Date Published
        if (data.datePublished.isEmpty()) {
            if (map.containsKey("datePublished")) {
                data.datePublished = String.valueOf(map.get("datePublished"));
            } else if (map.containsKey("dateCreated")) {
                data.datePublished = String.valueOf(map.get("dateCreated"));
            } else if (map.containsKey("dateModified")) {
                data.datePublished = String.valueOf(map.get("dateModified"));
            }
        }

        // Description
        if (data.description.isEmpty() && map.containsKey("description")) {
            data.description = String.valueOf(map.get("description"));
        }

        // Image
        if (data.imageUrl.isEmpty() && map.containsKey("image")) {
            data.imageUrl = extractImageUrl(map.get("image"));
        }

        // Publisher
        if (data.publisherName.isEmpty() && map.containsKey("publisher")) {
            Object pub = map.get("publisher");
            if (pub instanceof Map) {
                Object pName = ((Map<?, ?>) pub).get("name");
                if (pName != null) {
                    data.publisherName = String.valueOf(pName);
                }
            } else if (pub instanceof String) {
                data.publisherName = (String) pub;
            }
        }

        // Article body
        if (data.articleBody.isEmpty() && map.containsKey("articleBody")) {
            data.articleBody = String.valueOf(map.get("articleBody"));
        }
    }

    @SuppressWarnings("unchecked")
    private static String extractAuthor(Object authorObj) {
        if (authorObj == null) return "";
        if (authorObj instanceof String) {
            return (String) authorObj;
        }
        if (authorObj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) authorObj;
            Object name = map.get("name");
            return name != null ? String.valueOf(name) : "";
        }
        if (authorObj instanceof List) {
            List<?> list = (List<?>) authorObj;
            StringBuilder sb = new StringBuilder();
            for (Object item : list) {
                String a = extractAuthor(item);
                if (!a.isEmpty()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(a);
                }
            }
            return sb.toString();
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    private static String extractImageUrl(Object imageObj) {
        if (imageObj == null) return "";
        if (imageObj instanceof String) {
            return (String) imageObj;
        }
        if (imageObj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) imageObj;
            Object url = map.get("url");
            return url != null ? String.valueOf(url) : "";
        }
        if (imageObj instanceof List) {
            List<?> list = (List<?>) imageObj;
            for (Object item : list) {
                String u = extractImageUrl(item);
                if (!u.isEmpty()) return u;
            }
        }
        return "";
    }
}
