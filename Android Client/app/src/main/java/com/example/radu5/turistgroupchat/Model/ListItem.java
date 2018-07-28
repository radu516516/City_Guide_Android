package com.example.radu5.turistgroupchat.Model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by radu5 on 4/28/2018.
 */

public class ListItem implements Parcelable {
    private int id;
    private String nume;
    private String descriere;
    private double lat;
    private double lng;
    private String url;

    public ListItem(int id, String nume, String descriere, double lat, double lng, String url) {
        this.id = id;
        this.nume = nume;
        this.descriere = descriere;
        this.lat = lat;
        this.lng = lng;
        this.url = url;
    }
    public ListItem(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public String getDescriere() {
        return descriere;
    }

    public void setDescriere(String descriere) {
        this.descriere = descriere;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.nume);
        dest.writeString(this.descriere);
        dest.writeDouble(this.lat);
        dest.writeDouble(this.lng);
        dest.writeString(this.url);
    }

    protected ListItem(Parcel in) {
        this.id = in.readInt();
        this.nume = in.readString();
        this.descriere = in.readString();
        this.lat = in.readDouble();
        this.lng = in.readDouble();
        this.url = in.readString();
    }

    public static final Parcelable.Creator<ListItem> CREATOR = new Parcelable.Creator<ListItem>() {
        @Override
        public ListItem createFromParcel(Parcel source) {
            return new ListItem(source);
        }

        @Override
        public ListItem[] newArray(int size) {
            return new ListItem[size];
        }
    };

    @Override
    public String toString() {
        return nume;
    }
}
