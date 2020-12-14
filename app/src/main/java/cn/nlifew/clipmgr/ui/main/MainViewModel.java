package cn.nlifew.clipmgr.ui.main;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.nlifew.clipmgr.BuildConfig;
import cn.nlifew.clipmgr.app.ThisApp;
import cn.nlifew.clipmgr.bean.ActionRecord;
import cn.nlifew.clipmgr.bean.PackageRule;
import cn.nlifew.clipmgr.settings.Settings;
import cn.nlifew.clipmgr.ui.main.record.RecordWrapper;
import cn.nlifew.clipmgr.ui.main.rule.RuleWrapper;

public class MainViewModel extends ViewModel {
    private static final String TAG = "MainViewModel";

    public MainViewModel() {

    }

    private final MutableLiveData<String> mErrMsg = new MutableLiveData<>(null);
    private final MutableLiveData<List<RuleWrapper>> mPackageRuleList = new MutableLiveData<>(null);
    private final MutableLiveData<List<RecordWrapper>> mActionRecordList = new MutableLiveData<>(null);

    private String mFilterName;

    void setFilterName(String appName) {
        if (appName == null) {
            mFilterName = null;
        } else {
            mFilterName = appName.toLowerCase(Locale.getDefault());
        }
    }

    void clearAll() {
        mPackageRuleList.postValue(null);
        mActionRecordList.postValue(null);
    }

    void clearActionRecordList() {
        LitePal.deleteAll(ActionRecord.class);
        mActionRecordList.postValue(new ArrayList<>(0));
    }

    public void loadPackageRuleList() {
        new LoadPackageRuleTask().execute();
    }

    public void loadActionRecordList() {
        new LoadActionRecordTask().execute();
    }


    public LiveData<String> getErrMsg() { return mErrMsg; }

    public LiveData<List<RuleWrapper>> getPackageRuleList() { return mPackageRuleList; }

    public LiveData<List<RecordWrapper>> getActionRecordList() { return mActionRecordList; }


    private final class LoadPackageRuleTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "doInBackground: start");

            PackageManager pm = ThisApp
                    .currentApplication
                    .getPackageManager();

            final String filterName = mFilterName;
            boolean showSystemApp = Settings
                    .getInstance(ThisApp.currentApplication)
                    .isShowSystemApp();

            try {
                List<PackageRule> ruleList = LitePal.findAll(PackageRule.class);
                Map<String, PackageRule> ruleMap = new HashMap<>(ruleList.size());
                for (PackageRule rule : ruleList) {
                    ruleMap.put(rule.getPkg(), rule);
                }

                List<PackageInfo> infoList = pm.getInstalledPackages(0);
                List<RuleWrapper> wrapperList = new ArrayList<>(infoList.size());

                for (PackageInfo info : infoList) {

                    // 过滤掉系统应用
                    if (! showSystemApp && (info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        continue;
                    }
                    // 过滤掉我自己
                    if (BuildConfig.APPLICATION_ID.equals(info.packageName)) {
                        continue;
                    }
                    // 过滤掉不含关键字的
                    String appName = info.applicationInfo.loadLabel(pm).toString();
                    if (filterName != null && ! appName.contains(filterName)) {
                        continue;
                    }

                    RuleWrapper wrapper = new RuleWrapper();
                    wrapper.appName = appName;
                    wrapper.icon = info.applicationInfo.loadIcon(pm);
                    wrapper.rawRule = ruleMap.get(info.packageName);
                    if (wrapper.rawRule == null) {
                        wrapper.rawRule = new PackageRule(info.packageName, PackageRule.RULE_REQUEST);
                    }

                    wrapperList.add(wrapper);
                }

                mPackageRuleList.postValue(wrapperList);
            } catch (Throwable t) {
                Log.e(TAG, "doInBackground: ", t);
                mErrMsg.postValue(t.toString());
            }
            return null;
        }
    }

    private final class LoadActionRecordTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "doInBackground: start");
            PackageManager pm = ThisApp
                    .currentApplication
                    .getPackageManager();

            final String filterName = mFilterName;
            boolean showSystemApp = Settings
                    .getInstance(ThisApp.currentApplication)
                    .isShowSystemApp();

            try {
                List<ActionRecord> recordList = LitePal
                        .order(ActionRecord.Column.TIME + " desc")
                        .find(ActionRecord.class);

                List<RecordWrapper> wrapperList = new ArrayList<>(recordList.size());

                for (ActionRecord record : recordList) {

                    // 检查当前 app 是否还存在
                    ApplicationInfo info;
                    try {
                        info = pm.getApplicationInfo(record.getPkg(), 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.e(TAG, "doInBackground: missing package " + record.getPkg(), e);
                        record.delete();
                        continue;
                    }

                    // 过滤掉系统应用
                    if (! showSystemApp && (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        continue;
                    }
                    // 过滤掉我自己
                    if (BuildConfig.APPLICATION_ID.equals(info.packageName)) {
                        continue;
                    }
                    // 过滤掉不含关键词的
                    String appName = info.loadLabel(pm).toString();
                    if (filterName != null && ! appName
                            .toLowerCase(Locale.getDefault())
                            .contains(filterName)) {
                        continue;
                    }

                    RecordWrapper wrapper = new RecordWrapper();
                    wrapper.icon = info.loadIcon(pm);
                    wrapper.rawRecord = record;
                    wrapperList.add(wrapper);
                }
                mActionRecordList.postValue(wrapperList);
            } catch (Throwable t) {
                Log.e(TAG, "doInBackground: ", t);
                mErrMsg.postValue(t.toString());
            }
            return null;
        }
    }
}
