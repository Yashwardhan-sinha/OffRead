package com.offread.app.parser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class NetworkFetcher {

    private static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";
    private static final int TIMEOUT_MS = 15000;
    private static final int MAX_REDIRECTS = 7;
    private static final int MAX_BODY_BYTES = 10 * 1024 * 1024; // 10MB limit

    public static class FetchResult {
        public final String html;
        public final String finalUrl;
        public final int statusCode;
        public final String contentType;

        public FetchResult(String html, String finalUrl, int statusCode, String contentType) {
            this.html = html;
            this.finalUrl = finalUrl;
            this.statusCode = statusCode;
            this.contentType = contentType;
        }
    }

    public static FetchResult fetch(String initialUrl) throws Exception {
        String currentUrl = initialUrl;
        int redirects = 0;

        while (redirects < MAX_REDIRECTS) {
            URL url = new URL(currentUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            conn.setRequestProperty("Sec-Fetch-Dest", "document");
            conn.setRequestProperty("Sec-Fetch-Mode", "navigate");
            conn.setRequestProperty("Sec-Fetch-Site", "none");
            conn.setRequestProperty("Sec-Fetch-User", "?1");
            conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setInstanceFollowRedirects(false); // Manually handle redirects across protocols

            int responseCode = conn.getResponseCode();

            // Handle HTTP 3xx Redirects
            if (responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                    responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                    responseCode == HttpURLConnection.HTTP_SEE_OTHER ||
                    responseCode == 307 || responseCode == 308) {

                String location = conn.getHeaderField("Location");
                conn.disconnect();

                if (location != null && !location.trim().isEmpty()) {
                    currentUrl = TextAnalysisUtil.resolveUrl(currentUrl, location.trim());
                    redirects++;
                    continue;
                }
            }

            if (responseCode >= 400) {
                conn.disconnect();
                throw new Exception("HTTP error " + responseCode + " for " + currentUrl);
            }

            String contentType = conn.getContentType();
            String contentEncoding = conn.getContentEncoding();

            InputStream rawIn = conn.getInputStream();
            InputStream in = rawIn;

            if (contentEncoding != null) {
                String enc = contentEncoding.toLowerCase(Locale.ROOT);
                if (enc.contains("gzip")) {
                    in = new GZIPInputStream(rawIn);
                } else if (enc.contains("deflate")) {
                    in = new InflaterInputStream(rawIn, new Inflater(true));
                }
            }

            byte[] bytes;
            try {
                bytes = readAllBytesWithLimit(in, MAX_BODY_BYTES);
            } finally {
                in.close();
                conn.disconnect();
            }

            Charset charset = detectCharset(contentType, bytes);
            String html = new String(bytes, charset);

            return new FetchResult(html, currentUrl, responseCode, contentType != null ? contentType : "");
        }

        throw new Exception("Too many redirects (exceeded limit of " + MAX_REDIRECTS + ") for " + initialUrl);
    }

    private static byte[] readAllBytesWithLimit(InputStream in, int limit) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int total = 0;
        int nRead;
        while ((nRead = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
            total += nRead;
            if (total > limit) {
                break;
            }
        }
        return buffer.toByteArray();
    }

    private static Charset detectCharset(String contentType, byte[] bytes) {
        // 1. Check HTTP Content-Type header
        if (contentType != null) {
            Pattern p = Pattern.compile("(?i)charset=([a-zA-Z0-9_\\-]+)");
            Matcher m = p.matcher(contentType);
            if (m.find()) {
                try {
                    return Charset.forName(m.group(1));
                } catch (Exception ignored) {
                }
            }
        }

        // 2. Peek into first 2KB for <meta charset="..."> or <meta http-equiv ...>
        if (bytes != null && bytes.length > 0) {
            int peekLen = Math.min(bytes.length, 2048);
            String head = new String(bytes, 0, peekLen, StandardCharsets.ISO_8859_1);

            // <meta charset="UTF-8">
            Pattern p1 = Pattern.compile("(?i)<meta\\s+[^>]*charset=[\"']?([a-zA-Z0-9_\\-]+)");
            Matcher m1 = p1.matcher(head);
            if (m1.find()) {
                try {
                    return Charset.forName(m1.group(1));
                } catch (Exception ignored) {
                }
            }

            // <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
            Pattern p2 = Pattern.compile("(?i)<meta\\s+[^>]*content=[\"'][^\"']*charset=([a-zA-Z0-9_\\-]+)");
            Matcher m2 = p2.matcher(head);
            if (m2.find()) {
                try {
                    return Charset.forName(m2.group(1));
                } catch (Exception ignored) {
                }
            }
        }

        // Default to UTF-8
        return StandardCharsets.UTF_8;
    }
}
