package com.example.radu5.turistgroupchat.PathFinding;

import android.util.Log;

import com.example.radu5.turistgroupchat.Model.BusStop;
import com.example.radu5.turistgroupchat.Model.BusTrip;
import com.example.radu5.turistgroupchat.Utils.PathFindingUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Created by radu5 on 4/13/2018.
 */

public class MyPathFinder
{

    public static ArrayList<PathFinderNode> aStarSearch(double lat1, double lng1, double lat2, double lng2, ArrayList<BusTrip> busTrips, double asteptareInStatie){
        int stari_expandate=0;
        //todo PRIN LAT LONG IDENTIFIC O STATIE

        //Setup Start and Goal PathFinderNodes
        PathFinderNode start=new PathFinderNode("Walking","Start");
        PathFinderNode goal=new PathFinderNode("Walking","Goal");
        goal.setLat(lat2);
        goal.setLng(lng2);
        goal.setRouteName("Final");
        goal.setPrev(null);
        goal.setH(0);
        goal.setTripId(-1);
        goal.setStopId(-1);
        start.setLat(lat1);
        start.setLng(lng1);
        start.setRouteName("Start");
        start.setPrev(null);
        start.setNrOfBusRoutesChanged(0);
        start.setTripId(-1);
        start.setStopId(-1);
        start.setG(0);
        start.setH(calculareEuristica1(start,goal));
        start.setF(0+start.getH());
        start.setReached("Walking");


        ArrayList<PathFinderNode> closedSet=new ArrayList<>();
        PriorityQueue<PathFinderNode> openSet=new PriorityQueue<PathFinderNode>(Comparator.comparingDouble(PathFinderNode::getF));
        openSet.add(start);

        //A* LOOP
        while(!openSet.isEmpty()) {
            stari_expandate++;
            PathFinderNode current=openSet.poll();

            if(current.equals(goal)){
                System.out.println("Am gasit ruta : " + stari_expandate+" ACTUAL G COST(timp):"+(double) current.getG() * 60+" Minute"+ " RUTE FOLOSITE:"+current.getPrev().getNrOfBusRoutesChanged());
                ArrayList<PathFinderNode> lista=new ArrayList<>();
                printPath1(current,lista);
                return lista;
            }
            closedSet.add(current);


            ArrayList<PathFinderNode> posibilitati=getPosibilitati1(current,busTrips);
            PathFinderNode temp=new PathFinderNode("Walking","Goal");
            temp.setLng(goal.getLng());
            temp.setLat(goal.getLat());
            temp.setTripId(-1);
            temp.setStopId(-1);
            double costG=PathFindingUtils.havesineDistance(current.getLat(),current.getLng(),temp.getLat(),temp.getLng())/4+current.getG();//5KM/h
            double h1=calculareEuristica1(current,goal);
            temp.setF(costG+h1);
            temp.setH(h1);
            temp.setG(costG);
            temp.setRouteName("Final");
            temp.setPrev(current);

            Double distToCurrent=PathFindingUtils.havesineDistance(current.getLat(),current.getLng(),temp.getLat(),temp.getLng());

            if(distToCurrent<=1){
                posibilitati.add(temp);
            }
            else{
                temp.setF(9999);//todo ONLY CHANGE NU ERA AICI BUT SEEMS TO WORK
                posibilitati.add(temp);
            }
            for(PathFinderNode p:posibilitati){

                if(closedSet.contains(p)){
                    //ignore
                    continue;
                }
                else{

                    double g;
                    double h;
                    if(p.getReached().equals("Walking"))
                    {
                        p.setNrOfBusRoutesChanged(current.getNrOfBusRoutesChanged()+1);
                        g=PathFindingUtils.havesineDistance(current.getLat(),current.getLng(),p.getLat(),p.getLng())/4+asteptareInStatie+current.getG();
                      /*  if(PathFindingUtils.havesineDistance(current.getLat(),current.getLng(),p.getLat(),p.getLng())>0.7 && PathFindingUtils.havesineDistance(current.getLat(),current.getLng(),p.getLat(),p.getLng()) <2.5)
                        {
                            g=g*2;
                        }
                        else if(PathFindingUtils.havesineDistance(current.getLat(),current.getLng(),p.getLat(),p.getLng())>2.5){
                            g=g*10;
                        }*/
                    }
                    else{//Bus node
                        if(p.getTripId()!=current.getTripId()){
                            p.setNrOfBusRoutesChanged(current.getNrOfBusRoutesChanged()+1);
                            g=PathFindingUtils.havesineDistance(current.getLat(),current.getLng(),p.getLat(),p.getLng())/40+asteptareInStatie+current.getG();
                        }
                        else{
                            p.setNrOfBusRoutesChanged(current.getNrOfBusRoutesChanged());
                            g=PathFindingUtils.havesineDistance(current.getLat(),current.getLng(),p.getLat(),p.getLng())/40+current.getG();
                        }
                    }
                    h=calculareEuristica1(p,goal);
                    p.setG(g);
                    p.setH(h);
                    p.setF(g+h);

                    if(!openSet.contains(p)){

                        p.setPrev(current);
                        openSet.add(p);
                    }
                    else{
                        ArrayList<PathFinderNode> list=new ArrayList<>(openSet);

                        if(p.getF()>=list.get(list.indexOf(p)).getF()){
                            continue;
                        }
                        else{
                            System.out.println("Gasit stare mai buna:"+p.getNume()+","+p.getReached()+","+p.getF()+" Decat:"+list.get(list.indexOf(p)).getNume()+","+list.get(list.indexOf(p)).getReached()+","+list.get(list.indexOf(p)).getF());
                            list.get(list.indexOf(p)).setPrev(current);
                            list.get(list.indexOf(p)).setG(g);
                            list.get(list.indexOf(p)).setH(h);
                            list.get(list.indexOf(p)).setF(g+h);
                            list.get(list.indexOf(p)).setReached(p.getReached());
                            list.get(list.indexOf(p)).setTripId(p.getTripId());
                            list.get(list.indexOf(p)).setStopId(p.getStopId());
                            list.get(list.indexOf(p)).setRouteName(p.getRouteName());
                            list.get(list.indexOf(p)).setNume(p.getNume());
                            list.get(list.indexOf(p)).setNrOfBusRoutesChanged(p.getNrOfBusRoutesChanged());
                        }
                    }
                }
            }
        }
    System.out.println("Not Possible To Reach Goal!"+stari_expandate);
    return null;
    }

