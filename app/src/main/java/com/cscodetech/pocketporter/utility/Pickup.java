package com.grader.user.utility;



import android.os.Parcel;
import android.os.Parcelable;

public class Pickup implements Parcelable {

    double lat;
    double log;
    String address;

    public Pickup(Parcel in) {
        lat = in.readDouble();
        log = in.readDouble();
        address = in.readString();
    }

    public static final Creator<Pickup> CREATOR = new Creator<Pickup>() {
        @Override
        public Pickup createFromParcel(Parcel in) {
            return new Pickup(in);
        }

        @Override
        public Pickup[] newArray(int size) {
            return new Pickup[size];
        }
    };

    public Pickup() {

    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLog() {
        return log;
    }

    public void setLog(double log) {
        this.log = log;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(lat);
        parcel.writeDouble(log);
        parcel.writeString(address);
    }
}
