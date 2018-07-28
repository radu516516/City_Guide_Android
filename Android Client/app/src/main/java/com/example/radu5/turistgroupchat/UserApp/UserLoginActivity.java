package com.example.radu5.turistgroupchat.UserApp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.radu5.turistgroupchat.BusTracking.BusLoginActivity;
import com.example.radu5.turistgroupchat.BusTracking.BusTrackingActivity;
import com.example.radu5.turistgroupchat.MainActivity;
import com.example.radu5.turistgroupchat.R;
import com.example.radu5.turistgroupchat.Utils.ApiHelper;

public class UserLoginActivity extends AppCompatActivity {

    //todo REMEBER ASK FOR PERMISSIONS HERE

    private static final String TAG = "UserLoginActivity";
    private EditText txtNume;
    private EditText txtPass;
    private TextView signUp;
    private ProgressDialog prg;
    private Button button;

    private static final int REQUEST_SIGNUP=43;
    private final int ALL_PERMISSIONS = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        txtNume=findViewById(R.id.txtUserName);
        txtPass=findViewById(R.id.txtPassword);
        button=findViewById(R.id.btnLogIn);

        signUp=findViewById(R.id.link_signup);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Start Register
                Intent i = new Intent(UserLoginActivity.this, UserRegisterActivity.class);
                startActivityForResult(i,REQUEST_SIGNUP);
            }
        });

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

        if (!validate()) {
            Toast.makeText(UserLoginActivity.this,"Login Failed",Toast.LENGTH_LONG).show();
            button.setEnabled(true);
            return;
        }
        button.setEnabled(false);

        prg = new ProgressDialog(UserLoginActivity.this,
                R.style.Theme_AppCompat_DayNight_Dialog);
        prg.setIndeterminate(true);
        prg.setMessage("Authenticating...");
        prg.show();

        new LoginTask().execute(nume,pass);

    }

    private class LoginTask extends AsyncTask<String,Void,Integer> {


        @Override
        protected Integer doInBackground(String... strings) {
            //return ApiHelper.busLogin(strings[0].toString(),strings[1].toString());

            return  ApiHelper.userLogin(strings[0],strings[1],UserLoginActivity.this);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            prg.dismiss();
            if(integer==-1){
                //fail
                Toast.makeText(UserLoginActivity.this,"Login Failed",Toast.LENGTH_LONG).show();
                button.setEnabled(true);


            }
            else{
                Toast.makeText(UserLoginActivity.this,"Login Succes User Id:"+integer,Toast.LENGTH_LONG).show();
                button.setEnabled(true);
                Intent i = new Intent(UserLoginActivity.this, MainActivity.class);
                i.putExtra("id", integer);//-1 guest
                i.putExtra("name",txtNume.getText().toString());
                startActivity(i);
            }
        }
    }

    public boolean validate() {
        boolean valid = true;

        String name = txtNume.getText().toString();
        String password = txtPass.getText().toString();

        if (name.isEmpty() || name.length()<=5) {
           txtNume.setError("Enter a valid username");
            valid = false;
        } else {
            txtNume.setError(null);
        }

        if ( !password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{5,}$")) {
            txtPass.setError("min 5 chars,at least 1 number,1 letter");
            valid = false;
        } else {
            txtPass.setError(null);
        }
        return valid;
    }
    //todo registered
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_SIGNUP)
        {
            if(resultCode==RESULT_OK){
                int id = data.getIntExtra("id",-1);//guest
                String name=data.getStringExtra("name");
                //start main activity after register
                Intent i = new Intent(UserLoginActivity.this, MainActivity.class);
                i.putExtra("id",id);//-1 guest
                i.putExtra("name",name);
                startActivity(i);
            }
        }
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
