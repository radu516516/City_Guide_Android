package com.example.radu5.turistgroupchat.Map;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.radu5.turistgroupchat.PathFinding.BusRoute;
import com.example.radu5.turistgroupchat.PathFinding.BusStation;
import com.example.radu5.turistgroupchat.PathFinding.Node;
import com.example.radu5.turistgroupchat.PathFinding.PathFinder;
import com.example.radu5.turistgroupchat.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

/**
 * Created by radu5 on 4/15/2018.
 */

public class MyMapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener,GoogleMap.OnPolylineClickListener,GoogleMap.OnMapLongClickListener {

    private static final String TAG = "MyMapFragment";

    private boolean mLocationPermissionsGranted = false;

    //todo Views
    private SupportMapFragment supportMapFragment;
    private MapView mapView;
    private View mView;

    //todo vars
    GoogleMap mGoogleMap;
    double asteptareInStatie=0.15;//10% dintr-o ora

    Polyline polyline1;
    ArrayList<Marker> routeMarkers=new ArrayList<>();
    private MarkerOptions options = new MarkerOptions();
    ArrayList<BusRoute> trasee = new ArrayList<>();
    ArrayList<BusStation> statii = new ArrayList<>();
    ArrayList<LatLng> markerPoints;
    FusedLocationProviderClient fusedLocationProviderClient;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        statii.add(new BusStation("Cap Linie Sere", 44.151301, 28.602321));
        statii.add(new BusStation("Varful cu Dor", 44.150885, 28.604794));
        statii.add(new BusStation("Soseaua intre vii", 44.148039, 28.607873));
        statii.add(new BusStation("Angro", 44.145584, 28.610430));
        statii.add(new BusStation("Scoala Ion Minulescu", 44.143883, 28.613850));
        statii.add(new BusStation("Facultativa", 44.140561, 28.611211));
        statii.add(new BusStation("Hatman Arbore", 44.139137, 28.614492));
        statii.add(new BusStation("Facultativa", 44.138078, 28.619291));
        statii.add(new BusStation("Avia Motors", 44.132453, 28.618169));
        statii.add(new BusStation("Tiriac Auto", 44.136092, 28.619954));
        statii.add(new BusStation("KM5", 44.140327, 28.621976));
        statii.add(new BusStation("Salciilor", 44.142803, 28.623191));
        statii.add(new BusStation("Doraly Mall", 44.145511, 28.624540));
        statii.add(new BusStation("KM4", 44.154066, 28.628751));
        statii.add(new BusStation("Far", 44.158934, 28.631680));
        statii.add(new BusStation("Facultativa", 44.162694, 28.632332));
        statii.add(new BusStation("Stadion Portul", 44.165098, 28.633419));
        statii.add(new BusStation("Gara CFR", 44.169400, 28.633965));
        statii.add(new BusStation("Spital Boli Infectioase", 44.170728, 28.638184));
        statii.add(new BusStation("Politie", 44.173459, 28.641741));
        statii.add(new BusStation("Republica", 44.174915, 28.644456));
        statii.add(new BusStation("Fantasio", 44.177133, 28.648924));
        statii.add(new BusStation("Centru", 44.180058, 28.652827));
        statii.add(new BusStation("CMJ", 44.182904, 28.649608));
        statii.add(new BusStation("Bulevardul Mamaia", 44.185903, 28.647528));
        statii.add(new BusStation("Spital Militar", 44.189574, 28.651087));
        statii.add(new BusStation("Universitate", 44.193140, 28.650141));
        statii.add(new BusStation("Ion Ratiu", 44.197224, 28.648255));
        statii.add(new BusStation("George Enescu", 44.200229, 28.646794));
        statii.add(new BusStation("Delfinariu", 44.205592, 28.644228));
        statii.add(new BusStation("Complex ONT", 44.210746, 28.643161));
        statii.add(new BusStation("Pescarie", 44.215036, 28.642434));
        statii.add(new BusStation("Perla", 44.218755, 28.632589));
        statii.add(new BusStation("Campus", 44.216091, 28.626596));

