package cn.nlifew.clipmgr.core;

import android.content.IClipboard;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileDescriptor;
import java.lang.reflect.Field;

import de.robv.android.xposed.XposedBridge;


final class IClipBridge extends IBinderBridge {
    private static final String TAG = "IBinderWrapper";

    static int TRANSACTION_setPrimaryClip = -1;
    static String DESCRIPTOR = "";

    static {
        try {
            Class<? extends IClipboard.Stub> clazz = IClipboard.Stub.class;
            Field field = clazz.getDeclaredField("TRANSACTION_setPrimaryClip");
            field.setAccessible(true);
            TRANSACTION_setPrimaryClip = (Integer) field.get(null);

            field = clazz.getDeclaredField("DESCRIPTOR");
            field.setAccessible(true);
            DESCRIPTOR = (String) field.get(null);
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": static: failed to find TRANSACTION_setPrimary");
            XposedBridge.log(t);
        } finally {
            XposedBridge.log(TAG + ": static: TRANSACTION_setPrimaryClip: " +
                    TRANSACTION_setPrimaryClip + ", DESCRIPTOR: " + DESCRIPTOR);
        }
    }

    IClipBridge(IBinder remote) {
        super(remote);
    }

    private final XSetPrimaryClip mX = new XSetPrimaryClip();


    @Override
    public boolean transact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        // 一定要捕获住任何可能的异常，保证宿主不会崩溃
        try {
            if (code == TRANSACTION_setPrimaryClip) {
                return mX.setPrimaryClip(mRemote, code, data, reply, flags);
            }
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": transact: failed");
            XposedBridge.log(t);
        }
        return mRemote.transact(code, data, reply, flags);
    }

}
