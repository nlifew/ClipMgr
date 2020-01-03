package android.content;

import android.os.Parcel;
import android.os.Parcelable;

public class ClipDescription implements Parcelable {

    @Override
    public void writeToParcel(Parcel parcel, int flag) {

    }

    public static final CREATOR<ClipDescription> CREATOR = new CREATOR<ClipDescription>() {
        @Override
        public ClipDescription createFromParcel(Parcel source) {
            return null;
        }

        @Override
        public ClipDescription[] newArray(int size) {
            return new ClipDescription[0];
        }
    };
}
