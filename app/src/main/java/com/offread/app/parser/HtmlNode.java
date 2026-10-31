package com.offread.app.parser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

/* JADX INFO: loaded from: classes.dex */
public class HtmlNode {
    private final Map<String, String> attributes;
    private final List<HtmlNode> children;
    private HtmlNode parent;
    private double readabilityScore;
    private final String tagName;
    private String text;

    public HtmlNode(String str) {
        this.tagName = str != null ? str.trim().toLowerCase(Locale.ROOT) : "";
        this.text = null;
        this.attributes = new HashMap();
        this.children = new ArrayList();
        this.readabilityScore = 0.0d;
    }

    public static HtmlNode createTextNode(String str) {
        HtmlNode htmlNode = new HtmlNode("#text");
        if (str == null) {
            str = "";
        }
        htmlNode.text = str;
        return htmlNode;
    }

    public boolean isTextNode() {
        return "#text".equals(this.tagName);
    }

    public String getTagName() {
        return this.tagName;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String str) {
        this.text = str;
    }

    public String getAttribute(String str) {
        String str2;
        return (str == null || (str2 = this.attributes.get(str.toLowerCase(Locale.ROOT))) == null) ? "" : str2;
    }

    public boolean hasAttribute(String str) {
        if (str == null) {
            return false;
        }
        return this.attributes.containsKey(str.toLowerCase(Locale.ROOT));
    }

