package cn.nlifew.clipmgr.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

import cn.nlifew.clipmgr.service.AliveService;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.i(TAG, "onReceive: " + action);

        Intent it = new Intent(context, AliveService.class);
        ContextCompat.startForegroundService(context, it);
    }
}
