package cn.nlifew.clipmgr.provider;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.collection.SimpleArrayMap;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.nlifew.clipmgr.bean.PackageRule;


public class BundleCursor  {
    private static final String TAG = "BundleCursor";

    public static Cursor makeCursor(Map<String, ?> map) {
        String[] keySet = new String[map.size()];
        Object[] valueSet = new Object[map.size()];
        int index = 0;
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            keySet[index] = entry.getKey();
            valueSet[index] = entry.getValue();

            Log.d(TAG, "makeCursor: [" + keySet[index] + ", " + valueSet[index] + "]");

            index ++;
        }

        MatrixCursor cursor = new MatrixCursor(keySet);
        cursor.addRow(valueSet);

        return cursor;
    }
}
