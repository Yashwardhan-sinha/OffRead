package com.offread.app.parser;

import com.offread.app.data.AppPreferences;
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

/* JADX INFO: loaded from: classes.dex */
public class NetworkFetcher {
    private static final int MAX_BODY_BYTES = 10485760;
    private static final int MAX_REDIRECTS = 7;
    private static final int TIMEOUT_MS = 15000;
    private static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";

    public static class FetchResult {
        public final String contentType;
        public final String finalUrl;
        public final String html;
        public final int statusCode;

        public FetchResult(String str, String str2, int i, String str3) {
            this.html = str;
            this.finalUrl = str2;
            this.statusCode = i;
            this.contentType = str3;
        }
    }

    public static class BlockedTargetException extends Exception {
        BlockedTargetException() {
            super("Blocked network target");
        }
    }

    public static FetchResult fetch(String str) throws Exception {
        HttpURLConnection httpURLConnection;
        int responseCode;
        requireSafeTarget(str);
        int i = 0;
        while (true) {
            if (i < 7) {
                httpURLConnection = (HttpURLConnection) new URL(str).openConnection();
                httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);
                httpURLConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
                httpURLConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
                httpURLConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
                httpURLConnection.setRequestProperty("Sec-Fetch-Dest", "document");
                httpURLConnection.setRequestProperty("Sec-Fetch-Mode", "navigate");
                httpURLConnection.setRequestProperty("Sec-Fetch-Site", "none");
                httpURLConnection.setRequestProperty("Sec-Fetch-User", "?1");
                httpURLConnection.setRequestProperty("Upgrade-Insecure-Requests", "1");
                httpURLConnection.setConnectTimeout(TIMEOUT_MS);
                httpURLConnection.setReadTimeout(TIMEOUT_MS);
                httpURLConnection.setInstanceFollowRedirects(false);
                responseCode = httpURLConnection.getResponseCode();
                if (responseCode != 301 && responseCode != 302 && responseCode != 303 && responseCode != 307 && responseCode != 308) {
                    break;
                }
                String headerField = httpURLConnection.getHeaderField("Location");
                httpURLConnection.disconnect();
                if (headerField == null || headerField.trim().isEmpty()) {
                    break;
                }
                str = TextAnalysisUtil.resolveUrl(str, headerField.trim());
                requireSafeTarget(str);
                i++;
            } else {
                throw new Exception("Too many redirects (exceeded limit of 7)");
            }
        }
        if (responseCode >= 400) {
            httpURLConnection.disconnect();
            throw new Exception("HTTP error " + responseCode);
        }
        String contentType = httpURLConnection.getContentType();
        String contentEncoding = httpURLConnection.getContentEncoding();
        InputStream inputStream = httpURLConnection.getInputStream();
        if (contentEncoding != null) {
            String lowerCase = contentEncoding.toLowerCase(Locale.ROOT);
            if (lowerCase.contains("gzip")) {
                inputStream = new GZIPInputStream(inputStream);
            } else if (lowerCase.contains("deflate")) {
                inputStream = new InflaterInputStream(inputStream, new Inflater(true));
            }
        }
        try {
            byte[] allBytesWithLimit = readAllBytesWithLimit(inputStream, MAX_BODY_BYTES);
            inputStream.close();
            httpURLConnection.disconnect();
            String str2 = new String(allBytesWithLimit, detectCharset(contentType, allBytesWithLimit));
            if (contentType == null) {
                contentType = AppPreferences.TTS_LANGUAGE_AUTO;
            }
            return new FetchResult(str2, str, responseCode, contentType);
        } catch (Throwable th) {
            inputStream.close();
            httpURLConnection.disconnect();
            throw th;
        }
    }

    private static void requireSafeTarget(String str) throws BlockedTargetException {
        if (!SafeUrl.isSafeFetchTarget(str)) {
            throw new BlockedTargetException();
        }
    }

    private static byte[] readAllBytesWithLimit(InputStream inputStream, int i) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr = new byte[8192];
        int i2 = 0;
        do {
            int i3 = inputStream.read(bArr, 0, 8192);
            if (i3 == -1) {
                break;
            }
            byteArrayOutputStream.write(bArr, 0, i3);
            i2 += i3;
        } while (i2 <= i);
        return byteArrayOutputStream.toByteArray();
    }

    private static Charset detectCharset(String str, byte[] bArr) {
        if (str != null) {
            Matcher matcher = Pattern.compile("(?i)charset=([a-zA-Z0-9_\\-]+)").matcher(str);
            if (matcher.find()) {
                try {
                    return Charset.forName(matcher.group(1));
                } catch (Exception unused) {
                }
            }
        }
        if (bArr != null && bArr.length > 0) {
            String str2 = new String(bArr, 0, Math.min(bArr.length, 2048), StandardCharsets.ISO_8859_1);
            Matcher matcher2 = Pattern.compile("(?i)<meta\\s+[^>]*charset=[\"']?([a-zA-Z0-9_\\-]+)").matcher(str2);
            if (matcher2.find()) {
                try {
                    return Charset.forName(matcher2.group(1));
                } catch (Exception unused2) {
                }
            }
            Matcher matcher3 = Pattern.compile("(?i)<meta\\s+[^>]*content=[\"'][^\"']*charset=([a-zA-Z0-9_\\-]+)").matcher(str2);
            if (matcher3.find()) {
                try {
                    return Charset.forName(matcher3.group(1));
                } catch (Exception unused3) {
                }
            }
        }
        return StandardCharsets.UTF_8;
    }
}
