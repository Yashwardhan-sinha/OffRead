package com.offread.app.util;

import com.offread.app.data.Article;
import com.offread.app.parser.SafeUrl;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes.dex */
public class BackupHelper {
    public static final long MAX_IMPORT_BYTES = 8388608;
    public static final int MAX_IMPORT_DEPTH = 64;
    public static final int MAX_IMPORT_RECORDS = 20000;

    public static String exportToJson(List<Article> list) throws JSONException {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("version", 1);
        jSONObject.put("exportedAt", System.currentTimeMillis());
        jSONObject.put("app", "OffRead");
        JSONArray jSONArray = new JSONArray();
        Iterator<Article> it = list.iterator();
        while (it.hasNext()) {
            jSONArray.put(it.next().toJson());
        }
        jSONObject.put("articles", jSONArray);
        return jSONObject.toString(2);
    }

    public static List<Article> importFromJson(String str) throws JSONException {
        if (str == null) {
            throw new JSONException("Empty backup");
        }
        if (str.length() > MAX_IMPORT_BYTES) {
            throw new JSONException("Backup is too large");
        }
        if (exceedsImportDepth(str)) {
            throw new JSONException("Backup nesting is too deep");
        }
        JSONArray jSONArray = new JSONObject(str).getJSONArray("articles");
        int iMin = Math.min(jSONArray.length(), MAX_IMPORT_RECORDS);
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < iMin; i++) {
            Article articleFromJson = Article.fromJson(jSONArray.getJSONObject(i));
            if (isAcceptableImportUrl(articleFromJson.getUrl())) {
                articleFromJson.setUrl(articleFromJson.getUrl().trim());
                articleFromJson.setCanonicalUrl(sanitizeOptionalUrl(articleFromJson.getCanonicalUrl()));
                articleFromJson.setImageUrl(sanitizeOptionalUrl(articleFromJson.getImageUrl()));
                articleFromJson.setId(0L);
                arrayList.add(articleFromJson);
            }
        }
        return arrayList;
    }

    public static boolean isAcceptableImportUrl(String str) {
        if (str == null) {
            return true;
        }
        String strTrim = str.trim();
        return strTrim.isEmpty() || SafeUrl.isWellFormedHttpUrl(strTrim);
    }

    public static String sanitizeOptionalUrl(String str) {
        if (str == null) {
            return "";
        }
        String strTrim = str.trim();
        return (!strTrim.isEmpty() && SafeUrl.isWellFormedHttpUrl(strTrim)) ? strTrim : "";
    }

    public static boolean exceedsImportDepth(String str) {
        if (str == null) {
            return false;
        }
        boolean z = false;
        boolean z2 = false;
        int i = 0;
        for (int i2 = 0; i2 < str.length(); i2++) {
            char cCharAt = str.charAt(i2);
            if (z) {
                if (z2) {
                    z2 = false;
                } else if (cCharAt == '\\') {
                    z2 = true;
                } else if (cCharAt == '\"') {
                    z = false;
                }
            } else if (cCharAt == '\"') {
                z = true;
            } else if (cCharAt == '{' || cCharAt == '[') {
                i++;
                if (i > 64) {
                    return true;
                }
            } else if ((cCharAt == '}' || cCharAt == ']') && i > 0) {
                i--;
            }
        }
        return false;
    }

    public static File writeBackupToFile(File file, String str) throws Exception {
        if (!file.exists()) {
            file.mkdirs();
        }
        File file2 = new File(file, "offread_backup_" + System.currentTimeMillis() + ".json");
        FileOutputStream fileOutputStream = new FileOutputStream(file2);
        try {
            fileOutputStream.write(str.getBytes(StandardCharsets.UTF_8));
            fileOutputStream.close();
            return file2;
        } catch (Throwable th) {
            try {
                fileOutputStream.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
            throw th;
        }
    }

    public static String readBackupFromFile(File file) throws Exception {
        long length = file.length();
        if (length > MAX_IMPORT_BYTES) {
            throw new JSONException("Backup file is too large");
        }
        byte[] bArr = new byte[8192];
        long j = 0;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(length > 0 ? (int) length : 8192);
        FileInputStream fileInputStream = new FileInputStream(file);
        while (true) {
            try {
                int i = fileInputStream.read(bArr);
                if (i == -1) {
                    fileInputStream.close();
                    return new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
                }
                j += (long) i;
                if (j > MAX_IMPORT_BYTES) {
                    throw new JSONException("Backup file is too large");
                }
                byteArrayOutputStream.write(bArr, 0, i);
            } catch (Throwable th) {
                try {
                    fileInputStream.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        }
    }
}
