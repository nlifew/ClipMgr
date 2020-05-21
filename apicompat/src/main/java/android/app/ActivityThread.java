package android.app;

import android.os.IBinder;

import java.util.HashMap;
import java.util.Map;

public class ActivityThread {

    static final class ActivityClientRecord {
        // Activity activity;
    }

    // 这个类型不稳定，有的机型上是 ArrayMap 有的是 Map
    final Map<IBinder, ActivityClientRecord> mActivities = new HashMap<>();

    public static Application currentApplication() {
        // just compat
        return new Application();
    }

    public static ActivityThread currentActivityThread() {
        return new ActivityThread();
    }
}
