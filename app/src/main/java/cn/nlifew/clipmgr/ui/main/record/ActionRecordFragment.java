package cn.nlifew.clipmgr.ui.main.record;

import android.util.Log;

import java.util.ArrayList;

import cn.nlifew.clipmgr.bean.ActionRecord;
import cn.nlifew.clipmgr.ui.BaseFragment;
import cn.nlifew.clipmgr.ui.main.AbstractRecyclerFragment;

public class ActionRecordFragment extends AbstractRecyclerFragment {
    private static final String TAG = "ActionRecordFragment";

    @Override
    protected Adapter<?> createRecyclerAdapter() {
        return new ActionRecordAdapter(this,
                new ArrayList<ActionRecordWrapper>(128));
    }
}
