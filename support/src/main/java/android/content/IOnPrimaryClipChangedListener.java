package android.content;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOnPrimaryClipChangedListener extends IInterface {

    public static abstract class Stub extends Binder implements IOnPrimaryClipChangedListener {

        private static final String DESCRIPTOR = "android.content.IOnPrimaryClipChangedListener";
        static final int TRANSACTION_dispatchPrimaryClipChanged = 1;

        private static class Proxy implements IOnPrimaryClipChangedListener {
            private final IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void dispatchPrimaryClipChanged() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(TRANSACTION_dispatchPrimaryClipChanged, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOnPrimaryClipChangedListener asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IOnPrimaryClipChangedListener)) {
                return new Proxy(iBinder);
            }
            return (IOnPrimaryClipChangedListener) queryLocalInterface;
        }

        @Override
        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            String str = DESCRIPTOR;
            if (i == TRANSACTION_dispatchPrimaryClipChanged) {
                parcel.enforceInterface(str);
                dispatchPrimaryClipChanged();
                return true;
            } else if (i != 1598968902) {
                return super.onTransact(i, parcel, parcel2, i2);
            } else {
                parcel2.writeString(str);
                return true;
            }
        }

    }

    void dispatchPrimaryClipChanged() throws RemoteException;
}
