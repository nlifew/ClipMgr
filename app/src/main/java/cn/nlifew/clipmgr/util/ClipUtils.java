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
        if (cm != null) {
            cm.setPrimaryClip(clip);
        }
    }

    public static boolean equals(ClipData d1, ClipData d2) {
        Log.d(TAG, "equals: [" + d1 + "/" + d2 + "]");

        if (d1 == d2) {
            return true;
        }
        if (d1 == null || d2 == null) {
            return false;
        }
//        if (! Objects.equals(d1.getDescription(), d2.getDescription())) {
//            return false;
//        }

        int n1 = d1.getItemCount(), n2 = d2.getItemCount();
        if (n1 != n2) {
            return false;
        }
        for (int i = 0; i < n1; i++) {
            ClipData.Item it1 = d1.getItemAt(i);
            ClipData.Item it2 = d2.getItemAt(i);
            if (! equals(it1, it2)) {
                return false;
            }
        }
        return true;
    }

    private static boolean equals(ClipData.Item it1, ClipData.Item it2) {
        if (it1 == it2) {
            return true;
        }
        if (it1 == null || it2 == null) {
            return false;
        }
        return Objects.equals(it1.getText(), it2.getText())
                && Objects.equals(it1.getHtmlText(), it2.getHtmlText())
                && Objects.equals(it1.getIntent(), it2.getIntent())
                && Objects.equals(it1.getUri(), it2.getUri());
    }
}
