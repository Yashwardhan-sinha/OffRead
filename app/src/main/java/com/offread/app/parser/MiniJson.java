package com.offread.app.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MiniJson {

    public static Object parse(String json) {
        if (json == null) return null;
        String trimmed = json.trim();
        if (trimmed.isEmpty()) return null;
        return new Parser(trimmed).parseValue();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        Object res = parse(json);
        if (res instanceof Map) {
            return (Map<String, Object>) res;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> parseArray(String json) {
        Object res = parse(json);
        if (res instanceof List) {
            return (List<Object>) res;
        }
        return null;
    }

    private static class Parser {
        private final String src;
        private final int len;
        private int idx = 0;

        Parser(String src) {
            this.src = src;
            this.len = src.length();
        }

        Object parseValue() {
            skipWhitespace();
            if (idx >= len) return null;

            char c = src.charAt(idx);
            if (c == '{') {
                return parseObject();
            } else if (c == '[') {
                return parseArray();
            } else if (c == '"' || c == '\'') {
                return parseString();
            } else if (c == 't' || c == 'f') {
                return parseBoolean();
            } else if (c == 'n') {
                return parseNull();
            } else if (c == '-' || Character.isDigit(c)) {
                return parseNumber();
            }
            return null;
        }

        private Map<String, Object> parseObject() {
            idx++; // skip '{'
            Map<String, Object> map = new HashMap<>();
            skipWhitespace();
            if (idx < len && src.charAt(idx) == '}') {
                idx++;
                return map;
            }

            while (idx < len) {
                skipWhitespace();
                if (idx >= len) break;
                if (src.charAt(idx) == '}') {
                    idx++;
                    break;
                }

                String key = "";
                char c = src.charAt(idx);
                if (c == '"' || c == '\'') {
                    key = parseString();
                } else {
                    int start = idx;
                    while (idx < len && src.charAt(idx) != ':' && !Character.isWhitespace(src.charAt(idx))) {
                        idx++;
                    }
                    key = src.substring(start, idx);
                }

                skipWhitespace();
                if (idx < len && src.charAt(idx) == ':') {
                    idx++; // skip ':'
                }

                Object val = parseValue();
                map.put(key, val);

                skipWhitespace();
                if (idx < len && src.charAt(idx) == ',') {
                    idx++; // skip ','
                } else if (idx < len && src.charAt(idx) == '}') {
                    idx++;
                    break;
                } else {
                    break;
                }
            }
            return map;
        }

        private List<Object> parseArray() {
            idx++; // skip '['
            List<Object> list = new ArrayList<>();
            skipWhitespace();
            if (idx < len && src.charAt(idx) == ']') {
                idx++;
                return list;
            }

            while (idx < len) {
                skipWhitespace();
                if (idx >= len) break;
                if (src.charAt(idx) == ']') {
                    idx++;
                    break;
                }

                Object val = parseValue();
                list.add(val);

                skipWhitespace();
                if (idx < len && src.charAt(idx) == ',') {
                    idx++; // skip ','
                } else if (idx < len && src.charAt(idx) == ']') {
                    idx++;
                    break;
                } else {
                    break;
                }
            }
            return list;
        }

        private String parseString() {
            char quote = src.charAt(idx);
            idx++; // skip quote
            StringBuilder sb = new StringBuilder();

            while (idx < len) {
                char c = src.charAt(idx);
                if (c == quote) {
                    idx++;
                    break;
                }
                if (c == '\\' && idx + 1 < len) {
                    idx++;
                    char esc = src.charAt(idx);
                    if (esc == '"') sb.append('"');
                    else if (esc == '\'') sb.append('\'');
                    else if (esc == '\\') sb.append('\\');
                    else if (esc == '/') sb.append('/');
                    else if (esc == 'b') sb.append('\b');
                    else if (esc == 'f') sb.append('\f');
                    else if (esc == 'n') sb.append('\n');
                    else if (esc == 'r') sb.append('\r');
                    else if (esc == 't') sb.append('\t');
                    else if (esc == 'u' && idx + 4 < len) {
                        try {
                            String hex = src.substring(idx + 1, idx + 5);
                            int code = Integer.parseInt(hex, 16);
                            sb.append((char) code);
                            idx += 4;
                        } catch (Exception e) {
                            sb.append('u');
                        }
                    } else {
                        sb.append(esc);
                    }
                    idx++;
                } else {
                    sb.append(c);
                    idx++;
                }
            }
            return sb.toString();
        }

        private Boolean parseBoolean() {
            if (src.startsWith("true", idx)) {
                idx += 4;
                return Boolean.TRUE;
            } else if (src.startsWith("false", idx)) {
                idx += 5;
                return Boolean.FALSE;
            }
            return null;
        }

        private Object parseNull() {
            if (src.startsWith("null", idx)) {
                idx += 4;
            }
            return null;
        }

        private Number parseNumber() {
            int start = idx;
            if (idx < len && src.charAt(idx) == '-') idx++;
            while (idx < len && Character.isDigit(src.charAt(idx))) idx++;
            boolean isFloat = false;
            if (idx < len && src.charAt(idx) == '.') {
                isFloat = true;
                idx++;
                while (idx < len && Character.isDigit(src.charAt(idx))) idx++;
            }
            if (idx < len && (src.charAt(idx) == 'e' || src.charAt(idx) == 'E')) {
                isFloat = true;
                idx++;
                if (idx < len && (src.charAt(idx) == '+' || src.charAt(idx) == '-')) idx++;
                while (idx < len && Character.isDigit(src.charAt(idx))) idx++;
            }
            String numStr = src.substring(start, idx);
            try {
                if (isFloat) {
                    return Double.parseDouble(numStr);
                } else {
                    long l = Long.parseLong(numStr);
                    if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                        return (int) l;
                    }
                    return l;
                }
            } catch (Exception e) {
                return 0;
            }
        }

        private void skipWhitespace() {
            while (idx < len && Character.isWhitespace(src.charAt(idx))) {
                idx++;
            }
        }
    }
}
