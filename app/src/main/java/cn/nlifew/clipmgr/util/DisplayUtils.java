package cn.nlifew.clipmgr.util;

import android.content.Context;

public final class DisplayUtils {
    private DisplayUtils() {}

    public static int dp2px(Context c, float dp) {
        return (int) (c.getResources().getDisplayMetrics().
                density * dp + 0.5f);
    }

    public static int sp2px(Context c, float sp) {
        return (int) (c.getResources().getDisplayMetrics().
                scaledDensity * sp + 0.5f);
    }
}
