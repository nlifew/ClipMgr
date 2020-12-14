package cn.nlifew.clipmgr.ui.about;


import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.nlifew.clipmgr.R;
import cn.nlifew.clipmgr.ui.BaseActivity;
import cn.nlifew.clipmgr.util.ToastUtils;

public class AboutActivity extends BaseActivity {
    private static final String TAG = "AboutActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        setTitle(R.string.about_title);

        mRecyclerAdapter = new RecyclerAdapterImpl(this);
        RecyclerView view = findViewById(R.id.activity_about_recycler);
        view.setAdapter(mRecyclerAdapter);

        mViewModel = new ViewModelProvider(this).get(QASModel.class);
        mViewModel.error().observe(this, this::onQASErrChanged);
        mViewModel.list().observe(this, this::onQASListChanged);
    }

    private QASModel mViewModel;
    private RecyclerAdapterImpl mRecyclerAdapter;

    @Override
    protected void onResume() {
        super.onResume();

        List<QAS> list = mViewModel.list().getValue();
        if (list == null || list.size() == 0) {
            mViewModel.loadData("QAS.xml");
        }
    }

    private void onQASListChanged(List<QAS> list) {
        if (list == null) {
            return;
        }
        mRecyclerAdapter.updateDataSet(list);
    }

    private void onQASErrChanged(Exception e) {
        if (e == null) {
            return;
        }
        ToastUtils.getInstance(this).show(e.toString());
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
