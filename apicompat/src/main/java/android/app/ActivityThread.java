package android.app;

import android.os.IBinder;

import java.util.HashMap;
import java.util.Map;

public class ActivityThread {

    static final class ActivityClientRecord {
        // Activity activity;
    }

    // HashMap or Map or ArrayMap
    final Map<IBinder, ActivityClientRecord> mActivities = new HashMap<>();

    public static Application currentApplication() {
        // just compat
        return new Application();
    }

    public static ActivityThread currentActivityThread() {
        return new ActivityThread();
    }
}
