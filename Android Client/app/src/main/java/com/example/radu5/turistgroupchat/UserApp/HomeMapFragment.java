package com.example.radu5.turistgroupchat.UserApp;

import android.Manifest;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.example.radu5.turistgroupchat.Model.BusRoute;
import com.example.radu5.turistgroupchat.Model.BusStop;
import com.example.radu5.turistgroupchat.Model.BusTrip;
import com.example.radu5.turistgroupchat.Model.ListItem;
import com.example.radu5.turistgroupchat.PathFinding.MyPathFinder;
import com.example.radu5.turistgroupchat.PathFinding.PathFinderNode;
import com.example.radu5.turistgroupchat.R;
import com.example.radu5.turistgroupchat.Utils.ApiHelper;
import com.example.radu5.turistgroupchat.Utils.PathFindingUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.ButtCap;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

/**
 * Created by radu5 on 4/28/2018.
 */

//todo Fragment In care userul vede harta,poate selecta un punct, poate afla drumul ce mai scurt tracking etc
//todo DACA A SELECTAT O LISTA O AFISEAZA AICI

//todo PASS DATA between components with PARCELABLE (from listDetailFragment to main activity to HomeMapFragment)
public class HomeMapFragment extends Fragment implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener
{
    private static final float DEFAULT_ZOOM = 15f;
    //todo SAU ASTA INCEPE cand e selectata o lista

    private static final String TAG = "HomeMapFragment";

    //Vars
    private ArrayList<BusRoute> busRoutes;//Detalii despre rute care se downloadeaza cand incepe asta //todo DONWLOADED BUS ROUTES
    private ArrayList<BusTrip> trasee = new ArrayList<>();//todo Date de intrare A*
    private double asteptareInStatie=0.08;//10% dintr-o ora//Cat de mult sa afecteze algoritmu schimbarea rutei
    private boolean hasSelectedList=false;
    private ArrayList<ListItem> listItems;//todo CAN BE NULL, PASS TO ADAPTER IF NOT NULL
    private Marker visitPlaceMarker;
    private int cityId=1;//todo WILL BE PASSED FROM MAIN ACTIVITY
    //Real time tracking stuff

    //Map Vars
    private GoogleMap mGoogleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Polyline pathPolyline;//Draw the shortest path, trips involved etc
    private ArrayList<Marker> tripsBusStopsMarkers=new ArrayList<>();

    //Socket Vars
    private ArrayList<String> roomNames;//todo AFTER SHORTEST PATH START RECEIVING DATA ABOUT REAL TIME BUS TRACKING
    //roomNames se seteaza cand calculez shortest path, in cazul daca vreau sa fac tracking sa le am
    private boolean isSocketConnected = false;
    private Socket mSocket;
    private HashMap<String,Marker> buses=new HashMap<>();//todo EACH BUS HAS UNIQUE STRING IDENTIFIER, (USED TO UPDATE BUSES POSITION )
    //reset on buttonin
    private boolean tracking=false;
    //Views
    private RecyclerView selectedListRecyclerView;
    private MapView map;
    private SelectedListRecyclerViewAdapter selectedListRecyclerViewAdapter;
    private Button busTrackingBtn;
    

