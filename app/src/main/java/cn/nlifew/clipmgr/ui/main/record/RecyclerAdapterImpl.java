package cn.nlifew.clipmgr.ui.main.record;

import android.content.ClipData;
import android.content.Context;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.nlifew.clipmgr.R;
import cn.nlifew.clipmgr.bean.ActionRecord;
import cn.nlifew.clipmgr.util.ClipUtils;
import cn.nlifew.clipmgr.util.ToastUtils;

class RecyclerAdapterImpl extends RecyclerView.Adapter {
    private static final String TAG = "RecyclerAdapterImpl";

    RecyclerAdapterImpl(Fragment fragment) {
        mFragment = fragment;
    }

    private final Fragment mFragment;
    private final List<RecordWrapper> mDataSet = new ArrayList<>(64);

    void updateDataSet(List<RecordWrapper> list) {
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
        RecordWrapper record = mDataSet.get(position);

        holder.labelView.setText(record.rawRecord.getAppName());
        holder.iconView.setImageDrawable(record.icon);
        holder.actionView.setText(action2str(record.rawRecord.getAction()));

        mDate.setTime(record.rawRecord.getTime());
        holder.timeView.setText(mDateFormat.format(mDate));

        holder.itemView.setTag(record);
    }

    private final Date mDate = new Date();
    private final SimpleDateFormat mDateFormat = new SimpleDateFormat(
            "MM-dd HH:mm:ss", Locale.getDefault());

    private final class Holder extends RecyclerView.ViewHolder implements View.OnClickListener{
        Holder(@NonNull View itemView) {
            super(itemView);
            labelView = itemView.findViewById(R.id.label);
            iconView = itemView.findViewById(R.id.icon);
            timeView = itemView.findViewById(R.id.pkg);
            actionView = itemView.findViewById(R.id.action);

            itemView.setOnClickListener(this);
        }

        final ImageView iconView;
        final TextView labelView;
        final TextView timeView;
        final TextView actionView;

        @Override
        public void onClick(View v) {
            RecordWrapper record = (RecordWrapper) itemView.getTag();

            DialogInterface.OnClickListener cli = (dialog, which) -> {
                ClipData clipData = ClipData.newPlainText(
                        record.rawRecord.getAppName(),
                        record.rawRecord.getText());
                Context context = mFragment.getContext();
                ClipUtils.setPrimaryClip(context, clipData);
                ToastUtils.getInstance(context).show("已复制到剪贴板");
            };

            new AlertDialog.Builder(mFragment.getActivity())
                    .setTitle("访问记录")
                    .setPositiveButton("复制", cli)
                    .setNeutralButton(action2str(record.rawRecord.getAction()), null)
                    .setMessage(record.rawRecord.getText())
                    .show();
        }
    }

    private static String action2str(int action) {
        switch (action) {
            case ActionRecord.ACTION_GRANT:
                return ("已允许");
            case ActionRecord.ACTION_DENY:
                return ("已拒绝");
        }
        return ("未知操作");
    }
}