        trasee.add(new BusRoute(statii, "5-40_Dus", 0, 540, 4));

        //todo adaugare traseu  48 Dus
        statii = new ArrayList<>();
        statii.add(new BusStation("Cap Linie 48", 44.168939, 28.576594));
        statii.add(new BusStation("Spital CFR", 44.168970, 28.582456));
        statii.add(new BusStation("Scoala 31", 44.169302, 28.588090));
        statii.add(new BusStation("Complex Bratianu", 44.169928, 28.597366));
        statii.add(new BusStation("Alba Iulia", 44.170235, 28.602393));
        statii.add(new BusStation("Dispensar", 44.170821, 28.611129));
        statii.add(new BusStation("Topolog", 44.171187, 28.616760));
        statii.add(new BusStation("Salvare", 44.171380, 28.620717));
        statii.add(new BusStation("Intim", 44.171693, 28.624697));
        statii.add(new BusStation("Policlinica CFR", 44.171993, 28.629733));
        statii.add(new BusStation("Liceu Ovidius", 44.172428, 28.635542));
        statii.add(new BusStation("Politie", 44.173459, 28.641741));
        statii.add(new BusStation("Republica", 44.174915, 28.644456));
        statii.add(new BusStation("Poarta 2", 44.175127, 28.652172));

        trasee.add(new BusRoute(statii, "48_Dus", 0, 48, 1));


        //todo adaugare traseu  48 Intors
        statii = new ArrayList<>();
        statii.add(new BusStation("Poarta 2", 44.175127, 28.652172));
        statii.add(new BusStation("Fantasio", 44.175656, 28.649649));
        statii.add(new BusStation("Tomis", 44.178126, 28.646620));
        statii.add(new BusStation("Piata Grivitei", 44.176495, 28.641985));
        statii.add(new BusStation("Politie", 44.173537, 28.641640));
        statii.add(new BusStation("Liceu Ovidius", 44.172568, 28.635707));
        statii.add(new BusStation("Policlinica CFR", 44.172187, 28.629701));
        statii.add(new BusStation("Intim", 44.171883, 28.625134));
        statii.add(new BusStation("Salvare", 44.171595, 28.620682));
        statii.add(new BusStation("Topolog", 44.171353, 28.617133));
        statii.add(new BusStation("Dispensar", 44.170775, 28.608578));
        statii.add(new BusStation("Complex Bratianu", 44.170153, 28.598751));
        statii.add(new BusStation("Scoala 31", 44.169705, 28.591877));
        statii.add(new BusStation("Palas", 44.169296, 28.585887));
        statii.add(new BusStation("Spital CFR", 44.169033, 28.581431));
        statii.add(new BusStation("Cap Linie 48", 44.168939, 28.576594));

        trasee.add(new BusRoute(statii, "48_Intors", 1, 48, 2));


        //todo adaugare traseu  100 Dus
        statii = new ArrayList<>();
        statii.add(new BusStation("Gara Cap Linie", 44.168495, 28.632946));
        statii.add(new BusStation("Policlinica 2", 44.171769, 28.636904));
        statii.add(new BusStation("Bucuresti", 44.176403, 28.636139));
        statii.add(new BusStation("Casa de Cultura", 44.180878, 28.635899));
        statii.add(new BusStation("Saguna", 44.183918, 28.635584));
        statii.add(new BusStation("Trocadero", 44.189533, 28.633919));
        statii.add(new BusStation("Scoala 27", 44.192448, 28.633082));
        statii.add(new BusStation("Dacia", 44.195864, 28.632176));
        statii.add(new BusStation("Tomis 3", 44.199607, 28.631185));
        statii.add(new BusStation("City", 44.203360, 28.630204));
        statii.add(new BusStation("Suceava", 44.208748, 28.629075));
        statii.add(new BusStation("Sat Vacanta", 44.215334, 28.628410));
        statii.add(new BusStation("Cap Linie Sat Vacanta", 44.215835, 28.627727));
        trasee.add(new BusRoute(statii, "100_Dus", 0, 100, 3));

