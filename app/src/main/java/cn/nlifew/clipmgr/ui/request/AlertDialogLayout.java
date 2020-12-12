package cn.nlifew.clipmgr.ui.request;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;

import cn.nlifew.clipmgr.util.DisplayUtils;

/**
 * 这个 View 用于锁定 RequestDialog 的样式，防止因为每个 app 不同的 theme
 * 而出现不同的样式。
 * 为什么不使用 {@link androidx.appcompat.widget.AlertDialogLayout} ?
 * 因为这个 View 在宿主进程中被加载，一旦使用到 .xml 等资源文件，宿主进程
 * 就有可能崩溃。因此这个 View 也必须遵循同样的原则，所有资源都要通过 java 代码管理
 */
@SuppressLint("ViewConstructor")
public class AlertDialogLayout extends LinearLayout {
    private static final String TAG = "AlertDialogLayout";

    private static ContextThemeWrapper makeWrapper(Activity activity) {
        final @StyleRes int theme = android.R.style
                .Theme_Material_Light_Dialog_NoActionBar_MinWidth;
        return new ContextThemeWrapper(activity, theme);
    }

    public AlertDialogLayout(Activity activity) {
        super(makeWrapper(activity));

        Context context = getContext();

        setOrientation(VERTICAL);
        setBackgroundColor(Color.WHITE);

        addView(makeTitleLayout(context));
        addView(makeMessageLayout(context));
        addView(makeRememberLayout(context));
        addView(makeToolbarLayout(context));
    }

    private LinearLayout mTitleLayout;
    private ImageView mIconView;
    private TextView mTitleView;

    private TextView mMessageView;

    LinearLayout mRememberLayout;
    private TextView mRememberView;
    CheckBox mCheckBox;

    private LinearLayout mToolbarLayout;
    TextView mPositiveView;
    TextView mNegativeView;

    private View makeTitleLayout(Context context) {
        mTitleLayout = new LinearLayout(context);
        mTitleLayout.setOrientation(HORIZONTAL);
        mTitleLayout.setVisibility(GONE);

        int DP24 = DisplayUtils.dp2px(context, 24);
        int DP18 = DP24 / 4 * 3;
        mTitleLayout.setPadding(DP24, DP18, DP24, 0);

        mTitleLayout.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);

        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = DisplayUtils.dp2px(context, 5);
        mTitleLayout.setLayoutParams(lp);

        /* mIconView */

        mIconView = new ImageView(context);
        mIconView.setVisibility(GONE);

        int DP32 = DP24 / 3 * 4;
        lp = new LayoutParams(DP32, DP32);
        lp.setMarginEnd(DP24 / 3);
        mTitleLayout.addView(mIconView, lp);

        /* mTitleView */

        mTitleView = new TextView(context);
        mTitleView.setVisibility(GONE);
        mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        mTitleView.setTextColor(0xDE000000);
        mTitleView.setEllipsize(TextUtils.TruncateAt.END);

        lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mTitleLayout.addView(mTitleView, lp);

        return mTitleLayout;
    }

    private View makeMessageLayout(Context context) {
        mMessageView = new TextView(context);
        mMessageView.setVisibility(GONE);
        mMessageView.setTextColor(Color.BLACK);
        mMessageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        int DP24 = DisplayUtils.dp2px(context, 24);
        mMessageView.setPadding(DP24, 0, DP24, 0);
        mMessageView.setMinHeight(DP24 * 2);

        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = DisplayUtils.dp2px(context, 5);
        mMessageView.setLayoutParams(lp);

        return mMessageView;
    }

    private View makeRememberLayout(Context context) {
        mRememberLayout = new LinearLayout(context);
        mRememberLayout.setOrientation(HORIZONTAL);
        mRememberLayout.setVisibility(GONE);

        int DP24 = DisplayUtils.dp2px(context, 20);
        int DP10 = DisplayUtils.dp2px(context, 10);
        mRememberLayout.setPadding(DP24, DP10, DP24, DP10);

        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = DisplayUtils.dp2px(context, 5);
        mRememberLayout.setLayoutParams(lp);


        /* mCheckBox */
        mCheckBox = new CheckBox(context);
        lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
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

        lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mRememberLayout.addView(mRememberView, lp);

        return mRememberLayout;
    }


    private Button newToolButton(Context context) {
        ColorStateList csl = ColorStateList.valueOf(Color.GRAY);
        RippleDrawable ripple = new RippleDrawable(csl, null, null);
        ripple.addLayer(new ColorDrawable(Color.GRAY));
        ripple.setId(0, android.R.id.mask);

        Button btn = new Button(context);
        btn.setVisibility(GONE);
        btn.setTextColor(0xFFD81B60);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        btn.setBackground(ripple);
        return btn;
    }

    private View makeToolbarLayout(Context context) {
        mToolbarLayout = new LinearLayout(context);
        mToolbarLayout.setOrientation(HORIZONTAL);
        mToolbarLayout.setVisibility(GONE);
        mToolbarLayout.setGravity(Gravity.END);

        int DP12 = DisplayUtils.dp2px(context, 12);
        int DP4 = DP12 / 3;
        mToolbarLayout.setPadding(DP12, DP4, DP12, DP4);

        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mToolbarLayout.setLayoutParams(lp);

        /* mNegativeView */
        int DP64 = DisplayUtils.dp2px(context, 64);
        int DP48 = DisplayUtils.dp2px(context, 48);

        mNegativeView = newToolButton(context);
        mToolbarLayout.addView(mNegativeView, new ViewGroup.LayoutParams(
                DP64, DP48
        ));


        /* mPositiveView */
        mPositiveView = newToolButton(context);
        mToolbarLayout.addView(mPositiveView, new ViewGroup.LayoutParams(
                DP64, DP48
        ));
        return mToolbarLayout;
    }


    public void setTitle(@StringRes int title) {
        mTitleLayout.setVisibility(VISIBLE);
        mTitleView.setVisibility(VISIBLE);
        mTitleView.setText(title);
    }

    public void setIcon(@DrawableRes int icon) {
        mTitleLayout.setVisibility(VISIBLE);
        mIconView.setVisibility(VISIBLE);
        mIconView.setImageResource(icon);
    }

    public void setMessage(CharSequence msg) {
        if (TextUtils.isEmpty(msg)) {
            mMessageView.setVisibility(GONE);
        }
        else {
            mMessageView.setVisibility(VISIBLE);
            mMessageView.setText(msg);
        }
    }

    public void setRemember(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            mRememberLayout.setVisibility(GONE);
        }
        else {
            mRememberLayout.setVisibility(VISIBLE);
            mRememberView.setText(text);
        }
    }

    public void setPositive(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            mPositiveView.setVisibility(GONE);
            if (mNegativeView.getVisibility() == GONE) {
                mToolbarLayout.setVisibility(GONE);
            }
        }
        else {
            mToolbarLayout.setVisibility(VISIBLE);
            mPositiveView.setVisibility(VISIBLE);
            mPositiveView.setText(text);
        }
    }

    public void setNegative(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            mNegativeView.setText(View.GONE);
            if (mPositiveView.getVisibility() == GONE) {
                mToolbarLayout.setVisibility(GONE);
            }
        }
        else {
            mToolbarLayout.setVisibility(VISIBLE);
            mNegativeView.setVisibility(VISIBLE);
            mNegativeView.setText(text);
        }
    }
}
