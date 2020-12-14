package cn.nlifew.clipmgr.ui.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import cn.nlifew.clipmgr.R;
import cn.nlifew.clipmgr.provider.ExportedProvider;
import cn.nlifew.clipmgr.request.RequestDialogManager;
import cn.nlifew.clipmgr.service.AliveService;
import cn.nlifew.clipmgr.settings.Settings;
import cn.nlifew.clipmgr.ui.BaseActivity;
import cn.nlifew.clipmgr.ui.about.AboutActivity;
import cn.nlifew.clipmgr.util.DirtyUtils;
import cn.nlifew.clipmgr.util.PackageUtils;

public class MainActivity extends BaseActivity implements
        SearchView.OnQueryTextListener,
        SearchView.OnCloseListener {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PagerAdapterImpl adapter = new PagerAdapterImpl(this);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager pager = findViewById(R.id.pager);
        pager.setAdapter(adapter);
        tabLayout.setupWithViewPager(pager);

        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        Intent intent = new Intent(this, AliveService.class);
        startService(intent);
    }

    private MainViewModel mViewModel;


    @Override
    protected void onResume() {
        super.onResume();
        checkRemoteService();
    }

    private void checkRemoteService() {
        RequestDialogManager dm = RequestDialogManager.getInstance(this);
        if (dm.available()) {
            return;
        }

        DialogInterface.OnClickListener cli = (dialog, which) -> {
            dialog.dismiss();
            if (which != DialogInterface.BUTTON_POSITIVE) {
                return;
            }
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        };

        String msg = "无法获取到远程服务。可能是 xposed 未激活，或者是服务注册异常。" +
                "您可以在 xposed manager 的 \"日志\" 里获取必要的信息，并反馈给开发者。\n\n" +
                "点击下面的 \"确定\" 按钮，获取更多信息。";

        new AlertDialog.Builder(this)
                .setTitle("未能获取弹窗服务")
                .setMessage(msg)
                .setPositiveButton("确定", cli)
                .setNegativeButton("忽略", cli)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_main, menu);

        SearchView searchView = (SearchView) menu
                .findItem(R.id.options_search)
                .getActionView();

        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Settings settings = Settings.getInstance(this);

        menu.findItem(R.id.options_show_system)
                .setChecked(settings.isShowSystemApp());

        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.options_clear_history:
                mViewModel.clearActionRecordList();
                return true;
            case R.id.options_show_system:
                Settings.getInstance(this)
                        .setShowSystemApp(! item.isChecked());
                mViewModel.clearAll();
                return true;
            case R.id.options_about:
                showAboutDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.xposed_module_description)
                .setPositiveButton("确定", null)
                .show();
    }

    /* SearchView */

    @Override
    public boolean onClose() {
        mViewModel.setFilterName(null);
        mViewModel.clearAll();
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        mViewModel.setFilterName(query);
        mViewModel.clearAll();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
