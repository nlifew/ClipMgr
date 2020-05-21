package cn.nlifew.clipmgr.ui.main;

import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

import cn.nlifew.clipmgr.R;
import cn.nlifew.clipmgr.ui.main.record.RecordFragment;
import cn.nlifew.clipmgr.ui.main.rule.RuleFragment;

class PagerAdapterImpl extends FragmentPagerAdapter {
    private static final String TAG = "PagerAdapterImpl";

    PagerAdapterImpl(FragmentActivity activity) {
        super(activity.getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mActivity = activity;
    }

    private final Activity mActivity;

    @Override
    public int getCount() {
        return 2;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: return new RuleFragment();
            case 1: return new RecordFragment();
        }
        throw new ArrayIndexOutOfBoundsException(position);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0: return mActivity.getString(R.string.title_app);
            case 1: return mActivity.getString(R.string.title_history);
        }
        throw new ArrayIndexOutOfBoundsException(position);
    }
}
