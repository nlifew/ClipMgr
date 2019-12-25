package cn.nlifew.clipmgr.ui.main.rule;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cn.nlifew.clipmgr.R;
import cn.nlifew.clipmgr.adapter.ViewHolder;
import cn.nlifew.clipmgr.bean.PackageRule;
import cn.nlifew.clipmgr.ui.main.AbstractRecyclerFragment.Adapter;

final class PackageRuleAdapter extends Adapter<PackageRuleWrapper>
    implements View.OnClickListener  {
    private static final String TAG = "PackageRuleAdapter";

    PackageRuleAdapter(PackageRuleFragment f, List<PackageRuleWrapper> dataSet) {
        super(dataSet);
        mContext = f.getContext();
        mFragment = f;
    }

    private final PackageRuleFragment mFragment;
    private List<PackageRuleWrapper> mOldDataSet;
    final Context mContext;


    @Override
    public void onRefresh() {
        Log.d(TAG, "onRefresh: start");

        new LoadPackageRuleTask(this).execute();
        if (mOldDataSet != null) {
            mOldDataSet.clear();
        }
    }


    @Override
    public void onSearchFinish(String text) {
        Log.d(TAG, "onSearchFinish: " + text);
        if (mOldDataSet == null) {
            mOldDataSet = new ArrayList<>(mDataSet);
        }
        else if (mOldDataSet.size() == 0) { // 防止多次搜索丢失数据
            mOldDataSet.addAll(mDataSet);
        }
        mDataSet.clear();
        for (PackageRuleWrapper r : mOldDataSet) {
            if (r.appName.contains(text)) {
                mDataSet.add(r);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onSearchCancel() {
        if (mOldDataSet != null && mOldDataSet.size() != 0) {
            mDataSet.clear();
            mDataSet.addAll(mOldDataSet);
            mOldDataSet.clear();
            notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        final ViewHolder holder = (ViewHolder) v.getTag(R.id.tag_holder);
        final PackageRuleWrapper wrapper = (PackageRuleWrapper) v.getTag(R.id.tag_item);

        int selectedItem;
        switch (wrapper.mOrigin.getRule()) {
            case PackageRule.RULE_DENY: selectedItem = 0; break;
            case PackageRule.RULE_GRANT: selectedItem = 1; break;
            case PackageRule.RULE_REQUEST: selectedItem = 2; break;
            default: return;
        }
        DialogInterface.OnClickListener cli = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                int rule;
                switch (which) {
                    case 0: rule = PackageRule.RULE_DENY; break;
                    case 1: rule = PackageRule.RULE_GRANT; break;
                    case 2: rule = PackageRule.RULE_REQUEST; break;
                    default: return;
                }
                wrapper.mOrigin.setRule(rule);
                wrapper.mOrigin.save();
                onBindViewHolder(holder, wrapper);
            }
        };

        new AlertDialog.Builder(mContext)
                .setTitle(wrapper.appName)
                .setSingleChoiceItems(new String[]{"拒绝", "允许", "询问"}, selectedItem, cli)
                .show();
    }

    void stopLoading() {
        mFragment.mSwipeLayout.setRefreshing(false);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(mContext)
                .inflate(R.layout.fragment_main_item, parent, false);
        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        onBindViewHolder(holder, mDataSet.get(position));
    }

    private void onBindViewHolder(@NonNull ViewHolder holder, PackageRuleWrapper rule) {
        holder.itemView.setTag(R.id.tag_holder, holder);
        holder.itemView.setTag(R.id.tag_item, rule);

        holder.setTextViewText(R.id.fragment_main_rule_item_label, rule.appName)
                .setImageViewDrawable(R.id.fragment_main_rule_item_icon, rule.icon)
                .setTextViewText(R.id.fragment_main_rule_item_pkg, rule.pkg)
                .setTextViewText(R.id.fragment_main_rule_item_action, rule.getRule());
    }
}
