package com.example.radu5.turistgroupchat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.radu5.turistgroupchat.BusTracking.BusLoginActivity;
import com.example.radu5.turistgroupchat.UserApp.UserLoginActivity;

public class StartActivity extends AppCompatActivity {


    private Button btnTurist;
    private Button btnSofer;
    private final int ALL_PERMISSIONS = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        btnSofer=findViewById(R.id.btnSofer);
        btnTurist=findViewById(R.id.btnTurist);


        btnSofer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(StartActivity.this, BusLoginActivity.class);
                startActivity(i);
            }
        });

        btnTurist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(StartActivity.this, UserLoginActivity.class);
                startActivity(i);
            }
        });

        final String[] permissions = new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_NETWORK_STATE,Manifest.permission.INTERNET};
        ActivityCompat.requestPermissions(this,permissions,ALL_PERMISSIONS);

    }
    public void onBackPressed() {
        moveTaskToBack(true);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }
}
