package cn.nlifew.clipmgr.ui.about;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.nlifew.clipmgr.R;

final class RecyclerAdapterImpl extends RecyclerView.Adapter<RecyclerAdapterImpl.Holder> {

    RecyclerAdapterImpl(Context context) {
        mContext = context;
    }

    private final Context mContext;
    private final List<QAS> mDataSet = new ArrayList<>(8);

    void updateDataSet(List<QAS> list) {
        mDataSet.clear();   // todo: notifyItemRangeRemoved()
        mDataSet.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.activity_about_item, parent, false
        );
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        final QAS qas = mDataSet.get(position);

        holder.mQuestionView.setText(qas.question);
        holder.mAnswerView.setText(qas.answer);
    }

    static final class Holder extends RecyclerView.ViewHolder {

        Holder(@NonNull View itemView) {
            super(itemView);
            mQuestionView = itemView.findViewById(R.id.activity_about_item_q);
            mAnswerView = itemView.findViewById(R.id.activity_about_item_a);
        }

        final TextView mQuestionView;
        final TextView mAnswerView;
    }
}
