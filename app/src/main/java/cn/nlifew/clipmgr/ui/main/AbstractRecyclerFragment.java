package cn.nlifew.clipmgr.ui.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import cn.nlifew.clipmgr.R;
import cn.nlifew.clipmgr.adapter.ListRecyclerAdapter;
import cn.nlifew.clipmgr.adapter.ViewHolder;
import cn.nlifew.clipmgr.ui.BaseFragment;
import cn.nlifew.clipmgr.util.DisplayUtils;

public abstract class AbstractRecyclerFragment extends BaseFragment {
    private final String TAG = getClass().getSimpleName();

    public Adapter mAdapter;
    public RecyclerView mRecyclerView;
    public SwipeRefreshLayout mSwipeLayout;

    protected BroadcastReceiver mSearchEventReceiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final Context c = getContext();

        mRecyclerView = new RecyclerView(c);
        mRecyclerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        mSwipeLayout = new SwipeRefreshLayout(c);
        mSwipeLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        mSwipeLayout.addView(mRecyclerView);
        return mSwipeLayout;
    }

    protected abstract Adapter<?> createRecyclerAdapter();

    @Override
    protected void onLazyLoad() {
        super.onLazyLoad();

        final Context c = getContext();

        // 初始化 RecyclerView，SwipeRefreshLayout

        mAdapter = createRecyclerAdapter();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(c));
        mRecyclerView.setAdapter(mAdapter);

        mSwipeLayout.setOnRefreshListener(mAdapter);
        mSwipeLayout.setRefreshing(true);

        mAdapter.onRefresh();

        // 注册广播接收器，用来监听用户搜索关键词
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.ACTION_SEARCH_APP_FINISH);
        filter.addAction(MainActivity.ACTION_SEARCH_APP_CANCEL);
        mSearchEventReceiver = new BroadcastReceiver() {
            @Override
                public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                Log.d(TAG, "onReceive: " + action);

                if (MainActivity.ACTION_SEARCH_APP_FINISH.equals(action)) {
                    String text = intent.getStringExtra(MainActivity.EXTRA_SEARCH_APP_TEXT);
                    mAdapter.onSearchFinish(text);
                }
                else if (MainActivity.ACTION_SEARCH_APP_CANCEL.equals(action)) {
                    mAdapter.onSearchCancel();
                }
            }
        };
        LocalBroadcastManager
                .getInstance(c)
                .registerReceiver(mSearchEventReceiver, filter);
    }
    @Override
    public void onDestroyView() {
        // 卸载广播接收器
        if (mSearchEventReceiver != null) {
            LocalBroadcastManager
                    .getInstance(getContext())
                    .unregisterReceiver(mSearchEventReceiver);
        }
        super.onDestroyView();
    }

    public abstract static class Adapter<T> extends ListRecyclerAdapter<T> implements
            SwipeRefreshLayout.OnRefreshListener {
        private static final String TAG = "Adapter";


        public Adapter(List<T> dataSet) {
            super(dataSet);
        }

        public abstract void onSearchFinish(String text);

        public abstract void onSearchCancel();
    }
}
