package cn.nlifew.clipmgr.ui.request;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import cn.nlifew.clipmgr.BuildConfig;
import cn.nlifew.clipmgr.ui.BaseActivity;
import cn.nlifew.clipmgr.util.DisplayUtils;

public class RequestActivity extends BaseActivity  {
    private static final String TAG = "RequestActivity";

    private static final String PREFIX = RequestActivity.class.getName();

    public static final String ACTION_ACTIVITY_HAS_FOCUS =
            PREFIX + ".ACTION_ACTIVITY_HAS_FOCUS";

    public static final class Builder implements Parcelable {
        private static final String TAG = "Builder";
        private static final String EXTRA_BUILDER =
                Builder.class.getName() + ".EXTRA_BUILDER";

        String mIcon;
        String mTitle;
        String mMessage;
        String mPositive;
        String mNegative;
        String mRemember;
        String mPackageName;
        boolean mCancelable;
        IRequestFinish mCallback;

        public Builder() {
        }

        public Builder setIcon(String path) {
            this.mIcon = path;
            return this;
        }

        public Builder setTitle(String title) {
            this.mTitle = title;
            return this;
        }

        public Builder setMessage(String msg) {
            this.mMessage = msg;
            return this;
        }

        public Builder setPositive(String text) {
            this.mPositive = text;
            return this;
        }

        public Builder setNegative(String text) {
            this.mNegative = text;
            return this;
        }

        public Builder setCancelable(boolean b) {
            this.mCancelable = b;
            return this;
        }

        public Builder setRemember(String text) {
            this.mRemember = text;
            return this;
        }

        public Builder setPackageName(String packageName) {
            this.mPackageName = packageName;
            return this;
        }

        public Builder setCallback(OnRequestFinishListener callback) {
            this.mCallback = callback;
            return this;
        }

        public Intent build() {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(
                    BuildConfig.APPLICATION_ID,
                    RequestActivity.class.getName()
            ));
            intent.putExtra(EXTRA_BUILDER, this);
            return intent;
        }

        public AlertDialog.Builder buildDialog(Activity activity) {
            return new DialogHelper(activity, this).buildAlertDialog();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mIcon);
            dest.writeString(this.mTitle);
            dest.writeString(this.mMessage);
            dest.writeString(this.mPositive);
            dest.writeString(this.mNegative);
            dest.writeString(this.mRemember);
            dest.writeString(this.mPackageName);
            dest.writeByte(this.mCancelable ? (byte) 1 : (byte) 0);
            dest.writeStrongBinder(this.mCallback == null ? null :
                    this.mCallback.asBinder());
        }

        private Builder(Parcel in) {
            this.mIcon = in.readString();
            this.mTitle = in.readString();
            this.mMessage = in.readString();
            this.mPositive = in.readString();
            this.mNegative = in.readString();
            this.mRemember = in.readString();
            this.mPackageName = in.readString();
            this.mCancelable = in.readByte() != 0;
            this.mCallback = IRequestFinish.Stub.asInterface(in.readStrongBinder());
        }

        public static final Creator<Builder> CREATOR = new Creator<Builder>() {
            @Override
            public Builder createFromParcel(Parcel source) {
                return new Builder(source);
            }

            @Override
            public Builder[] newArray(int size) {
                return new Builder[size];
            }
        };
    }

    private static final class DialogHelper extends Handler implements
            DialogInterface.OnCancelListener,
            DialogInterface.OnClickListener {

        Builder mBuilder;
        CheckBox mCheckBox;
        Activity mActivity;
        boolean mShouldCallback = true;

        DialogHelper(Activity activity, Builder builder) {
            super(Looper.getMainLooper());
            this.mActivity = activity;
            this.mBuilder = builder;
        }

        private AlertDialog.Builder buildAlertDialog() {
            final @StyleRes int theme = android.R.style.Theme_Material_Light_Dialog_NoActionBar_MinWidth;

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, theme);

            builder.setMessage(mBuilder.mMessage)
                    .setCancelable(mBuilder.mCancelable)
                    .setOnCancelListener(this);
            if (mBuilder.mIcon != null) {
                builder.setIcon(BitmapDrawable.createFromPath(mBuilder.mIcon));
            }
            if (mBuilder.mTitle != null) {
                builder.setTitle(mBuilder.mTitle);
            }
            if (mBuilder.mPositive != null) {
                builder.setPositiveButton(mBuilder.mPositive, this);
            }
            if (mBuilder.mNegative != null) {
                builder.setNegativeButton(mBuilder.mNegative, this);
            }
            if (mBuilder.mRemember != null) {
                ContextThemeWrapper context = new ContextThemeWrapper(mActivity, theme);

                LinearLayout layout = new LinearLayout(context);
                layout.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                int dp20 = DisplayUtils.dp2px(mActivity, 20);
                layout.setPadding(dp20, dp20 / 2, dp20, dp20 / 2);

                mCheckBox = new CheckBox(context);
                ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                lp.rightMargin = dp20 / 2;
                layout.addView(mCheckBox, lp);

                TextView tv = new TextView(context);
                tv.setText(mBuilder.mRemember);
                tv.setGravity(Gravity.CENTER_VERTICAL);
                tv.setTextColor(0xFF737373);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                layout.addView(tv,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                builder.setView(layout);
            }
            return builder;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            dialog.dismiss();
            onRequestFinish(OnRequestFinishListener.RESULT_CANCEL);
            if (mActivity instanceof RequestActivity) {
                mActivity.finish();
            }
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
            if (mActivity instanceof RequestActivity) {
                mActivity.finish();
            }
        }

        private void onRequestFinish(int result) {
            if (! mShouldCallback) {
                return;
            }
            mShouldCallback = false;

            if (mCheckBox != null && mCheckBox.isChecked()) {
                result |= OnRequestFinishListener.RESULT_REMEMBER;
            }

            if (mBuilder.mCallback != null) {
                Message msg = Message.obtain();
                msg.what = WHAT_CALLBACK;
                msg.obj = mBuilder.mCallback;
                msg.arg1 = result;
                sendMessage(msg);
            }
        }

        private static final int WHAT_CALLBACK = 10;

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WHAT_CALLBACK) {
                IRequestFinish callback = (IRequestFinish) msg.obj;
                try {
                    callback.onRequestFinish(msg.arg1);
                } catch (Exception e) {
                    Log.e(TAG, "handleMessage: ", e);
                }
            }
        }
    }

    private DialogHelper mHelper;
    private boolean mShouldNotifyRequest = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Builder builder = getIntent().getParcelableExtra(Builder.EXTRA_BUILDER);
        if (builder == null) {
            Log.e(TAG, "onCreate: no Builder found. use Builder.build to build a Intent");
            finish();
            return;
        }
        mHelper = new DialogHelper(this, builder);
        mHelper.buildAlertDialog().show();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume: start");
        super.onResume();

        if (mShouldNotifyRequest) {
            mShouldNotifyRequest = false;

            Intent intent = new Intent(ACTION_ACTIVITY_HAS_FOCUS);

            String packageName = mHelper.mBuilder.mPackageName;
            if (packageName != null) {
                intent.setPackage(packageName);
            }

            sendBroadcast(intent);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHelper.mShouldCallback) {
            mHelper.onRequestFinish(OnRequestFinishListener.RESULT_UNKNOWN);
        }
    }
}
