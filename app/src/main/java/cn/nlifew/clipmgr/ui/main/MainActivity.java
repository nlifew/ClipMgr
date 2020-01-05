package cn.nlifew.clipmgr.ui.main;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import cn.nlifew.clipmgr.R;

import cn.nlifew.clipmgr.settings.Settings;
import cn.nlifew.clipmgr.ui.BaseActivity;

public class MainActivity extends BaseActivity implements
        SearchView.OnQueryTextListener,
        SearchView.OnCloseListener {

    private static final String TAG = "MainActivity";

    public static final String ACTION_SEARCH_APP_FINISH     =   "ACTION_SEARCH_APP_FINISH";
    public static final String ACTION_SEARCH_APP_CANCEL     =   "ACTION_SEARCH_APP_CANCEL";
    public static final String ACTION_CLEAR_ACTION_RECORD   =   "ACTION_CLEAR_ACTION_RECORD";

    public static final String EXTRA_SEARCH_APP_TEXT    =   "EXTRA_SEARCH_APP_TEXT";


    private Menu mOptionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);

        TabLayout tab = findViewById(R.id.activity_main_tab);
        ViewPager pager = findViewById(R.id.activity_main_pager);

        MainAdapter adapter = new MainAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        tab.setupWithViewPager(pager);

        Settings settings = Settings.getInstance(this);
        if (settings.isFirstOpen()) {
            showAboutDialog();
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mOptionsMenu = menu;
        getMenuInflater().inflate(R.menu.activity_main, menu);

        SearchView searchView = (SearchView) menu
                .findItem(R.id.activity_main_search)
                .getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);


        Settings settings = Settings.getInstance(this);

        MenuItem showSystemApp = menu.findItem(R.id.activity_main_system);
        showSystemApp.setChecked(settings.isShowSystemApp());

        MenuItem radicalMode = menu.findItem(R.id.activity_main_radical_mode);
        radicalMode.setChecked(settings.isRadicalMode());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_main_system: {
                Settings settings = Settings.getInstance(this);
                settings.setShowSystemApp(! item.isChecked());
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            }
            case R.id.activity_main_about: {
                showAboutDialog();
                return true;
            }
            case R.id.activity_main_clear_record: {
                Intent intent = new Intent(ACTION_CLEAR_ACTION_RECORD);
                LocalBroadcastManager
                        .getInstance(this)
                        .sendBroadcast(intent);
                return true;
            }
            case R.id.activity_main_radical_mode: {
                boolean enabled = item.isChecked();
                if (enabled) {
                    item.setChecked(false);
                    Settings.getInstance(this)
                            .setRadicalMode(false);
                } else {
                    showRadicalModeDialog();
                }
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void showRadicalModeDialog() {
        String msg = "开启激进模式后，app 会尝试在宿主应用直接展示 Dialog，" +
                "以解决后台死亡问题。\n\n" +
                "这个模式不能保证兼容性，而且不同应用会有不同的 Dialog 样式。" +
                "如果现在正常工作，请不要开启。";

        DialogInterface.OnClickListener cli = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    Settings.getInstance(MainActivity.this)
                            .setRadicalMode(true);
                    mOptionsMenu.findItem(R.id.activity_main_radical_mode)
                            .setChecked(true);
                }
            }
        };

        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(msg)
                .setPositiveButton("开启", cli)
                .setNegativeButton("取消", cli)
                .show();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d(TAG, "onQueryTextSubmit: " + query);

        // 当用户完成搜索框的输入时，我们通过 LocalBroadcast 通知 Fragment
        Intent intent = new Intent(ACTION_SEARCH_APP_FINISH);
        intent.putExtra(EXTRA_SEARCH_APP_TEXT, query);

        LocalBroadcastManager
                .getInstance(this)
                .sendBroadcast(intent);

        return false;
    }

    @Override
    public boolean onClose() {
        Log.d(TAG, "onClose: start");

        // 同上，通过 LocalBroadcast 通知 Fragment
        Intent intent = new Intent(ACTION_SEARCH_APP_CANCEL);
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(intent);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
