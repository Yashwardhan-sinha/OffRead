package com.offread.app.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;
import com.offread.app.R;
import com.offread.app.data.AppPreferences;
import com.offread.app.parser.SafeUrl;
import com.offread.app.parser.TextAnalysisUtil;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* JADX INFO: loaded from: classes.dex */
public final class RemoteImageLoader {
    private static final int CONNECT_TIMEOUT_MS = 8000;
    private static final int MAX_DECODE_EDGE = 1080;
    private static final int MAX_IMAGE_BYTES = 6291456;
    private static final int MAX_REDIRECTS = 5;
    private static final int READ_TIMEOUT_MS = 8000;
    private static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final ExecutorService POOL = Executors.newFixedThreadPool(3);
    private static final LruCache<String, Bitmap> CACHE = new LruCache<String, Bitmap>(Math.max(4, (int) ((Runtime.getRuntime().maxMemory() / 1024) / 8))) { // from class: com.offread.app.ui.RemoteImageLoader.1
        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.util.LruCache
        public int sizeOf(String str, Bitmap bitmap) {
            return bitmap.getByteCount() / 1024;
        }
    };

    private RemoteImageLoader() {
    }

    public static void into(ImageView imageView, String str) {
        if (imageView == null) {
            return;
        }
        final String strTrim = str == null ? AppPreferences.TTS_LANGUAGE_AUTO : str.trim();
        imageView.setTag(strTrim);
        if (strTrim.isEmpty() || !SafeUrl.isWellFormedHttpUrl(strTrim)) {
            imageView.setVisibility(8);
            imageView.setImageDrawable(null);
            return;
        }
        Bitmap bitmap = CACHE.get(strTrim);
        if (bitmap != null) {
            show(imageView, strTrim, bitmap);
        } else {
            final WeakReference weakReference = new WeakReference(imageView);
            POOL.execute(new Runnable() { // from class: com.offread.app.ui.RemoteImageLoader.0
                @Override // java.lang.Runnable
                public final void run() {
                    RemoteImageLoader.lambda$into$1(strTrim, weakReference);
                }
            });
        }
    }

    static /* synthetic */ void lambda$into$1(final String str, final WeakReference weakReference) {
        final Bitmap bitmapDecodeScaled = decodeScaled(str);
        if (bitmapDecodeScaled != null) {
            CACHE.put(str, bitmapDecodeScaled);
        }
        MAIN.post(new Runnable() { // from class: com.offread.app.ui.RemoteImageLoader.2
            @Override // java.lang.Runnable
            public final void run() {
                RemoteImageLoader.lambda$into$0(weakReference, str, bitmapDecodeScaled);
            }
        });
    }

    static /* synthetic */ void lambda$into$0(WeakReference weakReference, String str, Bitmap bitmap) {
        Object tag;
        ImageView imageView = (ImageView) weakReference.get();
        if (imageView == null || (tag = imageView.getTag()) == null || !str.equals(tag)) {
            return;
        }
        if (bitmap == null || bitmap.isRecycled()) {
            imageView.setVisibility(8);
        } else {
            show(imageView, str, bitmap);
        }
    }

    private static void show(ImageView imageView, String str, Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
        imageView.setContentDescription(alternateText(imageView.getContext(), str));
        imageView.setVisibility(0);
    }

    private static String alternateText(Context context, String str) {
        int i;
        int iLastIndexOf = str.lastIndexOf(47);
        if (iLastIndexOf >= 0 && (i = iLastIndexOf + 1) < str.length()) {
            str = str.substring(i);
        }
        int iLastIndexOf2 = str.lastIndexOf(46);
        if (iLastIndexOf2 > 0) {
            str = str.substring(0, iLastIndexOf2);
        }
        return context.getString(R.string.image_alt_prefix, str);
    }

    private static Bitmap decodeScaled(String str) {
        byte[] bArrDownload = download(str);
        if (bArrDownload != null && bArrDownload.length != 0) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            try {
                BitmapFactory.decodeByteArray(bArrDownload, 0, bArrDownload.length, options);
                if (options.outWidth > 0 && options.outHeight > 0) {
                    BitmapFactory.Options options2 = new BitmapFactory.Options();
                    options2.inSampleSize = sampleSizeFor(options.outWidth, options.outHeight);
                    return BitmapFactory.decodeByteArray(bArrDownload, 0, bArrDownload.length, options2);
                }
            } catch (Throwable unused) {
            }
        }
        return null;
    }

    private static byte[] download(String url) {
        for (int redirects = 0; redirects <= 5; redirects++) {
            if (!SafeUrl.isSafeFetchTarget(url)) return null;
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setInstanceFollowRedirects(false);
                connection.setRequestProperty("User-Agent", USER_AGENT);
                connection.setRequestProperty("Accept", "image/avif,image/webp,image/png,image/jpeg,image/gif,*/*;q=0.5");
                int responseCode = connection.getResponseCode();
                if (responseCode == 301 || responseCode == 302 || responseCode == 303 || responseCode == 307 || responseCode == 308) {
                    String location = connection.getHeaderField("Location");
                    if (location == null || location.trim().isEmpty()) return null;
                    String nextUrl = TextAnalysisUtil.resolveUrl(url, location.trim());
                    if (!SafeUrl.isSafeFetchTarget(nextUrl)) return null;
                    url = nextUrl;
                    continue;
                }
                if (responseCode < 200 || responseCode >= 300 || !isImageContentType(connection.getContentType())) return null;
                return readCapped(connection.getInputStream(), MAX_IMAGE_BYTES);
            } catch (Throwable ignored) {
                return null;
            } finally {
                if (connection != null) connection.disconnect();
            }
        }
        return null;
    }

    private static boolean isImageContentType(String str) {
        if (str == null || str.trim().isEmpty()) {
            return true;
        }
        return str.toLowerCase(Locale.ROOT).trim().startsWith("image/");
    }

    private static byte[] readCapped(InputStream inputStream, int i) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(Math.min(i, 65536));
        byte[] bArr = new byte[8192];
        int i2 = 0;
        while (true) {
            int i3 = inputStream.read(bArr, 0, 8192);
            if (i3 == -1) {
                return byteArrayOutputStream.toByteArray();
            }
            i2 += i3;
            if (i2 > i) {
                return null;
            }
            byteArrayOutputStream.write(bArr, 0, i3);
        }
    }

    private static int sampleSizeFor(int i, int i2) {
        int i3 = 1;
        do {
            int i4 = i / i3;
            if ((i4 <= MAX_DECODE_EDGE && i2 / i3 <= MAX_DECODE_EDGE) || i4 <= 0 || i2 / i3 <= 0) {
                break;
            }
            i3 *= 2;
        } while (i3 <= 32);
        return i3;
    }
}
