package com.offread.app.parser;

import com.offread.app.data.AppPreferences;
import com.offread.app.data.OffReadDbHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/* JADX INFO: loaded from: classes.dex */
public final class AdFilter {
    private static final Set<String> BOILERPLATE_TOKENS = new HashSet(Arrays.asList("ad", "ads", "advert", "adverts", "advertisement", "advertisements", "advertising", "adv", "advertorial", "sponsor", "sponsors", "sponsored", "sponsorship", "promo", "promos", "promotion", "promotions", "promoted", "commercial", "commercials", "monetized", "monetisation", "monetization", "banner", "banners", "leaderboard", "halfpage", "half-page", "megabar", "interstitial", "popunder", "popunderlayer", "popuplayer", "nativead", "adslot", "adunit", "adbox", "adsense", "adsbygoogle", "dfp", "gpt", "gemini", "mrec", "skyainer", "takeover", "netboard", "comment", "comments", "combx", "disqus", "remark", "shoutbox", "community", "sidebar", "nav", "navbar", "navmenu", "menu", "menubar", "navigation", "header", "foot", "footer", "bottombar", "topbar", "masthead", "breadcrumb", "breadcrumbs", "pagination", "pager", "pages", "jump", "share", "sharing", "social", "subscribe", "subscription", "newsletter", "signup", "register", "registration", "paywall", "subscribeunit", "cookie", "cookies", "consent", "gdpr", "privacy", "rgpd", "popup", "popups", "modal", "modals", "overlay", "toast", "interstitialmodal", "tracker", "tracking", "telemetry", "pixel", "pixels", "related", "relatedposts", "recommendation", "recommendations", "recommended", "trending", "mostread", "mostpopular", "outbrain", "taboola", "criteo", "nativo", "widget", "widgets", "rss", "feed", "feeds", "syndication", "advertise", "advertisingpolicy", "editorialpolicy"));
    private static final Set<String> AD_SPECIFIC_TOKENS = new HashSet(Arrays.asList("ad", "ads", "advert", "adverts", "advertisement", "advertisements", "advertising", "advertise", "advertorial", "sponsor", "sponsors", "sponsored", "adsense", "adsbygoogle", "adslot", "adunit", "adbox", "dfp", "gpt", "mrec", "interstitial", "popunder", "monetized", "commercial", "commercials"));
    private static final Set<String> PROTECTED_TOKENS = new HashSet(Arrays.asList("article", "body", OffReadDbHelper.COL_CONTENT, "entry", "main", "post", "story", "prose", "text", "page", "paragraph", "articlebody", "storybody"));
    private static final Set<String> AD_ATTRIBUTE_NAMES = new HashSet(Arrays.asList("data-ad-client", "data-ad-slot", "data-ad-format", "data-ad-width", "data-ad-height", "data-ad-region", "data-ad-unit", "data-ads", "data-adv", "data-advertisement", "data-doubleclick", "data-dfp", "data-gpt", "data-google-query-id", "data-google-ad", "data-widget-id", "data-mobile-ad", "data-sticky-ad", "data-native-ad", "data-sponsored", "data-sponsor", "data-promoted", "data-element-id", "data-legacy-widget-id", "data-ad-test", "data-adbreak"));
    private static final String[] AD_NETWORK_MARKERS = {"adsbygoogle", "googleadservices", "googlesyndication", "pagead", "doubleclick", "googletagservices", "gpt.js", "/dfp/", "dfp-gpt", "amazon-adsystem", "aax-us", "taboola", "outbrain", "criteo", "rubiconproject", "pubmatic", "adnxs", "adsafeprotected", "smartadserver", "adform", "spotxchange", "openx", "yieldmo", "medianet", "sovrn", "zedo", "threepassads", "betweendigital", "yandex-ads", "yads", "vk_ads", "moedendigital", "monetize", "zeotap", "nativo", "adshot", "activeads", "adsterra", "popads", "juicyads", "exoclick", "revcontent", "adstxt", "adserv", "ad-stg", "adn.", "adn.net", "moas", "businessinsider.ads", "/ads/", "?ad=", "adtag", "adtiny", "fling", "mgid", "plista", "vliplatform", "jimads", "revenuehits", "TPNAds", "tpnads", "a-ads", "voiceads", "widespace", "broadsign", "carbonads", "buysellads", "buysellads"};
    private static final Set<String> AD_NOUNS = new HashSet(Arrays.asList("ad", "ads", "advert", "advertisement", "advertisements", "advertising", "advertise", "sponsor", "sponsors", "sponsored", "sponsorship", "promo", "promos", "promotion", "promoted", "commercial", "commercials", "advertorial", "monetized"));
    private static final Set<String> AD_STATUS_WORDS = new HashSet(Arrays.asList("load", "loads", "loading", "loaded", "wait", "waiting", "please", "hold", "buffering", "pending", "initializing", "initialising", "starting", "starts", "ready", "appear", "appears", "appearing", "here", "will", "not", "no", "failed", "failure", "unavailable", "blocked", "disabled", "off", "paused", "stopped", "seconds", "second", "soon", "below", "above", "continues", "displayed", "display", "shown", "show", "playing", "next", "still", "almost"));
    private static final Set<String> AD_FILLER_WORDS = new HashSet(Arrays.asList("a", "an", "the", "this", "that", "is", "are", "was", "were", "be", "been", "your", "our", "my", "its", "of", "in", "on", "for", "to", "and", "or", "it", "we", "you", "us", "with", "by", "at", "as", "from"));
    private static final Set<String> KNOWN_AD_LABELS = new HashSet(Arrays.asList("ad", "ads", "advert", "advertisement", "advertising", "sponsor", "sponsored", "sponsored content", "sponsored stories", "sponsored list stories", "sponsored links", "sponsored placement", "sponsored post", "promotion", "promoted", "promoted content", "commercial content", "promo", "promo placement", "ads by google", "ads by", "google ads", "our ads", "more ads", "related ads", "other ads", "hide ad", "hide this ad", "hide advertisement", "close ad", "close ads", "close advertisement", "skip ad", "skip advertisement", "read the full ad", "read full advertisement", "continue after ad", "your ad here", "your advertisement here", "your ad could be here", "your ad appears here", "ad here", "advertisement here", "ad space", "ad slot", "ad unit", "ad placeholder", "placeholder for advertisement", "placeholder ad", "this ad is loading", "this is an ad", "this is an advertisement", "this was an advertisement", "advertisement placeholder", "ad loading", "advertising loading", "buy our stuff", "buy now", "shop now", "learn more", "ad continues", "advertisement continues", "advertisement continues below", "ad of the day", "paid promotion", "paid advert", "paid advertisement", "partner content", "partner zone", "branded content", "paid content", "from our sponsors", "from our partners", "offers from partners", "below is sponsored content", "top is sponsored content", "disclaimer", "affiliate disclosure", "as an affiliate we earn", "message from advertiser", "spot is reserved for advertisement", "no advertisement available", "ad not available", "advert not available", "adsense", "ad code", "advertisement code", "ad code goes here", "block ads", "ads blocked", "ad blocker detected", "please disable your ad blocker", "support us by disabling ad blocker", "get rid of ads", "remove ads", "go ad free", "subscribe to remove ads", "loading sponsored content", "loading more stories", "loading next story", "sponsored by", "sponsored by us", "sponsored by our partners", "sponsored by our partner", "sponsored by our sponsors", "sponsored by our advertising partners", "presented by our sponsors", "brought to you by our sponsors", "sponsored conversation", "sponsored feature", "sponsored article", "this story is sponsored", "an advertisement", "third party advertising partner", "offers from our partners"));

