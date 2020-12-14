package cn.nlifew.clipmgr.core;

import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.ServiceManager;
import android.system.Os;

import java.util.Objects;

import cn.nlifew.clipmgr.service.RequestDialogService;
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
        else {
            registerClipboardProxy();
        }
    }

    private static void registerRequestDialogService() {
        XposedBridge.log(TAG + " [" + Os.getuid() + ", " + Os.getpid() + "]");

        XposedBridge.hookAllMethods(ServiceManager.class,
                "addService", new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        final String name = (String) param.args[0];
////                        final IBinder service = (IBinder) param.args[1];
//
                        if (! Objects.equals(Context.CLIPBOARD_SERVICE, name)) {
                            return;
                        }

                        XposedBridge.log(TAG + ": registerRequestDialogService: ready");

                        RequestDialogService service = new RequestDialogService();
                        ServiceManager.addService(RequestDialogService.NAME, service);

                        XposedBridge.log(TAG + ": registerRequestDialogService: done");
                    }
                });
    }

    private static void registerClipboardProxy() {
//        XposedBridge.log(TAG + ": registerClipboardProxy: start");

        XposedHelpers.findAndHookMethod(ServiceManager.class,
                "getService", String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        final String name = (String) param.args[0];
                        final IBinder binder = (IBinder) param.getResult();

                        if (! Context.CLIPBOARD_SERVICE.equals(name)
                                || binder instanceof Binder) {
                            return;
                        }

                        XposedBridge.log(TAG + ": afterHookedMethod: return ClipProxy");

                        IBinder bridge = new IClipBridge(binder);
                        param.setResult(bridge);

                        XposedBridge.log(TAG + ": afterHookedMethod: " + bridge);
                    }
                });

        XposedBridge.log(TAG + ": registerClipboardProxy: done");
    }
}
