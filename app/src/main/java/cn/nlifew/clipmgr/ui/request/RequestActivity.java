package cn.nlifew.clipmgr.ui.request;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Bundle;
import android.os.Process;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import cn.nlifew.clipmgr.core.IClipMgr;
import cn.nlifew.clipmgr.ui.BaseActivity;
import cn.nlifew.clipmgr.util.DisplayUtils;

public class RequestActivity extends BaseActivity implements
        DialogInterface.OnClickListener,
        DialogInterface.OnCancelListener {
    private static final String TAG = "RequestActivity";

    public static final class Builder {
        private static final String PREFIX =
                "cn.nlifew.clipmgr.ui.Request.RequestActivity.EXTRA_BUILDER_";

        private static final String EXTRA_BUILDER_ID = PREFIX + "ID";
        private static final String EXTRA_BUILDER_ICON = PREFIX + "ICON";
        private static final String EXTRA_BUILDER_TITLE = PREFIX + "TITLE";
        private static final String EXTRA_BUILDER_MESSAGE = PREFIX + "MESSAGE";
        private static final String EXTRA_BUILDER_POSITIVE = PREFIX + "POSITIVE";
        private static final String EXTRA_BUILDER_NEGATIVE = PREFIX + "NEGATIVE";
        private static final String EXTRA_BUILDER_REMEMBER = PREFIX + "REMEMBER";
        private static final String EXTRA_BUILDER_CANCELABLE = PREFIX + "CANCELABLE";

        private final String mId;
        private CharSequence mTitle;
        private CharSequence mMessage;
        private CharSequence mPositive;
        private CharSequence mNegative;
        private CharSequence mRemember;
        private String mIcon;
        private boolean mCancelable;
        private OnRequestFinishListener mCallback;

        private static Map<String, SoftReference<OnRequestFinishListener>> CALLBACKS
                = new HashMap<>();

        public Builder(String id) {
            this.mId = id;
        }

        public Builder setTitle(CharSequence title) {
            mTitle = title;
            return this;
        }

        public Builder setMessage(CharSequence msg) {
            mMessage = msg;
            return this;
        }

        public Builder setPositive(CharSequence text) {
            mPositive = text;
            return this;
        }

        public Builder setNegative(CharSequence text) {
            mNegative = text;
            return this;
        }

        public Builder setRemember(CharSequence text) {
            mRemember = text;
            return this;
        }

        public Builder setIcon(String file) {
            mIcon = file;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            mCancelable = cancelable;
            return this;
        }

        public Builder setCallback(OnRequestFinishListener callback) {
            mCallback = callback;
            CALLBACKS.put(mId, new SoftReference<>(callback));
            return this;
        }

        public Intent build(Context c) {
            /* 到这里肯定有人会问：
             * 为什么不实现 Parcelable 呢，你这样一点都不优雅
             * 原因是：title，message 这些是 CharSequence 类型而不是 String
             * Parcel 并没有 writeCharSequence()，只有 writeString()
             */
            return new Intent(c, RequestActivity.class)
                    .putExtra(EXTRA_BUILDER_ID, mId)
                    .putExtra(EXTRA_BUILDER_ICON, mIcon)
                    .putExtra(EXTRA_BUILDER_TITLE, mTitle)
                    .putExtra(EXTRA_BUILDER_MESSAGE, mMessage)
                    .putExtra(EXTRA_BUILDER_POSITIVE, mPositive)
                    .putExtra(EXTRA_BUILDER_NEGATIVE, mNegative)
                    .putExtra(EXTRA_BUILDER_REMEMBER, mRemember)
                    .putExtra(EXTRA_BUILDER_CANCELABLE, mCancelable);
        }

        private Builder(Intent intent) {
            mId = intent.getStringExtra(EXTRA_BUILDER_ID);
            mIcon = intent.getStringExtra(EXTRA_BUILDER_ICON);
            mTitle = intent.getCharSequenceExtra(EXTRA_BUILDER_TITLE);
            mMessage = intent.getCharSequenceExtra(EXTRA_BUILDER_MESSAGE);
            mPositive = intent.getCharSequenceExtra(EXTRA_BUILDER_POSITIVE);
            mNegative = intent.getCharSequenceExtra(EXTRA_BUILDER_NEGATIVE);
            mRemember = intent.getCharSequenceExtra(EXTRA_BUILDER_REMEMBER);
            mCancelable = intent.getBooleanExtra(EXTRA_BUILDER_CANCELABLE, false);

            SoftReference<OnRequestFinishListener> callback;
            mCallback = (callback = CALLBACKS.remove(mId)) == null ?
                    null : callback.get();
        }
    }


    public interface OnRequestFinishListener {
        int RESULT_UNKNOWN  = 0;
        int RESULT_CANCEL   = 1;
        int RESULT_POSITIVE = 1 << 1;
        int RESULT_NEGATIVE = 1 << 2;
        int RESULT_REMEMBER = 1 << 3;

        void onRequestFinish(RequestActivity activity, String id, int result);
    }

    private Builder mRequest;
    private CheckBox mRemember;
    private boolean mShouldCallback = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();

        if (intent == null || (mRequest = new Builder(intent)).mId == null) {
            Log.w(TAG, "onCreate: use Builder to build a valid Intent");
            finish();
        }
        AlertDialog.Builder builder = buildAlertDialog();
        builder.show();
    }

    private AlertDialog.Builder buildAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(mRequest.mMessage)
                .setCancelable(mRequest.mCancelable)
                .setOnCancelListener(this);

        if (mRequest.mIcon != null) {
            builder.setIcon(BitmapDrawable.createFromPath(mRequest.mIcon));
        }
        if (mRequest.mTitle != null) {
            builder.setTitle(mRequest.mTitle);
        }
        if (mRequest.mPositive != null) {
            builder.setPositiveButton(mRequest.mPositive, this);
        }
        if (mRequest.mNegative != null) {
            builder.setNegativeButton(mRequest.mNegative, this);
        }
        if (mRequest.mRemember != null) {
            LinearLayout layout = new LinearLayout(this);
            layout.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            int dp20 = DisplayUtils.dp2px(this, 20);
            layout.setPadding(dp20, 0, dp20, 0);

            mRemember = new CheckBox(this);
            ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            lp.rightMargin = dp20 / 2;
            layout.addView(mRemember, lp);

            TextView tv = new TextView(this);
            tv.setText(mRequest.mRemember);
            tv.setGravity(Gravity.CENTER_VERTICAL);
            layout.addView(tv,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            builder.setView(layout);
        }
        return builder;
    }

    @Override
    protected void onDestroy() {
        if (mShouldCallback) {
            onRequestFinish(OnRequestFinishListener.RESULT_UNKNOWN);
        }
        super.onDestroy();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                onRequestFinish(OnRequestFinishListener.RESULT_POSITIVE);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                onRequestFinish(OnRequestFinishListener.RESULT_NEGATIVE);
                break;
        }
        finish();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        onRequestFinish(OnRequestFinishListener.RESULT_CANCEL);
        finish();
    }


    public void onRequestFinish(int result) {
        mShouldCallback = false;
        OnRequestFinishListener callback = mRequest.mCallback;
        if (callback == null) {
            return;
        }

        if (mRemember != null && mRemember.isChecked()) {
            result |= OnRequestFinishListener.RESULT_REMEMBER;
        }

        callback.onRequestFinish(this, mRequest.mId, result);
    }
}
