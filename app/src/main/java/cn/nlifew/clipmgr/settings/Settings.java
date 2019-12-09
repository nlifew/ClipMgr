package cn.nlifew.clipmgr.settings;

import android.content.Context;
import android.content.SharedPreferences;

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
        mPref = c.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private final SharedPreferences mPref;

    public static final String PREF_NAME = "settings";
    public static final String KEY_SHOW_SYSTEM_APP  =   "show_system_app";
    public static final String KEY_FIRST_OPEN       =   "is_first_open";

    public boolean isShowSystemApp() {
        return mPref.getBoolean(KEY_SHOW_SYSTEM_APP, true);
    }

    public void setShowSystemApp(boolean show) {
        mPref.edit().putBoolean(KEY_SHOW_SYSTEM_APP, show).apply();
    }

    public boolean isFirstOpen() {
        return mPref.getBoolean(KEY_FIRST_OPEN, true);
    }

    public void setFirstOpen(boolean first) {
        mPref.edit().putBoolean(KEY_FIRST_OPEN, first).apply();
    }
}
