package cn.nlifew.clipmgr.core;

import android.content.ClipData;
import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;
import android.system.Os;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ClipHook implements IXposedHookLoadPackage {
    private static final String TAG = "ClipHook";
    private static final String SYSTEM_SERVER = "android";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam param) throws Throwable {
        XposedBridge.log(TAG + " package: " + param.packageName +
                " pid: " + Os.getpid());

        if (Objects.equals(SYSTEM_SERVER, param.packageName)) {
            registerRequestDialogService();
        }
    }

    private static void registerRequestDialogService() {
        XposedBridge.log(TAG + " [" + Os.getuid() + ", " + Os.getpid() + "]");

        XposedBridge.hookAllMethods(ServiceManager.class,
                "addService", new XC_MethodHook() {

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        final String name = (String) param.args[0];
                        final IBinder service = (IBinder) param.args[1];

                        if (! Objects.equals(Context.CLIPBOARD_SERVICE, name)) {
                            return;
                        }

                        Method method = findAndCheckMethod(service);
                        if (method == null) {
                            XposedBridge.log(TAG + ": registerRequestDialogService: " +
                                    "missing setPrimaryClip(ClipData, String, ...)");
                            return;
                        }

                        Class<?>[] oldParams = method.getParameterTypes();
                        int length = oldParams.length;

                        Object[] newParams = new Object[length + 1];
                        System.arraycopy(oldParams, 0, newParams, 0, length);
                        newParams[length] = new XSetPrimaryClip2(method, service);

                        XposedHelpers.findAndHookMethod(service.getClass(),
                                "setPrimaryClip", newParams);

                        XposedBridge.log(TAG + ": registerRequestDialogService: done");
                    }
                });
    }

    private static Method findAndCheckMethod(IBinder service) {
        for (Method method : service.getClass().getDeclaredMethods()) {
            if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
                continue;
            }
            if (! "setPrimaryClip".equals(method.getName())) {
                continue;
            }

            Class<?>[] params = method.getParameterTypes();
            Class<?> result = method.getReturnType();

            if (params.length >= 2 && result == void.class
                    && params[0] == ClipData.class && params[1] == String.class) {
                return method;
            }
        }
        return null;
    }
}
