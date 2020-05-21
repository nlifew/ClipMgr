package cn.nlifew.clipmgr.util;

import android.content.Context;

import android.widget.Toast;

import androidx.annotation.StringRes;

public final class ToastUtils {

    private static ToastUtils sInstance;

    public static ToastUtils getInstance(Context c) {
        if (sInstance == null) {
            synchronized (ToastUtils.class) {
                if (sInstance == null) {
                    sInstance = new ToastUtils(c);
                }
            }
        }
        return sInstance;
    }

    private final Toast mToast;

    private ToastUtils(Context c) {
        mToast = Toast.makeText(c.getApplicationContext(),
                "", Toast.LENGTH_SHORT);
    }

    public void show(final @StringRes int text) {
        mToast.setText(text);
        mToast.show();
    }

    public void show(final @StringRes int text, final int time) {
        mToast.setText(text);
        mToast.setDuration(time);
        mToast.show();
    }

    public void show(final CharSequence text) {
        mToast.setText(text);
        mToast.show();
    }

    public void show(final CharSequence text, final int time) {
        mToast.setText(text);
        mToast.setDuration(time);
        mToast.show();
    }

    public Toast getToast() {
        return mToast;
    }
}
