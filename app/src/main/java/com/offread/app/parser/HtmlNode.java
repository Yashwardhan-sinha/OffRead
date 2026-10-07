package com.offread.app.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

public class HtmlNode {
    private final String tagName; // normalized lowercase, or "#text"
    private String text;
    private final Map<String, String> attributes;
    private HtmlNode parent;
    private final List<HtmlNode> children;
    private double readabilityScore;

    public HtmlNode(String tagName) {
        this.tagName = tagName != null ? tagName.trim().toLowerCase(Locale.ROOT) : "";
        this.text = null;
        this.attributes = new HashMap<>();
        this.children = new ArrayList<>();
        this.readabilityScore = 0.0;
    }

    public static HtmlNode createTextNode(String text) {
        HtmlNode node = new HtmlNode("#text");
        node.text = text != null ? text : "";
        return node;
    }

    public boolean isTextNode() {
        return "#text".equals(tagName);
    }

    public String getTagName() {
        return tagName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAttribute(String key) {
        if (key == null) return "";
        String val = attributes.get(key.toLowerCase(Locale.ROOT));
        return val != null ? val : "";
    }

    public boolean hasAttribute(String key) {
        if (key == null) return false;
        return attributes.containsKey(key.toLowerCase(Locale.ROOT));
    }

    public void setAttribute(String key, String value) {
        if (key == null) return;
        attributes.put(key.toLowerCase(Locale.ROOT), value != null ? value : "");
    }

    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public String getClassName() {
        return getAttribute("class");
    }

    public String getId() {
        return getAttribute("id");
    }

    public HtmlNode getParent() {
        return parent;
    }

    public void setParent(HtmlNode parent) {
        this.parent = parent;
    }

    public List<HtmlNode> getChildren() {
        return children;
    }

    public void addChild(HtmlNode child) {
        if (child == null) return;
        child.setParent(this);
        children.add(child);
    }

    public void removeChild(HtmlNode child) {
        if (child == null) return;
        if (children.remove(child)) {
            child.setParent(null);
        }
    }

    public double getReadabilityScore() {
        return readabilityScore;
    }

    public void setReadabilityScore(double score) {
        this.readabilityScore = score;
    }

    public void addReadabilityScore(double delta) {
        this.readabilityScore += delta;
    }

    public String getInnerText() {
        StringBuilder sb = new StringBuilder();
        collectInnerText(this, sb);
        return sb.toString().replaceAll("\\s+", " ").trim();
    }

    private static void collectInnerText(HtmlNode node, StringBuilder sb) {
        if (node.isTextNode()) {
            if (node.text != null && !node.text.isEmpty()) {
                sb.append(node.text);
            }
            return;
        }

        boolean isBlock = isBlockTag(node.tagName);
        if (isBlock && sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
            sb.append(' ');
        }

        for (HtmlNode child : node.children) {
            collectInnerText(child, sb);
        }

        if (isBlock && sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
            sb.append(' ');
        }
    }

    public int getTextLength() {
        return getInnerText().length();
    }

    public int countPunctuation() {
        String s = getInnerText();
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == ',' || c == ';' || c == ':' || c == '—' || c == '–' || c == '-') {
                count++;
            }
        }
        return count;
    }

    public double calculateLinkDensity() {
        int totalLen = getTextLength();
        if (totalLen == 0) return 0.0;

        int linkLen = 0;
        List<HtmlNode> links = getElementsByTag("a");
        for (HtmlNode a : links) {
            linkLen += a.getTextLength();
        }

        return Math.min(1.0, (double) linkLen / (double) totalLen);
    }

    public List<HtmlNode> getElementsByTag(String tag) {
        List<HtmlNode> result = new ArrayList<>();
        if (tag == null) return result;
        String targetTag = tag.toLowerCase(Locale.ROOT);
        findTagsRecursive(this, targetTag, result);
        return result;
    }

    private static void findTagsRecursive(HtmlNode node, String tag, List<HtmlNode> list) {
        if (!node.isTextNode() && node.tagName.equals(tag)) {
            list.add(node);
        }
        for (HtmlNode child : node.children) {
            findTagsRecursive(child, tag, list);
        }
    }

    public List<HtmlNode> findAll(Predicate<HtmlNode> predicate) {
        List<HtmlNode> result = new ArrayList<>();
        findAllRecursive(this, predicate, result);
        return result;
    }

    private static void findAllRecursive(HtmlNode node, Predicate<HtmlNode> predicate, List<HtmlNode> list) {
        if (predicate.test(node)) {
            list.add(node);
        }
        for (HtmlNode child : node.children) {
            findAllRecursive(child, predicate, list);
        }
    }

    public HtmlNode findFirst(Predicate<HtmlNode> predicate) {
        if (predicate.test(this)) {
            return this;
        }
        for (HtmlNode child : children) {
            HtmlNode found = child.findFirst(predicate);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    public HtmlNode cloneTree() {
        HtmlNode copy;
        if (isTextNode()) {
            copy = createTextNode(this.text);
        } else {
            copy = new HtmlNode(this.tagName);
            copy.attributes.putAll(this.attributes);
        }
        copy.readabilityScore = this.readabilityScore;
        for (HtmlNode child : this.children) {
            copy.addChild(child.cloneTree());
        }
        return copy;
    }

    public static boolean isBlockTag(String tag) {
        if (tag == null) return false;
        switch (tag) {
            case "p":
            case "div":
            case "article":
            case "section":
            case "main":
            case "header":
            case "footer":
            case "aside":
            case "nav":
            case "h1":
            case "h2":
            case "h3":
            case "h4":
            case "h5":
            case "h6":
            case "blockquote":
            case "pre":
            case "ul":
            case "ol":
            case "li":
            case "table":
            case "tr":
            case "td":
            case "th":
            case "figure":
            case "figcaption":
            case "hr":
            case "br":
                return true;
            default:
                return false;
        }
    }
}
