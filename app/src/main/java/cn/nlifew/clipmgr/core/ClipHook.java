package cn.nlifew.clipmgr.core;


import android.app.Activity;
import android.app.ActivityThread;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import androidx.annotation.NonNull;

import cn.nlifew.clipmgr.BuildConfig;
import cn.nlifew.clipmgr.bean.ActionRecord;
import cn.nlifew.clipmgr.bean.PackageRule;
import cn.nlifew.clipmgr.provider.ExportedProvider;
import cn.nlifew.clipmgr.settings.Settings;
import cn.nlifew.clipmgr.ui.request.OnRequestFinishListener;
import cn.nlifew.clipmgr.ui.request.RequestActivity;
import cn.nlifew.clipmgr.util.ClipUtils;
import cn.nlifew.clipmgr.util.DirtyUtils;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ClipHook implements IXposedHookLoadPackage {
    private static final String TAG = "ClipHook";
    private static final String MY_PACKAGE_NAME = BuildConfig.APPLICATION_ID;


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam param) throws Throwable {

        if (MY_PACKAGE_NAME.equals(param.packageName)) {
            // 我 信 我自己
            return;
        }

        XposedBridge.log(TAG + " package: " + param.packageName);


        XposedHelpers.findAndHookMethod(
                "android.content.ClipboardManager",
                param.classLoader,
                "setPrimaryClip",
                ClipData.class,
                new XSetPrimaryClip()
        );
    }

}
