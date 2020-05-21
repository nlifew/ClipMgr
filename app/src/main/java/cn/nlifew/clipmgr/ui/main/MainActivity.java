package cn.nlifew.clipmgr.ui.main;

import android.content.DialogInterface;
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

        menu.findItem(R.id.options_radical_mode)
                .setChecked(settings.isRadicalMode());

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
            case R.id.options_radical_mode:
                if (item.isChecked()) {
                    Settings.getInstance(this).setRadicalMode(false);
                }
                else {
                    showRadicalDialog();
                }
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

    private void showRadicalDialog() {
        DialogInterface.OnClickListener cli = (dialog, which) -> {
            dialog.dismiss();
            if (which == DialogInterface.BUTTON_POSITIVE) {
                Settings.getInstance(this).setRadicalMode(true);
                invalidateOptionsMenu();
            }
        };

        new AlertDialog.Builder(this)
                .setTitle("警告")
                .setMessage("开启激进模式后，应用会尝试在宿主进程直接打开 Dialog，以解决无法弹窗问题。\n" +
                        "激进模式无法保证兼容性，而且其 Dialog 样式会随着宿主进程的不同而不同。\n" +
                        "如果现在工作正常，请不要开启。")
                .setPositiveButton("确定", cli)
                .setNegativeButton("取消", cli)
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