    //cost in ore
    public static double calculareEuristica1(PathFinderNode a,PathFinderNode b){
        return PathFindingUtils.havesineDistance(a.getLat(),a.getLng(),b.getLat(),b.getLng())/40;
    }

    //todo Tranzitie dintr-un node
    public static ArrayList<PathFinderNode> getPosibilitati1(PathFinderNode cur,ArrayList<BusTrip> busTrips) {


        ArrayList<PathFinderNode> posibilitatiiTranzitie=new ArrayList<>();
        ArrayList<Integer> traseeCareContinStatia=new ArrayList<>();
        //1.Statiile in care se poate ajunge cu autobuzul
        for(BusTrip i:busTrips){
            int pos=i.getStatii().indexOf(new BusStop("temp",cur.getLat(),cur.getLng()));//pos statie in trip
            if(pos!=-1 && pos!=i.getStatii().size()-1){
                BusStop s=i.getStatii().get(pos+1);
                PathFinderNode temp=new PathFinderNode("Bus",s.getStopName());
                temp.setStopId(s.getStopId());
                temp.setLat(s.getLat());
                temp.setLng(s.getLng());
                temp.setTripId(i.getTripId());
                temp.setRouteName(i.getTripName());
                posibilitatiiTranzitie.add(temp);
                traseeCareContinStatia.add(i.getTripId());
            }
        }
for(BusTrip i:busTrips){
    if(!traseeCareContinStatia.contains(i.getTripId())){
        BusStop closestStatie=i.getStatii().get(0);
        for(BusStop j:i.getStatii()){
            if(PathFindingUtils.havesineDistance(closestStatie.getLat(), closestStatie.getLng(), cur.getLat(), cur.getLng())>PathFindingUtils.havesineDistance(j.getLat(), j.getLng(), cur.getLat(),cur.getLng())){
                closestStatie=j;
            }
        }
        if(PathFindingUtils.havesineDistance(closestStatie.getLat(), closestStatie.getLng(), cur.getLat(), cur.getLng())>1){
            continue;
        }
        PathFinderNode temp=new PathFinderNode("Walking",closestStatie.getStopName());
        temp.setStopId(closestStatie.getStopId());
        temp.setLat(closestStatie.getLat());
        temp.setRouteName(i.getTripName());
        temp.setLng(closestStatie.getLng());
        temp.setTripId(i.getTripId());
        posibilitatiiTranzitie.add(temp);
    }
}
        return posibilitatiiTranzitie;
    }
    public static void printPath1(PathFinderNode root,ArrayList<PathFinderNode> a)
    {
        if (root.getPrev() == null)
        {
            a.add(root);
            System.out.println(root.getTripId()+" ["+root.getRouteName()+"] "+" : "+root.getNume()+" Timp:"+(double) (root.getG()) * 60+" Minute");
            System.out.println("##########");
            return;
        }
        printPath1(root.getPrev(),a);
        a.add(root);
        System.out.println(root.getTripId()+" ["+root.getRouteName()+"] "+" : "+root.getNume()+"  ("+root.getReached()+")" +" Timp:"+(double) (root.getG()-root.getPrev().getG()) * 60+" Minute");
        System.out.println("##########");
    }
}
