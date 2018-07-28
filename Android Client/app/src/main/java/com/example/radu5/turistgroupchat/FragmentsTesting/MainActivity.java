package com.example.radu5.turistgroupchat.FragmentsTesting;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.radu5.turistgroupchat.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragments_testing_activity);

        //Load Fragments
        Fragment1 fragment=new Fragment1();

        //Add Fragment Replace Frame layout
        //todo ALLOWS TO ADD FRAGMENTS TO FRAMELAYOUT
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_placeholder,fragment).commit();
        //todo Transaction = animations,backstack,show and hide fragments




    }
}
