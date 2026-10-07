package com.offread.app.parser;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class HtmlParser {

    private static final Set<String> VOID_TAGS = new HashSet<>(Arrays.asList(
            "area", "base", "br", "col", "embed", "hr", "img", "input",
            "link", "meta", "param", "source", "track", "wbr"
    ));

    public static HtmlNode parse(String html) {
        if (html == null || html.trim().isEmpty()) {
            return new HtmlNode("html");
        }

        HtmlNode root = new HtmlNode("root");
        Deque<HtmlNode> stack = new ArrayDeque<>();
        stack.push(root);

        int len = html.length();
        int pos = 0;

        while (pos < len) {
            int nextTag = html.indexOf('<', pos);
            if (nextTag == -1) {
                String text = html.substring(pos);
                if (!text.isEmpty()) {
                    stack.peek().addChild(HtmlNode.createTextNode(TextAnalysisUtil.decodeHtmlEntities(text)));
                }
                break;
            }

            if (nextTag > pos) {
                String text = html.substring(pos, nextTag);
                if (!text.isEmpty()) {
                    stack.peek().addChild(HtmlNode.createTextNode(TextAnalysisUtil.decodeHtmlEntities(text)));
                }
            }

            // We are at '<'
            // 1. Comments <!-- ... -->
            if (html.startsWith("<!--", nextTag)) {
                int endComment = html.indexOf("-->", nextTag + 4);
                pos = (endComment == -1) ? len : endComment + 3;
                continue;
            }

            // 2. CDATA <![CDATA[ ... ]]>
            if (html.startsWith("<![CDATA[", nextTag)) {
                int endCdata = html.indexOf("]]>", nextTag + 9);
                if (endCdata != -1) {
                    String cdataText = html.substring(nextTag + 9, endCdata);
                    stack.peek().addChild(HtmlNode.createTextNode(cdataText));
                    pos = endCdata + 3;
                } else {
                    pos = len;
                }
                continue;
            }

            // 3. DOCTYPE or XML declarations
            if (html.startsWith("<!", nextTag) || html.startsWith("<?", nextTag)) {
                int endDecl = html.indexOf('>', nextTag + 2);
                pos = (endDecl == -1) ? len : endDecl + 1;
                continue;
            }

            // 4. Closing tag </tag>
            if (nextTag + 1 < len && html.charAt(nextTag + 1) == '/') {
                int endClose = html.indexOf('>', nextTag + 2);
                if (endClose == -1) break;
                String tagName = html.substring(nextTag + 2, endClose).trim().toLowerCase(Locale.ROOT);
                closeTag(tagName, stack);
                pos = endClose + 1;
                continue;
            }

            // 5. Opening tag <tag attr="val">
            int endTag = findTagEnd(html, nextTag + 1);
            if (endTag == -1) break;

            String tagContent = html.substring(nextTag + 1, endTag).trim();
            pos = endTag + 1;

            if (tagContent.isEmpty()) continue;

            boolean isExplicitSelfClosing = tagContent.endsWith("/");
            if (isExplicitSelfClosing) {
                tagContent = tagContent.substring(0, tagContent.length() - 1).trim();
            }

            ParsedTag parsed = parseTagContent(tagContent);
            String tagName = parsed.name.toLowerCase(Locale.ROOT);
            if (tagName.isEmpty()) continue;

            HtmlNode element = new HtmlNode(tagName);
            for (String key : parsed.keys) {
                element.setAttribute(key, parsed.getValue(key));
            }

            handleAutoClose(tagName, stack);

            stack.peek().addChild(element);

            boolean isVoid = VOID_TAGS.contains(tagName) || isExplicitSelfClosing;
            if (!isVoid) {
                stack.push(element);

                // Raw text tags like <script> or <style>
                if ("script".equals(tagName) || "style".equals(tagName) || "noscript".equals(tagName) || "template".equals(tagName)) {
                    pos = captureRawText(html, pos, tagName, element, stack);
                }
            }
        }

        // Return the top-level html node if found, or root
        HtmlNode htmlElem = root.findFirst(n -> "html".equals(n.getTagName()));
        return htmlElem != null ? htmlElem : root;
    }

    private static int findTagEnd(String html, int start) {
        int len = html.length();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        for (int i = start; i < len; i++) {
            char c = html.charAt(i);
            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
            } else if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
            } else if (c == '>' && !inSingleQuote && !inDoubleQuote) {
                return i;
            }
        }
        return -1;
    }

    private static void closeTag(String tagName, Deque<HtmlNode> stack) {
        // Find matching tag in stack
        boolean found = false;
        for (HtmlNode node : stack) {
            if (node.getTagName().equals(tagName)) {
                found = true;
                break;
            }
        }
        if (found) {
            while (!stack.isEmpty()) {
                HtmlNode top = stack.pop();
                if (top.getTagName().equals(tagName)) {
                    break;
                }
            }
        }
    }

    private static void handleAutoClose(String tagName, Deque<HtmlNode> stack) {
        if (stack.size() <= 1) return;
        HtmlNode top = stack.peek();
        String topTag = top.getTagName();

        if ("p".equals(tagName) && "p".equals(topTag)) {
            stack.pop();
        } else if ("li".equals(tagName) && "li".equals(topTag)) {
            stack.pop();
        } else if ("tr".equals(tagName) && "tr".equals(topTag)) {
            stack.pop();
        } else if (("td".equals(tagName) || "th".equals(tagName)) && ("td".equals(topTag) || "th".equals(topTag))) {
            stack.pop();
        } else if (("dt".equals(tagName) || "dd".equals(tagName)) && ("dt".equals(topTag) || "dd".equals(topTag))) {
            stack.pop();
        }
    }

    private static int captureRawText(String html, int start, String tagName, HtmlNode element, Deque<HtmlNode> stack) {
        String closingPattern = "</" + tagName;
        int len = html.length();
        int closeIdx = -1;

        // Search case-insensitively for </tagName
        for (int i = start; i <= len - closingPattern.length(); i++) {
            if (html.regionMatches(true, i, closingPattern, 0, closingPattern.length())) {
                closeIdx = i;
                break;
            }
        }

        if (closeIdx != -1) {
            String rawContent = html.substring(start, closeIdx);
            if (!rawContent.isEmpty()) {
                element.addChild(HtmlNode.createTextNode(rawContent));
            }
            int tagEnd = html.indexOf('>', closeIdx);
            if (stack.peek() == element) {
                stack.pop();
            }
            return (tagEnd != -1) ? tagEnd + 1 : len;
        } else {
            // Unclosed script/style
            String rawContent = html.substring(start);
            if (!rawContent.isEmpty()) {
                element.addChild(HtmlNode.createTextNode(rawContent));
            }
            if (stack.peek() == element) {
                stack.pop();
            }
            return len;
        }
    }

    private static ParsedTag parseTagContent(String content) {
        ParsedTag result = new ParsedTag();
        int len = content.length();
        int i = 0;

        // Skip leading whitespace
        while (i < len && Character.isWhitespace(content.charAt(i))) i++;

        // Read tag name
        int nameStart = i;
        while (i < len && !Character.isWhitespace(content.charAt(i))) i++;
        result.name = content.substring(nameStart, i);

        // Read attributes
        while (i < len) {
            while (i < len && Character.isWhitespace(content.charAt(i))) i++;
            if (i >= len) break;

            // Read attribute key
            int keyStart = i;
            while (i < len && !Character.isWhitespace(content.charAt(i)) && content.charAt(i) != '=') i++;
            String key = content.substring(keyStart, i).toLowerCase(Locale.ROOT);

            while (i < len && Character.isWhitespace(content.charAt(i))) i++;

            String value = "";
            if (i < len && content.charAt(i) == '=') {
                i++; // skip '='
                while (i < len && Character.isWhitespace(content.charAt(i))) i++;

                if (i < len) {
                    char quote = content.charAt(i);
                    if (quote == '"' || quote == '\'') {
                        i++; // skip quote
                        int valStart = i;
                        while (i < len && content.charAt(i) != quote) i++;
                        value = content.substring(valStart, i);
                        if (i < len) i++; // skip quote
                    } else {
                        int valStart = i;
                        while (i < len && !Character.isWhitespace(content.charAt(i))) i++;
                        value = content.substring(valStart, i);
                    }
                }
            }

            if (!key.isEmpty()) {
                result.keys.add(key);
                result.put(key, TextAnalysisUtil.decodeHtmlEntities(value));
            }
        }

        return result;
    }

    private static class ParsedTag {
        String name = "";
        java.util.List<String> keys = new java.util.ArrayList<>();
        java.util.Map<String, String> values = new java.util.HashMap<>();

        void put(String k, String v) {
            values.put(k, v);
        }

        String getValue(String k) {
            String v = values.get(k);
            return v != null ? v : "";
        }
    }
}
