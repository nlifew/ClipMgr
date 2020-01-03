package android.os;

public class Binder implements IBinder {

    public void attachInterface(IInterface i, String s) {

    }

    @Override
    public IInterface queryLocalInterface(String s) {
        return null;
    }

    @Override
    public boolean transact(int code, Parcel param, Parcel result, int flag) {
        return false;
    }

    @Override
    public boolean onTransact(int code, Parcel param, Parcel result, int flag) throws RemoteException {
        return false;
    }
}
