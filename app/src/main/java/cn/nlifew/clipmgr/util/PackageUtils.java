package cn.nlifew.clipmgr.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import de.robv.android.xposed.XposedBridge;

public final class PackageUtils {
    private static final String TAG = "PackageUtils";

    private PackageUtils() {  }


    public static void uninstall(Activity activity, String packageName) {
        Uri uri = Uri.fromParts("package", packageName, null);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        activity.startActivity(intent);
    }

    public static ApplicationInfo getApplicationInfo(PackageManager pm, String packageName) {
        try {
            return pm.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            XposedBridge.log(TAG + ": getCallingApp: " + packageName);
            XposedBridge.log(e);
        }
        return null;
    }
}
