package com.offread.app.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.offread.app.R;
import com.offread.app.data.Article;

import java.util.ArrayList;
import java.util.List;

public class ArticleAdapter extends BaseAdapter {

    public interface OnArticleActionListener {
        void onArticleClick(Article article);
        void onFavoriteToggle(Article article);
        void onArchiveToggle(Article article);
    }

    private final Context context;
    private final LayoutInflater inflater;
    private List<Article> articles = new ArrayList<>();
    private final OnArticleActionListener listener;

    public ArticleAdapter(Context context, OnArticleActionListener listener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles != null ? articles : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return articles.size();
    }

    @Override
    public Article getItem(int position) {
        return articles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return articles.get(position).getId();
    }

    private static class ViewHolder {
        TextView tvDomain;
        TextView tvReadingTime;
        ImageButton btnFavorite;
        ImageButton btnArchive;
        TextView tvTitle;
        TextView tvExcerpt;
        ProgressBar progressRead;
        TextView tvProgress;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_article, parent, false);
            holder = new ViewHolder();
            holder.tvDomain = convertView.findViewById(R.id.tv_item_domain);
            holder.tvReadingTime = convertView.findViewById(R.id.tv_item_reading_time);
            holder.btnFavorite = convertView.findViewById(R.id.btn_item_favorite);
            holder.btnArchive = convertView.findViewById(R.id.btn_item_archive);
            holder.tvTitle = convertView.findViewById(R.id.tv_item_title);
            holder.tvExcerpt = convertView.findViewById(R.id.tv_item_excerpt);
            holder.progressRead = convertView.findViewById(R.id.progress_item_read);
            holder.tvProgress = convertView.findViewById(R.id.tv_item_progress);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Article article = getItem(position);

        String domain = article.getDomain();
        if (domain == null || domain.isEmpty()) {
            domain = article.getCategory();
        }
        holder.tvDomain.setText(domain);

        int time = article.getReadingTimeMinutes();
        holder.tvReadingTime.setText(time + " min read • " + article.getWordCount() + " words");

        holder.tvTitle.setText(article.getTitle());
        holder.tvExcerpt.setText(article.getExcerpt());

        if (article.isFavorite()) {
            holder.btnFavorite.setImageResource(R.drawable.ic_star_filled);
            holder.btnFavorite.setColorFilter(Color.parseColor("#F59E0B"));
        } else {
            holder.btnFavorite.setImageResource(R.drawable.ic_star);
            holder.btnFavorite.setColorFilter(Color.parseColor("#94A3B8"));
        }

        if (article.isArchived()) {
            holder.btnArchive.setImageResource(R.drawable.ic_unarchive);
            holder.btnArchive.setColorFilter(Color.parseColor("#0D9488"));
        } else {
            holder.btnArchive.setImageResource(R.drawable.ic_archive);
            holder.btnArchive.setColorFilter(Color.parseColor("#94A3B8"));
        }

        int progress = article.getProgressPercent();
        holder.progressRead.setProgress(progress);
        if (article.isArchived() || progress >= 95) {
            holder.tvProgress.setText("Finished");
            holder.tvProgress.setTextColor(Color.parseColor("#10B981"));
        } else if (progress > 0) {
            holder.tvProgress.setText(progress + "% read");
            holder.tvProgress.setTextColor(Color.parseColor("#38BDF8"));
        } else {
            holder.tvProgress.setText("Unread");
            holder.tvProgress.setTextColor(Color.parseColor("#64748B"));
        }

        convertView.setOnClickListener(v -> {
            if (listener != null) listener.onArticleClick(article);
        });

        holder.btnFavorite.setOnClickListener(v -> {
            if (listener != null) listener.onFavoriteToggle(article);
        });

        holder.btnArchive.setOnClickListener(v -> {
            if (listener != null) listener.onArchiveToggle(article);
        });

        return convertView;
    }
}
