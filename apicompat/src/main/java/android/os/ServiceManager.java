package android.os;

import java.util.HashMap;

public final class ServiceManager {

    private static HashMap<String, IBinder> sCache = new HashMap<String, IBinder>();

    public static IBinder getService(String name) {
        throw new UnsupportedOperationException("getService");
    }
}
