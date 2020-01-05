package cn.nlifew.clipmgr.bean;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.crud.LitePalSupport;

public final class ActionRecord extends LitePalSupport implements Parcelable {
    public static final int ACTION_DENY     =   1;
    public static final int ACTION_GRANT    =   0;

    private String pkg;
    private String appName;
    private long time;
    private String text;
    private int action;

    public ActionRecord(String name, String pkg, String text, int action) {
        this.appName = name;
        this.pkg = pkg;
        this.text = text;
        this.action = action;
        this.time = System.currentTimeMillis();
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }


    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.pkg);
        dest.writeLong(this.time);
        dest.writeString(this.text);
        dest.writeInt(this.action);
        dest.writeString(this.appName);
    }

    public ActionRecord() {
    }

    private ActionRecord(Parcel in) {
        this.pkg = in.readString();
        this.time = in.readLong();
        this.text = in.readString();
        this.action = in.readInt();
        this.appName = in.readString();
    }

    public static final Creator<ActionRecord> CREATOR = new Creator<ActionRecord>() {
        @Override
        public ActionRecord createFromParcel(Parcel source) {
            return new ActionRecord(source);
        }

        @Override
        public ActionRecord[] newArray(int size) {
            return new ActionRecord[size];
        }
    };
}
