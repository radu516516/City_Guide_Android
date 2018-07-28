package com.example.radu5.turistgroupchat.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.radu5.turistgroupchat.PathFinding.BusStation;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by radu5 on 4/23/2018.
 */


import java.util.HashMap;
import java.util.Map;

//parcable allows me to pass instances of this class from activity to activity etc wrapped up in intents
public class BusStop implements Parcelable {

    private int stopId;
    private String stopName;
    private int order;
    private double lat;
    private double lng;

    public BusStop(String nume, double lat, double lng) {
        this.stopName = nume;
        this.lat = lat;
        this.lng = lng;
    }


    public int getStopId() {
        return stopId;
    }

    public void setStopId(int stopId) {
        this.stopId = stopId;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.stopId);
        dest.writeString(this.stopName);
        dest.writeInt(this.order);
        dest.writeDouble(this.lat);
        dest.writeDouble(this.lng);
    }

    public BusStop() {
    }

    protected BusStop(Parcel in) {
        this.stopId = in.readInt();
        this.stopName = in.readString();
        this.order = in.readInt();
        this.lat = in.readDouble();
        this.lng = in.readDouble();
    }

    public static final Parcelable.Creator<BusStop> CREATOR = new Parcelable.Creator<BusStop>() {
        @Override
        public BusStop createFromParcel(Parcel source) {
            return new BusStop(source);
        }

        @Override
        public BusStop[] newArray(int size) {
            return new BusStop[size];
        }
    };

    @Override
    public boolean equals(Object obj) {
        boolean isEqual= false;
        if (obj != null && obj instanceof BusStop)
        {
            isEqual = (this.lat == ((BusStop) obj).lat && this.lng == ((BusStop) obj).lng);
        }
        return isEqual;
    }
}