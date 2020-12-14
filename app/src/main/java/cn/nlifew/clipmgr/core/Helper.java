package cn.nlifew.clipmgr.core;

import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.Parcel;

import cn.nlifew.clipmgr.bean.ActionRecord;
import cn.nlifew.clipmgr.bean.PackageRule;
import cn.nlifew.clipmgr.provider.ExportedProvider;
import cn.nlifew.clipmgr.util.ClipUtils;
import de.robv.android.xposed.XposedBridge;

final class Helper {
    private static final String TAG = "Helper";

    private Helper() {  }


    static int getPackageRule(Context context, String packageName) {
        Cursor cursor = null;
        try {
            Uri uri = Uri.parse("content://" + ExportedProvider.AUTHORITY
                    + "/" + ExportedProvider.PATH_PACKAGE_RULE
                    + "/" + packageName);
            cursor = context.getContentResolver().query(uri, null,
                    null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                return cursor.getInt(cursor.getColumnIndex(PackageRule.Column.RULE));
            }
        } catch (Exception e) {
            XposedBridge.log(TAG + ": getPackageRule: failed to query rule of " + packageName);
            XposedBridge.log(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return PackageRule.RULE_REQUEST;
    }

    static void saveActionRecord(Context context, String packageName,
                                         ClipData clipData, int action) {

        ContentValues values = new ContentValues();
        values.put(ActionRecord.Column.PACKAGE, packageName);
        values.put(ActionRecord.Column.ACTION, action);
        values.put(ActionRecord.Column.TEXT, ClipUtils.clip2String(clipData));
        values.put(ActionRecord.Column.TIME, System.currentTimeMillis());
        values.put(ActionRecord.Column.APP_NAME, packageName);

        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            values.put(ActionRecord.Column.APP_NAME, info.loadLabel(pm).toString());

            Uri uri = Uri.parse("content://" + ExportedProvider.AUTHORITY
                    + "/" + ExportedProvider.PATH_ACTION_RECORD);
            context.getContentResolver().insert(uri, values);
        } catch (Exception e) {
            XposedBridge.log(TAG + ": saveActionRecord: failed to insert ActionRecord: " +
                    packageName + ", " + ClipUtils.clip2String(clipData) + ", " + action);
            XposedBridge.log(e);
        }
    }

    static void savePackageRule(Context context, String packageName, int rule) {
        ContentResolver resolver = context.getContentResolver();

        try {
            Uri uri = Uri.parse("content://" + ExportedProvider.AUTHORITY
                    + "/" + ExportedProvider.PATH_PACKAGE_RULE
                    + "/" + packageName);
            resolver.delete(uri, null, null);

            ContentValues values = new ContentValues();
            values.put(PackageRule.Column.PACKAGE, packageName);
            values.put(PackageRule.Column.RULE, rule);

            resolver.insert(uri, values);
        } catch (Exception e) {
            XposedBridge.log(TAG + ": savePackageRule: failed to save the rule [" +
                            rule + "] of package: [" + packageName + "]");
            XposedBridge.log(e);
        }


    }

    static String clip2SimpleText(ClipData clipData) {
        StringBuilder sb = new StringBuilder(64)
                .append("尝试修改剪贴板为：");
        ClipUtils.clip2SimpleString(clipData, sb);
        return sb.toString();
    }
}
