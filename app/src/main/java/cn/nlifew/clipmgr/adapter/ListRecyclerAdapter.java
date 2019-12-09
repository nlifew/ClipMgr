package cn.nlifew.clipmgr.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

public abstract class ListRecyclerAdapter<T> extends RecyclerView.Adapter<ViewHolder> {

    public ListRecyclerAdapter(List<T> dataSet) {
        mDataSet = dataSet;
    }

    public final List<T> mDataSet;

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }


    public void updateDataSet(List<T> set) {
        mDataSet.clear();
        mDataSet.addAll(set);
        notifyDataSetChanged();
    }

    public void addAllDataSet(List<T> set) {
        int oldSize = mDataSet.size();
        mDataSet.addAll(set);
        notifyItemRangeChanged(oldSize, set.size());
    }
}