        //todo adaugare traseu  5-40 Dus


        //todo adaugare traseu  42 DUS
        statii = new ArrayList<>();

        statii.add(new BusStation("Poarta 2", 44.175127, 28.652172));
        statii.add(new BusStation("Fantasio", 44.175656, 28.649649));
        statii.add(new BusStation("Tomis", 44.178126, 28.646620));
        statii.add(new BusStation("Scoala 13", 44.181035, 28.643200));
        statii.add(new BusStation("Institutu de proiectare", 44.185842, 28.644635));
        statii.add(new BusStation("Spitalul Judetean", 44.188173, 28.642511));
        statii.add(new BusStation("Colegiul Tehnic Tomis", 44.192062, 28.639181));
        statii.add(new BusStation("Dacia", 44.196404, 28.632876));
        statii.add(new BusStation("Tomis 3", 44.199792, 28.627766));
        statii.add(new BusStation("Brotacei", 44.203318, 28.623301));
        statii.add(new BusStation("Scoala 29", 44.208024, 28.622898));
        statii.add(new BusStation("Cap Linie Tomis Nord", 44.211251, 28.623962));

        trasee.add(new BusRoute(statii, "42_Dus", 0, 42, 5));

        //todo 102 dus
        statii = new ArrayList<>();
        statii.add(new BusStation("Depu 102", 44.158816, 28.586893));
        statii.add(new BusStation("Fabrica de bere", 44.159580, 28.594547));
        statii.add(new BusStation("BD Aurel Vlaicu", 44.161442, 28.604600));
        statii.add(new BusStation("Meconst", 44.165097, 28.607435));
        statii.add(new BusStation("BD I.C Bratianu", 44.171701, 28.610263));
        statii.add(new BusStation("Eliberari", 44.177310, 28.614721));
        statii.add(new BusStation("Scoala 23", 44.180279, 28.617750));
        statii.add(new BusStation("Baba Novac", 44.184286, 28.621003));
        statii.add(new BusStation("Izvor", 44.187376, 28.623347));
        statii.add(new BusStation("Independetul", 44.191941, 28.624428));
        statii.add(new BusStation("Galeriile Soveja", 44.194658, 28.623981));
        statii.add(new BusStation("Bulevardul Tomis", 44.199621, 28.625938));
        statii.add(new BusStation("Tomis 3", 44.201198, 28.627861));
        statii.add(new BusStation("City", 44.202394, 28.631299));
        statii.add(new BusStation("Parcul copiilor", 44.202634, 28.637220));
        statii.add(new BusStation("Delfinariu", 44.204992, 28.645264));
        statii.add(new BusStation("Facultativa", 44.205646, 28.647756));
        statii.add(new BusStation("Faleza Nord Debarcare", 44.207124, 28.649895));

        trasee.add(new BusRoute(statii, "102_Dus", 0, 102, 6));

        //todo 100 intors
        statii = new ArrayList<>();
        statii.add(new BusStation("Cap Linie Sat Vacanta", 44.215835, 28.627727));
        statii.add(new BusStation("Suceava", 44.209198, 28.628777));
        statii.add(new BusStation("City", 44.203439, 28.630021));
        statii.add(new BusStation("Tomis3", 44.198867, 28.631225));
        statii.add(new BusStation("Scoala 27", 44.192473, 28.632897));
        statii.add(new BusStation("Trocadero", 44.189320, 28.633769));
        statii.add(new BusStation("Saguna", 44.184295, 28.635312));
        statii.add(new BusStation("Casa de cultura", 44.181525, 28.635679));
        statii.add(new BusStation("Bucuresti", 44.176592, 28.635967));
        statii.add(new BusStation("Kaufland", 44.173084, 28.636452));
        statii.add(new BusStation("Gara Cap Linie", 44.168495, 28.632946));