    public void setAttribute(String str, String str2) {
        if (str == null) {
            return;
        }
        Map<String, String> map = this.attributes;
        String lowerCase = str.toLowerCase(Locale.ROOT);
        if (str2 == null) {
            str2 = "";
        }
        map.put(lowerCase, str2);
    }

    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(this.attributes);
    }

    public String getClassName() {
        return getAttribute("class");
    }

    public String getId() {
        return getAttribute("id");
    }

    public HtmlNode getParent() {
        return this.parent;
    }

    public void setParent(HtmlNode htmlNode) {
        this.parent = htmlNode;
    }

    public List<HtmlNode> getChildren() {
        return this.children;
    }

    public void addChild(HtmlNode htmlNode) {
        if (htmlNode == null) {
            return;
        }
        htmlNode.setParent(this);
        this.children.add(htmlNode);
    }

    public void removeChild(HtmlNode htmlNode) {
        if (htmlNode != null && this.children.remove(htmlNode)) {
            htmlNode.setParent(null);
        }
    }

    public double getReadabilityScore() {
        return this.readabilityScore;
    }

    public void setReadabilityScore(double d) {
        this.readabilityScore = d;
    }

    public void addReadabilityScore(double d) {
        this.readabilityScore += d;
    }

    public String getInnerText() {
        StringBuilder sb = new StringBuilder();
        collectInnerText(this, sb);
        return sb.toString().replaceAll("\\s+", " ").trim();
    }

    private static void collectInnerText(HtmlNode htmlNode, StringBuilder sb) {
        ArrayDeque arrayDeque = new ArrayDeque();
        arrayDeque.push(new Visit(htmlNode));
        while (!arrayDeque.isEmpty()) {
            Visit visit = (Visit) arrayDeque.peek();
            HtmlNode htmlNode2 = visit.node;
            if (!visit.entered) {
                visit.entered = true;
                if (htmlNode2.isTextNode()) {
                    String str = htmlNode2.text;
                    if (str != null && !str.isEmpty()) {
                        sb.append(htmlNode2.text);
                    }
                    arrayDeque.pop();
                    continue;
                } else if (isBlockTag(htmlNode2.tagName) && sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
                    sb.append(' ');
                }
            }
            if (visit.cursor < htmlNode2.children.size()) {
                List<HtmlNode> list = htmlNode2.children;
                int i = visit.cursor;
                visit.cursor = i + 1;
                arrayDeque.push(new Visit(list.get(i)));
            } else {
                if (!htmlNode2.isTextNode() && isBlockTag(htmlNode2.tagName) && sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
                    sb.append(' ');
                }
                arrayDeque.pop();
            }
        }
    }

    private static final class Visit {
        int cursor;
        boolean entered;
        final HtmlNode node;

        Visit(HtmlNode htmlNode) {
            this.node = htmlNode;
        }
    }

    public int getTextLength() {
        return getInnerText().length();
    }

    public int countPunctuation() {
        String innerText = getInnerText();
        int i = 0;
        for (int i2 = 0; i2 < innerText.length(); i2++) {
            char cCharAt = innerText.charAt(i2);
            if (cCharAt == ',' || cCharAt == ';' || cCharAt == ':' || cCharAt == 8212 || cCharAt == 8211 || cCharAt == '-') {
                i++;
            }
        }
        return i;
    }

    public double calculateLinkDensity() {
        int textLength = getTextLength();
        if (textLength == 0) {
            return 0.0d;
        }
        Iterator<HtmlNode> it = getElementsByTag("a").iterator();
        int textLength2 = 0;
        while (it.hasNext()) {
            textLength2 += it.next().getTextLength();
        }
        return Math.min(1.0d, ((double) textLength2) / ((double) textLength));
    }

    public List<HtmlNode> getElementsByTag(String str) {
        if (str == null) {
            return new ArrayList();
        }
        final String lowerCase = str.toLowerCase(Locale.ROOT);
        return collect(new Predicate() { // from class: com.offread.app.parser.HtmlNode.0
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return HtmlNode.lambda$getElementsByTag$0(lowerCase, (HtmlNode) obj);
            }
        });
    }

    static /* synthetic */ boolean lambda$getElementsByTag$0(String str, HtmlNode htmlNode) {
        return !htmlNode.isTextNode() && htmlNode.tagName.equals(str);
    }

    public List<HtmlNode> findAll(Predicate<HtmlNode> predicate) {
        return collect(predicate);
    }

    private List<HtmlNode> collect(Predicate<HtmlNode> predicate) {
        ArrayList arrayList = new ArrayList();
        ArrayDeque arrayDeque = new ArrayDeque();
        arrayDeque.push(this);
        while (!arrayDeque.isEmpty()) {
            HtmlNode htmlNode = (HtmlNode) arrayDeque.pop();
            if (predicate.test(htmlNode)) {
                arrayList.add(htmlNode);
            }
            List<HtmlNode> list = htmlNode.children;
            for (int size = list.size() - 1; size >= 0; size--) {
                arrayDeque.push(list.get(size));
            }
        }
        return arrayList;
    }

    public HtmlNode findFirst(Predicate<HtmlNode> predicate) {
        ArrayDeque arrayDeque = new ArrayDeque();
        arrayDeque.push(this);
        while (!arrayDeque.isEmpty()) {
            HtmlNode htmlNode = (HtmlNode) arrayDeque.pop();
            if (predicate.test(htmlNode)) {
                return htmlNode;
            }
            List<HtmlNode> list = htmlNode.children;
            for (int size = list.size() - 1; size >= 0; size--) {
                arrayDeque.push(list.get(size));
            }
        }
        return null;
    }

    public HtmlNode cloneTree() {
        HtmlNode htmlNodeShallowCopy = shallowCopy(this);
        ArrayDeque arrayDeque = new ArrayDeque();
        arrayDeque.push(new HtmlNode[]{this, htmlNodeShallowCopy});
        while (!arrayDeque.isEmpty()) {
            HtmlNode[] htmlNodeArr = (HtmlNode[]) arrayDeque.pop();
            for (HtmlNode htmlNode : htmlNodeArr[0].children) {
                HtmlNode htmlNodeShallowCopy2 = shallowCopy(htmlNode);
                htmlNodeArr[1].addChild(htmlNodeShallowCopy2);
                arrayDeque.push(new HtmlNode[]{htmlNode, htmlNodeShallowCopy2});
            }
        }
        return htmlNodeShallowCopy;
    }

    private static HtmlNode shallowCopy(HtmlNode htmlNode) {
        HtmlNode htmlNode2;
        if (htmlNode.isTextNode()) {
            htmlNode2 = createTextNode(htmlNode.text);
        } else {
            htmlNode2 = new HtmlNode(htmlNode.tagName);
            htmlNode2.attributes.putAll(htmlNode.attributes);
        }
        htmlNode2.readabilityScore = htmlNode.readabilityScore;
        return htmlNode2;
    }

    public static boolean isBlockTag(String str) {
        if (str == null) {
            return false;
        }
        str.hashCode();
        switch (str) {
            case "figure":
            case "footer":
            case "header":
            case "article":
            case "p":
            case "br":
            case "h1":
            case "h2":
            case "h3":
            case "h4":
            case "h5":
            case "h6":
            case "hr":
            case "li":
            case "ol":
            case "td":
            case "th":
            case "tr":
            case "ul":
            case "div":
            case "nav":
            case "pre":
            case "main":
            case "aside":
            case "table":
            case "figcaption":
            case "blockquote":
            case "section":
                return true;
            default:
                return false;
        }
    }
}
