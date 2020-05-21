package cn.nlifew.clipmgr.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import org.litepal.LitePal;

import java.util.Map;

import cn.nlifew.clipmgr.app.ThisApp;
import cn.nlifew.clipmgr.bean.ActionRecord;
import cn.nlifew.clipmgr.bean.PackageRule;
import cn.nlifew.clipmgr.settings.Settings;
import cn.nlifew.clipmgr.util.ClipUtils;
import cn.nlifew.clipmgr.util.ToastUtils;

public class ExportedProvider extends ContentProvider {
    private static final String TAG = "ExportedProvider";
    public static final String AUTHORITY = "cn.nlifew.clipmgr.provider";

    public static final String PATH_PACKAGE_RULE = "rule";
    public static final String PATH_ACTION_RECORD = "record";
    public static final String PATH_SETTINGS = "settings";

    private static final int TABLE_PACKAGE_RULE_DIR = 1;
    private static final int TABLE_PACKAGE_RULE_ITEM = 2;
    private static final int TABLE_ACTION_RECORD_DIR = 3;
    private static final int TABLE_ACTION_RECORD_ITEM = 4;
    private static final int TABLE_SETTINGS_DIR = 5;

    private static final UriMatcher sUriMatcher;
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, PATH_PACKAGE_RULE, TABLE_PACKAGE_RULE_DIR);
        sUriMatcher.addURI(AUTHORITY, PATH_PACKAGE_RULE + "/*", TABLE_PACKAGE_RULE_ITEM);
        sUriMatcher.addURI(AUTHORITY, PATH_ACTION_RECORD, TABLE_ACTION_RECORD_DIR);
        sUriMatcher.addURI(AUTHORITY, PATH_ACTION_RECORD + "/*", TABLE_ACTION_RECORD_ITEM);
        sUriMatcher.addURI(AUTHORITY, PATH_SETTINGS, TABLE_SETTINGS_DIR);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        Log.i(TAG, "delete: " + uri);
        switch (sUriMatcher.match(uri)) {
            case TABLE_PACKAGE_RULE_DIR:
                return LitePal.getDatabase().delete(PackageRule.Table.NAME, selection, selectionArgs);
            case TABLE_PACKAGE_RULE_ITEM:
                return LitePal.getDatabase().delete(PackageRule.Table.NAME,
                        PackageRule.Column.PACKAGE + "= ?",
                        new String[]{uri.getLastPathSegment()});
        }
        return 0;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        Log.i(TAG, "getType: " + uri);
        switch (sUriMatcher.match(uri)) {
            case TABLE_PACKAGE_RULE_DIR:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + PATH_PACKAGE_RULE;
            case TABLE_PACKAGE_RULE_ITEM:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + "." + PATH_PACKAGE_RULE;
            case TABLE_ACTION_RECORD_DIR:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + PATH_ACTION_RECORD;
            case TABLE_ACTION_RECORD_ITEM:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + "." + PATH_ACTION_RECORD;
            case TABLE_SETTINGS_DIR:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + PATH_SETTINGS;
        }
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Log.i(TAG, "insert: " + uri);

        long id = -1;

        switch (sUriMatcher.match(uri)) {
            case TABLE_PACKAGE_RULE_DIR:
            case TABLE_PACKAGE_RULE_ITEM:
                id = LitePal.getDatabase().insert(PackageRule.Table.NAME, null, values);
                return Uri.parse("content://" + AUTHORITY + "/" + PATH_PACKAGE_RULE + "/" + values.get(PackageRule.Column.PACKAGE));
            case TABLE_ACTION_RECORD_DIR:
            case TABLE_ACTION_RECORD_ITEM:
                id = LitePal.getDatabase().insert(ActionRecord.Table.NAME, null, values);
                showActionMessage(values);
                return Uri.parse("content://" + AUTHORITY + "/" + PATH_ACTION_RECORD + "/" + values.get(ActionRecord.Column.PACKAGE));
        }
        return null;
    }

    private void showActionMessage(ContentValues values) {
        StringBuilder sb = new StringBuilder(64);
        switch (values.getAsInteger(ActionRecord.Column.ACTION)) {
            case ActionRecord.ACTION_DENY: sb.append("已拒绝"); break;
            case ActionRecord.ACTION_GRANT: sb.append("已允许"); break;
            default: return;
        }
        sb.append(values.getAsString(ActionRecord.Column.APP_NAME))
                .append("修改剪贴板为：");
        String text = values.getAsString(ActionRecord.Column.TEXT);
        if (text.length() > 16) {
            sb.append(text, 0, 16).append("...");
        }
        else {
            sb.append(text);
        }
        ThisApp.mH.post(() -> ToastUtils.getInstance(getContext()).show(sb));
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Log.i(TAG, "query: " + uri);

        switch (sUriMatcher.match(uri)) {
            case TABLE_PACKAGE_RULE_DIR:
                return LitePal.getDatabase().query(PackageRule.Table.NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
            case TABLE_PACKAGE_RULE_ITEM:
                return LitePal.getDatabase().query(PackageRule.Table.NAME, projection,
                        PackageRule.Column.PACKAGE + "= ?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, sortOrder);
            case TABLE_ACTION_RECORD_DIR:
                return LitePal.getDatabase().query(ActionRecord.Table.NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
            case TABLE_ACTION_RECORD_ITEM:
                return LitePal.getDatabase().query(ActionRecord.Table.NAME, projection,
                        ActionRecord.Column.PACKAGE + "= ?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, sortOrder);
            case TABLE_SETTINGS_DIR:
                Map<String, ?> map = Settings.getInstance(getContext()).getAll();
                return BundleCursor.makeCursor(map);
        }
        return null;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Log.i(TAG, "update: " + uri);
        switch (sUriMatcher.match(uri)) {
            case TABLE_PACKAGE_RULE_DIR:
                return LitePal.getDatabase().update(PackageRule.Table.NAME, values, selection, selectionArgs);
            case TABLE_PACKAGE_RULE_ITEM:
                return LitePal.getDatabase().update(PackageRule.Table.NAME, values,
                        PackageRule.Column.PACKAGE + "= ?",
                        new String[]{uri.getLastPathSegment()});
            case TABLE_ACTION_RECORD_DIR:
                return LitePal.getDatabase().update(ActionRecord.Table.NAME, values, selection, selectionArgs);
            case TABLE_ACTION_RECORD_ITEM:
                return LitePal.getDatabase().update(ActionRecord.Table.NAME, values,
                        ActionRecord.Column.PACKAGE + "= ?",
                        new String[] {uri.getLastPathSegment()});
        }
        return 0;
    }
}
