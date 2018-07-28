package com.example.radu5.turistgroupchat.Model;

/**
 * Created by radu5 on 4/23/2018.
 */

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusRoute implements Parcelable {

    private int busRouteId;
    private String busRouteName;
    private String cityName;
    private List<BusTrip> trips = null;

    public int getBusRouteId() {
        return busRouteId;
    }

    public void setBusRouteId(int busRouteId) {
        this.busRouteId = busRouteId;
    }

    public String getBusRouteName() {
        return busRouteName;
    }

    public void setBusRouteName(String busRouteName) {
        this.busRouteName = busRouteName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public List<BusTrip> getTrips() {
        return trips;
    }

    public void setTrips(List<BusTrip> trips) {
        this.trips = trips;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.busRouteId);
        dest.writeString(this.busRouteName);
        dest.writeString(this.cityName);
        dest.writeTypedList(this.trips);
    }

    public BusRoute() {
    }

    protected BusRoute(Parcel in) {
        this.busRouteId = in.readInt();
        this.busRouteName = in.readString();
        this.cityName = in.readString();
        this.trips = in.createTypedArrayList(BusTrip.CREATOR);
    }

    public static final Parcelable.Creator<BusRoute> CREATOR = new Parcelable.Creator<BusRoute>() {
        @Override
        public BusRoute createFromParcel(Parcel source) {
            return new BusRoute(source);
        }

        @Override
        public BusRoute[] newArray(int size) {
            return new BusRoute[size];
        }
    };
}