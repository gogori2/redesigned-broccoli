package hr.s1.rma.fbmapa;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class User implements Parcelable {

    private int id;             // identifikator studenta
    private String name;        // ime studenta
    private String surname;     // prezime studenta

    private byte[] imageByteArray;  // avatar (slika)
    private int imageLength;        // velicina slike

    protected User(Parcel in) {
        id = in.readInt();
        name = in.readString();
        surname = in.readString();
        imageByteArray = in.createByteArray();
        imageLength = in.readInt();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(surname);
        dest.writeByteArray(imageByteArray);
        dest.writeInt(imageLength);
    }
}
