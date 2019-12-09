package android.content;

import android.os.Parcel;
import android.os.Parcelable;

public class ClipData implements Parcelable {

    @Override
    public void writeToParcel(Parcel parcel, int flag) {

    }

    public static final CREATOR<ClipData> CREATOR = new CREATOR<ClipData>() {
        @Override
        public ClipData createFromParcel(Parcel source) {
            return null;
        }

        @Override
        public ClipData[] newArray(int size) {
            return new ClipData[0];
        }
    };
}
