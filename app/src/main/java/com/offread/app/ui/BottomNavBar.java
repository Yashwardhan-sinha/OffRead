package com.offread.app.ui;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.offread.app.R;
import com.offread.app.util.ThemeManager;

/* JADX INFO: loaded from: classes.dex */
public class BottomNavBar extends LinearLayout {
    private static final int[] ICONS = {R.drawable.ic_book, R.drawable.ic_bookmark, R.drawable.ic_settings};
    private static final int[] LABELS = {R.string.nav_home, R.string.nav_bookmarks, R.string.nav_settings};
    private final ImageView[] icons;
    private OnTabSelectedListener listener;
    private int selected;
    private final LinearLayout[] tabs;
    private final TextView[] texts;

    public interface OnTabSelectedListener {
        void onTabSelected(int i);
    }

    public BottomNavBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.tabs = new LinearLayout[3];
        this.icons = new ImageView[3];
        this.texts = new TextView[3];
        build(context);
    }

    public BottomNavBar(Context context) {
        super(context);
        this.tabs = new LinearLayout[3];
        this.icons = new ImageView[3];
        this.texts = new TextView[3];
        build(context);
    }

    private void build(Context context) {
        setOrientation(0);
        setBackgroundColor(ThemeManager.color((Activity) context, R.attr.colorSurface));
        setMinimumHeight(context.getResources().getDimensionPixelSize(R.dimen.nav_bar_height));
        for (int index = 0; index < 3; index++) {
            final int i = index;
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(1);
            linearLayout.setGravity(17);
            linearLayout.setClickable(true);
            linearLayout.setFocusable(true);
            TypedValue typedValue = new TypedValue();
            if (context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)) {
                linearLayout.setBackground(context.getDrawable(typedValue.resourceId));
            }
            ImageView imageView = new ImageView(context);
            imageView.setImageDrawable(context.getDrawable(ICONS[i]));
            int dimensionPixelSize = context.getResources().getDimensionPixelSize(R.dimen.icon_size);
            linearLayout.addView(imageView, new LinearLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize));
            TextView textView = new TextView(context);
            textView.setText(LABELS[i]);
            textView.setTextSize(2, 12.0f);
            textView.setGravity(17);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
            layoutParams.topMargin = dp(context, 2.0f);
            linearLayout.addView(textView, layoutParams);
            linearLayout.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.BottomNavBar.0
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    BottomNavBar.this.lambda$build$0(i, view);
                }
            });
            addView(linearLayout, new LinearLayout.LayoutParams(0, -1, 1.0f));
            this.tabs[i] = linearLayout;
            this.icons[i] = imageView;
            this.texts[i] = textView;
        }
        paintTabs();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$build$0(int i, View view) {
        setSelected(i);
        OnTabSelectedListener onTabSelectedListener = this.listener;
        if (onTabSelectedListener != null) {
            onTabSelectedListener.onTabSelected(i);
        }
    }

    private static int dp(Context context, float f) {
        return (int) TypedValue.applyDimension(1, f, context.getResources().getDisplayMetrics());
    }

    public void setOnTabSelectedListener(OnTabSelectedListener onTabSelectedListener) {
        this.listener = onTabSelectedListener;
    }

    public void setSelected(int i) {
        if (i < 0 || i >= this.tabs.length) {
            i = 0;
        }
        this.selected = i;
        paintTabs();
    }

    public int getSelected() {
        return this.selected;
    }

    private void paintTabs() {
        Context context = getContext();
        if (!(context instanceof Activity)) {
            return;
        }
        Activity activity = (Activity) context;
        int iColor = ThemeManager.color(activity, R.attr.colorIcon);
        int iColor2 = ThemeManager.color(activity, R.attr.colorAccent);
        int i = 0;
        while (true) {
            LinearLayout[] linearLayoutArr = this.tabs;
            int length = linearLayoutArr.length;
            int i2 = this.selected;
            if (i < length) {
                int i3 = i == i2 ? iColor2 : iColor;
                this.icons[i].setColorFilter(i3);
                this.texts[i].setTextColor(i3);
                this.icons[i].setContentDescription(context.getString(LABELS[i]));
                i++;
            } else {
                linearLayoutArr[i2].setContentDescription(context.getString(LABELS[i2]) + ", selected");
                return;
            }
        }
    }
}
