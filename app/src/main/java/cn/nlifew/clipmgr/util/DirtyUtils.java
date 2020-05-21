package cn.nlifew.clipmgr.util;

import android.app.Activity;
import android.app.ActivityThread;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Map;

public class DirtyUtils {
    private static final String TAG = "DirtyUtils";


    private static boolean sIgnoreGetTopActivity;

    private static Field sActivitiesField;
    private static Field sStoppedField;
    private static Field sActivityField;

    public static Activity getTopActivity() {
        if (sIgnoreGetTopActivity) {
            return null;
        }
        try {

            if (sActivitiesField == null) {
                sActivitiesField = ReflectUtils.getDeclaredField(
                        ActivityThread.class, "mActivities"
                );

                Class<?> cls = Class.forName("android.app.ActivityThread$ActivityClientRecord");
                sActivityField = ReflectUtils.getDeclaredField(cls, "activity");
                sStoppedField = ReflectUtils.getDeclaredField(cls, "stopped");
            }
            @SuppressWarnings("unchecked")
            final Map<Object, Object> mActivities = (Map<Object, Object>)
                    sActivitiesField.get(ActivityThread.currentActivityThread());

            for (Object r : mActivities.values()) {
                Activity activity = (Activity) sActivityField.get(r);
                if (activity.isFinishing() || activity.isDestroyed()
                        || ((boolean) sStoppedField.get(r))) {
                    continue;
                }
                return activity;
            }
        } catch (Exception e) {
            sIgnoreGetTopActivity = true;
            Log.e(TAG, "getTopActivity: ", e);
        }
        return null;
    }
}
