package cn.nlifew.clipmgr.ui.main.record;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

import cn.nlifew.clipmgr.R;
import cn.nlifew.clipmgr.adapter.ViewHolder;
import cn.nlifew.clipmgr.bean.ActionRecord;
import cn.nlifew.clipmgr.ui.main.AbstractRecyclerFragment;

class ActionRecordAdapter extends AbstractRecyclerFragment.Adapter<ActionRecordWrapper>
        implements View.OnClickListener {
    private static final String TAG = "ActionRecordAdapter";

    ActionRecordAdapter(ActionRecordFragment f, List<ActionRecordWrapper> dataSet) {
        super(dataSet);
        mContext = f.getContext();
        mFragment = f;
    }

    private final ActionRecordFragment mFragment;
    final Context mContext;

    private List<ActionRecordWrapper> mOldDataSet;

    @Override
    public void onRefresh() {
        Log.d(TAG, "onRefresh: start");
        new LoadActionRecordTask(this).execute();
        if (mOldDataSet != null) {
            mOldDataSet.clear();
        }
    }

    @Override
    public void onSearchFinish(String text) {
        // 当用户完成一次搜索时，会调用此方法
        // 这个逻辑很简单，先保存一个当前数据集合的副本，
        // 然后把满足条件的数据挑出来作为新的数据集

        Log.d(TAG, "onSearchFinish: " + text);
        if (mOldDataSet == null) {
            mOldDataSet = new ArrayList<>(mDataSet);
        }
        else if (mOldDataSet.size() == 0) {
            // 防止连续多次搜索丢失原始数据
            mOldDataSet.addAll(mDataSet);
        }
        mDataSet.clear();

        for (ActionRecordWrapper r : mOldDataSet) {
            if (r.mRecord.getAppName().contains(text)) {
                mDataSet.add(r);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onSearchCancel() {
        // 恢复备份的旧数据集
        if (mOldDataSet != null && mOldDataSet.size() != 0) {
            mDataSet.clear();
            mDataSet.addAll(mOldDataSet);
            mOldDataSet.clear();
            notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        final ActionRecordWrapper r = (ActionRecordWrapper) v.getTag();

        new AlertDialog.Builder(mContext)
                .setMessage(r.mRecord.getText())
                .show();
    }

    void stopLoading() {
        mFragment.mSwipeLayout.setRefreshing(false);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.fragment_main_item, parent, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ActionRecordWrapper record = mDataSet.get(position);
        holder.itemView.setTag(record);
        holder.setTextViewText(R.id.fragment_main_rule_item_label, record.mRecord.getAppName())
                .setImageViewDrawable(R.id.fragment_main_rule_item_icon, record.mIcon)
                .setTextViewText(R.id.fragment_main_rule_item_pkg, record.mRecord.getText())
                .setTextViewText(R.id.fragment_main_rule_item_action, record.mTime);
    }
}

