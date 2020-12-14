package cn.nlifew.clipmgr.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ServiceManager;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Map;

import cn.nlifew.clipmgr.BuildConfig;
import cn.nlifew.clipmgr.R;
import cn.nlifew.clipmgr.ui.about.AboutActivity;
import cn.nlifew.clipmgr.ui.main.MainActivity;
import cn.nlifew.clipmgr.util.ToastUtils;

public class EmptyActivity extends BaseActivity {
    private static final String TAG = "EmptyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);

        findViewById(R.id.activity_main_btn1)
                .setOnClickListener(v -> {
                    Intent intent = new Intent(this, AboutActivity.class);
                    startActivity(intent);
                });
        findViewById(R.id.activity_main_btn2)
                .setOnClickListener(v -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                });
    }

    static {
        try {
            install();
        } catch (Throwable e) {
            Log.e(TAG, "onCreate: ", e);
        }
    }

    private static void install() throws Throwable {
        if (true) {
            return;
        }

        IBinder bridge = ServiceManager.getService("clipmgr_bridge");
        if (bridge == null) {
            Log.e(TAG, "install: null clipmgr_bridge service");
            return;
        }

        Field sCacheField = ServiceManager.class.getDeclaredField("sCache");
        sCacheField.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, IBinder> sCache = (Map<String, IBinder>)
                sCacheField.get(null);

        sCache.put(Context.CLIPBOARD_SERVICE, bridge);
    }
}
