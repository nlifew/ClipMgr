package cn.nlifew.clipmgr.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public final class PackageUtils {
    private static final String TAG = "PackageUtils";

    private PackageUtils() {  }

    public static String getAppName(PackageManager pm, String pkg) {
        try {
            ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
            return info.loadLabel(pm).toString();
        } catch (Exception e) {
            Log.e(TAG, "getApplicationLabelByPackageName: " + pkg, e);
        }
        return "";
    }


    public static boolean isPackageInstalled(Context c, String pkg) {
        final PackageManager pm = c.getPackageManager();
        try {
            pm.getApplicationInfo(pkg, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
