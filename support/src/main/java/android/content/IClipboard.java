package android.content;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

public interface IClipboard extends IInterface {

    public static abstract class Stub extends Binder implements IClipboard {

        private static final String DESCRIPTOR = "android.content.IClipboard";
        static final int TRANSACTION_setPrimaryClip                     = 1;
        static final int TRANSACTION_clearPrimaryClip                   = 2;
        static final int TRANSACTION_getPrimaryClip                     = 3;
        static final int TRANSACTION_getPrimaryClipDescription          = 4;
        static final int TRANSACTION_hasPrimaryClip                     = 5;
        static final int TRANSACTION_addPrimaryClipChangedListener      = 6;
        static final int TRANSACTION_removePrimaryClipChangedListener   = 7;
        static final int TRANSACTION_hasClipboardText                   = 8;

        private static class Proxy implements IClipboard {
            private final IBinder mRemote;

            private Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void setPrimaryClip(ClipData clipData, String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (clipData != null) {
                        obtain.writeInt(1);
                        clipData.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeString(str);
                    this.mRemote.transact(TRANSACTION_setPrimaryClip,
                            obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void clearPrimaryClip(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    this.mRemote.transact(TRANSACTION_clearPrimaryClip,
                            obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public ClipData getPrimaryClip(String str) throws RemoteException {
                ClipData clipData;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    this.mRemote.transact(TRANSACTION_getPrimaryClip,
                            obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        clipData = (ClipData) ClipData.CREATOR.createFromParcel(obtain2);
                    } else {
                        clipData = null;
                    }
                    return clipData;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public ClipDescription getPrimaryClipDescription(String str) throws RemoteException {
                ClipDescription clipDescription;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    this.mRemote.transact(TRANSACTION_getPrimaryClipDescription,
                            obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        clipDescription = ClipDescription.CREATOR.createFromParcel(obtain2);
                    } else {
                        clipDescription = null;
                    }
                    return clipDescription;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public boolean hasPrimaryClip(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    boolean z = false;
                    this.mRemote.transact(TRANSACTION_hasPrimaryClip,
                            obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        z = true;
                    }
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void addPrimaryClipChangedListener(IOnPrimaryClipChangedListener iOnPrimaryClipChangedListener, String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeStrongBinder(iOnPrimaryClipChangedListener != null ?
                            iOnPrimaryClipChangedListener.asBinder() :
                            null);
                    obtain.writeString(str);
                    this.mRemote.transact(TRANSACTION_addPrimaryClipChangedListener,
                            obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void removePrimaryClipChangedListener(IOnPrimaryClipChangedListener iOnPrimaryClipChangedListener) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeStrongBinder(iOnPrimaryClipChangedListener != null ?
                            iOnPrimaryClipChangedListener.asBinder() :
                            null);
                    this.mRemote.transact(TRANSACTION_removePrimaryClipChangedListener,
                            obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public boolean hasClipboardText(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    boolean z = false;
                    this.mRemote.transact(TRANSACTION_hasClipboardText,
                            obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        z = true;
                    }
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IClipboard asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IClipboard)) {
                return new Proxy(iBinder);
            }
            return (IClipboard) queryLocalInterface;
        }

        @Override
        public IBinder asBinder() {
            return this;
        }

        @Override
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            ClipData clipData;
            String str = DESCRIPTOR;
            if (i != 1598968902) {
                switch (i) {
                    case TRANSACTION_setPrimaryClip:
                        parcel.enforceInterface(str);
                        if (parcel.readInt() != 0) {
                            clipData = ClipData.CREATOR.createFromParcel(parcel);
                        } else {
                            clipData = null;
                        }
                        setPrimaryClip(clipData, parcel.readString());
                        parcel2.writeNoException();
                        return true;
                    case TRANSACTION_clearPrimaryClip:
                        parcel.enforceInterface(str);
                        clearPrimaryClip(parcel.readString());
                        parcel2.writeNoException();
                        return true;
                    case TRANSACTION_getPrimaryClip:
                        parcel.enforceInterface(str);
                        ClipData primaryClip = getPrimaryClip(parcel.readString());
                        parcel2.writeNoException();
                        if (primaryClip != null) {
                            parcel2.writeInt(1);
                            primaryClip.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_getPrimaryClipDescription:
                        parcel.enforceInterface(str);
                        ClipDescription primaryClipDescription = getPrimaryClipDescription(parcel.readString());
                        parcel2.writeNoException();
                        if (primaryClipDescription != null) {
                            parcel2.writeInt(1);
                            primaryClipDescription.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_hasPrimaryClip:
                        parcel.enforceInterface(str);
                        boolean hasPrimaryClip = hasPrimaryClip(parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(hasPrimaryClip ? 1 : 0);
                        return true;
                    case TRANSACTION_addPrimaryClipChangedListener:
                        parcel.enforceInterface(str);
                        addPrimaryClipChangedListener(IOnPrimaryClipChangedListener.Stub.asInterface(parcel.readStrongBinder()), parcel.readString());
                        parcel2.writeNoException();
                        return true;
                    case TRANSACTION_removePrimaryClipChangedListener:
                        parcel.enforceInterface(str);
                        removePrimaryClipChangedListener(IOnPrimaryClipChangedListener.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        return true;
                    case TRANSACTION_hasClipboardText:
                        parcel.enforceInterface(str);
                        boolean hasClipboardText = hasClipboardText(parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(hasClipboardText ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(i, parcel, parcel2, i2);
                }
            } else {
                parcel2.writeString(str);
                return true;
            }
        }
    }

    void addPrimaryClipChangedListener(IOnPrimaryClipChangedListener iOnPrimaryClipChangedListener, String str) throws RemoteException;

    void clearPrimaryClip(String str) throws RemoteException;

    ClipData getPrimaryClip(String str) throws RemoteException;

    ClipDescription getPrimaryClipDescription(String str) throws RemoteException;

    boolean hasClipboardText(String str) throws RemoteException;

    boolean hasPrimaryClip(String str) throws RemoteException;

    void removePrimaryClipChangedListener(IOnPrimaryClipChangedListener iOnPrimaryClipChangedListener) throws RemoteException;

    void setPrimaryClip(ClipData clipData, String str) throws RemoteException;
}