        trasee.add(new BusRoute(statii, "100_Intors", 1, 100, 7));


        //todo 5-40 Intors
        statii = new ArrayList<>();
        statii.add(new BusStation("Campus", 44.216091, 28.626596));
        statii.add(new BusStation("Tomis Nord", 44.213830, 28.624074));
        statii.add(new BusStation("Parc", 44.218291, 28.633686));
        statii.add(new BusStation("Pescarie", 44.214935, 28.642171));
        statii.add(new BusStation("Conplex ONT", 44.210769, 28.642867));
        statii.add(new BusStation("Delfinariu", 44.205939, 28.643795));
        statii.add(new BusStation("Dragoslavele", 44.201856, 28.645738));
        statii.add(new BusStation("Dorobanti", 44.198589, 28.647292));
        statii.add(new BusStation("Ion Ratiu", 44.194746, 28.649135));
        statii.add(new BusStation("Turda", 44.192661, 28.649986));
        statii.add(new BusStation("Bulevardul Mamaia", 44.186345, 28.647990));
        statii.add(new BusStation("Colegiu Constantion Bratescu", 44.182466, 28.645741));
        statii.add(new BusStation("Piata Unirii", 44.178648, 28.650371));
        statii.add(new BusStation("Fantasio", 44.177583, 28.649663));
        statii.add(new BusStation("Republica", 44.175639, 28.645680));
        statii.add(new BusStation("Politie", 44.173537, 28.641640));
        statii.add(new BusStation("Spitalul Municipal", 44.170915, 28.638220));
        statii.add(new BusStation("Gara CFR", 44.168708, 28.633598));
        statii.add(new BusStation("Facultativa", 44.162724, 28.632143));
        statii.add(new BusStation("Far", 44.159032, 28.631552));
        statii.add(new BusStation("KM4", 44.153412, 28.628289));//comun 48 intors
        statii.add(new BusStation("Dorally Mall", 44.145548, 28.624410));
        statii.add(new BusStation("Pandurului", 44.141788, 28.621929));
        statii.add(new BusStation("Facultativa", 44.143037, 28.617840));
        statii.add(new BusStation("Scoala Ion Minulescu", 44.144264, 28.613923));
        statii.add(new BusStation("Angro", 44.145791, 28.610364));
        statii.add(new BusStation("Soseaua din vii", 44.147643, 28.608432));
        statii.add(new BusStation("Varful cu Dor", 44.150956, 28.604929));
        statii.add(new BusStation("Capat Linie KM5", 44.151592, 28.602613));

        trasee.add(new BusRoute(statii, "5-40_Intors", 1, 540, 8));


        //todo 102 Intors
        statii = new ArrayList<>();
        statii.add(new BusStation("Faleza Nord Imbarcare", 44.207124, 28.649895));
        statii.add(new BusStation("Delfinariu", 44.204364, 28.642614));
        statii.add(new BusStation("Parcul Copiilor", 44.202732, 28.637413));
        statii.add(new BusStation("City", 44.202610, 28.631915));
        statii.add(new BusStation("Bd Tomis", 44.201020, 28.627450));
        statii.add(new BusStation("Gareliile Soveja", 44.195931, 28.623769));
        statii.add(new BusStation("Independentul", 44.191775, 28.624317));
        statii.add(new BusStation("Izvor", 44.188087, 28.623770));
        statii.add(new BusStation("Baba Novac", 44.184508, 28.620808));
        statii.add(new BusStation("Scoala 23", 44.181240, 28.618238));
        statii.add(new BusStation("Eliberarii", 44.177471, 28.614595));
        statii.add(new BusStation("BD IC BRATIANU", 44.171621, 28.610025));
        statii.add(new BusStation("Mencost", 44.164869, 28.607076));
        statii.add(new BusStation("BD Aurel Vlaicu", 44.161300, 28.604364));
        statii.add(new BusStation("Furnirom", 44.160007, 28.598132));
        statii.add(new BusStation("Fabrica de Bere", 44.159614, 28.593565));
        statii.add(new BusStation("Depou Tranvaie", 44.158960, 28.587163));

