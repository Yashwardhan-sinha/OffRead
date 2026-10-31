package com.offread.app.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.offread.app.R;
import com.offread.app.data.Article;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class BookmarkAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private List<Article> items = new ArrayList();
    private final OnBookmarkActionListener listener;

    public interface OnBookmarkActionListener {
        void onBookmarkClick(Article article);

        void onBookmarkRemove(Article article);
    }

    public BookmarkAdapter(Context context, OnBookmarkActionListener onBookmarkActionListener) {
        this.inflater = LayoutInflater.from(context);
        this.listener = onBookmarkActionListener;
    }

    public void setItems(List<Article> list) {
        if (list == null) {
            list = new ArrayList<>();
        }
        this.items = list;
        notifyDataSetChanged();
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.items.size();
    }

    @Override // android.widget.Adapter
    public Article getItem(int i) {
        return this.items.get(i);
    }

    @Override // android.widget.Adapter
    public long getItemId(int i) {
        return this.items.get(i).getId();
    }

    private static class ViewHolder {
        ImageView cover;
        TextView date;
        ImageButton remove;
        TextView source;
        TextView title;

        private ViewHolder() {
        }
    }

    @Override // android.widget.Adapter
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = this.inflater.inflate(R.layout.item_bookmark, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.cover = (ImageView) view.findViewById(R.id.img_bookmark_cover);
            viewHolder.title = (TextView) view.findViewById(R.id.tv_bookmark_title);
            viewHolder.source = (TextView) view.findViewById(R.id.tv_bookmark_source);
            viewHolder.date = (TextView) view.findViewById(R.id.tv_bookmark_date);
            viewHolder.remove = (ImageButton) view.findViewById(R.id.btn_bookmark_remove);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        final Article item = getItem(i);
        viewHolder.title.setText(item.getTitle());
        viewHolder.source.setText(item.getDomain().isEmpty() ? item.getCategory() : item.getDomain());
        viewHolder.date.setText(ReadingDateFormatter.format(view.getContext(), item.getCreatedAt() > 0 ? item.getCreatedAt() : System.currentTimeMillis()));
        viewHolder.remove.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.BookmarkAdapter.0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                BookmarkAdapter.this.lambda$getView$0(item, view2);
            }
        });
        viewHolder.remove.setContentDescription(view.getContext().getString(R.string.action_remove_bookmark));
        String imageUrl = item.getImageUrl();
        if (imageUrl.isEmpty()) {
            viewHolder.cover.setVisibility(8);
        } else {
            RemoteImageLoader.into(viewHolder.cover, imageUrl);
        }
        view.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.BookmarkAdapter.1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                BookmarkAdapter.this.lambda$getView$1(item, view2);
            }
        });
        return view;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$getView$0(Article article, View view) {
        OnBookmarkActionListener onBookmarkActionListener = this.listener;
        if (onBookmarkActionListener != null) {
            onBookmarkActionListener.onBookmarkRemove(article);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$getView$1(Article article, View view) {
        OnBookmarkActionListener onBookmarkActionListener = this.listener;
        if (onBookmarkActionListener != null) {
            onBookmarkActionListener.onBookmarkClick(article);
        }
    }
}
