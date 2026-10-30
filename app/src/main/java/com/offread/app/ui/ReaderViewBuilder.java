package com.offread.app.ui;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.offread.app.R;
import com.offread.app.data.AppPreferences;
import com.offread.app.parser.ArticleContentBlocks;
import com.offread.app.parser.SafeUrl;
import com.offread.app.parser.TextAnalysisUtil;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public final class ReaderViewBuilder {
    private static final float CODE_SCALE = 0.88f;
    private static final float H1_SCALE = 1.42f;
    private static final float H2_SCALE = 1.22f;
    private static final float H3_SCALE = 1.08f;
    private static final int MAX_RENDERED_BLOCKS = 600;
    private static final float QUOTE_SCALE = 0.98f;

    public static final class Style {
        public int accentColor;
        public float bodySp;
        public int dividerColor;
        public float headingGapAfter;
        public float headingGapBefore;
        public float headingLineHeight;
        public float imageGap;
        public float imageMaxHeight;
        public int inkColor;
        public float lineHeight;
        public float listGap;
        public float miniHeadingGapAfter;
        public float miniHeadingGapBefore;
        public int mutedColor;
        public float paragraphGap;
        public float quoteGap;
        public int quoteTextColor;
        public float ruleGap;
        public float subheadingGapAfter;
        public float subheadingGapBefore;
        public Typeface typeface;
    }

    private ReaderViewBuilder() {
    }

    public static int render(LinearLayout linearLayout, List<ArticleContentBlocks.Block> list, Style style) {
        int i = 0;
        if (linearLayout == null) {
            return 0;
        }
        linearLayout.removeAllViews();
        if (list != null && !list.isEmpty()) {
            for (ArticleContentBlocks.Block block : list) {
                if (i >= MAX_RENDERED_BLOCKS) {
                    break;
                }
                View viewCreateView = createView(linearLayout, block, style);
                if (viewCreateView != null) {
                    linearLayout.addView(viewCreateView);
                    i++;
                }
            }
        }
        return i;
    }

    private static View createView(LinearLayout linearLayout, ArticleContentBlocks.Block block, Style style) {
        switch (block.type) {
            case 1:
                return textBlock(linearLayout, block.plainText(), style.bodySp * H1_SCALE, bold(style.typeface), style.inkColor, style.headingLineHeight, style.headingGapBefore, style.headingGapAfter);
            case 2:
                return textBlock(linearLayout, block.plainText(), style.bodySp * H2_SCALE, bold(style.typeface), style.inkColor, style.headingLineHeight, style.subheadingGapBefore, style.subheadingGapAfter);
            case 3:
                return textBlock(linearLayout, block.plainText(), style.bodySp * H3_SCALE, bold(style.typeface), style.inkColor, style.headingLineHeight, style.miniHeadingGapBefore, style.miniHeadingGapAfter);
            case ArticleContentBlocks.TYPE_LIST_ITEM /* 4 */:
                return textBlock(linearLayout, "•   " + block.plainText(), style.bodySp, style.typeface, style.inkColor, style.lineHeight, 0.0f, style.listGap);
            case ArticleContentBlocks.TYPE_QUOTE /* 5 */:
                return quotedBlock(linearLayout, block.plainText(), style);
            case ArticleContentBlocks.TYPE_CODE /* 6 */:
                return codeBlock(linearLayout, block.text, style);
            case ArticleContentBlocks.TYPE_IMAGE /* 7 */:
                return imageBlock(linearLayout, block, style);
            case ArticleContentBlocks.TYPE_RULE /* 8 */:
                return ruleBlock(linearLayout, style);
            default:
                return textBlock(linearLayout, richInline(block.text, style), style.bodySp, style.typeface, style.inkColor, style.lineHeight, 0.0f, style.paragraphGap);
        }
    }

    private static View textBlock(LinearLayout linearLayout, CharSequence charSequence, float f, Typeface typeface, int i, float f2, float f3, float f4) {
        TextView textView = new TextView(linearLayout.getContext());
        textView.setText(charSequence);
        textView.setTypeface(typeface);
        textView.setTextColor(i);
        textView.setTextSize(2, f);
        textView.setLineSpacing(0.0f, f2);
        textView.setTextIsSelectable(false);
        textView.setIncludeFontPadding(false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.topMargin = (int) f3;
        layoutParams.bottomMargin = (int) f4;
        textView.setLayoutParams(layoutParams);
        return textView;
    }

    private static View quotedBlock(LinearLayout linearLayout, String str, Style style) {
        TextView textView = new TextView(linearLayout.getContext());
        textView.setText(richInline(str, style));
        textView.setTypeface(style.typeface, 2);
        textView.setTextColor(style.quoteTextColor);
        textView.setTextSize(2, style.bodySp * QUOTE_SCALE);
        textView.setLineSpacing(0.0f, style.lineHeight);
        textView.setIncludeFontPadding(false);
        textView.setBackgroundResource(R.drawable.bg_quote_block);
        int i = (int) style.imageGap;
        int i2 = i / 2;
        textView.setPadding(i, i2, i, i2);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.topMargin = (int) style.quoteGap;
        layoutParams.bottomMargin = (int) style.quoteGap;
        layoutParams.setMarginStart((int) style.listGap);
        textView.setLayoutParams(layoutParams);
        return textView;
    }

    private static View codeBlock(LinearLayout linearLayout, String str, Style style) {
        TextView textView = new TextView(linearLayout.getContext());
        textView.setText(stripFenceNoise(str));
        textView.setTypeface(Typeface.MONOSPACE);
        textView.setTextColor(style.inkColor);
        textView.setTextSize(2, style.bodySp * CODE_SCALE);
        textView.setLineSpacing(0.0f, 1.4f);
        textView.setBackgroundResource(R.drawable.bg_code_block);
        textView.setTextIsSelectable(true);
        int i = (int) (style.listGap * 2.0f);
        textView.setPadding(i, i, i, i);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.topMargin = (int) style.quoteGap;
        layoutParams.bottomMargin = (int) style.quoteGap;
        textView.setLayoutParams(layoutParams);
        return textView;
    }

    private static View imageBlock(LinearLayout linearLayout, ArticleContentBlocks.Block block, Style style) {
        ImageView imageView = new ImageView(linearLayout.getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.topMargin = (int) style.imageGap;
        layoutParams.bottomMargin = (int) style.imageGap;
        imageView.setLayoutParams(layoutParams);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setMaxHeight((int) style.imageMaxHeight);
        imageView.setBackgroundResource(R.drawable.bg_image_frame);
        imageView.setVisibility(8);
        String str = block.imageUrl;
        if (str == null || !SafeUrl.isWellFormedHttpUrl(str)) {
            return null;
        }
        RemoteImageLoader.into(imageView, str);
        return imageView;
    }

    private static View ruleBlock(LinearLayout linearLayout, Style style) {
        View view = new View(linearLayout.getContext());
        view.setBackgroundColor(style.dividerColor);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, (int) dp(linearLayout, 1.0f));
        layoutParams.topMargin = (int) style.ruleGap;
        layoutParams.bottomMargin = (int) style.ruleGap;
        layoutParams.setMarginStart((int) style.ruleGap);
        layoutParams.setMarginEnd((int) style.ruleGap);
        view.setLayoutParams(layoutParams);
        return view;
    }

    private static float dp(LinearLayout linearLayout, float f) {
        return TypedValue.applyDimension(1, f, linearLayout.getContext().getResources().getDisplayMetrics());
    }

    private static Typeface bold(Typeface typeface) {
        if (typeface == null) {
            typeface = Typeface.DEFAULT;
        }
        return Typeface.create(typeface, 1);
    }

    private static String stripFenceNoise(String str) {
        if (str == null) {
            return AppPreferences.TTS_LANGUAGE_AUTO;
        }
        return str.replaceAll("\n{3,}", "\n\n").trim().replace("\t", "    ");
    }

    public static CharSequence richInline(String str, Style style) {
        String source = str == null ? AppPreferences.TTS_LANGUAGE_AUTO : TextAnalysisUtil.decodeHtmlEntities(str);
        SpannableStringBuilder output = new SpannableStringBuilder();
        int index = 0;
        while (index < source.length()) {
            if (source.startsWith("**", index)) {
                int end = source.indexOf("**", index + 2);
                if (end > index + 2) {
                    appendStyled(output, source.substring(index + 2, end), 1);
                    index = end + 2;
                    continue;
                }
            }
            char current = source.charAt(index);
            if (current == '*' && index + 1 < source.length() && source.charAt(index + 1) != '*') {
                int end = source.indexOf('*', index + 1);
                if (end > index + 1) {
                    appendStyled(output, source.substring(index + 1, end), 2);
                    index = end + 1;
                    continue;
                }
            }
            if (current == '`') {
                int end = source.indexOf('`', index + 1);
                if (end > index + 1) {
                    int start = output.length();
                    output.append((CharSequence) source.substring(index + 1, end));
                    output.setSpan(new TypefaceSpan("monospace"), start, output.length(), 33);
                    if (style != null) output.setSpan(new ForegroundColorSpan(style.mutedColor), start, output.length(), 33);
                    index = end + 1;
                    continue;
                }
            }
            boolean image = current == '!' && index + 1 < source.length() && source.charAt(index + 1) == '[';
            if (image || current == '[') {
                int labelStart = index + (image ? 2 : 1);
                int marker = source.indexOf("](", labelStart);
                int end = marker < 0 ? -1 : source.indexOf(')', marker + 2);
                if (marker > labelStart && end > marker) {
                    int start = output.length();
                    output.append((CharSequence) source.substring(labelStart, marker));
                    if (!image && style != null) output.setSpan(new ForegroundColorSpan(style.accentColor), start, output.length(), 33);
                    index = end + 1;
                    continue;
                }
            }
            output.append(current);
            index++;
        }
        return output;
    }

    private static void appendStyled(SpannableStringBuilder spannableStringBuilder, String str, int i) {
        int length = spannableStringBuilder.length();
        spannableStringBuilder.append((CharSequence) str);
        spannableStringBuilder.setSpan(new StyleSpan(i), length, spannableStringBuilder.length(), 33);
    }
}
