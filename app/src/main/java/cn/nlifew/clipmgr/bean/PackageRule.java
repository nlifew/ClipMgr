package cn.nlifew.clipmgr.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.crud.LitePalSupport;

public final class PackageRule extends LitePalSupport implements Parcelable {
    public static final int RULE_GRANT      =   1;
    public static final int RULE_DENY       =   2;
    public static final int RULE_REQUEST    =   0;

    public static final String COLUMN_PACKAGE   =   "pkg";
    public static final String COLUMN_RULE      =   "rule";

    public PackageRule(String pkg, int rule) {
        this.pkg = pkg;
        this.rule = rule;
    }

    private String pkg;
    private int rule;

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public int getRule() {
        return rule;
    }

    public void setRule(int rule) {
        this.rule = rule;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.pkg);
        dest.writeInt(this.rule);
    }

    public PackageRule() {
    }

    private PackageRule(Parcel in) {
        this.pkg = in.readString();
        this.rule = in.readInt();
    }

    public static final Creator<PackageRule> CREATOR = new Creator<PackageRule>() {
        @Override
        public PackageRule createFromParcel(Parcel source) {
            return new PackageRule(source);
        }

        @Override
        public PackageRule[] newArray(int size) {
            return new PackageRule[size];
        }
    };
}
