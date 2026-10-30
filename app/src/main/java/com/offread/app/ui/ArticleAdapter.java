package com.offread.app.ui;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.offread.app.R;
import com.offread.app.data.Article;
import com.offread.app.util.ThemeManager;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class ArticleAdapter extends BaseAdapter {
    private List<Article> articles = new ArrayList();
    private final Context context;
    private final LayoutInflater inflater;
    private final OnArticleActionListener listener;

    public interface OnArticleActionListener {
        void onArchiveToggle(Article article);

        void onArticleClick(Article article);

        void onBookmarkToggle(Article article);
    }

    public ArticleAdapter(Context context, OnArticleActionListener onArticleActionListener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.listener = onArticleActionListener;
    }

    public void setArticles(List<Article> list) {
        if (list == null) {
            list = new ArrayList<>();
        }
        this.articles = list;
        notifyDataSetChanged();
    }

    public List<Article> getArticles() {
        return this.articles;
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.articles.size();
    }

    @Override // android.widget.Adapter
    public Article getItem(int i) {
        return this.articles.get(i);
    }

    @Override // android.widget.Adapter
    public long getItemId(int i) {
        return this.articles.get(i).getId();
    }

    private static class ViewHolder {
        ImageButton archive;
        ImageButton bookmark;
        ImageView cover;
        TextView domain;
        TextView excerpt;
        ProgressBar progress;
        TextView progressLabel;
        View progressRow;
        TextView readingTime;
        TextView title;

        private ViewHolder() {
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // android.widget.Adapter
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = this.inflater.inflate(R.layout.item_article, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.cover = (ImageView) view.findViewById(R.id.img_article_cover);
            viewHolder.domain = (TextView) view.findViewById(R.id.tv_item_domain);
            viewHolder.readingTime = (TextView) view.findViewById(R.id.tv_item_reading_time);
            viewHolder.title = (TextView) view.findViewById(R.id.tv_item_title);
            viewHolder.excerpt = (TextView) view.findViewById(R.id.tv_item_excerpt);
            viewHolder.progressLabel = (TextView) view.findViewById(R.id.tv_item_progress);
            viewHolder.progress = (ProgressBar) view.findViewById(R.id.progress_item_read);
            viewHolder.progressRow = view.findViewById(R.id.row_item_progress);
            viewHolder.bookmark = (ImageButton) view.findViewById(R.id.btn_item_favorite);
            viewHolder.archive = (ImageButton) view.findViewById(R.id.btn_item_archive);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        final Article item = getItem(i);
        Context context = this.context;
        Activity activity = context instanceof Activity ? (Activity) context : null;
        int iColor = ThemeManager.color(activity, R.attr.colorIcon);
        int iColor2 = ThemeManager.color(activity, R.attr.colorAccent);
        int iColor3 = ThemeManager.color(activity, R.attr.colorSuccess);
        int iColor4 = ThemeManager.color(activity, R.attr.colorTextMuted);
        String domain = item.getDomain();
        if (domain.isEmpty()) {
            domain = item.getCategory();
        }
        viewHolder.domain.setText(domain);
        String str = item.getReadingTimeMinutes() + " min • " + item.getWordCount() + " words";
        String str2 = ReadingDateFormatter.format(this.context, item.getLastReadAt());
        if (!str2.isEmpty()) {
            str = str + " • " + str2;
        }
        viewHolder.readingTime.setText(str);
        viewHolder.title.setText(item.getTitle());
        String excerpt = item.getExcerpt();
        viewHolder.excerpt.setText(excerpt);
        viewHolder.excerpt.setVisibility(excerpt.isEmpty() ? 8 : 0);
        RemoteImageLoader.into(viewHolder.cover, item.getImageUrl());
        viewHolder.bookmark.setImageResource(item.isFavorite() ? R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark);
        viewHolder.bookmark.setColorFilter(item.isFavorite() ? iColor2 : iColor);
        viewHolder.bookmark.setContentDescription(this.context.getString(item.isFavorite() ? R.string.action_unbookmark : R.string.action_bookmark));
        viewHolder.archive.setImageResource(item.isArchived() ? R.drawable.ic_unarchive : R.drawable.ic_archive);
        ImageButton imageButton = viewHolder.archive;
        if (item.isArchived()) {
            iColor = iColor2;
        }
        imageButton.setColorFilter(iColor);
        viewHolder.archive.setContentDescription(this.context.getString(item.isArchived() ? R.string.action_unarchive : R.string.action_archive));
        int progressPercent = item.getProgressPercent();
        viewHolder.progress.setProgress(progressPercent);
        if (item.isArchived() || progressPercent >= 95) {
            viewHolder.progressLabel.setText(R.string.progress_finished);
            viewHolder.progressLabel.setTextColor(iColor3);
        } else if (progressPercent > 0) {
            viewHolder.progressLabel.setText(this.context.getString(R.string.progress_percent, Integer.valueOf(progressPercent)));
            viewHolder.progressLabel.setTextColor(iColor4);
        } else {
            viewHolder.progressLabel.setText(R.string.progress_unread);
            viewHolder.progressLabel.setTextColor(iColor4);
        }
        viewHolder.progressRow.setVisibility(progressPercent <= 0 ? 8 : 0);
        view.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.ArticleAdapter.0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                ArticleAdapter.this.lambda$getView$0(item, view2);
            }
        });
        viewHolder.bookmark.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.ArticleAdapter.1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                ArticleAdapter.this.lambda$getView$1(item, view2);
            }
        });
        viewHolder.archive.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.ArticleAdapter.2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                ArticleAdapter.this.lambda$getView$2(item, view2);
            }
        });
        return view;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$getView$0(Article article, View view) {
        OnArticleActionListener onArticleActionListener = this.listener;
        if (onArticleActionListener != null) {
            onArticleActionListener.onArticleClick(article);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$getView$1(Article article, View view) {
        OnArticleActionListener onArticleActionListener = this.listener;
        if (onArticleActionListener != null) {
            onArticleActionListener.onBookmarkToggle(article);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$getView$2(Article article, View view) {
        OnArticleActionListener onArticleActionListener = this.listener;
        if (onArticleActionListener != null) {
            onArticleActionListener.onArchiveToggle(article);
        }
    }
}
