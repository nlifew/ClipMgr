package android.os;

public class Parcel {

    public static Parcel obtain() {
        return new Parcel();
    }

    public void writeInterfaceToken(String s) {

    }

    public void writeInt(int value) {

    }

    public void writeString(String value) {

    }

    public void writeStrongBinder(IBinder binder) {

    }

    public void writeNoException() {

    }

    public int readInt() {
        return 0;
    }

    public String readString() {
        return null;
    }

    public IBinder readStrongBinder() {
        return null;
    }

    public void readException() {

    }

    public void enforceInterface(String s) {

    }

    public void recycle() {

    }
}