        trasee.add(new BusRoute(statii, "102_Intors", 1, 102, 9));


        //todo 42 intors
        statii = new ArrayList<>();
        statii.add(new BusStation("Cap Linie Tomis Nord", 44.211251, 28.623962));
        statii.add(new BusStation("Dobrila Eugeniu", 44.208628, 28.619832));
        statii.add(new BusStation("Brotacei", 44.204838, 28.623586));
        statii.add(new BusStation("Furnicuta", 44.201728, 28.624550));
        statii.add(new BusStation("Tomis3", 44.199395, 28.628053));
        statii.add(new BusStation("Dacia", 44.196073, 28.633142));
        statii.add(new BusStation("Liceul4", 44.191255, 28.639690));
        statii.add(new BusStation("Spitalul Judetean", 44.188031, 28.642487));
        statii.add(new BusStation("Institutul de proiectare", 44.185549, 28.644737));
        statii.add(new BusStation("Scoala 13", 44.180744, 28.643415));
        statii.add(new BusStation("Magazinul Tomis", 44.178552, 28.645996));
        statii.add(new BusStation("Poarta 2", 44.175127, 28.652172));//same as 48 and 42dus


        trasee.add(new BusRoute(statii, "42_Intors", 1, 42, 10));

        //todo 44 DUS
        statii = new ArrayList<>();
        statii.add(new BusStation("Poarta 1", 44.171153, 28.659299));
        statii.add(new BusStation("Radio Romania", 44.173623, 28.656454));
        statii.add(new BusStation("Poarta 2", 44.175127, 28.652172));
        statii.add(new BusStation("Fantasio", 44.175656, 28.649649));
        statii.add(new BusStation("Tomis", 44.178126, 28.646620));
        statii.add(new BusStation("I.G Duca", 44.177607, 28.640938));
        statii.add(new BusStation("Balada", 44.179978, 28.637796));
        statii.add(new BusStation("Rapsodia", 44.181349, 28.632197));
        statii.add(new BusStation("Rasuri", 44.182443, 28.628703));
        statii.add(new BusStation("Frunzelor", 44.183654, 28.625155));
        statii.add(new BusStation("Institutu de Marina", 44.184706, 28.621946));
        statii.add(new BusStation("Orizont", 44.186740, 28.615789));
        statii.add(new BusStation("Cartierul Tineretului", 44.188668, 28.611456));
        statii.add(new BusStation("Energia", 44.190361, 28.608137));
        statii.add(new BusStation("Bariera CFR", 44.192566, 28.603840));
        statii.add(new BusStation("Metro", 44.197861, 28.605407));
        statii.add(new BusStation("Targ/Maritimo", 44.200607, 28.607773));
         statii.add(new BusStation("CL Maritimo", 44.200680, 28.609169));
        statii.add(new BusStation("Euroderma", 44.201114, 28.613109));
        statii.add(new BusStation("Graniceri", 44.198941, 28.617124));
        statii.add(new BusStation("Gareriile Soveja debarcare", 44.196750, 28.622314));

        trasee.add(new BusRoute(statii, "44_Dus", 0, 44, 11));

