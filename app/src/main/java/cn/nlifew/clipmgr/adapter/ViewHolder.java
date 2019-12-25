package cn.nlifew.clipmgr.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewHolder extends RecyclerView.ViewHolder {

    public ViewHolder(View v) {
        super(v);
    }

    private SparseArray<View> mViews;

    @SuppressWarnings("unchecked")
    public <T extends View> T getView(int id) {
        View v = null;

        if (mViews == null) {
            mViews = new SparseArray<>(8);
        } else {
            v = mViews.get(id);
        }
        if (v == null) {
            v = itemView.findViewById(id);
            mViews.put(id, v);
        }
        return (T) v;
    }

    public ViewHolder setTextViewText(@IdRes int id, CharSequence text) {
        TextView tv = getView(id);
        if (tv != null) tv.setText(text);
        return this;
    }

    public ViewHolder setImageViewDrawable(@IdRes int id, Drawable d) {
        ImageView iv = getView(id);
        if (iv != null)  iv.setImageDrawable(d);
        return this;
    }
}
