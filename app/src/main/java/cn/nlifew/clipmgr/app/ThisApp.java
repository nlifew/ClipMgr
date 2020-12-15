package cn.nlifew.clipmgr.app;



import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Keep;

import org.litepal.LitePalApplication;

@Keep
public class ThisApp extends LitePalApplication {
    private static final String TAG = "ThisApp";

    public static final Handler mH = new Handler(Looper.getMainLooper());

    public static Application currentApplication;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        currentApplication = this;
    }

}
