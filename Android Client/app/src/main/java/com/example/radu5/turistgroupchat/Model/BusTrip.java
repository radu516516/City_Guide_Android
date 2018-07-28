package com.example.radu5.turistgroupchat.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.radu5.turistgroupchat.PathFinding.BusStation;

import java.util.List;

/**
 * Created by radu5 on 4/23/2018.
 */



public class BusTrip implements Parcelable {

    private int tripId;
    private String tripName;
    private int direction;
    private int nrStatii;
    private List<BusStop> statii = null;

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getNrStatii() {
        return nrStatii;
    }

    public void setNrStatii(int nrStatii) {
        this.nrStatii = nrStatii;
    }

    public List<BusStop> getStatii() {
        return statii;
    }

    public void setStatii(List<BusStop> statii) {
        this.statii = statii;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.tripId);
        dest.writeString(this.tripName);
        dest.writeInt(this.direction);
        dest.writeInt(this.nrStatii);
        dest.writeTypedList(this.statii);
    }

    public BusTrip() {
    }

    protected BusTrip(Parcel in) {
        this.tripId = in.readInt();
        this.tripName = in.readString();
        this.direction = in.readInt();
        this.nrStatii = in.readInt();
        this.statii = in.createTypedArrayList(BusStop.CREATOR);
    }

    public static final Parcelable.Creator<BusTrip> CREATOR = new Parcelable.Creator<BusTrip>() {
        @Override
        public BusTrip createFromParcel(Parcel source) {
            return new BusTrip(source);
        }

        @Override
        public BusTrip[] newArray(int size) {
            return new BusTrip[size];
        }
    };

    @Override
    public String toString() {
        return this.tripName;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual= false;

        if (obj != null && obj instanceof BusTrip)
        {
            isEqual =this.tripId==((BusTrip) obj).getTripId();
        }
        return isEqual;
    }
}
