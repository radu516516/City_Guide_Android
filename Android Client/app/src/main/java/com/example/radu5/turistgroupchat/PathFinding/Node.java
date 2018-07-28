package com.example.radu5.turistgroupchat.PathFinding;

/**
 * Created by radu5 on 4/13/2018.
 */

public class Node
{
    
    //A* expands paths starting from node
    //Fringe = nodes from to select, Selects the lowest
    //IF H admissible and monotone A* more efficient , No Node needs to be processed more than once
    //(Closed set)
    //todo Ignores nodes allready visited saving time and its more efficient(euristica monotona) daca nu era monotona puteam sa revizitez o stare vizitata deja daca costul de ajungere pana aici este mai mic

    double g; //time to reach (COST)
    double h; //estimated time to reach goal(HEURISTIC)
    double f; //evaluation function
    Node prev;

    String reached;//"Walking" or "Bus";
    String nume;//"Start" "End" or "Nume_Statie"
    int routeId; //Din ce traseu face parte statia
    String routeName;
    int nrOfBusRoutesChanged;//Cate Trasee A Schimbat pt a ajunge in aceasta locatie

    double lat;//Locatia (Starile se identifica prin locatia lor)
    double lng;

    //Todo UN NOD POATE FI REVIZITAT , => II MODIFIC PARINTELE(Heuristica non monotona consistenta)


    public Node(String reached, String nume) {
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

    public Node getPrev() {
        return prev;
    }

    public void setPrev(Node prev) {
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

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
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

    //todo O stare se indentifica unic prin latitudinea si longitudinea sa
    //todo Daca o statie a fost vizitata deja si are un scor mai mare ca a doua vizitare o inlocuiesc
    //todo Daca o statie este in openset aleg modul cel mai ieftin de a ajunge in aceea statie sa fie in openset
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (Double.compare(node.lat, lat) != 0) return false;
        if(Double.compare(node.lng, lng) != 0) return false ;
        return Integer.compare(node.getRouteId(),getRouteId())==0 ;//&& node.getReached().equals(getReached());
        //todo Sa am in arbore si 48 Porta2 si 42 Poarta 2 chiar daca au aceasi locatie
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
