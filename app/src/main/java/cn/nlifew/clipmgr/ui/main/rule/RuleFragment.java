package cn.nlifew.clipmgr.ui.main.rule;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import java.util.List;

import cn.nlifew.clipmgr.ui.main.MainFragment;
import cn.nlifew.clipmgr.util.ToastUtils;

public class RuleFragment extends MainFragment {
    private static final String TAG = "RuleFragment";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mRecyclerAdapter = new RecyclerAdapterImpl(this);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        LifecycleOwner owner = getViewLifecycleOwner();
        mViewModel.getErrMsg().observe(owner, this::onErrMsgChanged);
        mViewModel.getPackageRuleList().observe(owner, this::onPackageRuleListChanged);

        return view;
    }

    private RecyclerAdapterImpl mRecyclerAdapter;

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        mViewModel.loadPackageRuleList();
    }

    private void onErrMsgChanged(String msg) {
        if (msg != null) {
            ToastUtils.getInstance(getContext()).show(msg);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private void onPackageRuleListChanged(List<RuleWrapper> list) {
        if (list == null) {
            onRefresh();
            return;
        }
        mSwipeRefreshLayout.setRefreshing(false);
        mRecyclerAdapter.updateDataSet(list);
    }
}