        //todo 44 Intors
        statii = new ArrayList<>();
        statii.add(new BusStation("Gareriile Soveja Imbarcare", 44.196934, 28.622070));
        statii.add(new BusStation("Graniceri", 44.198727, 28.617897));
        statii.add(new BusStation("Euroderma", 44.200806, 28.613587));
        statii.add(new BusStation("Targ/Maritimo", 44.201517, 28.608504));
        statii.add(new BusStation("Metro", 44.197298, 28.604819));
        statii.add(new BusStation("Bariera CFR", 44.192342, 28.603950));
        statii.add(new BusStation("Energia", 44.190082, 28.608459));
        statii.add(new BusStation("Cartierul Tineretului", 44.188414, 28.611712));
        statii.add(new BusStation("Orizont", 44.186875, 28.615083));
        statii.add(new BusStation("Institutu de Marina", 44.184341, 28.622719));
        statii.add(new BusStation("Frunzelor", 44.183387, 28.625661));
        statii.add(new BusStation("Rasuri", 44.182469, 28.628377));
        statii.add(new BusStation("Rapsodia", 44.181181, 28.632261));
        statii.add(new BusStation("Balada", 44.179980, 28.637200));
        statii.add(new BusStation("Scoala 13", 44.180744, 28.643415));//same as 42 intors
        statii.add(new BusStation("Magazinul Tomis", 44.178552, 28.645996));//same as 42 intors
        statii.add(new BusStation("Poarta 2", 44.175127, 28.652172));//same as 42 48 51
        statii.add(new BusStation("Radio Romania", 44.173430, 28.656629));
        statii.add(new BusStation("Poarta 1 Port Coborare", 44.170287, 28.659944));

        trasee.add(new BusRoute(statii, "44_Intors", 1, 44, 12));





    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        MapsInitializer.initialize(getContext());


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_my_map, container, false);
        //Init map
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView) mView.findViewById(R.id.map);
        if (mapView != null) {
            //todo init mapView
            mapView.onCreate(null);
            mapView.onResume();
            //map callback
            mapView.getMapAsync(this);
        }
        Button btn=(Button)mView.findViewById(R.id.button3);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(asteptareInStatie==0.15){
                    asteptareInStatie=0.01;

                }
                else{
                    asteptareInStatie=0.15;
                }
                btn.setText("Schimbare Traseu Wait:"+asteptareInStatie);
            }
        });
    }

    //todo Map is ready
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

        //listeners
        mGoogleMap.setOnMapClickListener(this);
        mGoogleMap.setOnPolylineClickListener(this);
        mGoogleMap.setOnMapLongClickListener(this);


    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapClick(LatLng latLng) {




    }


    @Override
    public void onPolylineClick(Polyline polyline) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if(polyline1!=null) {
            polyline1.remove();
            for (Marker marker: routeMarkers) {
                marker.remove();
            }
            routeMarkers.clear();

        }
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            //Toast.makeText(getContext(), "CLICKED AT : " + location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_LONG).show();
                            ArrayList<Node> result= PathFinder.aStarSearch1(location.getLatitude(),location.getLongitude() ,latLng.latitude, latLng.longitude,trasee,asteptareInStatie);
                            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                            alert.setTitle("Title");
                            String s="";
                            //Polyline p=mGoogleMap.addPolyline()
                            PolylineOptions po=new PolylineOptions().clickable(true);
                            if(result==null){

                                return;//todo THERE IS NO ROUTE ( TO REACH)
                            }
                            for(Node i:result){
                                s+=i.toString()+"\n";
                                LatLng l=new LatLng(i.getLat(),i.getLng());
                                po.add(l);
                                options.position(l);
                                options.title(i.getNume());
                                options.snippet(i.toString());
                                Marker marker=mGoogleMap.addMarker(options);
                               // marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_route_item));
                                routeMarkers.add(marker);
                            }
                            polyline1 = mGoogleMap.addPolyline(po);
                            polyline1.setTag("A");
                            polyline1.setStartCap(new RoundCap());
                            polyline1.setEndCap(new RoundCap());
                            polyline1.setJointType(JointType.ROUND);
                            polyline1.setColor(Color.GREEN);

                            alert.setMessage(s);
// Create TextView
                            final TextView input = new TextView (getContext());
                            alert.setView(input);

                            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    input.setText("hi");
                                    // Do something with value!
                                }
                            });

                            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // Canceled.
                                }
                            });
                            alert.show();
                        }
                    }
                });
    }

    //Todo ADD CHECKING PERMISIONS

}
