package cn.nlifew.clipmgr.ui.request;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.telecom.Call;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.StyleRes;

import cn.nlifew.clipmgr.util.DisplayUtils;

public class SystemRequestDialog extends AlertDialog {
    private static final String TAG = "RequestDialog2";

    private static final @StyleRes int THEME = android.R.style
            .Theme_Material_Light_Dialog_NoActionBar_MinWidth;

    public SystemRequestDialog(Context context) {
        super(context, THEME);

        applyFlags(this);
    }

    private LinearLayout mRememberLayout;
    private CheckBox mCheckBox;
    private TextView mRememberView;

    private static void applyFlags(Dialog dialog) {
        final Window window = dialog.getWindow();

        window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        WindowManager.LayoutParams attrs = window.getAttributes();
        attrs.setTitle("ClipMgrBridge");

        window.setAttributes(attrs);
    }


    public void setRemember(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            if (mRememberLayout != null) {
                mRememberLayout.setVisibility(View.GONE);
            }
            return;
        }

        if (mRememberLayout == null) {
            Context context = new ContextThemeWrapper(getContext(), THEME);
            makeRememberLayout(context);
            setView(mRememberLayout);   // NOT setContentView() !!!
        }
        mRememberLayout.setVisibility(View.VISIBLE);
        mRememberView.setText(text);
    }

    public boolean isRememberChecked() {
        return mRememberLayout != null
                && mRememberLayout.getVisibility() == View.VISIBLE
                && mCheckBox.isChecked();
    }


    private void makeRememberLayout(Context context) {
        mRememberLayout = new LinearLayout(context);
        mRememberLayout.setOrientation(LinearLayout.HORIZONTAL);
        mRememberLayout.setVisibility(View.GONE);

        int DP24 = DisplayUtils.dp2px(context, 20);
        int DP10 = DisplayUtils.dp2px(context, 10);
        mRememberLayout.setPadding(DP24, DP10, DP24, DP10);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = DisplayUtils.dp2px(context, 5);
        mRememberLayout.setLayoutParams(lp);


        /* mCheckBox */
        mCheckBox = new CheckBox(context);
        lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        lp.rightMargin = DP10;
        mRememberLayout.addView(mCheckBox, lp);

        /* mRememberView */
        mRememberView = new TextView(context);
        mRememberView.setGravity(Gravity.CENTER_VERTICAL);
        mRememberView.setTextColor(0xFF737373);
        mRememberView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mRememberView.setOnClickListener(v -> mCheckBox.setChecked(! mCheckBox.isChecked()));

        lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mRememberLayout.addView(mRememberView, lp);
    }


    public static class Callback implements OnRequestFinishListener,
            DialogInterface.OnClickListener,
            DialogInterface.OnCancelListener,
            DialogInterface.OnDismissListener {

        public Callback() {
            mCallback = this;
        }

        public Callback(OnRequestFinishListener callback) {
            mCallback = callback;
        }

        private final OnRequestFinishListener mCallback;

        @Override
        public void onCancel(DialogInterface dialog) {
            dialog.dismiss();
            performCallback(dialog, OnRequestFinishListener.RESULT_CANCEL);
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    performCallback(dialog, RESULT_POSITIVE);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    performCallback(dialog, OnRequestFinishListener.RESULT_NEGATIVE);
                    break;
            }
        }

        @Override
        public void onDismiss(DialogInterface dialog) {

        }

        private void performCallback(DialogInterface d, @flag int result) {
            SystemRequestDialog dialog = (SystemRequestDialog) d;
            if (dialog.isRememberChecked()) {
                result |= RESULT_REMEMBER;
            }

            if (mCallback != null) {
                onRequestFinish(result);
            }
        }


        @Override
        public void onRequestFinish(int result) {

        }
    }
}
