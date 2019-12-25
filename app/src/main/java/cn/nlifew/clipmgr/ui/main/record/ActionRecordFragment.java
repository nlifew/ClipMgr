package cn.nlifew.clipmgr.ui.main.record;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;

import cn.nlifew.clipmgr.bean.ActionRecord;
import cn.nlifew.clipmgr.ui.BaseFragment;
import cn.nlifew.clipmgr.ui.main.AbstractRecyclerFragment;
import cn.nlifew.clipmgr.ui.main.MainActivity;

public class ActionRecordFragment extends AbstractRecyclerFragment {
    private static final String TAG = "ActionRecordFragment";


    private BroadcastReceiver mClearRecordReceiver;

    @Override
    protected Adapter<?> createRecyclerAdapter() {
        return new ActionRecordAdapter(this,
                new ArrayList<ActionRecordWrapper>(128));
    }

    @Override
    protected void onLazyLoad() {
        super.onLazyLoad();

        // 注册清除历史记录广播
        mClearRecordReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ActionRecordAdapter adapter = (ActionRecordAdapter) mAdapter;
                adapter.onClearActionRecord();
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.ACTION_CLEAR_ACTION_RECORD);
        LocalBroadcastManager
                .getInstance(getContext())
                .registerReceiver(mClearRecordReceiver, filter);
    }

    @Override
    public void onDestroyView() {
        if (mClearRecordReceiver != null) {
            LocalBroadcastManager
                    .getInstance(getContext())
                    .unregisterReceiver(mClearRecordReceiver);
        }

        super.onDestroyView();
    }
}
