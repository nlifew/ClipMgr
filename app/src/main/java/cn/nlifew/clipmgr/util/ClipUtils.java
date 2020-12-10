package cn.nlifew.clipmgr.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Objects;

public class ClipUtils {
    private static final String TAG = "ClipUtils";


    public static String clip2String(ClipData clipData) {
        int n;
        if (clipData == null || (n = clipData.getItemCount()) == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            ClipData.Item item = clipData.getItemAt(i);
            CharSequence text;
            if (item != null && (text = item.getText()) != null) {
                sb.append(text);
            }
        }
        return sb.toString();
    }

    public static void clip2SimpleString(ClipData clipData, StringBuilder sb) {
        int n;
        if (clipData == null || sb == null || (n = clipData.getItemCount()) == 0) {
            return;
        }
        int length = sb.length() + 25;
        for (int i = 0; i < n; i++) {
            ClipData.Item item = clipData.getItemAt(i);
            CharSequence text;
            if (item != null && (text = item.getText()) != null
                    && sb.append(text).length() >= length) {
                sb.setLength(length);
                sb.append("...");
                return;
            }
        }
    }

    public static void setPrimaryClip(Context context, ClipData clip) {
        ClipboardManager cm = (ClipboardManager) context
                .getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm == null) {
            Log.w(TAG, "setPrimaryClip: no ClipboardManager found");
        } else {
            cm.setPrimaryClip(clip);
        }
    }

    public static boolean hasPrimaryClip(Context context, ClipData clip) {
        ClipboardManager cm = (ClipboardManager) context
                .getSystemService(Context.CLIPBOARD_SERVICE);
        return cm != null && equals(cm.getPrimaryClip(), clip);
    }


    public static boolean equals(ClipData d1, ClipData d2) {
        if (d1 == d2) {
            return true;
        }
        if (d1 == null || d2 == null) {
            return false;
        }
//        if (! Objects.equals(d1.getDescription(), d2.getDescription())) {
//            return false;
//        }
        return Objects.equals(d1.toString(), d2.toString());
    }
}
