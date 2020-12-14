package android.os;

import java.util.HashMap;

public final class ServiceManager {

    private static IServiceManager sServiceManager;

    private static IServiceManager getIServiceManager() {
        throw new UnsupportedOperationException("getIServiceManager");
    }


    private static HashMap<String, IBinder> sCache = new HashMap<String, IBinder>();

    public static IBinder getService(String name) {
        throw new UnsupportedOperationException("getService");
    }

    public static void addService(String name, IBinder service) {
        throw new UnsupportedOperationException("addService");
    }
}
