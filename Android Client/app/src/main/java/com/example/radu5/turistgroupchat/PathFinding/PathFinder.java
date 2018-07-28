package com.example.radu5.turistgroupchat.PathFinding;

import com.example.radu5.turistgroupchat.Utils.PathFindingUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Created by radu5 on 4/13/2018.
 */

public class PathFinder
{

    //todo Probbly The best and most efficient version not sure yet
    //todo A* V1 (Heuristic Monotone) Only Visiting Nodes once , only getting best options from open set
    public static ArrayList<Node> aStarSearch1(double lat1,double lng1,double lat2,double lng2,ArrayList<BusRoute> busRoutes,double asteptareInStatie){
        int stari_expandate=0;

        //Setup Start and Goal Nodes
        Node start=new Node("Walking","Start");
        Node goal=new Node("Walking","Goal");
        goal.setLat(lat2);
        goal.setLng(lng2);
        goal.setRouteName("Final");
        goal.setPrev(null);
        goal.setH(0);
        goal.setRouteId(-1);
        start.setLat(lat1);
        start.setLng(lng1);
        start.setRouteName("Start");
        start.setPrev(null);
        start.setNrOfBusRoutesChanged(0);
        start.setRouteId(-1);
        start.setG(0);
        start.setH(calculareEuristica1(start,goal));
        start.setF(0+start.getH());
        start.setReached("Walking");

        //Setup closedSet and openSet
        ArrayList<Node> closedSet=new ArrayList<>();//nodes allready evaluated
        PriorityQueue<Node> openSet=new PriorityQueue<Node>(Comparator.comparingDouble(Node::getF));//discovered nodes not yet evaluated
        openSet.add(start);
        //A* LOOP
        while(!openSet.isEmpty()) {
            stari_expandate++;
            Node current=openSet.poll();//lowest fscore
            //todo H=monoton, Pov vizita un nod o singura data pt ca dupa prorpietate este cel mai mic scor de a ajunge pana in el
            if(current.equals(goal)){
                System.out.println("Am gasit ruta : " + stari_expandate+" ACTUAL G COST(timp):"+(double) current.getG() * 60+" Minute"+ " RUTE FOLOSITE:"+current.getPrev().getNrOfBusRoutesChanged());
                ArrayList<Node> lista=new ArrayList<>();
                printPath1(current,lista);

                return lista;
            }
            closedSet.add(current);
            //todo Generare posibilitati De mers din acest nod
            //todo Walking Nodes and Bus Nodes

            ArrayList<Node> posibilitati=getPosibilitati1(current,busRoutes);
            //Posibilitate default ( Din orice Node pot sa merg pe jos pana la statia finala)
            Node temp=new Node("Walking","Goal");
            temp.setLng(goal.getLng());
            temp.setLat(goal.getLat());
            temp.setRouteId(-1);
            double costG=PathFindingUtils.havesineDistance(current.getLat(),current.getLng(),temp.getLat(),temp.getLng())/5+current.getG();//5KM/h
            double h1=calculareEuristica1(current,goal);
            temp.setF(costG+h1);
            temp.setH(h1);
            temp.setG(costG);
            temp.setRouteName("Final");
            temp.setPrev(current);
            //todo MAYBE OPTIMIZATION
            //todo DACA DISTANTA E MAI MARE DE 0.5 MARESC COSTU INMULTESC CU 2 SAU 3 ETC
            //todo LAFEL SI LA GENERAREA DE WALKING NODES
            if(PathFindingUtils.havesineDistance(current.getLat(),current.getLng(),temp.getLat(),temp.getLng())<0.7){
                //daca e mai putin de 1k de mers pe jos din locatia asta o adaug ca posibilitate
                posibilitati.add(temp);

            }

            for(Node p:posibilitati){
                //Todo DACA SE AFLA IN CLOSED SET IL IGNOR(PROPRIETATE HEURISTICA MONOTONA)
                //todo CHECK CLOSED SET
                if(closedSet.contains(p)){
                    //ignore

                    continue;
                }
                else{
                    //todo Nu a mai fost evaluat(not in closeSet)
                    double g;
                    double h;
                    if(p.getReached().equals("Walking"))//Walk Node
                    {
                        p.setNrOfBusRoutesChanged(current.getNrOfBusRoutesChanged()+1);
                        g=PathFindingUtils.havesineDistance(current.getLat(),current.getLng(),p.getLat(),p.getLng())/5+asteptareInStatie+current.getG();
                        //todo ?? +asteptareInStatie Cred ca e problema sa astepte de 2 ori
                        ///5 = merge cu 5kmh average +0.05 a schimbat traseul 5 minute wait time
                    }
                    else{//Bus node
                        if(p.getRouteId()!=current.getRouteId()){
                            //A schimbat traseu adaug wait time
                            p.setNrOfBusRoutesChanged(current.getNrOfBusRoutesChanged()+1);
                            g=PathFindingUtils.havesineDistance(current.getLat(),current.getLng(),p.getLat(),p.getLng())/40+asteptareInStatie+current.getG();//plus 20% dintr-o ora wait time
                        }
                        else{
                            //nu a schimbat traseul
                            p.setNrOfBusRoutesChanged(current.getNrOfBusRoutesChanged());
                            g=PathFindingUtils.havesineDistance(current.getLat(),current.getLng(),p.getLat(),p.getLng())/40+current.getG();
                            //todo cu chestia asta il fac sa mearga cat mai mult cu un autobuz aka sa schimbe cat mai rar
                        }
                    }
                    h=calculareEuristica1(p,goal);
                    p.setG(g);
                    p.setH(h);
                    p.setF(g+h);



                    //todo CHECK OPEN SET
                    //todo Important (same node does not appear in the priority queue more than once)
                    //todo check if node to be added appears in pq if it does then parent pointers are changed to the lower cost path)
                    if(!openSet.contains(p)){
                        //todo Discover a new node
                        p.setPrev(current);
                        openSet.add(p);
                    }
                    else{
                        //Este in fringe verific daca am gasit o optiune mai buna de a ajunge aici
                        //todo Exists in openSet
                        ArrayList<Node> list=new ArrayList<>(openSet);

                        //IF THIS IS A BETTWER PATH
                        //todo BETTER G SCORE FOR THE SAME NODE FOUND?
                        if(p.getF()>=list.get(list.indexOf(p)).getF()){
                            //THIS IS NOT A BETTER PATH
                           // System.out.println("Starea se afla deja in open set si are F mai MIC DECAT P");
                            continue;

                        }
                        else{//todo THIS PATH IS THE BEST UNTIL NOW ( SE AFLA  DEJA IN OPEN SET DAR AM GASIT P CU UN SCOR MAI IEFTIN ASA CA INLOCUIESC)
                            //System.out.println("Starea se afla deja in open set si are F mai MARE DECAT P");
                            list.get(list.indexOf(p)).setPrev(current);
                            list.get(list.indexOf(p)).setG(g);
                            list.get(list.indexOf(p)).setH(h);
                            list.get(list.indexOf(p)).setF(g+h);
                            list.get(list.indexOf(p)).setReached(p.getReached());
                            list.get(list.indexOf(p)).setRouteId(p.getRouteId());
                            list.get(list.indexOf(p)).setRouteName(p.getRouteName());
                            list.get(list.indexOf(p)).setNume(p.getNume());
                            list.get(list.indexOf(p)).setNrOfBusRoutesChanged(p.getNrOfBusRoutesChanged());
                        }
                    }
                }
            }
            //todo IMPORTANT!;
            /*
            -Code Above Assumes Heuristic is consistent (Frequent case for shortest distance in road networks)
            -Daca nu era adevarat nodes from CLOSED SET ar putea fi rediscoperiti si costul lor impunatatit
            -Closed SET can be omited or check visited list if it has a better score and remove anything generated starting from that
            (NEW NODES ARE ADDED TO THE OPEN SET ONLY IF THEY HAVE A LOWER VALUE THAT AT ANY PREVIOUS ITERATION)
             */
            //todo Herustic admisible (never overestimates the actual min cost of reaching the goal) then A* is optimal if we dont use a closed set
            //todo If closed set is used => h must be monotonic for A* to be optimal (h(x) <=d(x,y)+h(y))
        }
    System.out.println("Not Possible To Reach Goal!"+stari_expandate);
        //NU S_A CGASIT TRASET IN FCT DE SETARILE MELE, SA NU MERGE MAI MULT DE 500 PE JOS
    return null;
    }
    //todo Euristica = cost in hours to reach
    public static double calculareEuristica1(Node a,Node b){//Fastest Path
        //Presupune Distanta timp pana la goal cu cel mai rapid autobuz(AKA 45km/h)
        return PathFindingUtils.havesineDistance(a.getLat(),a.getLng(),b.getLat(),b.getLng())/40;//cost in hours
    }
    //todo All possible nodes from current
    public static ArrayList<Node> getPosibilitati1(Node cur,ArrayList<BusRoute> busRoutes) {
        ArrayList<Node> posibilitatiiTranzitie=new ArrayList<>();
        ArrayList<Integer> traseeCareContinStatia=new ArrayList<>();

        //1.Statiile in care se poate ajunge cu autobuzul
        for(BusRoute i:busRoutes){
            ArrayList<BusStation> statii=i.getStatii();

            int pos=statii.indexOf(new BusStation("temp",cur.getLat(),cur.getLng()));//pozitia statiei in traseu
            if(pos!=-1 && pos!=statii.size()-1){
                //Daca exista statia si daca nu e ultima din traseu
                BusStation s=statii.get(pos+1);
                Node temp=new Node("Bus",s.getNume());
                temp.setLat(s.getLat());
                temp.setLng(s.getLng());
                temp.setRouteId(i.getId());
                temp.setRouteName(i.getNume());
                posibilitatiiTranzitie.add(temp);
                traseeCareContinStatia.add(i.getId());
            }
        }
        //2. Statiile in care pot ajunge mergand pe jos
        for(BusRoute i:busRoutes){
            if(!traseeCareContinStatia.contains(i.getId())){
                //nu are rost sa mearga pe jos intr-o statie dintr-un traseu care contine statia asta
                ArrayList<BusStation> statii=i.getStatii();
                BusStation closestStatie=statii.get(0);
                //cea mai apropriata statie de mers pe jos din orice alt traseu
                for(BusStation j:statii){

                    if(PathFindingUtils.havesineDistance(closestStatie.getLat(), closestStatie.getLng(), cur.getLat(), cur.getLng())>PathFindingUtils.havesineDistance(j.getLat(), j.getLng(), cur.getLat(),cur.getLng())){
                        closestStatie=j;
                    }
                }
                //Optimizare cat doreste sa mearga pe jos intre trasee maxim
                if(PathFindingUtils.havesineDistance(closestStatie.getLat(), closestStatie.getLng(), cur.getLat(), cur.getLng())>0.7){//daca e mai mult de 10km de mers pe jos
                    //daca e de mers pe jos mai mult de atat ignora
                    continue;
                }
                Node temp=new Node("Walking",closestStatie.getNume());
                temp.setLat(closestStatie.getLat());
                temp.setRouteName(i.getNume());
                temp.setLng(closestStatie.getLng());
                //todo PROBLEM: Daca statia cea mai apropriata din 2 trasee este aceasi CUM ALEGE PE CARE O PASTRAZA? CA AU ACELASI SCOR
                //cred ca ii baga pe ambii si il ia pe primu daca nu o sa il ia pe asta
                temp.setRouteId(i.getId());
                posibilitatiiTranzitie.add(temp);
            }
        }
        return posibilitatiiTranzitie;
    }
    public static void printPath1(Node root,ArrayList<Node> a)
    {
        if (root.getPrev() == null)
        {
            a.add(root);
            System.out.println(root.getRouteId()+" ["+root.getRouteName()+"] "+" : "+root.getNume()+" Timp:"+(double) (root.getG()) * 60+" Minute");
            System.out.println("##########");
            return;
        }
        printPath1(root.getPrev(),a);
        a.add(root);
        System.out.println(root.getRouteId()+" ["+root.getRouteName()+"] "+" : "+root.getNume()+"  ("+root.getReached()+")" +" Timp:"+(double) (root.getG()-root.getPrev().getG()) * 60+" Minute");
        System.out.println("##########");
    }
}
