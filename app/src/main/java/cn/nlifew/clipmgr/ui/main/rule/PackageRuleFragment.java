package cn.nlifew.clipmgr.ui.main.rule;

import java.util.ArrayList;

import cn.nlifew.clipmgr.ui.main.AbstractRecyclerFragment;

public class PackageRuleFragment extends AbstractRecyclerFragment {

    @Override
    protected Adapter<?> createRecyclerAdapter() {
        return new PackageRuleAdapter(this,
                new ArrayList<PackageRuleWrapper>(128));
    }
}
