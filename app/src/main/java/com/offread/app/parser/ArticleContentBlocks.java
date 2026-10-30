package com.offread.app.parser;

import com.offread.app.data.AppPreferences;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public final class ArticleContentBlocks {
    private static final int MAX_BLOCKS = 800;
    public static final int TYPE_CODE = 6;
    public static final int TYPE_HEADING = 1;
    public static final int TYPE_IMAGE = 7;
    public static final int TYPE_LIST_ITEM = 4;
    public static final int TYPE_MINI_HEADING = 3;
    public static final int TYPE_PARAGRAPH = 0;
    public static final int TYPE_QUOTE = 5;
    public static final int TYPE_RULE = 8;
    public static final int TYPE_SUBHEADING = 2;

    private static final class ImageRef {
        final String alt;
        final int end;
        final int start;
        final String url;

        ImageRef(String str, String str2, int i, int i2) {
            this.url = str;
            this.alt = str2;
            this.start = i;
            this.end = i2;
        }
    }

    public static final class Block {
        public final String alt;
        public final String imageUrl;
        public final String text;
        public final int type;

        Block(int i, String str, String str2, String str3) {
            this.type = i;
            this.text = str == null ? AppPreferences.TTS_LANGUAGE_AUTO : str;
            this.imageUrl = str2 == null ? AppPreferences.TTS_LANGUAGE_AUTO : str2;
            this.alt = str3 == null ? AppPreferences.TTS_LANGUAGE_AUTO : str3;
        }

        public boolean isImage() {
            return this.type == 7;
        }

        public boolean isHeading() {
            int i = this.type;
            return i == 1 || i == 2 || i == 3;
        }

        public String plainText() {
            return ArticleContentBlocks.inlinePlain(this.text);
        }

        public String toString() {
            return "Block{type=" + this.type + ", text='" + plainText() + "', img='" + this.imageUrl + "'}";
        }
    }

    private ArticleContentBlocks() {
    }

    public static List<Block> parse(String str) {
        ArrayList arrayList = new ArrayList();
        String strNormalizeArticle = TextNormalizer.normalizeArticle(str);
        if (strNormalizeArticle.isEmpty()) {
            return arrayList;
        }
        String[] strArrSplit = strNormalizeArticle.split("\n", -1);
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        StringBuilder sb3 = null;
        boolean z = false;
        for (String str2 : strArrSplit) {
            String strTrim = str2.trim();
            if (sb3 != null) {
                if (strTrim.startsWith("```")) {
                    arrayList.add(new Block(6, sb3.toString(), AppPreferences.TTS_LANGUAGE_AUTO, AppPreferences.TTS_LANGUAGE_AUTO));
                    sb3 = null;
                } else if (arrayList.size() < MAX_BLOCKS) {
                    if (sb3.length() > 0) {
                        sb3.append('\n');
                    }
                    sb3.append(str2);
                }
                if (arrayList.size() >= MAX_BLOCKS) {
                    break;
                }
            } else {
                if (strTrim.startsWith("```")) {
                    flushParagraph(arrayList, sb);
                    flushQuote(arrayList, sb2);
                    sb3 = new StringBuilder();
                } else if (strTrim.isEmpty()) {
                    flushParagraph(arrayList, sb);
                    flushQuote(arrayList, sb2);
                } else if (isRule(strTrim)) {
                    flushParagraph(arrayList, sb);
                    flushQuote(arrayList, sb2);
                    addBlock(arrayList, new Block(8, AppPreferences.TTS_LANGUAGE_AUTO, AppPreferences.TTS_LANGUAGE_AUTO, AppPreferences.TTS_LANGUAGE_AUTO));
                } else {
                    int iHeadingLevelOf = headingLevelOf(strTrim);
                    int i = 1;
                    if (iHeadingLevelOf > 0) {
                        flushParagraph(arrayList, sb);
                        flushQuote(arrayList, sb2);
                        String strTrim2 = strTrim.replaceFirst("^#{1,6}[ \\t]+", AppPreferences.TTS_LANGUAGE_AUTO).trim();
                        if (iHeadingLevelOf != 1) {
                            i = 2;
                            if (iHeadingLevelOf != 2) {
                                i = 3;
                            }
                        }
                        emitWithInlineImages(arrayList, i, strTrim2);
                    } else {
                        if (strTrim.startsWith(">")) {
                            flushParagraph(arrayList, sb);
                            String strTrim3 = strTrim.replaceFirst("^>+[[ \\t]]*", AppPreferences.TTS_LANGUAGE_AUTO).trim();
                            if (!strTrim3.isEmpty()) {
                                if (z && sb2.length() > 0) {
                                    sb2.append(' ');
                                }
                                sb2.append(strTrim3);
                                z = true;
                            }
                        } else if (isListLine(strTrim)) {
                            flushParagraph(arrayList, sb);
                            flushQuote(arrayList, sb2);
                            String strTrim4 = strTrim.replaceFirst("^[\\u2022\\-*+][ \\t]+", AppPreferences.TTS_LANGUAGE_AUTO).trim();
                            if (!strTrim4.isEmpty()) {
                                emitWithInlineImages(arrayList, 4, strTrim4);
                            }
                        } else {
                            String strStandaloneImageOf = standaloneImageOf(strTrim);
                            if (strStandaloneImageOf != null) {
                                flushParagraph(arrayList, sb);
                                flushQuote(arrayList, sb2);
                                ImageRef image = parseImage(strStandaloneImageOf);
                                if (image != null) {
                                    addBlock(arrayList, new Block(7, AppPreferences.TTS_LANGUAGE_AUTO, image.url, image.alt));
                                }
                            } else {
                                emitInlineImagesInto(arrayList, sb, strTrim);
                                if (arrayList.size() >= MAX_BLOCKS) {
                                    break;
                                }
                            }
                        }
                    }
                }
                z = false;
            }
        }
        if (sb3 != null && sb3.length() > 0) {
            addBlock(arrayList, new Block(6, sb3.toString(), AppPreferences.TTS_LANGUAGE_AUTO, AppPreferences.TTS_LANGUAGE_AUTO));
        }
        flushParagraph(arrayList, sb);
        flushQuote(arrayList, sb2);
        return pruneJunkBlocks(arrayList);
    }

    public static String toPlainText(List<Block> list) {
        StringBuilder sb = new StringBuilder();
        for (Block block : list) {
            if (block.type != 7 && block.type != 8) {
                String strPlainText = block.plainText();
                if (!strPlainText.isEmpty()) {
                    if (sb.length() > 0) {
                        sb.append("\n\n");
                    }
                    sb.append(strPlainText);
                }
            }
        }
        return sb.toString();
    }

    private static List<Block> pruneJunkBlocks(List<Block> list) {
        ArrayList arrayList = new ArrayList(list.size());
        while (true) {
            String str = null;
            for (Block block : list) {
                if (block.type == 8) {
                    if (arrayList.isEmpty() || !blockLooksLikeSeparator(arrayList)) {
                        arrayList.add(block);
                    }
                } else if (block.type == 7) {
                    if (!block.imageUrl.isEmpty()) {
                        arrayList.add(block);
                    }
                } else {
                    String strPlainText = block.plainText();
                    if (!strPlainText.isEmpty() && !AdFilter.isAdPlaceholderText(strPlainText) && !AdFilter.isSeparatorOrSpinner(strPlainText) && (str == null || !str.equals(strPlainText))) {
                        arrayList.add(block);
                        str = strPlainText;
                    }
                }
            }
            return arrayList;
        }
    }

    private static boolean blockLooksLikeSeparator(List<Block> list) {
        return list.isEmpty() || list.get(list.size() - 1).type == 8;
    }

    private static void addBlock(List<Block> list, Block block) {
        if (list.size() >= MAX_BLOCKS) {
            return;
        }
        list.add(block);
    }

    private static void flushParagraph(List<Block> list, StringBuilder sb) {
        if (sb.length() == 0) {
            return;
        }
        String strTrim = sb.toString().trim();
        sb.setLength(0);
        if (strTrim.isEmpty() || AdFilter.isAdPlaceholderText(strTrim)) {
            return;
        }
        addBlock(list, new Block(0, strTrim, AppPreferences.TTS_LANGUAGE_AUTO, AppPreferences.TTS_LANGUAGE_AUTO));
    }

    private static void flushQuote(List<Block> list, StringBuilder sb) {
        if (sb.length() == 0) {
            return;
        }
        String strTrim = sb.toString().trim();
        sb.setLength(0);
        if (strTrim.isEmpty() || AdFilter.isAdPlaceholderText(strTrim)) {
            return;
        }
        addBlock(list, new Block(5, strTrim, AppPreferences.TTS_LANGUAGE_AUTO, AppPreferences.TTS_LANGUAGE_AUTO));
    }

    private static void emitWithInlineImages(List<Block> list, int i, String str) {
        StringBuilder sb = new StringBuilder();
        emitInlineImagesInto(list, sb, str);
        String strTrim = sb.toString().trim();
        if (strTrim.isEmpty()) {
            return;
        }
        addBlock(list, new Block(i, strTrim, AppPreferences.TTS_LANGUAGE_AUTO, AppPreferences.TTS_LANGUAGE_AUTO));
    }

    private static void emitInlineImagesInto(List<Block> list, StringBuilder sb, String str) {
        int i = 0;
        for (ImageRef imageRef : findImages(str)) {
            String strTrim = str.substring(i, imageRef.start).trim();
            if (!strTrim.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(strTrim);
            }
            if (imageRef.url.length() > 0) {
                addBlock(list, new Block(7, AppPreferences.TTS_LANGUAGE_AUTO, imageRef.url, imageRef.alt));
            }
            i = imageRef.end;
        }
        if (i != 0) {
            str = str.substring(i).trim();
        }
        if (str.isEmpty()) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(' ');
        }
        sb.append(str);
    }

    private static List<ImageRef> findImages(String str) {
        ArrayList arrayList = new ArrayList(1);
        if (str != null) {
            int i = 0;
            while (true) {
                int iIndexOf = str.indexOf("![", i);
                if (iIndexOf < 0) {
                    break;
                }
                int iIndexOf2 = str.indexOf(41, iIndexOf);
                int iIndexOf3 = str.indexOf("](", iIndexOf);
                if (iIndexOf2 < 0 || iIndexOf3 < 0 || iIndexOf3 > iIndexOf2) {
                    break;
                }
                String strSubstring = str.substring(iIndexOf + 2, iIndexOf3);
                String strTrim = str.substring(iIndexOf3 + 2, iIndexOf2).trim();
                int iIndexOf4 = strTrim.indexOf(32);
                if (iIndexOf4 > 0) {
                    strTrim = strTrim.substring(0, iIndexOf4);
                }
                if (!strTrim.isEmpty() && SafeUrl.isWellFormedHttpUrl(strTrim)) {
                    arrayList.add(new ImageRef(strTrim, strSubstring, iIndexOf, iIndexOf2 + 1));
                }
                i = iIndexOf2 + 1;
            }
        }
        return arrayList;
    }

    private static String standaloneImageOf(String str) {
        if (str.startsWith("![") && str.endsWith(")")) {
            List<ImageRef> listFindImages = findImages(str);
            if (listFindImages.size() != 1) {
                return null;
            }
            ImageRef imageRef = listFindImages.get(0);
            if (imageRef.start == 0 && imageRef.end == str.length()) {
                return str;
            }
        }
        return null;
    }

    private static ImageRef parseImage(String str) {
        List<ImageRef> listFindImages = findImages(str);
        if (listFindImages.isEmpty()) {
            return null;
        }
        return listFindImages.get(0);
    }

    private static int headingLevelOf(String str) {
        if (str.length() < 3 || str.charAt(0) != '#') {
            return 0;
        }
        int i = 0;
        while (i < str.length() && str.charAt(i) == '#') {
            i++;
        }
        if (i > 4) {
            i = 4;
        }
        if (i >= str.length()) {
            return 0;
        }
        char cCharAt = str.charAt(i);
        if (cCharAt == ' ' || cCharAt == '\t') {
            return i;
        }
        return 0;
    }

    private static boolean isListLine(String str) {
        if (str.isEmpty()) {
            return false;
        }
        char cCharAt = str.charAt(0);
        if (cCharAt == 8226 || cCharAt == '-' || cCharAt == '*' || cCharAt == '+') {
            return str.length() == 1 || str.charAt(1) == ' ' || str.charAt(1) == '\t';
        }
        return false;
    }

    private static boolean isRule(String str) {
        if (str.length() < 3) {
            return false;
        }
        String strReplace = str.replace(" ", AppPreferences.TTS_LANGUAGE_AUTO);
        return strReplace.equals("---") || strReplace.equals("***") || strReplace.equals("___");
    }

    public static String resolveImageUrl(String str, String str2) {
        if (str != null && !str.trim().isEmpty()) {
            String strResolveUrl = TextAnalysisUtil.resolveUrl(str2, str.trim());
            if (!strResolveUrl.isEmpty() && SafeUrl.isWellFormedHttpUrl(strResolveUrl)) {
                return strResolveUrl;
            }
        }
        return AppPreferences.TTS_LANGUAGE_AUTO;
    }

    public static String inlinePlain(String str) {
        if (str == null || str.isEmpty()) {
            return AppPreferences.TTS_LANGUAGE_AUTO;
        }
        return TextAnalysisUtil.decodeHtmlEntities(str).replaceAll("!\\[[^\\]]*\\]\\([^)]*\\)", AppPreferences.TTS_LANGUAGE_AUTO).replaceAll("\\[([^\\]]+)\\]\\([^)]*\\)", "$1").replaceAll("`([^`]+)`", "$1").replaceAll("\\*\\*([^*]*)\\*\\*", "$1").replaceAll("__([^_]*)__", "$1").replaceAll("\\*([^*]*)\\*", "$1").replaceAll("_([^_]*)_", "$1").replaceAll("\\s+", " ").trim();
    }

    public static String describeType(int i) {
        switch (i) {
            case 0:
                return "paragraph";
            case 1:
                return "heading";
            case 2:
                return "subheading";
            case 3:
                return "minor heading";
            case TYPE_LIST_ITEM /* 4 */:
                return "list item";
            case TYPE_QUOTE /* 5 */:
                return "quote";
            case TYPE_CODE /* 6 */:
                return "code";
            case TYPE_IMAGE /* 7 */:
                return "image";
            case TYPE_RULE /* 8 */:
                return "divider";
            default:
                return "unknown";
        }
    }
}
