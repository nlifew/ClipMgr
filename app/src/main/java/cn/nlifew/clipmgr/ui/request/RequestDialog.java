package cn.nlifew.clipmgr.ui.request;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import cn.nlifew.clipmgr.util.DisplayUtils;

public class RequestDialog {

    public interface OnRequestFinishListener {
        int RESULT_UNKNOWN  =   0;
        int RESULT_CANCEL   =   1;
        int RESULT_POSITIVE =   1 << 1;
        int RESULT_NEGATIVE =   1 << 2;
        int RESULT_REMEMBER =   1 << 3;


        @Retention(RetentionPolicy.SOURCE)
        @IntDef({RESULT_UNKNOWN, RESULT_CANCEL, RESULT_POSITIVE,
                RESULT_NEGATIVE, RESULT_REMEMBER})
        @interface flag {  }

        void onRequestFinish(@flag int result);
    }

    public static class Builder {

        private CharSequence mMessage;
        private CharSequence mPositive, mNegative;
        private CharSequence mRemember;
        private boolean mCancelable;
        private @StringRes int mTitle;
        private @DrawableRes int mIcon;
        private OnRequestFinishListener mCallback;

        public Builder setTitle(@StringRes int title) {
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

        public Builder setCancelable(boolean cancelable) {
            mCancelable = cancelable;
            return this;
        }

        public Builder setIcon(@DrawableRes int icon) {
            mIcon = icon;
            return this;
        }

        public Builder setCallback(OnRequestFinishListener callback) {
            mCallback = callback;
            return this;
        }

        public AlertDialog.Builder buildDialog(Activity activity) {
            return new DialogHelper(activity, this)
                    .buildAlertDialog();
        }
    }

    private static final class DialogHelper implements DialogInterface.OnClickListener,
            DialogInterface.OnCancelListener {

        DialogHelper(Activity activity, Builder builder) {
            mActivity = activity;
            mBuilder = builder;
        }

        private final Activity mActivity;
        private final Builder mBuilder;
        private CheckBox mCheckBox;
        private boolean mShouldCallback = true;


        AlertDialog.Builder buildAlertDialog() {
            final @StyleRes int theme = android.R.style.Theme_Material_Light_Dialog_NoActionBar_MinWidth;

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, theme);

            builder.setTitle(mBuilder.mTitle)
                    .setMessage(mBuilder.mMessage)
                    .setIcon(mBuilder.mIcon)
                    .setPositiveButton(mBuilder.mPositive, this)
                    .setNegativeButton(mBuilder.mNegative, this)
                    .setCancelable(mBuilder.mCancelable)
                    .setOnCancelListener(this);

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
                tv.setOnClickListener(v -> mCheckBox.setChecked(! mCheckBox.isChecked()));

                layout.addView(tv,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                builder.setView(layout);
            }
            return builder;
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
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            dialog.dismiss();
            onRequestFinish(OnRequestFinishListener.RESULT_CANCEL);
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
                mBuilder.mCallback.onRequestFinish(result);
            }
        }
    }
}
