package com.offread.app.parser;

import com.offread.app.data.AppPreferences;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/* JADX INFO: loaded from: classes.dex */
public class HtmlParser {
    private static final int MAX_ELEMENTS = 30000;
    private static final int MAX_NESTING_DEPTH = 150;
    private static final Set<String> VOID_TAGS = new HashSet(Arrays.asList("area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta", "param", "source", "track", "wbr"));

    public static HtmlNode parse(String str) {
        int iIndexOf;
        int iIndexOf2;
        if (str == null || str.trim().isEmpty()) {
            return new HtmlNode("html");
        }
        HtmlNode htmlNode = new HtmlNode("root");
        ArrayDeque arrayDeque = new ArrayDeque();
        arrayDeque.push(htmlNode);
        int length = str.length();
        int iCaptureRawText = 0;
        int i = 0;
        while (iCaptureRawText < length) {
            int iIndexOf3 = str.indexOf(60, iCaptureRawText);
            if (iIndexOf3 == -1) {
                String strSubstring = str.substring(iCaptureRawText);
                if (!strSubstring.isEmpty()) {
                    ((HtmlNode) arrayDeque.peek()).addChild(HtmlNode.createTextNode(TextAnalysisUtil.decodeHtmlEntities(strSubstring)));
                    break;
                }
                break;
            }
            if (iIndexOf3 > iCaptureRawText) {
                String strSubstring2 = str.substring(iCaptureRawText, iIndexOf3);
                if (!strSubstring2.isEmpty()) {
                    ((HtmlNode) arrayDeque.peek()).addChild(HtmlNode.createTextNode(TextAnalysisUtil.decodeHtmlEntities(strSubstring2)));
                }
            }
            if (str.startsWith("<!--", iIndexOf3)) {
                iIndexOf = str.indexOf("-->", iIndexOf3 + 4);
                iCaptureRawText = iIndexOf == -1 ? length : iIndexOf + 3;
            } else if (str.startsWith("<![CDATA[", iIndexOf3)) {
                int i2 = iIndexOf3 + 9;
                iIndexOf = str.indexOf("]]>", i2);
                if (iIndexOf != -1) {
                    ((HtmlNode) arrayDeque.peek()).addChild(HtmlNode.createTextNode(str.substring(i2, iIndexOf)));
                }
            } else if (str.startsWith("<!", iIndexOf3) || str.startsWith("<?", iIndexOf3)) {
                iIndexOf2 = str.indexOf(62, iIndexOf3 + 2);
                if (iIndexOf2 != -1) {
                    iCaptureRawText = iIndexOf2 + 1;
                }
            } else {
                int i3 = iIndexOf3 + 1;
                if (i3 < length && str.charAt(i3) == '/') {
                    int i4 = iIndexOf3 + 2;
                    iIndexOf2 = str.indexOf(62, i4);
                    if (iIndexOf2 == -1) {
                        break;
                    }
                    closeTag(str.substring(i4, iIndexOf2).trim().toLowerCase(Locale.ROOT), arrayDeque);
                    iCaptureRawText = iIndexOf2 + 1;
                } else {
                    int iFindTagEnd = findTagEnd(str, i3);
                    if (iFindTagEnd == -1) {
                        break;
                    }
                    String strTrim = str.substring(i3, iFindTagEnd).trim();
                    int i5 = iFindTagEnd + 1;
                    if (!strTrim.isEmpty()) {
                        boolean zEndsWith = strTrim.endsWith("/");
                        if (zEndsWith) {
                            strTrim = strTrim.substring(0, strTrim.length() - 1).trim();
                        }
                        ParsedTag tagContent = parseTagContent(strTrim);
                        String lowerCase = tagContent.name.toLowerCase(Locale.ROOT);
                        if (!lowerCase.isEmpty()) {
                            HtmlNode htmlNode2 = new HtmlNode(lowerCase);
                            for (String str2 : tagContent.keys) {
                                htmlNode2.setAttribute(str2, tagContent.getValue(str2));
                            }
                            handleAutoClose(lowerCase, arrayDeque);
                            i++;
                            if (i > MAX_ELEMENTS) {
                                break;
                            }
                            ((HtmlNode) arrayDeque.peek()).addChild(htmlNode2);
                            if (!VOID_TAGS.contains(lowerCase) && !zEndsWith) {
                                if (arrayDeque.size() <= MAX_NESTING_DEPTH) {
                                    arrayDeque.push(htmlNode2);
                                }
                                if ("script".equals(lowerCase) || "style".equals(lowerCase) || "noscript".equals(lowerCase) || "template".equals(lowerCase)) {
                                    iCaptureRawText = captureRawText(str, i5, lowerCase, htmlNode2, arrayDeque);
                                }
                            }
                        }
                    }
                    iCaptureRawText = i5;
                }
            }
        }
        HtmlNode htmlNodeFindFirst = htmlNode.findFirst(new Predicate() { // from class: com.offread.app.parser.HtmlParser.0
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return HtmlParser.lambda$parse$0((HtmlNode) obj);
            }
        });
        return htmlNodeFindFirst != null ? htmlNodeFindFirst : htmlNode;
    }

    static /* synthetic */ boolean lambda$parse$0(HtmlNode htmlNode) {
        return "html".equals(htmlNode.getTagName());
    }

    private static int findTagEnd(String str, int i) {
        int length = str.length();
        boolean z = false;
        boolean z2 = false;
        while (i < length) {
            char cCharAt = str.charAt(i);
            if (cCharAt == '\'' && !z) {
                z2 = !z2;
            } else if (cCharAt == '\"' && !z2) {
                z = !z;
            } else if (cCharAt == '>' && !z2 && !z) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private static void closeTag(String str, Deque<HtmlNode> deque) {
        Iterator<HtmlNode> it = deque.iterator();
        while (it.hasNext()) {
            if (it.next().getTagName().equals(str)) {
                while (!deque.isEmpty() && !deque.pop().getTagName().equals(str)) {
                }
                return;
            }
        }
    }

    private static void handleAutoClose(String str, Deque<HtmlNode> deque) {
        if (deque.size() <= 1) {
            return;
        }
        String tagName = deque.peek().getTagName();
        if ("p".equals(str) && "p".equals(tagName)) {
            deque.pop();
            return;
        }
        if ("li".equals(str) && "li".equals(tagName)) {
            deque.pop();
            return;
        }
        if ("tr".equals(str) && "tr".equals(tagName)) {
            deque.pop();
            return;
        }
        if (("td".equals(str) || "th".equals(str)) && ("td".equals(tagName) || "th".equals(tagName))) {
            deque.pop();
            return;
        }
        if ("dt".equals(str) || "dd".equals(str)) {
            if ("dt".equals(tagName) || "dd".equals(tagName)) {
                deque.pop();
            }
        }
    }

    private static int captureRawText(String str, int i, String str2, HtmlNode htmlNode, Deque<HtmlNode> deque) {
        String str3;
        String str4 = "</" + str2;
        int length = str.length();
        int i2 = i;
        while (true) {
            if (i2 > length - str4.length()) {
                str3 = str;
                i2 = -1;
                break;
            }
            str3 = str;
            if (str3.regionMatches(true, i2, str4, 0, str4.length())) {
                break;
            }
            i2++;
            str = str3;
        }
        if (i2 != -1) {
            String strSubstring = str3.substring(i, i2);
            if (!strSubstring.isEmpty()) {
                htmlNode.addChild(HtmlNode.createTextNode(strSubstring));
            }
            int iIndexOf = str3.indexOf(62, i2);
            if (deque.peek() == htmlNode) {
                deque.pop();
            }
            if (iIndexOf != -1) {
                return iIndexOf + 1;
            }
        } else {
            String strSubstring2 = str3.substring(i);
            if (!strSubstring2.isEmpty()) {
                htmlNode.addChild(HtmlNode.createTextNode(strSubstring2));
            }
            if (deque.peek() == htmlNode) {
                deque.pop();
            }
        }
        return length;
    }

    private static ParsedTag parseTagContent(String str) {
        ParsedTag parsedTag = new ParsedTag();
        int length = str.length();
        int i = 0;
        while (i < length && Character.isWhitespace(str.charAt(i))) {
            i++;
        }
        int i2 = i;
        while (i2 < length && !Character.isWhitespace(str.charAt(i2))) {
            i2++;
        }
        parsedTag.name = str.substring(i, i2);
        while (i2 < length) {
            while (i2 < length && Character.isWhitespace(str.charAt(i2))) {
                i2++;
            }
            if (i2 >= length) {
                break;
            }
            int i3 = i2;
            while (i3 < length && !Character.isWhitespace(str.charAt(i3)) && str.charAt(i3) != '=') {
                i3++;
            }
            String lowerCase = str.substring(i2, i3).toLowerCase(Locale.ROOT);
            while (i3 < length && Character.isWhitespace(str.charAt(i3))) {
                i3++;
            }
            String strSubstring = AppPreferences.TTS_LANGUAGE_AUTO;
            if (i3 < length && str.charAt(i3) == '=') {
                do {
                    i3++;
                    if (i3 >= length) {
                        break;
                    }
                } while (Character.isWhitespace(str.charAt(i3)));
                if (i3 < length) {
                    char cCharAt = str.charAt(i3);
                    if (cCharAt == '\"' || cCharAt == '\'') {
                        int i4 = i3 + 1;
                        int i5 = i4;
                        while (i5 < length && str.charAt(i5) != cCharAt) {
                            i5++;
                        }
                        String strSubstring2 = str.substring(i4, i5);
                        if (i5 < length) {
                            i5++;
                        }
                        int i6 = i5;
                        strSubstring = strSubstring2;
                        i3 = i6;
                    } else {
                        int i7 = i3;
                        while (i7 < length && !Character.isWhitespace(str.charAt(i7))) {
                            i7++;
                        }
                        strSubstring = str.substring(i3, i7);
                        i3 = i7;
                    }
                }
            }
            if (!lowerCase.isEmpty()) {
                parsedTag.keys.add(lowerCase);
                parsedTag.put(lowerCase, TextAnalysisUtil.decodeHtmlEntities(strSubstring));
            }
            i2 = i3;
        }
        return parsedTag;
    }

    private static class ParsedTag {
        List<String> keys;
        String name;
        Map<String, String> values;

        private ParsedTag() {
            this.name = AppPreferences.TTS_LANGUAGE_AUTO;
            this.keys = new ArrayList();
            this.values = new HashMap();
        }

        void put(String str, String str2) {
            this.values.put(str, str2);
        }

        String getValue(String str) {
            String str2 = this.values.get(str);
            return str2 != null ? str2 : AppPreferences.TTS_LANGUAGE_AUTO;
        }
    }
}
