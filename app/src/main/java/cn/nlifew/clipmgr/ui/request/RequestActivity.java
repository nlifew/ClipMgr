package cn.nlifew.clipmgr.ui.request;

import android.app.AlertDialog;
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

import java.util.Map;

import cn.nlifew.clipmgr.ui.BaseActivity;
import cn.nlifew.clipmgr.util.DisplayUtils;

public class RequestActivity extends BaseActivity implements
        DialogInterface.OnClickListener,
        DialogInterface.OnCancelListener {
    private static final String TAG = "RequestActivity";
    private static final String EXTRA_BUILDER = "EXTRA_BUILDER";

    public static final class Builder implements Parcelable {
        private final String mId;
        private String mTitle;
        private String mMessage;
        private String mPositive;
        private String mNegative;
        private String mRemember;
        private String mIcon;
        private boolean mCancelable;
        private IRequestFinish mCallback;

        public Builder(String id) {
            this.mId = id;
        }

        public Builder setTitle(CharSequence title) {
            mTitle = title == null ? null : title.toString();
            return this;
        }

        public Builder setMessage(CharSequence msg) {
            mMessage = msg == null ? null : msg.toString();
            return this;
        }

        public Builder setPositive(CharSequence text) {
            mPositive = text == null ? null : text.toString();
            return this;
        }

        public Builder setNegative(CharSequence text) {
            mNegative = text == null ? null : text.toString();
            return this;
        }

        public Builder setRemember(CharSequence text) {
            mRemember = text == null ? null : text.toString();
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

        public Builder setCallback(OnRequestFinishListener l) {
            mCallback = l;
            return this;
        }

        public Intent build(Context c) {
            Intent intent = new Intent(c, RequestActivity.class);
            intent.putExtra(EXTRA_BUILDER, this);
            return intent;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mId);
            dest.writeString(this.mTitle);
            dest.writeString(this.mMessage);
            dest.writeString(this.mPositive);
            dest.writeString(this.mNegative);
            dest.writeString(this.mRemember);
            dest.writeString(this.mIcon);
            dest.writeByte(this.mCancelable ? (byte) 1 : (byte) 0);
            dest.writeStrongBinder(this.mCallback == null ? null : this.mCallback.asBinder());
        }

        private Builder(Parcel in) {
            this.mId = in.readString();
            this.mTitle = in.readString();
            this.mMessage = in.readString();
            this.mPositive = in.readString();
            this.mNegative = in.readString();
            this.mRemember = in.readString();
            this.mIcon = in.readString();
            this.mCancelable = in.readByte() != 0;
            this.mCallback = OnRequestFinishListener.asInterface(in.readStrongBinder());
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

    private Builder mRequest;
    private CheckBox mRemember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        mRequest = intent.getParcelableExtra(EXTRA_BUILDER);
        if (mRequest == null) {
            Log.w(TAG, "onCreate: call Request.request() to start this Activity");
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
    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                onRequestFinish(IRequestFinish.RESULT_POSITIVE);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                onRequestFinish(IRequestFinish.RESULT_NEGATIVE);
                break;
        }
        finish();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        onRequestFinish(IRequestFinish.RESULT_CANCEL);
        finish();
    }

    private void onRequestFinish(int result) {
        IRequestFinish callback = mRequest.mCallback;
        if (callback == null) {
            // 这一点都没有情趣，不回调你请求个毛啊
            return;
        }

        if (mRemember != null && mRemember.isChecked()) {
            result |= IRequestFinish.RESULT_REMEMBER;
        }

        callback.onRequestFinish(mRequest.mId, result);
    }


    private interface IRequestFinish extends IInterface {
        int RESULT_CANCEL   = 1;
        int RESULT_POSITIVE = 1 << 1;
        int RESULT_NEGATIVE = 1 << 2;
        int RESULT_REMEMBER = 1 << 3;
        void onRequestFinish(String id, int result);
    }

    private static final class IRequestFinishProxy implements IRequestFinish {

        private final IBinder mBinder;

        IRequestFinishProxy(IBinder binder) {
            mBinder = binder;
        }

        @Override
        public IBinder asBinder() {
            return mBinder;
        }

        @Override
        public void onRequestFinish(String id, int result) {
            final Parcel data = Parcel.obtain();
            final Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken(OnRequestFinishListener.DESCRIPTOR);
                data.writeString(id);
                data.writeInt(result);
                mBinder.transact(OnRequestFinishListener.TRANSACTION_onRequestFinish,
                        data, reply, 0);
                reply.readException();
            } catch (RemoteException e) {
                Log.e(TAG, "onRequestFinish: ", e);
            } finally {
                data.recycle();
                reply.recycle();
            }
        }
    }

    public static abstract class OnRequestFinishListener extends Binder implements IRequestFinish {

        private static final String DESCRIPTOR = OnRequestFinishListener.class.getName();

        private static final int TRANSACTION_onRequestFinish = FIRST_CALL_TRANSACTION;


        public OnRequestFinishListener() {
            attachInterface(this, DESCRIPTOR);
        }

        static IRequestFinish asInterface(IBinder binder) {
            if (binder == null) {
                return null;
            }
            final IInterface local = binder.queryLocalInterface(DESCRIPTOR);
            if (local instanceof IRequestFinish) {
                return (IRequestFinish) local;
            }
            return new IRequestFinishProxy(binder);
        }

        @Override
        public final IBinder asBinder() {
            return this;
        }

        @Override
        public final boolean onTransact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case INTERFACE_TRANSACTION:
                    reply.writeString(DESCRIPTOR);
                    return true;
                case TRANSACTION_onRequestFinish:
                    this.onRequestFinish(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
            }
            return super.onTransact(code, data, reply, flags);
        }
    }

}
