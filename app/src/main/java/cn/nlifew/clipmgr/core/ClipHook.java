package cn.nlifew.clipmgr.core;

import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;
import android.system.Os;

import java.util.Objects;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
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

                        XposedBridge.hookAllMethods(service.getClass(),
                                "setPrimaryClip",
                                new XSetPrimaryClip2(service));

                        XposedBridge.log(TAG + ": registerRequestDialogService: done");
                    }
                });
    }
}
