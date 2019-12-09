package cn.nlifew.clipmgr.ui.main;


import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import cn.nlifew.clipmgr.ui.main.record.ActionRecordFragment;
import cn.nlifew.clipmgr.ui.main.rule.PackageRuleFragment;

final class MainAdapter extends FragmentPagerAdapter {

    MainAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: return new PackageRuleFragment();
            case 1: return new ActionRecordFragment();
        }
        return null;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0: return "应用程序";
            case 1: return "使用记录";
        }
        return "";
    }
}
