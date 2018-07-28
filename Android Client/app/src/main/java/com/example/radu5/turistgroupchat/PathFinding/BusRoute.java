package com.example.radu5.turistgroupchat.PathFinding;

import java.util.ArrayList;

//todo pojo class for server route data
public class BusRoute
{
    private String nume;
    private int tip;
    private int id_bus_line;//id bus line
    private int id;//id traseu
    private ArrayList<BusStation> statii;

    public BusRoute( ArrayList<BusStation> statii,String nume, int tip, int id_bus_line, int id) {
        this.nume = nume;
        this.tip = tip;
        this.id_bus_line = id_bus_line;
        this.id = id;
        this.statii = statii;
    }

    public BusStation getStatie(int nr){
        return statii.get(nr);//get statia cu nr asta
    }


    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public int getTip() {
        return tip;
    }

    public void setTip(int tip) {
        this.tip = tip;
    }

    public int getId_bus_line() {
        return id_bus_line;
    }

    public void setId_bus_line(int id_bus_line) {
        this.id_bus_line = id_bus_line;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<BusStation> getStatii() {
        return statii;
    }

    public void setStatii(ArrayList<BusStation> statii) {
        this.statii = statii;
    }
}
