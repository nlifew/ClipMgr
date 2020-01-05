package cn.nlifew.clipmgr.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

/**
 * @Deprecated use PackageRule instead
 */
@Deprecated
public class UserRule extends LitePalSupport implements Parcelable {
    public static final int RULE_GRANT = 1;
    public static final int RULE_DENY  = -1;
    public static final int RULE_REQUIRE = 0;
    public static final int RULE_DEFAULT = RULE_REQUIRE;

    public static int findPackageRule(String pkg) {
        UserRule rule = LitePal
                .where("pkg = ?", pkg)
                .findFirst(UserRule.class);
        return rule == null ? RULE_REQUIRE : rule.flag;
    }

    public static void savePackageRule(String pkg, int rule) {
        UserRule old = LitePal
                .where("pkg = ?", pkg)
                .findFirst(UserRule.class);
        if (old == null) {
            old = new UserRule(pkg);
        }
        old.flag = rule;
        old.save();
    }

    private String pkg;
    private int flag;


    public UserRule(String pkg) {
        this.pkg = pkg;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.pkg);
        dest.writeInt(this.flag);
    }

    public UserRule() {
    }

    public UserRule(String pkg, int rule) {
        this.pkg = pkg;
        this.flag = rule;
    }

    private UserRule(Parcel in) {
        this.pkg = in.readString();
        this.flag = in.readInt();
    }

    public static final Parcelable.Creator<UserRule> CREATOR = new Parcelable.Creator<UserRule>() {
        @Override
        public UserRule createFromParcel(Parcel source) {
            return new UserRule(source);
        }

        @Override
        public UserRule[] newArray(int size) {
            return new UserRule[size];
        }
    };
}
