package com.offread.app.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class MiniJson {
    private static final int MAX_DEPTH = 64;

    public static Object parse(String str) {
        if (str == null) {
            return null;
        }
        String strTrim = str.trim();
        if (strTrim.isEmpty()) {
            return null;
        }
        Parser parser = new Parser(strTrim);
        Object value = parser.parseValue();
        if (parser.nestedTooDeep) {
            return null;
        }
        return value;
    }

    public static Map<String, Object> parseObject(String str) {
        Object obj = parse(str);
        if (obj instanceof Map) {
            return (Map) obj;
        }
        return null;
    }

    public static List<Object> parseArray(String str) {
        Object obj = parse(str);
        if (obj instanceof List) {
            return (List) obj;
        }
        return null;
    }

    private static class Parser {
        private final int len;
        private final String src;
        private int idx = 0;
        private int depth = 0;
        private boolean nestedTooDeep = false;

        Parser(String str) {
            this.src = str;
            this.len = str.length();
        }

        Object parseValue() {
            skipWhitespace();
            if (this.idx >= this.len) {
                return null;
            }
            int i = this.depth;
            if (i >= 64) {
                this.nestedTooDeep = true;
                return null;
            }
            this.depth = i + 1;
            try {
                return parseValueAtDepth();
            } finally {
                this.depth--;
            }
        }

        Object parseValueAtDepth() {
            char cCharAt = this.src.charAt(this.idx);
            if (cCharAt == '{') {
                return parseObject();
            }
            if (cCharAt == '[') {
                return parseArray();
            }
            if (cCharAt == '\"' || cCharAt == '\'') {
                return parseString();
            }
            if (cCharAt == 't' || cCharAt == 'f') {
                return parseBoolean();
            }
            if (cCharAt == 'n') {
                return parseNull();
            }
            if (cCharAt == '-' || Character.isDigit(cCharAt)) {
                return parseNumber();
            }
            return null;
        }

        private Map<String, Object> parseObject() {
            String string;
            this.idx++;
            HashMap map = new HashMap();
            skipWhitespace();
            int i = this.idx;
            if (i < this.len && this.src.charAt(i) == '}') {
                this.idx++;
                return map;
            }
            while (this.idx < this.len) {
                skipWhitespace();
                int i2 = this.idx;
                if (i2 >= this.len) {
                    break;
                }
                if (this.src.charAt(i2) == '}') {
                    this.idx++;
                    return map;
                }
                char cCharAt = this.src.charAt(this.idx);
                if (cCharAt == '\"' || cCharAt == '\'') {
                    string = parseString();
                } else {
                    int i3 = this.idx;
                    while (true) {
                        int i4 = this.idx;
                        if (i4 >= this.len || this.src.charAt(i4) == ':' || Character.isWhitespace(this.src.charAt(this.idx))) {
                            break;
                        }
                        this.idx++;
                    }
                    string = this.src.substring(i3, this.idx);
                }
                skipWhitespace();
                int i5 = this.idx;
                if (i5 < this.len && this.src.charAt(i5) == ':') {
                    this.idx++;
                }
                map.put(string, parseValue());
                skipWhitespace();
                int i6 = this.idx;
                if (i6 < this.len && this.src.charAt(i6) == ',') {
                    this.idx++;
                } else {
                    int i7 = this.idx;
                    if (i7 >= this.len || this.src.charAt(i7) != '}') {
                        break;
                    }
                    this.idx++;
                    break;
                }
            }
            return map;
        }

        private List<Object> parseArray() {
            this.idx++;
            ArrayList arrayList = new ArrayList();
            skipWhitespace();
            int i = this.idx;
            if (i < this.len && this.src.charAt(i) == ']') {
                this.idx++;
                return arrayList;
            }
            while (this.idx < this.len) {
                skipWhitespace();
                int i2 = this.idx;
                if (i2 >= this.len) {
                    break;
                }
                if (this.src.charAt(i2) == ']') {
                    this.idx++;
                    return arrayList;
                }
                arrayList.add(parseValue());
                skipWhitespace();
                int i3 = this.idx;
                if (i3 < this.len && this.src.charAt(i3) == ',') {
                    this.idx++;
                } else {
                    int i4 = this.idx;
                    if (i4 >= this.len || this.src.charAt(i4) != ']') {
                        break;
                    }
                    this.idx++;
                    break;
                }
            }
            return arrayList;
        }

        /* JADX WARN: Code duplicated, block: B:45:0x00bd  */
        private String parseString() {
            char quote = this.src.charAt(this.idx++);
            StringBuilder result = new StringBuilder();
            while (this.idx < this.len) {
                char current = this.src.charAt(this.idx++);
                if (current == quote) return result.toString();
                if (current != '\\') {
                    result.append(current);
                    continue;
                }
                if (this.idx >= this.len) break;
                char escaped = this.src.charAt(this.idx++);
                switch (escaped) {
                    case '"': result.append('"'); break;
                    case '\'': result.append('\''); break;
                    case '\\': result.append('\\'); break;
                    case '/': result.append('/'); break;
                    case 'b': result.append('\b'); break;
                    case 'f': result.append('\f'); break;
                    case 'n': result.append('\n'); break;
                    case 'r': result.append('\r'); break;
                    case 't': result.append('\t'); break;
                    case 'u':
                        if (this.idx + 4 <= this.len) {
                            try {
                                result.append((char) Integer.parseInt(this.src.substring(this.idx, this.idx + 4), 16));
                                this.idx += 4;
                            } catch (NumberFormatException ignored) {
                                result.append('u');
                            }
                        } else result.append('u');
                        break;
                    default: result.append(escaped); break;
                }
            }
            return result.toString();
        }

        private Boolean parseBoolean() {
            if (this.src.startsWith("true", this.idx)) {
                this.idx += 4;
                return Boolean.TRUE;
            }
            if (!this.src.startsWith("false", this.idx)) {
                return null;
            }
            this.idx += 5;
            return Boolean.FALSE;
        }

        private Object parseNull() {
            if (!this.src.startsWith("null", this.idx)) {
                return null;
            }
            this.idx += 4;
            return null;
        }

        private Number parseNumber() {
            boolean z;
            int i = this.idx;
            boolean z2 = true;
            if (i < this.len && this.src.charAt(i) == '-') {
                this.idx++;
            }
            while (true) {
                int i2 = this.idx;
                if (i2 >= this.len || !Character.isDigit(this.src.charAt(i2))) {
                    break;
                }
                this.idx++;
            }
            int i3 = this.idx;
            if (i3 >= this.len || this.src.charAt(i3) != '.') {
                z = false;
            } else {
                this.idx++;
                while (true) {
                    int i4 = this.idx;
                    if (i4 >= this.len || !Character.isDigit(this.src.charAt(i4))) {
                        break;
                    }
                    this.idx++;
                }
                z = true;
            }
            int i5 = this.idx;
            if (i5 >= this.len || !(this.src.charAt(i5) == 'e' || this.src.charAt(this.idx) == 'E')) {
                z2 = z;
            } else {
                int i6 = this.idx + 1;
                this.idx = i6;
                if (i6 < this.len && (this.src.charAt(i6) == '+' || this.src.charAt(this.idx) == '-')) {
                    this.idx++;
                }
                while (true) {
                    int i7 = this.idx;
                    if (i7 >= this.len || !Character.isDigit(this.src.charAt(i7))) {
                        break;
                    }
                    this.idx++;
                }
            }
            String strSubstring = this.src.substring(i, this.idx);
            try {
                if (z2) {
                    return Double.valueOf(Double.parseDouble(strSubstring));
                }
                long j = Long.parseLong(strSubstring);
                if (j >= -2147483648L && j <= 2147483647L) {
                    return Integer.valueOf((int) j);
                }
                return Long.valueOf(j);
            } catch (Exception unused) {
                return 0;
            }
        }

        private void skipWhitespace() {
            while (true) {
                int i = this.idx;
                if (i >= this.len || !Character.isWhitespace(this.src.charAt(i))) {
                    return;
                } else {
                    this.idx++;
                }
            }
        }
    }
}