    //selected list
    public static HomeMapFragment newInstance(ArrayList<ListItem> list,int Id) {
        Bundle args = new Bundle();
        args.putParcelableArrayList("list",list);//lista poate sa fie null
        args.putInt("cityid",Id);
        HomeMapFragment fragment = new HomeMapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        MapsInitializer.initialize(getContext());

        listItems=new ArrayList<>();


        listItems=getArguments().getParcelableArrayList("list");
        cityId=getArguments().getInt("cityid");

        if(listItems==null || listItems.size()==0){
            hasSelectedList=false;
        }
        else{
            hasSelectedList=true;
        }

        //TODO START ROUTE INFO DONWLOAD
        new FetchCityBusRouteDetails().execute(cityId);//get data, cant do anything without

    }
    private void setupAdapter() {
        if (isAdded()) {
            selectedListRecyclerViewAdapter=new SelectedListRecyclerViewAdapter(listItems,getContext());
            selectedListRecyclerView.setAdapter(selectedListRecyclerViewAdapter);
            //Attack click handler
            //todo HE CLICKED THE BUTTON
            selectedListRecyclerViewAdapter.setOnItemClickListener(new SelectedListRecyclerViewAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(ListItem i) {
                    if(visitPlaceMarker!=null)//daca am selectat alt place inainte
                    {visitPlaceMarker.remove();}
                    visitPlaceMarker=mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(i.getLat(),i.getLng())).title(i.getNume()).zIndex(1.01f));
                    moveCamera(new LatLng(i.getLat(),i.getLng()),DEFAULT_ZOOM);
                    calculateShortestPath(new LatLng(i.getLat(),i.getLng()));
                }
            });
        }
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //Setup Views
        View view = inflater.inflate(R.layout.fragment_user_main_map, container, false);

        if(hasSelectedList) {//daca a selectat o lista incarc recyclerview
            selectedListRecyclerView = view.findViewById(R.id.selectedListRecyclerView);


            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
            selectedListRecyclerView.setLayoutManager(layoutManager);
            selectedListRecyclerView.setHasFixedSize(true);
            selectedListRecyclerView.setItemViewCacheSize(20);
            selectedListRecyclerView.setDrawingCacheEnabled(true);
            selectedListRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            // selectedListRecyclerView.setItemAnimator(new SlideInUpAnimator());

            SnapHelper snapHelper = new LinearSnapHelper();
            snapHelper.attachToRecyclerView(selectedListRecyclerView);

            //Setup recyclerview adapter
            setupAdapter();
        }
        busTrackingBtn=view.findViewById(R.id.btnBusTrack);

        busTrackingBtn.setOnClickListener(new View.OnClickListener() {//todo E ca 2 butoane, start->stop tracking
            @Override
            public void onClick(View v) {
                tracking = !tracking;
                if(tracking){//Start Tracking
                    //Start tracking the trips in routeTrips;
                    if(roomNames!=null) {//todo ONLY IF CALCULATED SHORTEST PATH, CONNECTS TO TRACK (SELECT LOCATION , OR LONG CLICK ON MAP)
                        if (roomNames.size() > 0) {
                            buses = new HashMap<>();//new Bus Marckers,Clear Old
                            //todo
                            //Am rooms setat
                            connectSocket();
                            busTrackingBtn.setText("STOP TRACKING");
                        }
                    }
                    else{//NU am rooms setate
                        tracking=false;
                    }
                }
                else{
                    //Stop Tracking
                    disconnectSocket();
                    for(Marker i:buses.values())
                    {
                        i.remove();
                    }
                    buses=new HashMap<>();
                    busTrackingBtn.setText("START TRACKING");
                }
            }
        });

        return view;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        map = (MapView) view.findViewById(R.id.mapView);
        if (map != null) {
            //todo init mapView
            map.onCreate(null);
            map.onResume();
            //map callback
            map.getMapAsync(this);//get map
        }
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        mGoogleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);



        mGoogleMap.setOnMapLongClickListener(this);
    }
    private void moveCamera(LatLng latLng, float zoom) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mGoogleMap.animateCamera(cameraUpdate);
    }


    //todo CLICKED ON MAP FIND SHORTEST PATH TO ANY CLICKED POINT
    //todo PRACTIC REDESENEAZA PE HARTA SI SETEAZA roomNames;
    @Override
    public void onMapLongClick(LatLng latLng) {

        //todo SHORTEST PATH TO ANY LOCATION
        calculateShortestPath(latLng);//todo Calculeaza shortest path,deseneaza
    }

    public void calculateShortestPath(LatLng latLng){//todo Destinatia
        //todo Daca nu am datele astea nu pot calcula
        if(busRoutes!=null&&trasee!=null){//AKA DACA AVEM DATE
            try {
                Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        //todo got my location
                        if (task.isSuccessful()) {//&&task.getRestult()!=null
                            Location currentLocation = (Location) task.getResult();
                            if(currentLocation==null){
                                Toast.makeText(getActivity(),"couldnt get your location!",Toast.LENGTH_LONG).show();
                                return;
                            }
                            LatLng latLng1 = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            Log.d(TAG, "onComplete: got device location");

                            //Calc shortest path
                            ArrayList<PathFinderNode> result= MyPathFinder.aStarSearch(latLng1.latitude,latLng1.longitude ,latLng.latitude, latLng.longitude,trasee,asteptareInStatie);

                            //todo RESET, CLEAR MAP, DISCONNECTED FROM SERVER RE INIT VALS
                            //todo IF ALLREADY TRACKING STOP AND CLEAR MAP
                            if(mSocket!=null){
                                if(mSocket.connected()==true){
                                    disconnectSocket();

                                    for(Marker i:buses.values())//redundant cuz map.clear under
                                    {
                                        i.remove();
                                    }
                                    buses=new HashMap<>();
                                    busTrackingBtn.setText("START TRACKING");
                                    tracking=false;
                                    //todo DISCONNECT EACH TIME U RECALCULATE NEW ROUTE SO IT LEAVES ALL ROOMS
                                }
                            }
                            mGoogleMap.clear();
                            roomNames=new ArrayList<>();//RESET ROOM NAMES
                            buses=new HashMap<>();//De fiecare data cand recalculez un drum clar resetes autobuzele
                            //mGoogleMap.clear();
                            //Draw Traseu On Map
                            PatternItem dot=new Dot();
                            PatternItem gap=new Gap(5);
                            List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(gap, dot);
                            Set<Integer> stops=new HashSet<Integer>();
                            HashMap<Integer, Integer> displayedStops=new HashMap<>();//stop id tripit
                            Set<Integer> switchRouteStops=new HashSet<Integer>();
                            Set<Integer> trips = new HashSet<Integer>();//TRIPS CONTINUTE DE SHORTEST PATH
                            //todo DRAW PART
                            if(result==null){

                                return;//todo THERE IS NO ROUTE ( TO REACH)
                            }
                            else{
                                //todo SELECT TRIPS AND STOPS CARE ALCATUIESC SOLUTIA
                                for(int i=0;i<result.size();i++){

                                    if(result.get(i).getTripId()!=-1 &&result.get(i).getStopId()!=-1) {
                                        trips.add(result.get(i).getTripId());//todo TRIPS I NEED TO REAL TIME TRACK
                                        stops.add(result.get(i).getStopId());
                                    }
                                    //Check if next station is schimbare de ruta
                                    if(i+1<result.size()){
                                        if(result.get(i).getTripId()!=result.get(i+1).getTripId()){
                                            if (result.get(i + 1).getNume().equals("Goal")) {
                                                switchRouteStops.add(result.get(i).getStopId());
                                                displayedStops.put(result.get(i).getStopId(),result.get(i).getTripId());
                                            }
                                            else{
                                                //Se schimba trip aici, tin minte statia
                                                //todo DACA E WALKING AFISEAZA SI COBORAT SI URCAT
                                                if(result.get(i+1).getReached().equals("Walking")){
                                                    switchRouteStops.add(result.get(i).getStopId());
                                                    switchRouteStops.add(result.get(i+1).getStopId());
                                                    displayedStops.put(result.get(i).getStopId(),result.get(i).getTripId());
                                                    displayedStops.put(result.get(i+1).getStopId(),result.get(i+1).getTripId());
                                                }
                                                else{//todo SWITCH ROUTE BUT WITH BUS, NEED TO DISPLAY PROPERLY

                                                    //todo TRY AFISARE DIRECT AICI A MARKERELOR

                                                    displayedStops.put(result.get(i).getStopId(),result.get(i+1).getTripId());//todo THIS WORKS
                                                    switchRouteStops.add(result.get(i).getStopId());//Nu stie in cine sa schimbe
                                                }

                                            }
                                        }
                                    }
                                }
                                int[] tripColors= getContext().getResources().getIntArray(R.array.tripcolors);
                                int c=0;
                                //todo DRAW ENTIRE TRIP
                                BitmapDescriptor defaultMarker =
                                        BitmapDescriptorFactory.fromBitmap(getMarker(R.drawable.ic_bus_stop3));
                                for(BusRoute b:busRoutes){
                                    for (BusTrip t : b.getTrips()) {
                                        if(trips.contains(t.getTripId())) {
                                            PolylineOptions po = new PolylineOptions().width(10).jointType(2).startCap(new ButtCap());
                                            for (BusStop s : t.getStatii()) {
                                                LatLng point = new LatLng(s.getLat(), s.getLng());
                                                //todo temp
                                                if(displayedStops.get(s.getStopId())!=null)
                                                {
                                                    //daca exista statia aia
                                                    if(displayedStops.get(s.getStopId()).equals(t.getTripId())==true)
                                                    {
                                                        mGoogleMap.addMarker(new MarkerOptions()
                                                                .position(point).title("Route: "+b.getBusRouteName()).snippet("Trip: "+t.getTripName()+" | Nr: "+s.getOrder()+" | Stop: "+s.getStopName()).icon(defaultMarker));
                                                    }
                                                }
                                                //todo temp

                                              /*  if(switchRouteStops.contains(s.getStopId())){
                                                    mGoogleMap.addMarker(new MarkerOptions()
                                                            .position(point).title("Route: "+b.getBusRouteName()).snippet("Trip: "+t.getTripName()+" | Nr: "+s.getOrder()+" | Stop: "+s.getStopName()).icon(defaultMarker));
                                                }*/
                                                po.add(point);
                                            }
                                            mGoogleMap.addPolyline(po.color(tripColors[c]));
                                            c++;
                                        }
                                    }
                                }
                                //todo OVERLAP THE DRAW ROUTES
                                //start point
                                LatLng l=new LatLng(result.get(0).getLat(),result.get(0).getLng());
                                //todo draw result
                                for(int i = 1;i<=result.size()-1;i++){

                                    if(result.get(i).getReached().equals("Walking")){
                                        mGoogleMap.addPolyline(new PolylineOptions().add(l).add(new LatLng(result.get(i).getLat(),result.get(i).getLng())).width(10).color(Color.BLACK).jointType(2).pattern(PATTERN_POLYLINE_DOTTED));
                                        l=new LatLng(result.get(i).getLat(),result.get(i).getLng());
                                    }
                                    else{

                                        mGoogleMap.addPolyline(new PolylineOptions().add(l).add(new LatLng(result.get(i).getLat(),result.get(i).getLng())).width(13).color(Color.GREEN).jointType(2).zIndex(1).endCap(new RoundCap()));
                                        l=new LatLng(result.get(i).getLat(),result.get(i).getLng());
                                    }
                                }
                                mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(result.get(result.size()-1).getLat(),result.get(result.size()-1).getLng())).title("Goal").snippet("Final Location"));
                                //todo END OF DRAWING

                                //todo START TRACKING

                                //todo CREATE ROOM NAMES
                                for(Integer i:trips){
                                    roomNames.add("TrackingRoom"+i);//Rooms To Join
                                }
                                //Connect Socket
                                // connectSocket();
                                //todo CONNECT START TRACKING ONLY ON BUSCLICK

                            }
                        } else {
                            Toast.makeText(getActivity(), "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } catch (SecurityException e) {
                Log.e(TAG, "getDeviceLocation: " + e.getMessage());
            }
        }
    }



    //todo TODO LATER : MAYBE USE ASYNC TASK LOADER TO DOALOAD THIS
    //todo ASYNC TASK TO GET BUS ROUTES DATA
    private class FetchCityBusRouteDetails extends AsyncTask<Integer,Void,ArrayList<BusRoute>> {

        @Override
        protected ArrayList<BusRoute> doInBackground(Integer... ints) {//city id
            return ApiHelper.getBusRoutesCity(Integer.valueOf(ints[0]));
        }

        @Override
        protected void onPostExecute(ArrayList<BusRoute> b) {
            if(b==null ){
                busRoutes=null;
                return;
            }
            if(b.size()==0){
                busRoutes=null;
                return;
            }
            //todo Download completed its nice
            Toast.makeText(getActivity(),"Downloaded",Toast.LENGTH_SHORT).show();
            busRoutes=b;
            //todo CAN START FINDING SHORTEST PATHS NOW
            for(BusRoute i:b){
                trasee.addAll(i.getTrips());
            }
        }
    }

    public void connectSocket(){
        try{
            Manager manager=new Manager(new URI("http://raduhdd.asuscomm.com:3000"));
            mSocket = manager.socket("/bus-tracking");
            mSocket.connect();
            isSocketConnected=true;
            configSocketEvents();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
    public void disconnectSocket(){
        Log.d(TAG, "disconnectSocket: DISCONNECTED FROM SERVER");
        isSocketConnected=false;
        if(mSocket==null){
            return;
        }
        mSocket.off(Socket.EVENT_CONNECT,onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT,onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR,onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT,onConnectError);
        mSocket.off("socketID",onSocketId );
        mSocket.off("serverMessage",onServerMessage);
        mSocket.off("busMoved",onUpdateLocation);
        mSocket.off("busLeftRoom",busLeftRoom);
        mSocket.disconnect();
    }
    public void configSocketEvents(){
        mSocket.on(Socket.EVENT_CONNECT,onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT,onDisconnect );
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("socketID",onSocketId );
        mSocket.on("serverMessage",onServerMessage);
        mSocket.on("busMoved",onUpdateLocation);
        mSocket.on("busLeftRoom",busLeftRoom);
    }
    private Emitter.Listener onConnect=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "onConnect: Client connected to server");
            JSONArray arr=new JSONArray();
            JSONObject obj=new JSONObject();
            try {
                if(roomNames.size()>0){
                    obj.put("type","user");
                    for(String i:roomNames){
                        arr.put(i);
                    }
                    obj.put("rooms",arr);
                    mSocket.emit("joinBusTrackingRoom", obj);//Join multiple rooms
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private Emitter.Listener onDisconnect=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "onDisconnect: Client Disconnected to the server");
        }
    };
    private Emitter.Listener onConnectError=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "Client error connecting");
            //todo it keeps trying to reconnect to infinity
            //mSocket.disconnect();

        }
    };
    private Emitter.Listener onSocketId=new Emitter.Listener() {
        @Override
        public void call(Object... args) {


            JSONObject data=(JSONObject)args[0];
            try {
                String id=data.getString("id");
                Log.d(TAG, "SocketID: Your socket id on the server is : "+id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    //todo Message From server
    private Emitter.Listener onServerMessage= new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            JSONObject data=(JSONObject)args[0];
            String username;
            String message;
            String createdAt;
            try {
                username=data.getString("from");
                message=data.getString("text");
                createdAt=data.getString("createdAt");
                Log.d(TAG, "ServerMessage: "+username+":"+" "+message+" "+createdAt);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    //todo FIECARE BUS ARE UN NUME UNIC
    //todo UN UTILIZATOR PRIMESTE EVENIMENTE DE LA AUTOBUZE DIN MAI MULTE CAMERE
    private Emitter.Listener onUpdateLocation=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //Got an update from some bus
            JSONObject data=(JSONObject)args[0];
            String busName;
            String lat;
            String lng;
            try {
                busName=data.getString("from");//Name of the bus
                lat=data.getString("latitude");
                lng=data.getString("longitude");
                Log.d(TAG, "Update Bus Location: "+busName+":"+" "+lat+","+lng);
                //todo REMOVE AND UPDATE MARKER ON MAP
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!buses.containsKey(busName)){
                            BitmapDescriptor defaultMarker= BitmapDescriptorFactory.fromBitmap(getMarker(R.drawable.ic_bus_vector));
                            Marker otherBusMaker=mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),Double.parseDouble(lng))).title(busName).icon(defaultMarker).zIndex(1.0f));
                            buses.put(busName,otherBusMaker);
                        }else{
                            //exista deja marker remove it
                            if(buses.get(busName)!=null)
                            {
                                buses.get(busName).remove();
                                //reafiseazal
                                BitmapDescriptor defaultMarker= BitmapDescriptorFactory.fromBitmap(getMarker(R.drawable.ic_bus_vector));
                                Marker otherBusMaker=mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),Double.parseDouble(lng))).title(busName).icon(defaultMarker).zIndex(1.0f));
                                buses.put(busName,otherBusMaker);
                            }
                        }
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    //todo bus has left some room i am tracking

    private Emitter.Listener busLeftRoom=new Emitter.Listener() {
        //todo a bus has left the room i am in, remove its marker
        @Override
        public void call(Object... args) {
            JSONObject data=(JSONObject)args[0];

            String busName;
            String room;

            try {
                busName=data.getString("from");//Name of the bus
                room=data.getString("text");
                Log.d(TAG, "BUS HAS LEFT YOUR ROOM("+room+") :"+busName);

                //todo REMOVE AND UPDATE MARKER ON MAP
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(buses.containsKey(busName)){
                            if(buses.get(busName)!=null)
                            {
                                buses.get(busName).remove();//remove marker
                                buses.remove(busName);//remove from hashmap
                            }
                        }
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    //lifecycle stuff
    @Override
    public void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        map.onResume();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        if(mSocket!=null){
            disconnectSocket();  //works in pause
        }
        if(map!=null){
//            map.onDestroy();
        }

    }

    //todo 28 may change
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        super.onDestroy();
        if(mSocket!=null){
            disconnectSocket();  //todo dont want it to work in pause
        }
        if(map!=null){
         //   map.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        Log.d(TAG, "onLowMemory: ");
        super.onLowMemory();
        map.onLowMemory();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
        map.onPause();
    }


    //todo MOVE TO UTIL LATER


    //todo Use vector as marker
    public Bitmap getMarker(int res) {
        IconGenerator iconGen = new IconGenerator(getContext());

        // Define the size you want from dimensions file
        int shapeSize = getContext().getResources().getDimensionPixelSize(R.dimen.custom_marker_size);

        Drawable shapeDrawable = ResourcesCompat.getDrawable(getContext().getResources(),
                res, null);
        iconGen.setBackground(shapeDrawable);

        // Create a view container to set the size
        View view = new View(getContext());
        view.setLayoutParams(new ViewGroup.LayoutParams(shapeSize, shapeSize));
        iconGen.setContentView(view);

        // Create the bitmap
        Bitmap bitmap = iconGen.makeIcon();
        return bitmap;
    }
}
