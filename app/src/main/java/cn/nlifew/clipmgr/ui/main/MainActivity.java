package cn.nlifew.clipmgr.ui.main;

import android.app.Activity;
import android.content.DialogInterface;
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
import cn.nlifew.clipmgr.settings.Settings;
import cn.nlifew.clipmgr.ui.BaseActivity;
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

        Activity activity = DirtyUtils.getTopActivity();
        if (activity == null) {
            String msg = "我无法在你的设备上无法获取活动的 Activity，" +
                    "可能是您的厂商更改了相关接口\n\n" +
                    "您不应该继续使用该 app，并立即卸载之";

            DialogInterface.OnClickListener cli = (dialog, which) -> {
                dialog.dismiss();
                PackageUtils.uninstall(MainActivity.this, getPackageName());
            };

            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("失败")
                    .setMessage(msg)
                    .setPositiveButton("卸载", cli)
                    .show();
        }
    }

    private MainViewModel mViewModel;

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
        String msg = "这个 Xposed 模块通过 hook ClipboardManager.setPrimary() 函数，" +
                "来拦截所有试图修改剪贴板的操作\n\n" +
                "app 使用了一些 Android 隐藏的 API，如果您的厂商更改了这些接口，可能会无法使用\n\n" +
                "作者：coolapk @ablist97";

        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(msg)
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
