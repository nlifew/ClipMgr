package cn.nlifew.clipmgr.request;

import android.os.Parcel;
import android.os.Parcelable;

public final class RequestDialogParam implements Parcelable {

    public boolean icon;
    public String title;
    public String message;
    public String positive;
    public String negative;
    public String remember;
    public boolean cancelable;
    public IRequestFinish callback;

    public RequestDialogParam() {
    }

    private RequestDialogParam(Parcel in) {
        icon = in.readInt() == 1;
        title = in.readString();
        message = in.readString();
        positive = in.readString();
        negative = in.readString();
        remember = in.readString();
        cancelable = in.readInt() == 1;
        callback = IRequestFinish.Stub.asInterface(in.readStrongBinder());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(icon ? 1 : 0);
        dest.writeString(title);
        dest.writeString(message);
        dest.writeString(positive);
        dest.writeString(negative);
        dest.writeString(remember);
        dest.writeInt(cancelable ? 1 : 0);
        dest.writeStrongInterface(callback);
    }

    public static final Creator<RequestDialogParam> CREATOR = new Creator<RequestDialogParam>() {
        @Override
        public RequestDialogParam createFromParcel(Parcel source) {
            return new RequestDialogParam(source);
        }

        @Override
        public RequestDialogParam[] newArray(int size) {
            return new RequestDialogParam[size];
        }
    };
}