    private static boolean isSpinnerChar(char c) {
        if (c == ':' || c == ';' || c == '=' || c == '>' || c == '_' || c == '|' || c == '~' || c == 183 || c == 8226 || c == 8231 || c == 8729 || c == 12539) {
            return true;
        }
        switch (c) {
            case '*':
            case '+':
            case ',':
            case '-':
            case '.':
                return true;
            default:
                return false;
        }
    }

    private AdFilter() {
    }

    public static int removeAdElements(HtmlNode htmlNode) {
        int iRemoveAdElements = 0;
        if (htmlNode == null) {
            return 0;
        }
        ArrayList arrayList = null;
        for (HtmlNode htmlNode2 : htmlNode.getChildren()) {
            if (!htmlNode2.isTextNode() && shouldRemoveElement(htmlNode2)) {
                if (arrayList == null) {
                    arrayList = new ArrayList(2);
                }
                arrayList.add(htmlNode2);
            } else {
                iRemoveAdElements += removeAdElements(htmlNode2);
            }
        }
        if (arrayList != null) {
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                htmlNode.removeChild((HtmlNode) it.next());
                iRemoveAdElements++;
            }
        }
        return iRemoveAdElements;
    }

    public static boolean shouldRemoveElement(HtmlNode htmlNode) {
        if (htmlNode != null && !htmlNode.isTextNode()) {
            if (isAdSlotElement(htmlNode)) {
                return true;
            }
            Set<String> set = tokenize(htmlNode.getClassName() + " " + htmlNode.getId());
            if (containsAny(set, AD_SPECIFIC_TOKENS) && !containsAny(set, PROTECTED_TOKENS)) {
                return true;
            }
            if (containsAny(set, BOILERPLATE_TOKENS) && !containsAny(set, PROTECTED_TOKENS)) {
                return isAdLabeled(htmlNode) || hasAdNetworkMarker(htmlNode) || hasEmptyPayload(htmlNode);
            }
            if (isAdLabeled(htmlNode)) {
                return true;
            }
            if (hasEmptyPayload(htmlNode) && isPlaceholderShell(htmlNode)) {
                return true;
            }
        }
        return false;
    }

    public static boolean looksLikeBoilerplate(HtmlNode htmlNode) {
        if (htmlNode == null || htmlNode.isTextNode()) {
            return false;
        }
        if (isAdSlotElement(htmlNode)) {
            return true;
        }
        Set<String> set = tokenize(htmlNode.getClassName() + " " + htmlNode.getId());
        return containsAny(set, BOILERPLATE_TOKENS) || containsAny(set, AD_SPECIFIC_TOKENS);
    }

    public static boolean looksLikeArticleContent(HtmlNode htmlNode) {
        if (htmlNode == null || htmlNode.isTextNode()) {
            return false;
        }
        return containsAny(tokenize(htmlNode.getClassName() + " " + htmlNode.getId()), PROTECTED_TOKENS);
    }

    public static boolean isAdSlotElement(HtmlNode htmlNode) {
        if (htmlNode == null || htmlNode.isTextNode()) {
            return false;
        }
        Iterator<String> it = htmlNode.getAttributes().keySet().iterator();
        while (it.hasNext()) {
            if (AD_ATTRIBUTE_NAMES.contains(it.next())) {
                return true;
            }
        }
        if (hasAdNetworkMarker(htmlNode)) {
            return true;
        }
        String tagName = htmlNode.getTagName();
        String lowerCase = htmlNode.getId().toLowerCase(Locale.ROOT);
        if ("ins".equals(tagName) && (lowerCase.isEmpty() || lowerCase.startsWith("adsbygoogle") || htmlNode.hasAttribute("data-ad-client"))) {
            return containsAny(tokenize(htmlNode.getClassName()), AD_SPECIFIC_TOKENS);
        }
        return lowerCase.startsWith("div-gpt-ad") || lowerCase.startsWith("google_ads") || lowerCase.startsWith("ad-iframes") || lowerCase.startsWith("taboola-") || lowerCase.startsWith("outbrain") || lowerCase.startsWith("criteo") || lowerCase.startsWith("yads") || lowerCase.startsWith("dfp-ad") || lowerCase.startsWith("ad-slot");
    }

    private static boolean hasAdNetworkMarker(HtmlNode htmlNode) {
        Iterator<Map.Entry<String, String>> it = htmlNode.getAttributes().entrySet().iterator();
        while (true) {
            if (!it.hasNext()) {
                return false;
            }
            String value = it.next().getValue();
            if (value != null && !value.isEmpty()) {
                String lowerCase = value.toLowerCase(Locale.ROOT);
                for (String str : AD_NETWORK_MARKERS) {
                    if (lowerCase.contains(str.toLowerCase(Locale.ROOT))) {
                        return true;
                    }
                }
            }
        }
    }

    private static boolean isAdLabeled(HtmlNode htmlNode) {
        String strFirstNonEmpty = firstNonEmpty(htmlNode.getAttribute("aria-label"), htmlNode.getAttribute("aria-roledescription"), htmlNode.getAttribute("data-ad-label"), htmlNode.getAttribute(OffReadDbHelper.COL_TITLE), htmlNode.getAttribute("alt"), htmlNode.getAttribute("data-label"));
        if (strFirstNonEmpty.isEmpty()) {
            return false;
        }
        return containsAny(wordTokens(normalizePhrase(strFirstNonEmpty)), AD_SPECIFIC_TOKENS);
    }

    private static boolean hasEmptyPayload(HtmlNode htmlNode) {
        if (!TextNormalizer.scrubInvisible(htmlNode.getInnerText()).trim().isEmpty()) {
            return false;
        }
        for (HtmlNode htmlNode2 : htmlNode.findAll(new Predicate<HtmlNode>() {
            @Override
            public boolean test(HtmlNode obj) {
                return AdFilter.lambda$hasEmptyPayload$0(obj);
            }
        })) {
            String tagName = htmlNode2.getTagName();
            if ("img".equals(tagName) || "svg".equals(tagName) || "video".equals(tagName) || "audio".equals(tagName) || "canvas".equals(tagName) || "iframe".equals(tagName) || "table".equals(tagName) || "pre".equals(tagName) || "code".equals(tagName) || "hr".equals(tagName) || ("a".equals(tagName) && !htmlNode2.getInnerText().isEmpty())) {
                return false;
            }
        }
        return true;
    }

    static /* synthetic */ boolean lambda$hasEmptyPayload$0(HtmlNode htmlNode) {
        return !htmlNode.isTextNode();
    }

    private static boolean isPlaceholderShell(HtmlNode htmlNode) {
        String tagName = htmlNode.getTagName();
        tagName.hashCode();
        switch (tagName) {
            case "center":
            case "figure":
            case "iframe":
            case "article":
            case "a":
            case "p":
            case "li":
            case "ol":
            case "ul":
            case "div":
            case "ins":
            case "font":
            case "span":
            case "aside":
            case "figcaption":
            case "noscript":
            case "section":
                return true;
            default:
                return false;
        }
    }

    /* JADX WARN: Code duplicated, block: B:23:0x004b  */
    public static boolean isAdPlaceholderText(String str) {
        String strNormalizePhrase = normalizePhrase(str);
        if (strNormalizePhrase.isEmpty() || isSpinnerJunk(strNormalizePhrase)) {
            return false;
        }
        if (KNOWN_AD_LABELS.contains(strNormalizePhrase)) {
            return true;
        }
        String[] strArrSplit = strNormalizePhrase.split(" ");
        if (strArrSplit.length > 10) {
            return false;
        }
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        for (String str2 : strArrSplit) {
            if (!str2.isEmpty() && !AD_FILLER_WORDS.contains(str2)) {
                i++;
                if (AD_NOUNS.contains(str2)) {
                    i2++;
                } else if (AD_STATUS_WORDS.contains(str2)) {
                    i3++;
                } else if (KNOWN_AD_LABELS.contains(str2)) {
                    i2++;
                }
            }
        }
        if (i != 0 && i2 != 0) {
            if (i2 == i) {
                return true;
            }
            if (i3 >= 1 && strArrSplit.length <= 8) {
                return true;
            }
        }
        return false;
    }

    public static boolean mentionsAdvertising(String str) {
        return containsAny(wordTokens(normalizePhrase(str)), AD_NOUNS);
    }

    public static boolean isSeparatorOrSpinner(String str) {
        String strNormalizePhrase = normalizePhrase(str);
        if (strNormalizePhrase.isEmpty()) {
            return true;
        }
        for (int i = 0; i < strNormalizePhrase.length(); i++) {
            if (!isSpinnerChar(strNormalizePhrase.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSpinnerJunk(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!isSpinnerChar(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static String normalizePhrase(String str) {
        if (str == null) {
            return AppPreferences.TTS_LANGUAGE_AUTO;
        }
        String strDecodeHtmlEntities = TextAnalysisUtil.decodeHtmlEntities(str);
        StringBuilder sb = new StringBuilder(strDecodeHtmlEntities.length());
        for (int i = 0; i < strDecodeHtmlEntities.length(); i++) {
            char cCharAt = strDecodeHtmlEntities.charAt(i);
            if (cCharAt != 8203 && cCharAt != 8204 && cCharAt != 8205 && cCharAt != 65279 && cCharAt != 173) {
                if (cCharAt == 160) {
                    cCharAt = ' ';
                }
                sb.append(cCharAt);
            }
        }
        return sb.toString().toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ").replaceAll("^[.·•‧∙・*_=\\-–—~|:,;\\s]+", AppPreferences.TTS_LANGUAGE_AUTO).replaceAll("[.·•‧∙・*_=\\-–—~|:,;\\s]+$", AppPreferences.TTS_LANGUAGE_AUTO).trim();
    }

    private static Set<String> tokenize(String str) {
        HashSet hashSet = new HashSet();
        if (str != null) {
            for (String str2 : str.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
                if (!str2.isEmpty()) {
                    hashSet.add(str2);
                }
            }
        }
        return hashSet;
    }

    private static Set<String> wordTokens(String str) {
        HashSet hashSet = new HashSet();
        if (str != null) {
            for (String str2 : str.split("[^a-z0-9']+")) {
                if (!str2.isEmpty()) {
                    hashSet.add(str2);
                }
            }
        }
        return hashSet;
    }

    private static boolean containsAny(Set<String> set, Set<String> set2) {
        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            if (set2.contains(it.next())) {
                return true;
            }
        }
        return false;
    }

    private static String firstNonEmpty(String... strArr) {
        if (strArr == null) {
            return AppPreferences.TTS_LANGUAGE_AUTO;
        }
        for (String str : strArr) {
            if (str != null && !str.trim().isEmpty()) {
                return str;
            }
        }
        return AppPreferences.TTS_LANGUAGE_AUTO;
    }
}
