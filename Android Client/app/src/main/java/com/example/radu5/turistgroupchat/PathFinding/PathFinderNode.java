package com.example.radu5.turistgroupchat.PathFinding;

/**
 * Created by radu5 on 4/13/2018.
 */

public class PathFinderNode
{

    double g;
    double h;
    double f;
    PathFinderNode prev;

    String reached;//"Walking" or "Bus";
    String nume;//"Start" "End" or "Nume_Statie"
    int stopId;
    int tripId; //Din ce traseu face parte statia
    String routeName;
    int nrOfBusRoutesChanged;

    double lat;
    double lng;



    public PathFinderNode(String reached, String nume) {
        this.reached = reached;
        this.nume = nume;
    }

    public double getG() {
        return g;
    }

    public void setG(double g) {
        this.g = g;
    }

    public double getH() {
        return h;
    }

    public void setH(double h) {
        this.h = h;
    }

    public double getF() {
        return f;
    }

    public void setF(double f) {
        this.f = f;
    }

    public PathFinderNode getPrev() {
        return prev;
    }

    public void setPrev(PathFinderNode prev) {
        this.prev = prev;
    }

    public String getReached() {
        return reached;
    }

    public void setReached(String reached) {
        this.reached = reached;
    }

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public int getTripId() {
        return tripId;
    }

    public void setStopId(int stopId) {
        this.stopId = stopId;
    }

    public int getStopId() {
        return stopId;
    }

    public int getNrOfBusRoutesChanged() {
        return nrOfBusRoutesChanged;
    }

    public void setNrOfBusRoutesChanged(int nrOfBusRoutesChanged) {
        this.nrOfBusRoutesChanged = nrOfBusRoutesChanged;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getRouteName() {
        return routeName;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathFinderNode node = (PathFinderNode) o;
        if (Double.compare(node.lat, lat) != 0) return false;
        if(Double.compare(node.lng, lng) != 0) return false ;
        return Integer.compare(node.getTripId(),getTripId())==0 ;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(lat);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lng);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "["+getRouteName()+"] ("+getReached()+") "+"["+getNume()+"]"+" Timp:"+(double) (getG()) * 60+" Minute";
    }
}
