package cn.nlifew.clipmgr.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

import java.util.Map;

import cn.nlifew.clipmgr.BuildConfig;

public final class Settings {

    private static Settings sInstance;

    public static Settings getInstance(Context c) {
        if (sInstance == null) {
            synchronized (Settings.class) {
                if (sInstance == null) {
                    sInstance = new Settings(c);
                }
            }
        }
        return sInstance;
    }

    private Settings(Context c) {
        mContext = c.getApplicationContext();
        mPref = mContext.getSharedPreferences(
                PREF_NAME, Context.MODE_PRIVATE);
    }

    private final Context mContext;
    private final SharedPreferences mPref;

    public static final String PREF_NAME = "settings";
    public static final String KEY_SHOW_SYSTEM_APP  =   "show_system_app";
    public static final String KEY_VERSION_CODE     =   "version_code";

    public boolean isShowSystemApp() {
        return mPref.getBoolean(KEY_SHOW_SYSTEM_APP, true);
    }

    public void setShowSystemApp(boolean show) {
        mPref.edit().putBoolean(KEY_SHOW_SYSTEM_APP, show).apply();
    }

    public int getVersionCode() {
        // 这个 key 在 versionCode 为 5 的时候添加
        return mPref.getInt(KEY_VERSION_CODE, 5);
    }

    public void setVersionCode(int code) {
        mPref.edit().putInt(KEY_VERSION_CODE, code).apply();
    }

    public Map<String, ?> getAll() { return mPref.getAll(); }
}
