package cn.nlifew.clipmgr.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

public final class PackageUtils {
    private static final String TAG = "PackageUtils";

    private PackageUtils() {  }


    public static void uninstall(Activity activity, String packageName) {
        Uri uri = Uri.fromParts("package", packageName, null);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        activity.startActivity(intent);
    }
}
