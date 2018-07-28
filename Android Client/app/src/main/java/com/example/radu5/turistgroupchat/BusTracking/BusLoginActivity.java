package com.example.radu5.turistgroupchat.BusTracking;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.radu5.turistgroupchat.R;
import com.example.radu5.turistgroupchat.Utils.ApiHelper;

public class BusLoginActivity extends AppCompatActivity {

    private static final String TAG = "BusLoginActivity";
    private EditText txtNume;
    private EditText txtPass;
    private Button button;
    private final int ALL_PERMISSIONS = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_login);

        txtNume=findViewById(R.id.txtNrInmatriculare);
        txtPass=findViewById(R.id.txtPassword);
        button=findViewById(R.id.btnLogIn);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLogin();
            }
        });

        //ask permissions
        final String[] permissions = new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_NETWORK_STATE,Manifest.permission.INTERNET};
        ActivityCompat.requestPermissions(this,permissions,ALL_PERMISSIONS);


    }

    public void checkLogin(){
        String nume=txtNume.getText().toString();
        String pass=txtPass.getText().toString();

        new LoginTask().execute(nume,pass);

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

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    private class LoginTask extends AsyncTask<String,Void,Integer>{


        @Override
        protected Integer doInBackground(String... strings) {
            //return ApiHelper.busLogin(strings[0].toString(),strings[1].toString());

            return  ApiHelper.busLogin(strings[0],strings[1]);
        }

        @Override
        protected void onPostExecute(Integer integer) {

            if(integer==-1){
                //fail
                Toast.makeText(BusLoginActivity.this,"Login Failed",Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(BusLoginActivity.this,"Login Succes Route Id:"+integer,Toast.LENGTH_LONG).show();
                //Start next activity
                Intent i = new Intent(BusLoginActivity.this, BusTrackingActivity.class);
                i.putExtra("routeId", integer);
                i.putExtra("busName",txtNume.getText().toString());
                startActivity(i);
            }
        }
    }
}
