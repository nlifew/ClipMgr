package android.app;

public class ActivityThread {

    public static Application currentApplication() {
        // just compat
        return new Application();
    }
}
