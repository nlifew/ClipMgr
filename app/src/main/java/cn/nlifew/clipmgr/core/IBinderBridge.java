package cn.nlifew.clipmgr.core;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileDescriptor;

class IBinderBridge implements IBinder {

    IBinderBridge(IBinder remote) {
        mRemote = remote;
    }

    final IBinder mRemote;


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
        return mRemote.queryLocalInterface(descriptor);
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
    public void linkToDeath(@NonNull DeathRecipient recipient, int flags) throws RemoteException {
        mRemote.linkToDeath(recipient, flags);
    }

    @Override
    public boolean unlinkToDeath(@NonNull DeathRecipient recipient, int flags) {
        return mRemote.unlinkToDeath(recipient, flags);
    }

    @Override
    public boolean transact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        return mRemote.transact(code, data, reply, flags);
    }
}
