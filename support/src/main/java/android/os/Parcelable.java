package android.os;

public interface Parcelable {

    void writeToParcel(Parcel parcel, int flag);

    interface CREATOR<T> {
        T createFromParcel(Parcel source);
        T[] newArray(int size);
    }
}
