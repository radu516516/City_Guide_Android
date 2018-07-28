package com.example.radu5.turistgroupchat.PathFinding;

//todo Load Data From Server in this class
public class BusStation
{
    private String nume;
    private double lat;
    private double lng;

    public BusStation(String nume, double lat, double lng) {
        this.nume = nume;
        this.lat = lat;
        this.lng = lng;
    }
    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
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
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual= false;

        if (obj != null && obj instanceof BusStation)
        {
            isEqual = (this.lat == ((BusStation) obj).lat && this.lng == ((BusStation) obj).lng);
        }

        return isEqual;
    }
}
