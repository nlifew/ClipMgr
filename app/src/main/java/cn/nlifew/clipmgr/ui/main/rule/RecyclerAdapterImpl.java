package cn.nlifew.clipmgr.ui.main.rule;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.nlifew.clipmgr.R;
import cn.nlifew.clipmgr.bean.PackageRule;

class RecyclerAdapterImpl extends RecyclerView.Adapter {

    RecyclerAdapterImpl(Fragment fragment) {
        mFragment = fragment;
    }

    private final Fragment mFragment;
    private final List<RuleWrapper> mDataSet = new ArrayList<>(64);

    void updateDataSet(List<RuleWrapper> list) {
        mDataSet.clear();
        mDataSet.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mFragment.getContext())
                .inflate(R.layout.fragment_main_item, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
        Holder holder = (Holder) h;
        RuleWrapper rule = mDataSet.get(position);

        holder.labelView.setText(rule.appName);
        holder.packageView.setText(rule.rawRule.getPkg());
        holder.iconView.setImageDrawable(rule.icon);

        switch (rule.rawRule.getRule()) {
            case PackageRule.RULE_REQUEST:
                holder.actionView.setText("询问");
                break;
            case PackageRule.RULE_GRANT:
                holder.actionView.setText("允许");
                break;
            case PackageRule.RULE_DENY:
                holder.actionView.setText("拒绝");
                break;
            default:
                holder.actionView.setText("");
        }
        holder.itemView.setTag(rule);
    }

    private final class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Holder(@NonNull View itemView) {
            super(itemView);
            labelView = itemView.findViewById(R.id.label);
            iconView = itemView.findViewById(R.id.icon);
            packageView = itemView.findViewById(R.id.pkg);
            actionView = itemView.findViewById(R.id.action);

            itemView.setOnClickListener(this);
        }

        final ImageView iconView;
        final TextView labelView;
        final TextView packageView;
        final TextView actionView;

        @Override
        public void onClick(View v) {
            RuleWrapper rule = (RuleWrapper) itemView.getTag();

            DialogInterface.OnClickListener cli = (dialog, which) -> {
                dialog.dismiss();
                switch (which) {
                    case 0: rule.rawRule.setRule(PackageRule.RULE_REQUEST); break;
                    case 1: rule.rawRule.setRule(PackageRule.RULE_GRANT); break;
                    case 2: rule.rawRule.setRule(PackageRule.RULE_DENY); break;
                }
                rule.rawRule.save();
                notifyItemChanged(getAdapterPosition());
            };
            int checkedItem = 0;
            switch (rule.rawRule.getRule()) {
                case PackageRule.RULE_REQUEST: checkedItem = 0; break;
                case PackageRule.RULE_GRANT: checkedItem = 1; break;
                case PackageRule.RULE_DENY: checkedItem = 2; break;
            }

            new AlertDialog.Builder(mFragment.getActivity())
                    .setTitle(rule.appName)
                    .setSingleChoiceItems(new String[] {"询问", "允许", "拒绝"}, checkedItem, cli)
                    .show();
        }
    }
}
