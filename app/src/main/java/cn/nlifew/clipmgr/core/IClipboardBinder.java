package cn.nlifew.clipmgr.core;

import android.content.IClipboard;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileDescriptor;
import java.lang.reflect.Proxy;

import de.robv.android.xposed.XposedBridge;

final class IClipboardBinder implements IBinder {
    private static final String TAG = "IBinderWrapper";

    IClipboardBinder(IBinder remote) {
        mRemote = remote;
    }

    private final IBinder mRemote;


    @Nullable
    @Override
    public String getInterfaceDescriptor() throws RemoteException {
        return mRemote.getInterfaceDescriptor();
    }

    @Override
    public boolean pingBinder() {
        return mRemote.pingBinder();
    }

    @Override
    public boolean isBinderAlive() {
        return mRemote.isBinderAlive();
    }

    @Nullable
    @Override
    public IInterface queryLocalInterface(@NonNull String descriptor) {
        // 加入我们自己的逻辑
        IInterface iInterface = mRemote.queryLocalInterface(descriptor);
        if (iInterface != null) {
            XposedBridge.log(TAG + ": queryLocalInterface: " +
                    "ignore " + descriptor + " because mRemote returns a nonnull value");
            return iInterface;
        }

        try {
            IClipboard impl = IClipboard.Stub.asInterface(mRemote);
            Class<?> cls = impl.getClass();
            return (IInterface) Proxy.newProxyInstance(
                    cls.getClassLoader(),
                    cls.getInterfaces(),
                    new IClipboardImpl(impl));
        } catch (Throwable t) {
            // 尝试 catch 住任何可能的异常，否则可能造成源程序崩溃
            XposedBridge.log(TAG + ": queryLocalInterface: " + descriptor + "\n" + t);
        }
        return null;
    }

    @Override
    public void dump(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {
        mRemote.dump(fd, args);
    }

    @Override
    public void dumpAsync(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {
        mRemote.dumpAsync(fd, args);
    }

    @Override
    public boolean transact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        return mRemote.transact(code, data, reply, flags);
    }

    @Override
    public void linkToDeath(@NonNull DeathRecipient recipient, int flags) throws RemoteException {
        mRemote.linkToDeath(recipient, flags);
    }

    @Override
    public boolean unlinkToDeath(@NonNull DeathRecipient recipient, int flags) {
        return mRemote.unlinkToDeath(recipient, flags);
    }
}
