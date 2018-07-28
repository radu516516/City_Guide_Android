package com.example.radu5.turistgroupchat.Map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.radu5.turistgroupchat.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by radu5 on 4/15/2018.
 */

public class MainMapActivity extends AppCompatActivity
{
    private static final String TAG = "MainMapActivity";

    //vars
    private boolean mLocationPermissionsGranted=false;
    private GoogleMap mMap;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_map);
        Log.d(TAG, "onCreate: Created");
        getLocationPermission();
    }


    private void getLocationPermission(){
        String[] permissions={Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "getLocationPermission: WE HAVE PERMISSIONS");
                mLocationPermissionsGranted=true;
                initMap();
            }
            else{
                Log.d(TAG, "getLocationPermission: ASKING FOR PERMISSIONS");
                ActivityCompat.requestPermissions(this,permissions,1234);
            }
        } else{
            Log.d(TAG, "getLocationPermission: ASKING FOR PERMISSIONS");
            ActivityCompat.requestPermissions(this,permissions,1234);
        }
    }

    private void initMap(){
        //todo map fragment
        final SupportMapFragment mapFragment=(SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d(TAG, "onMapReady: MAP IS READY");
                //Prepare our map
                mMap=googleMap;
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted=false;
        switch(requestCode){
            case 1234:{
                if(grantResults.length>0){
                    for(int i = 0 ; i <grantResults.length;i++){
                        if(grantResults[i]!=PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionsGranted=true;
                    Log.d(TAG, "onRequestPermissionsResult: REQUEST FOR LOCATION PERMISSIONS GRANTED");
                    //todo INIT MAP
                    initMap();
                }
            }
        }
    }
}
