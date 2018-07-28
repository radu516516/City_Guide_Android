package com.example.radu5.turistgroupchat.Map;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.radu5.turistgroupchat.MainActivity;
import com.example.radu5.turistgroupchat.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MapActivity extends AppCompatActivity {

    private static final String TAG = "MapActivity";

    private static final int ERROR_DIALOG_REQUEST=9001;//Check google play version

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_map);
        if(isServicesOK()){
            init();
        }
    }

    private void init(){
        //Button btnMap=(Button)findViewById(R.id.btnMap);
       // btnMap.setOnClickListener(new View.OnClickListener() {
         //   @Override
        //    public void onClick(View v) {
        //        Intent intent=new Intent(MapActivity.this,MainMapActivity.class);
         //       startActivity(intent);
        //    }
      //  });
    }
    //Check services
    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: Checking google services version");
        int available= GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapActivity.this);
        if(available== ConnectionResult.SUCCESS){
            Log.d(TAG, "isServicesOK: Google play services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Log.d(TAG, "isServicesOK:an error ocurred but we can fix it");
            Dialog dialog=GoogleApiAvailability.getInstance().getErrorDialog(MapActivity.this,available,ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Log.d(TAG, "isServicesOK: Google play services not working cant make map requests");
        }
        return false;
    }
}
