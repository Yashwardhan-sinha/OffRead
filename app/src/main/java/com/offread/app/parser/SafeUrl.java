package com.offread.app.parser;

import java.net.IDN;
import java.net.InetAddress;
import java.net.URI;
import java.util.Locale;

/* JADX INFO: loaded from: classes.dex */
public final class SafeUrl {
    public static final int MAX_URL_LENGTH = 4096;

    private SafeUrl() {
    }

    public static boolean isWellFormedHttpUrl(String str) {
        return parseHost(str) != null;
    }

    public static boolean isSafeFetchTarget(String str) {
        String host = parseHost(str);
        if (host == null) {
            return false;
        }
        return isPublicHost(host);
    }

    public static String normalize(String str) {
        return isWellFormedHttpUrl(str) ? str.trim() : "";
    }

    public static boolean isPublicHost(String str) {
        if (isSafeHostText(str)) {
            return allPublic(addressesFor(str));
        }
        return false;
    }

    public static boolean allPublic(InetAddress[] inetAddressArr) {
        if (inetAddressArr == null || inetAddressArr.length == 0) {
            return false;
        }
        for (InetAddress inetAddress : inetAddressArr) {
            if (inetAddress == null || isBlockedAddress(inetAddress) || isBlockedBytes(inetAddress.getAddress())) {
                return false;
            }
        }
        return true;
    }

    private static InetAddress[] addressesFor(String str) {
        if (isIpv4Text(str)) {
            byte[] ipv4Bytes = parseIpv4Bytes(str);
            if (ipv4Bytes == null) {
                return null;
            }
            return new InetAddress[]{toInetAddress(ipv4Bytes)};
        }
        if (isIpv6Text(str)) {
            return lookUpLiteral(stripIpv6Brackets(str));
        }
        return lookUpAll(str);
    }

    private static String parseHost(String str) {
        String strHostOf;
        if (str == null) {
            return null;
        }
        String strTrim = str.trim();
        if (strTrim.isEmpty() || strTrim.length() > 4096 || hasControlCharacters(strTrim)) {
            return null;
        }
        try {
            URI uri = new URI(strTrim);
            String scheme = uri.getScheme();
            if (scheme == null) {
                return null;
            }
            String lowerCase = scheme.toLowerCase(Locale.ROOT);
            if ((!"http".equals(lowerCase) && !"https".equals(lowerCase)) || uri.getUserInfo() != null || (strHostOf = hostOf(uri)) == null) {
                return null;
            }
            int port = uri.getPort();
            if (port < 0 || (port >= 1 && port <= 65535)) {
                return strHostOf;
            }
            return null;
        } catch (Exception unused) {
            return null;
        }
    }

    private static String hostOf(URI uri) {
        String lowerCase;
        int iLastIndexOf;
        String host = uri.getHost();
        if (host == null) {
            String authority = uri.getAuthority();
            if (authority == null || authority.isEmpty() || authority.indexOf(64) >= 0) {
                return null;
            }
            if (authority.indexOf(91) != 0 && (iLastIndexOf = authority.lastIndexOf(58)) > 0 && isDigits(authority.substring(iLastIndexOf + 1))) {
                authority = authority.substring(0, iLastIndexOf);
            }
            host = authority;
        } else if (host.indexOf(64) >= 0) {
            return null;
        }
        if (host.indexOf(91) == 0) {
            lowerCase = host.toLowerCase(Locale.ROOT);
        } else {
            try {
                lowerCase = IDN.toASCII(host, 2).toLowerCase(Locale.ROOT);
            } catch (Throwable unused) {
                return null;
            }
        }
        if (!(isDigitsAndDots(lowerCase) && parseIpv4Bytes(lowerCase) == null) && isSafeHostText(lowerCase)) {
            return lowerCase;
        }
        return null;
    }

