package android.app;

public class ActivityThread {

    static final class ActivityClientRecord {
        // Activity activity;
    }

//    final ArrayMap<IBinder, ActivityClientRecord> mActivities = new ArrayMap<>();

    public static Application currentApplication() {
        // just compat
        return new Application();
    }

    public static ActivityThread currentActivityThread() {
        return new ActivityThread();
    }
}
