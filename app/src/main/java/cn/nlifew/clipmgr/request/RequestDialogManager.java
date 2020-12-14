package cn.nlifew.clipmgr.request;


import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import java.util.Objects;

import cn.nlifew.clipmgr.service.RequestDialogService;

public class RequestDialogManager {
    private static final String TAG = "RequestDialogManager";

    public static RequestDialogManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (RequestDialogManager.class) {
                if (sInstance == null) {
                    sInstance = new RequestDialogManager(context);
                }
            }
        }
        return sInstance;
    }

    private static RequestDialogManager sInstance;

    private RequestDialogManager(Context context) {
        IBinder binder = ServiceManager.getService(RequestDialogService.NAME);
        mInterface = IRequestDialog.Stub.asInterface(binder);
        mContext = context.getApplicationContext();
    }

    private final Context mContext;
    private final IRequestDialog mInterface;

    public boolean available() {
        return mInterface != null;
    }

    public void show(RequestDialogParam param) {
        Objects.requireNonNull(param);
        String packageName = mContext.getPackageName();
        try {
            mInterface.show(packageName, param);
        } catch (RemoteException e) {
            Log.e(TAG, "show: failed when " + packageName, e);
        }
    }
}