    private static boolean isDigitsAndDots(String str) {
        if (str.isEmpty()) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            char cCharAt = str.charAt(i);
            if (cCharAt != '.' && (cCharAt < '0' || cCharAt > '9')) {
                return false;
            }
        }
        return true;
    }

    private static boolean isDigits(String str) {
        if (str.isEmpty()) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) < '0' || str.charAt(i) > '9') {
                return false;
            }
        }
        return true;
    }

    private static InetAddress[] lookUpAll(String str) {
        try {
            return InetAddress.getAllByName(str);
        } catch (Throwable unused) {
            return null;
        }
    }

    private static InetAddress[] lookUpLiteral(String str) {
        try {
            return new InetAddress[]{InetAddress.getByName(str)};
        } catch (Throwable unused) {
            return null;
        }
    }

    private static boolean isBlockedAddress(InetAddress inetAddress) {
        return inetAddress.isLoopbackAddress() || inetAddress.isLinkLocalAddress() || inetAddress.isSiteLocalAddress() || inetAddress.isAnyLocalAddress() || inetAddress.isMulticastAddress();
    }

    private static boolean isBlockedBytes(byte[] bArr) {
        if (bArr == null) {
            return true;
        }
        if (bArr.length == 16) {
            byte[] bArrEmbeddedIpv4 = embeddedIpv4(bArr);
            if (bArrEmbeddedIpv4 != null) {
                return isBlockedBytes(bArrEmbeddedIpv4);
            }
            return isBlockedIpv6Range(bArr);
        }
        if (bArr.length != 4) {
            return true;
        }
        int i = bArr[0] & 255;
        int i2 = bArr[1] & 255;
        if (i == 0 || i == 10 || i == 127) {
            return true;
        }
        if (i == 169 && i2 == 254) {
            return true;
        }
        if (i == 172 && i2 >= 16 && i2 <= 31) {
            return true;
        }
        if (i == 192 && i2 == 168) {
            return true;
        }
        if (i == 100 && i2 >= 64 && i2 <= 127) {
            return true;
        }
        if (i == 192 && i2 == 0) {
            return true;
        }
        if (i == 198 && (i2 == 18 || i2 == 19)) {
            return true;
        }
        if (i == 198 && i2 == 51) {
            return true;
        }
        return (i == 203 && i2 == 0) || i >= 224;
    }

    private static boolean isBlockedIpv6Range(byte[] bArr) {
        byte b = bArr[0];
        int i = b & 255;
        byte b2 = bArr[1];
        int i2 = b2 & 255;
        if (i == 0 || i == 1 || (b & 254) == 252) {
            return true;
        }
        if ((i == 254 && (b2 & 192) == 128) || i == 255) {
            return true;
        }
        if (i == 63 && i2 == 254) {
            return true;
        }
        if (i == 32 && i2 == 1) {
            int i3 = 255 & bArr[2];
            byte b3 = bArr[3];
            int i4 = b3 & 255;
            if (i3 == 0 && i4 == 0) {
                return true;
            }
            if (i3 == 13 && i4 == 184) {
                return true;
            }
            if (i3 == 0 && (b3 & 240) == 16) {
                return true;
            }
            if (i3 == 0 && i4 == 2 && bArr[4] == 0 && bArr[5] == 0) {
                return true;
            }
        }
        return false;
    }

    private static byte[] embeddedIpv4(byte[] bArr) {
        if (bArr.length != 16) {
            return null;
        }
        int i = bArr[0] & 255;
        int i2 = bArr[1] & 255;
        if (i == 32 && i2 == 2) {
            return new byte[]{bArr[2], bArr[3], bArr[4], bArr[5]};
        }
        if (i == 0 && i2 == 100 && (bArr[2] & 255) == 255 && (bArr[3] & 255) == 155 && isZeroRun(bArr, 4, 12)) {
            return lastFour(bArr);
        }
        if (isZeroRun(bArr, 0, 10)) {
            byte b = bArr[10];
            if (b == -1 && bArr[11] == -1) {
                return lastFour(bArr);
            }
            if (b == 0 && bArr[11] == 0) {
                return lastFour(bArr);
            }
        }
        return null;
    }

    private static byte[] lastFour(byte[] bArr) {
        return new byte[]{bArr[12], bArr[13], bArr[14], bArr[15]};
    }

    private static boolean isZeroRun(byte[] bArr, int i, int i2) {
        while (i < i2) {
            if (bArr[i] != 0) {
                return false;
            }
            i++;
        }
        return true;
    }

    private static InetAddress toInetAddress(byte[] bArr) {
        try {
            return InetAddress.getByAddress(bArr);
        } catch (Throwable unused) {
            return null;
        }
    }

    private static String stripIpv6Brackets(String str) {
        return (str.length() > 2 && str.charAt(0) == '[' && str.charAt(str.length() - 1) == ']') ? str.substring(1, str.length() - 1) : str;
    }

    private static boolean isIpv4Text(String str) {
        for (int i = 0; i < str.length(); i++) {
            char cCharAt = str.charAt(i);
            if (cCharAt != '.' && (cCharAt < '0' || cCharAt > '9')) {
                return false;
            }
        }
        return parseIpv4Bytes(str) != null;
    }

    private static boolean isIpv6Text(String str) {
        String strStripIpv6Brackets = stripIpv6Brackets(str);
        if (strStripIpv6Brackets.indexOf(58) < 0) {
            return false;
        }
        if (str.charAt(0) != '[' || str.charAt(str.length() - 1) == ']') {
            return isIpv6LiteralChars(strStripIpv6Brackets);
        }
        return false;
    }

    private static byte[] parseIpv4Bytes(String str) {
        String[] strArrSplit = str.split("\\.", -1);
        if (strArrSplit.length != 4) {
            return null;
        }
        byte[] bArr = new byte[4];
        for (int i = 0; i < 4; i++) {
            String str2 = strArrSplit[i];
            if (str2.isEmpty() || str2.length() > 3) {
                return null;
            }
            if (str2.length() > 1 && str2.charAt(0) == '0') {
                return null;
            }
            int i2 = 0;
            for (int i3 = 0; i3 < str2.length(); i3++) {
                char cCharAt = str2.charAt(i3);
                if (cCharAt < '0' || cCharAt > '9') {
                    return null;
                }
                i2 = (i2 * 10) + (cCharAt - '0');
            }
            if (i2 > 255) {
                return null;
            }
            bArr[i] = (byte) i2;
        }
        return bArr;
    }

    private static boolean isSafeHostText(String str) {
        if (str == null || str.isEmpty() || str.length() > 253 || hasControlCharacters(str)) {
            return false;
        }
        if (str.indexOf(91) == 0) {
            if (str.indexOf(93) != str.length() - 1) {
                return false;
            }
            String strSubstring = str.substring(1, str.length() - 1);
            return strSubstring.indexOf(91) < 0 && isIpv6LiteralChars(strSubstring);
        }
        if (str.indexOf(93) >= 0) {
            return false;
        }
        if (str.indexOf(58) >= 0) {
            return isIpv6LiteralChars(str);
        }
        for (int i = 0; i < str.length(); i++) {
            char cCharAt = str.charAt(i);
            if ((cCharAt < 'a' || cCharAt > 'z') && ((cCharAt < 'A' || cCharAt > 'Z') && !((cCharAt >= '0' && cCharAt <= '9') || cCharAt == '.' || cCharAt == '-' || cCharAt == '_'))) {
                return false;
            }
        }
        return str.indexOf(46) > 0;
    }

    private static boolean isIpv6LiteralChars(String str) {
        if (str.isEmpty() || str.length() > 45) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            char cCharAt = str.charAt(i);
            if ((cCharAt < '0' || cCharAt > '9') && ((cCharAt < 'a' || cCharAt > 'f') && !((cCharAt >= 'A' && cCharAt <= 'F') || cCharAt == ':' || cCharAt == '.'))) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasControlCharacters(String str) {
        for (int i = 0; i < str.length(); i++) {
            char cCharAt = str.charAt(i);
            if (cCharAt <= ' ' || cCharAt == 127) {
                return true;
            }
        }
        return false;
    }
}
