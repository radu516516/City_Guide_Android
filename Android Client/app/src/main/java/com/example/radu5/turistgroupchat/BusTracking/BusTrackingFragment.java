package com.example.radu5.turistgroupchat.BusTracking;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.radu5.turistgroupchat.Model.BusRoute;
import com.example.radu5.turistgroupchat.Model.BusStop;
import com.example.radu5.turistgroupchat.Model.BusTrip;
import com.example.radu5.turistgroupchat.R;
import com.example.radu5.turistgroupchat.Utils.ApiHelper;
import com.example.radu5.turistgroupchat.Utils.PathFindingUtils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.ButtCap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.socket.client.Ack;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


/**
 * Created by radu5 on 4/24/2018.
 */

//todo daca nu vrea tracking in background Look at onPause onResume

    //todo DACA SE DECONECTEAZA SOCKETU-L INCEARCA SA SE CONECTEZE DINOU, DECI RULEAZA ON CREATE CARE ULTERIOR RULEAZA JOIN ROOM SI PRACTIC CONTINUA DE UNDE A RAMAS SA ITS ALL GOOD
public class BusTrackingFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private static final String TAG = "BusTrackingFragment";

    //Variabile
    private BusRoute busRoute;//Detali despre ruta din care face parte (AKA TRIPS AND STUFF) (DOWNLOAD THIS AT THE START)
    private static int busRouteId;
    private String busName;
    private String roomName = null;//Changes when route changes //todo CURRENT TRACKING ROOM(Changes on press button,or automaticly)
    private int currentTripId=-1;
    private boolean tracking = false;

    private boolean isSocketConnected = false;
    private Socket mSocket;//The socket of the bus connection to server

    private GoogleMap mGoogleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;//for real time tracking
    private LocationRequest mLocationRequest;//define the tracking
    private LocationCallback mLocationCallback;//run each time bus moved
    private Marker myBusMarker;
    private Polyline tripPolyline;//draw route polyline
    private ArrayList<Marker> tripBusStopsMarkers=new ArrayList<>();//draw the bus stations of the trip
    private HashMap<String,Marker> otherBusesInTrip=new HashMap<>();//todo other buses in the same trip room
    private static final float DEFAULT_ZOOM = 15f;
    private static final int REQUEST_CHECK_SETTINGS = 5;
    //Location Request Parameters
    private long UPDATE_INTERVAL = 6000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */


    //Views
    private Button btnTracking;
    private TextView txtRoute;
    private TextView txtLiveTrack;
    private TextView txtBusName;
    private Spinner spinnerTrip;
    private MapView map;


    public static BusTrackingFragment newInstance(int routeId,String busName) {

        Bundle args = new Bundle();
        args.putInt("routeId", routeId);
        args.putString("busName",busName);
        BusTrackingFragment fragment = new BusTrackingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());//make sure its initiated
        MapsInitializer.initialize(getContext());//start init map
        busRouteId = getArguments().getInt("routeId");
        busName=getArguments().getString("busName");

        mLocationCallback=new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {//updated bus location tracking
                //todo Got Location
                busLocationUpdated(locationResult.getLastLocation());//todo (TRIMITE LA SERVER LOCATIA)
            }
        };

        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_bus_tracking, container, false);
        txtRoute = view.findViewById(R.id.txtRoute);
        txtLiveTrack = view.findViewById(R.id.txtLiveTrack);
        txtBusName = view.findViewById(R.id.txtBusName);
      //  txtTrip = view.findViewById(R.id.txtTrip);
        spinnerTrip=view.findViewById(R.id.spinnerTrip);

        btnTracking = view.findViewById(R.id.btnTrack);//todo IS DISABLED UNTIL DOWNLOADED INFO ABOUT ROUTE IS DONE

        //todo EVERYTHING WILL BE ABLE TO RUN , AFTER DOWNLOADING ROUTE DETAILS AND IF SUCCESFULL

        //todo START AND STOP TRACKING WITH BUTTON (ALSO CAN CHANGE ROUTE AUTOMATICLY)
        btnTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //todo START TRACKING button
                tracking = !tracking;
                if (tracking) {//Start Tracking
                    spinnerTrip.setEnabled(false);//Cant change while tracking
                    //Clicked Tracking And Data is Downloaded
                    otherBusesInTrip=new HashMap<>();//todo init other buses

                    //Selected Trip
                    BusTrip selectedTrip= (BusTrip) spinnerTrip.getSelectedItem();
                    roomName="TrackingRoom"+selectedTrip.getTripId();//Got Room Name
                    currentTripId=selectedTrip.getTripId();
                    Log.d(TAG, "onClick: TRACKING ROOM :"+roomName);

                    //todo Draw Trip On Map
                    drawBusTrip(selectedTrip);//todo STERGE PRECEDENTU
                    //todo end of draw trip on map

                    //map stuff
                    getDeviceLocation();//get location once
                    //todo CONNECT TO THE SERVER
                    connectSocket();//todo CONNECT SOCKET AND SETUP EVENTS
                    //todo IN EVENIMENTU ON CONNECT AUTOMAT INTRU IN ROOM=roomName

                    startLocationUpdates();//Start tracking
                    //todo START TRACKING ( WILL EMIT EVENTS ABOUT LOCATION FROM HERE)

                    btnTracking.setText("STOP TRACKING");
                    txtLiveTrack.setText("ON");
                    return;

                } else {//todo Stop Tracking Button

                    spinnerTrip.setEnabled(true);//Enabled when not tracking

                    roomName=null;
                    currentTripId=-1;

                    //todo Disconnect from the sever
                    disconnectSocket();//todo DISCONNECT FROM THE SERVER(AKA LEAVING ALL ROOMS)
                    stopLocationUpdates();//Stops the tracking


                    //Clear map when stop routing
                    if(tripPolyline!=null){
                        tripPolyline.remove();
                        for (Marker marker: tripBusStopsMarkers) {
                            marker.remove();
                        }
                       tripBusStopsMarkers.clear();
                    }
                    //clear other buses markers
                    for(Marker i:otherBusesInTrip.values())
                    {
                        i.remove();
                    }
                    otherBusesInTrip=new HashMap<>();
                    btnTracking.setText("START TRACKING");
                    txtLiveTrack.setText("OFF");
                    return;
                }
            }
        });
        //Disabled until download
        btnTracking.setEnabled(false);//can click it after bus route data has been downloaded
        spinnerTrip.setEnabled(false);//until download cant do anything

        new FetchBusRouteDetails().execute(busRouteId);//Get route details
        //todo GET ROUTE DETAILS ( IS EITHER NULL , OR THE DATA) (DUPA CE SUNT DOWNLOADATE I CAN ENABLE BUTTON)

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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
    public void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        map.onResume();
    }

    //todo VREAU SA MEARGA SI ON PAUSE DEAIA NU MA DECONECTEZ ACOLO
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        if(mSocket!=null){
            disconnectSocket();  //works in pause
        }
        if(fusedLocationProviderClient!=null){
            stopLocationUpdates();//todo STOP GETTING LOCATION WHEN DESTROYED ( WORKS WHEN ITS PAUSE, STILL SENDING LOCATION SO GOOD)
        }
        map.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        map.onLowMemory();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
        map.onPause();
    }

    //todo Map loaded
    @Override
    public void onMapClick(LatLng latLng) {

    }


    //todo MAP IS READy
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
        //todo SETUP MAP EVENTS AND STUFFERINOOOO

        //mGoogleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);

        //listeners
        //mGoogleMap.setOnMapClickListener(this);
    }

    //todo get current device location
    //todo ONLY GETS THE LOCATION ONCE
    private void getDeviceLocation() {
        // fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this.getContext());
        try {
            //IF ( locationPermissionsGranted)
            Task location = fusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        if(task.getResult()!=null){
                            Location currentLocation = (Location) task.getResult();
                            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            Log.d(TAG, "onComplete: got device location");
                            moveCamera(latLng, DEFAULT_ZOOM);
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

    private void moveCamera(LatLng latLng, float zoom) {
        // mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mGoogleMap.animateCamera(cameraUpdate);
    }

    //Setup Location Requests
    //todo TRIGGER NEW LOCATION UPDATES AT INTERVAL (REGULAR UPDATES)
    private void startLocationUpdates() {
      //  mLocationRequest = new LocationRequest();
        mLocationRequest=LocationRequest.create();//vers 12.0.0 nu 15.0.0
        mLocationRequest.setInterval(UPDATE_INTERVAL);//5seconds (prefers to update the location (MAY BE FASTER THAN  THIS)(OR SLOWER)
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);//NO FASTER UPDATES THAN 3 SECONDS
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//REAL TIME LOCATION VERY ACCURATE
        //mLocationRequest.setSmallestDisplacement(10); //every 10 meters trigger location update

        /*
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setNumUpdates(1);
        request.setInterval(0);
         */

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        //Check Settings Satisfied

        //check current location settings
        SettingsClient client = LocationServices.getSettingsClient(this.getContext());
        client.checkLocationSettings(locationSettingsRequest);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            Log.d(TAG, "startLocationUpdates: location settings not cool and good");
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper());

    }
    private void stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    //todo FUNCTIE CARE RULEAZA CAND SE UPDATEAZA POZITIA AUTOBUZULUI
    public void busLocationUpdated(Location location){//Runs every 2 - 7 seconds
        LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
        Log.d(TAG, "busLocationUpdated: "+latLng.toString());

       if(myBusMarker!=null)
        {myBusMarker.remove();}
        BitmapDescriptor defaultMarker= BitmapDescriptorFactory.fromBitmap(getMarker(R.drawable.ic_bus_vector));
        myBusMarker=mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(busName).icon(defaultMarker).zIndex(1.01f));

        moveCamera(latLng, DEFAULT_ZOOM);


        //todo SEND DATA TO SOCKET
        if(mSocket!=null && isSocketConnected){//socket ia valoare cand se conecteaza la inceput

            //todo CHECK TO CHANGE ROOM
            BusTrip selectedTrip= (BusTrip) spinnerTrip.getSelectedItem();
            if(PathFindingUtils.havesineDistance(latLng.latitude,latLng.longitude,selectedTrip.getStatii().get(selectedTrip.getStatii().size()-1).getLat(),selectedTrip.getStatii().get(selectedTrip.getStatii().size()-1).getLng())<0.1){
                //todo DACA DISTANTA DIN ACEASTA POZITIE PANA LA ULTIMA STATIE DIN TRIPUL CURENT ESTA  MAI MICA CA 100 DE METRI , START THE PROCESS OF CHANGING ROOM

                boolean hasAnotherTrip=false;
                SpinnerAdapter adapter=spinnerTrip.getAdapter();//Change to other one
                for(int i = 0 ; i <adapter.getCount();i++){
                    if(!adapter.getItem(i).equals(selectedTrip)){
                        //switch to it
                        hasAnotherTrip=true;
                        spinnerTrip.setSelection(i);//found another trip
                        break;
                    }
                }
                //todo VERIFIC DACA AM AJUNS CAM LA ULTIMA STATIE DIN TRIP,ASA CA SCHIMB ROOM-UL
                if(hasAnotherTrip==true){
                    //todo CHANGE THE TRACKING ROOM,set VARS
                    Log.d(TAG, "busLocationUpdated: SHOULD CHANGE ROUTE FROM:"+selectedTrip.getTripName()+" to:"+((BusTrip) spinnerTrip.getSelectedItem()).getTripName());


                    //todo DONT DISCONNECT SOCKET,TRACKING ( DOAR SCHIMBAM RUTA)

                    //wait 5 seconds before changing rooms
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            String oldRoom=roomName;
                            roomName="TrackingRoom"+((BusTrip) spinnerTrip.getSelectedItem()).getTripId();//traseu schimbat
                            currentTripId=((BusTrip) spinnerTrip.getSelectedItem()).getTripId();
                            JSONObject obj=new JSONObject();
                            try {
                                obj.put("room",oldRoom);
                                mSocket.emit("leaveBusTrackingRoom", obj, new Ack() {
                                    @Override
                                    public void call(Object... args) {
                                        String a=String.valueOf(args[0]);
                                        Log.d(TAG, "leaveBusTrackingRoom:  "+a);
                                        JSONObject obj=new JSONObject();

                                        try {
                                            obj.put("busName",busName);
                                            obj.put("room",roomName);
                                            obj.put("type","bus");
                                            mSocket.emit("joinBusTrackingRoom", obj);

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    //Clear map,remove everything
                                    //clear other buses markers
                                    for(Marker i:otherBusesInTrip.values())
                                    {
                                        i.remove();
                                    }
                                    otherBusesInTrip=new HashMap<>();
                                    drawBusTrip(((BusTrip) spinnerTrip.getSelectedItem()));//Draw the new changed trip
                                }
                            });
                        }
                    }, 5000);//10 seconde de cand este la 100 de metri de ultima statie pana schimba trip

                }

            }


            JSONObject msg=new JSONObject();
            try {
                msg.put("latitude",latLng.latitude);
                msg.put("longitude",latLng.longitude);
                mSocket.emit("busMoved", msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    //todo draw bus trip
    public void drawBusTrip(BusTrip t){
        if(tripPolyline!=null){//daca a fost deja deseneta
            tripPolyline.remove();
            for (Marker marker: tripBusStopsMarkers) {
                marker.remove();
            }
            tripBusStopsMarkers.clear();
        }

        PolylineOptions po=new PolylineOptions().width(15).color(Color.BLUE).jointType(2).startCap(new ButtCap());
        BitmapDescriptor defaultMarker =
                BitmapDescriptorFactory.fromBitmap(getMarker(R.drawable.ic_bus_stop3));


        for(BusStop busStop:t.getStatii()){
            LatLng point=new LatLng(busStop.getLat(),busStop.getLng());
            po.add(point);


            Marker marker=mGoogleMap.addMarker(new MarkerOptions()
            .position(point).title(busStop.getOrder()+":"+busStop.getStopName()).icon(defaultMarker));
            tripBusStopsMarkers.add(marker);//so can remove later
        }
        tripPolyline=mGoogleMap.addPolyline(po);
    }


    private class FetchBusRouteDetails extends AsyncTask<Integer,Void,BusRoute> {

        @Override
        protected BusRoute doInBackground(Integer... ints) {
            return ApiHelper.getBusRounte(Integer.valueOf(ints[0]));
        }

        @Override
        protected void onPostExecute(BusRoute b) {
            if(b==null){
                busRoute=null;
                //todo NU S-A DOWNLOADAT, PROBLEMA, RUTA RESPECTIVA NU ARE TRIPS SAU STATII ETC CV DE GENU
                return;
            }
            //todo Download completed its nice
            Toast.makeText(getActivity(),"Downloaded",Toast.LENGTH_SHORT).show();
            busRoute=b;
            btnTracking.setEnabled(true);//Can start tracking now
            spinnerTrip.setEnabled(true);
            busName=busRoute.getBusRouteName()+":"+busName;
            //todo SET UP TEXT VIEWS
            txtRoute.setText(busRoute.getBusRouteName());
            txtBusName.setText(busName);

            //Populate trip spinner
            List<BusTrip> trips=busRoute.getTrips();//trips

            ArrayAdapter<BusTrip> adapter=new ArrayAdapter<BusTrip>(getContext(),R.layout.spinner_trip_dropdown,trips);
            adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

            spinnerTrip.setAdapter(adapter);//Contine toate informatiile despre trips
        }
    }

    //todo SOCKET IO FUNCTIONS
    public void connectSocket(){//todo (CONNECTED TO ONE ROOM)
        try{
            Manager manager=new Manager(new URI("http://raduhdd.asuscomm.com:3000"));
            // mSocket= IO.socket("http://raduhdd.asuscomm.com:3000");
            mSocket = manager.socket("/bus-tracking");//conenct to namespace
            configSocketEvents();
            mSocket.connect();//todo as soon as connected join a room
            isSocketConnected=true;
            Log.d(TAG, "connectSocket: CONNECTED TO SERVER");
        }catch(Exception e){
            System.out.println(e);
        }
    }
    public void configSocketEvents(){
        mSocket.on(Socket.EVENT_CONNECT,onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT,onDisconnect );
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        //todo my events from server
        mSocket.on("socketID",onSocketId );
        mSocket.on("serverMessage",onServerMessage);
        mSocket.on("busMoved",onUpdateLocation);
        mSocket.on("busLeftRoom",busLeftRoom);
    }
    public void disconnectSocket(){
        Log.d(TAG, "disconnectSocket: DISCONNECTED FROM SERVER");
        isSocketConnected=false;
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

    //todo Socket Io Events
    //Socket event listeners
    private Emitter.Listener onConnect=new Emitter.Listener() {//imediat dupa ce se conecteaza intra in room
        @Override
        public void call(Object... args) {
            Log.d(TAG, "onConnect: Client connected to server");
            JSONObject obj=new JSONObject();
            //todo Conectare la camera in care isi trimite pozitia
            try {
                obj.put("busName",busName);
                obj.put("room",roomName);//ii dau room care se seteaza la buton si dupaia se schimba automat
                obj.put("type","bus");
                mSocket.emit("joinBusTrackingRoom", obj);

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
    //todo Got location from a bus din acelasi room in care esti
    private Emitter.Listener onUpdateLocation=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //Got an update from some bus
            JSONObject data=(JSONObject)args[0];
            String busName;
            String lat;
            String lng;
            //todo IDENTIFIC UNIC UN AUTOBUZ PRIN BUS_NAME AKA NR INMATRICULARE UNIC
            //todo hasmap de markere si vad pe care il updatez
            try {
                busName=data.getString("from");//Name of the bus
                lat=data.getString("latitude");
                lng=data.getString("longitude");
                Log.d(TAG, "Update Bus Location: "+busName+":"+" "+lat+","+lng);

                //todo REMOVE AND UPDATE MARKER ON MAP
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!otherBusesInTrip.containsKey(busName)){
                            //Nu exist markerul deja e prima data ( nu exista deja inainte dont have to remove)


                            BitmapDescriptor defaultMarker= BitmapDescriptorFactory.fromBitmap(getMarker(R.drawable.ic_bus_vector));
                            Marker otherBusMaker=mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),Double.parseDouble(lng))).title(busName).icon(defaultMarker).zIndex(1.0f));
                            otherBusesInTrip.put(busName,otherBusMaker);

                        }else{
                            //exista deja marker remove it
                            if(otherBusesInTrip.get(busName)!=null)
                            {
                                otherBusesInTrip.get(busName).remove();
                                //reafiseazal
                                BitmapDescriptor defaultMarker= BitmapDescriptorFactory.fromBitmap(getMarker(R.drawable.ic_bus_vector));
                                Marker otherBusMaker=mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),Double.parseDouble(lng))).title(busName).icon(defaultMarker).zIndex(1.0f));
                                otherBusesInTrip.put(busName,otherBusMaker);
                            }

                        }

                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
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
                //Remove it from the hashmap and the googlemap

                //todo REMOVE AND UPDATE MARKER ON MAP
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(otherBusesInTrip.containsKey(busName)){
                            if(otherBusesInTrip.get(busName)!=null)
                            {
                                otherBusesInTrip.get(busName).remove();//remove marker
                                otherBusesInTrip.remove(busName);//remove from hashmap
                            }
                        }
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


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
