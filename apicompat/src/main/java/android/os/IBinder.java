package android.os;

public interface IBinder {
    int FIRST_CALL_TRANSACTION  = 0x00000001;
    int LAST_CALL_TRANSACTION   = 0x00ffffff;
    int INTERFACE_TRANSACTION   = ('_'<<24)|('N'<<16)|('T'<<8)|'F';

    boolean transact(int code, Parcel param, Parcel result, int flag);
    boolean onTransact(int code, Parcel param, Parcel result, int flag) throws RemoteException;
    IInterface queryLocalInterface(String s);
}
