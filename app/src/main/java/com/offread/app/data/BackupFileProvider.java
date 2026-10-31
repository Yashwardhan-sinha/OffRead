package com.offread.app.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.regex.Pattern;

/* JADX INFO: loaded from: classes.dex */
public class BackupFileProvider extends ContentProvider {
    public static final String AUTHORITY = "com.offread.app.backup";
    private static final Pattern BACKUP_FILE_NAME = Pattern.compile("offread_backup_[0-9]{1,20}\\.json");
    public static final String PATH_SEGMENT = "backup";
    private Context appContext;

    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        return null;
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        this.appContext = getContext() != null ? getContext().getApplicationContext() : null;
        return true;
    }

    @Override // android.content.ContentProvider
    public ParcelFileDescriptor openFile(Uri uri, String str) throws FileNotFoundException {
        if (uri == null || !"r".equals(str)) {
            throw new FileNotFoundException("OffRead backups are shared read only");
        }
        if (!AUTHORITY.equals(uri.getAuthority())) {
            throw new FileNotFoundException("Unknown authority");
        }
        List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() == 2) {
            if (PATH_SEGMENT.equals(pathSegments.get(0))) {
                String str2 = pathSegments.get(1);
                if (!BACKUP_FILE_NAME.matcher(str2).matches()) {
                    throw new FileNotFoundException("Unknown backup name");
                }
                Context context = this.appContext;
                if (context == null) {
                    context = getContext();
                }
                if (context == null) {
                    throw new FileNotFoundException("Provider has no context");
                }
                for (File file : roots(context)) {
                    File fileInsideRoot = insideRoot(file, str2);
                    if (fileInsideRoot != null) {
                        return ParcelFileDescriptor.open(fileInsideRoot, ParcelFileDescriptor.MODE_READ_ONLY);
                    }
                }
                throw new FileNotFoundException("No such backup");
            }
        }
        throw new FileNotFoundException("Unknown backup address");
    }

    private static File insideRoot(File file, String str) {
        try {
            String canonicalPath = file.getCanonicalPath();
            File file2 = new File(file, str);
            if (!file2.isFile()) {
                return null;
            }
            String canonicalPath2 = file2.getCanonicalPath();
            if (canonicalPath2.startsWith(canonicalPath + File.separator) && canonicalPath2.substring(canonicalPath.length() + 1).equals(str)) {
                return file2;
            }
            return null;
        } catch (Throwable unused) {
            return null;
        }
    }

    static File[] roots(Context context) {
        return new File[]{context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), context.getFilesDir()};
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {
        return "application/json";
    }

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues contentValues) {
        throw new UnsupportedOperationException("OffRead backups cannot be written through this provider");
    }

    @Override // android.content.ContentProvider
    public int delete(Uri uri, String str, String[] strArr) {
        throw new UnsupportedOperationException("OffRead backups cannot be deleted through this provider");
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        throw new UnsupportedOperationException("OffRead backups cannot be changed through this provider");
    }
}
