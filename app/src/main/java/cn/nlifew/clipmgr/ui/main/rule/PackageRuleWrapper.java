package cn.nlifew.clipmgr.ui.main.rule;

import android.graphics.drawable.Drawable;

import cn.nlifew.clipmgr.bean.PackageRule;
import cn.nlifew.clipmgr.bean.UserRule;

class PackageRuleWrapper {

    final PackageRule mOrigin;

    PackageRuleWrapper(PackageRule rule) {
        mOrigin = rule;
    }

    String appName;
    Drawable icon;
    String rule;
    String pkg;
}
