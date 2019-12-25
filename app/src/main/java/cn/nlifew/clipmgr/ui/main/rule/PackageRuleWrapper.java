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
    String pkg;

    String getRule() {
        switch (mOrigin.getRule()) {
            case PackageRule.RULE_REQUEST: return "询问";
            case PackageRule.RULE_GRANT: return "允许";
            case PackageRule.RULE_DENY: return "拒绝";
        }
        return "?_?";
    }
}
