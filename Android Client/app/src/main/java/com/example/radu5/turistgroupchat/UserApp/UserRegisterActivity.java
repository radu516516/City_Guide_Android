package com.example.radu5.turistgroupchat.UserApp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.radu5.turistgroupchat.R;
import com.example.radu5.turistgroupchat.Utils.ApiHelper;

public class UserRegisterActivity extends AppCompatActivity {
    private EditText txtNume;
    private EditText txtPass;
    private Button button;
    private ProgressDialog prg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);

        txtNume=findViewById(R.id.txtUserName);
        txtPass=findViewById(R.id.txtPassword);
        button=findViewById(R.id.btnLogIn);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nume=txtNume.getText().toString();
                String pass=txtPass.getText().toString();

                if (!validate()) {
                    Toast.makeText(UserRegisterActivity.this,"Register Failed",Toast.LENGTH_LONG).show();
                    button.setEnabled(true);
                    return;
                }
                button.setEnabled(false);

                prg = new ProgressDialog(UserRegisterActivity.this,
                        R.style.Theme_AppCompat_DayNight_Dialog);
                prg.setIndeterminate(true);
                prg.setMessage("Authenticating...");
                prg.show();

                new RegisterTask().execute(nume,pass);
            }
        });
    }

    private class RegisterTask extends AsyncTask<String,Void,Integer> {


        @Override
        protected Integer doInBackground(String... strings) {
            //return ApiHelper.busLogin(strings[0].toString(),strings[1].toString());

            return  ApiHelper.userRegister(strings[0],strings[1],UserRegisterActivity.this);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            prg.dismiss();
            if(integer==-1){
                //fail
                //Toast.makeText(UserRegisterActivity.this,"Register Failed,username taken",Toast.LENGTH_LONG).show();
                button.setEnabled(true);


            }
            else{
                Toast.makeText(UserRegisterActivity.this,"Register Succes User Id:"+integer,Toast.LENGTH_LONG).show();
                button.setEnabled(true);
                Intent resultIntent = new Intent();

                resultIntent.putExtra("id", integer);
                resultIntent.putExtra("name",txtNume.getText().toString());
                setResult(RESULT_OK,resultIntent);
                finish();//REgister was succes now go to main activity

                //Start next activity
               /* Intent i = new Intent(UserLoginActivity.this, BusTrackingActivity.class);
                i.putExtra("routeId", integer);
                i.putExtra("busName",txtNume.getText().toString());
                startActivity(i);*/
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
}
