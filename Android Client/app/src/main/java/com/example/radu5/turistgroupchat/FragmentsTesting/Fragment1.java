package com.example.radu5.turistgroupchat.FragmentsTesting;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.radu5.turistgroupchat.R;

/**
 * Created by radu5 on 4/17/2018.
 */

public class Fragment1 extends Fragment {
    //todo Fragment=class implementing a portion of an activity(Embeted in activity cannot run independently)
    //todo Fragment=XML LAYOUT + JAVA CLASS
    //todo Fragments encapsulate views and logic (easyer to reuse withing activities)(standalone components)
    //todo Fragment-oriented arhitecture => Activities = navigational containers(responsible for navigation to other activities,presenting fragments and passing data)

    //todo Activities = navigation controllers
    //1.Navigating through intents
    //2.presenting nav components(navigation drawer,view pager)
    //3.hiding and showing relevant fragments using the fragment manager
    //4.getting data from intents and passing data between fragments

    //todo Fragments=content controllers
    //1.Layouts,Views,displaying content
    //2.event handling logic
    //3.logic erro handling
    //4.Triggering network requests
    //5.retrieve and store data through model objects

    //todo @@@ ACTIVITIES = NAVIGATION , FRAGMENTS=VIEWS AND LOGIC
    //ADD FRAGMENTS with the FragmentManager
    //ADD THEM TO A PLACEHOLDER    FRAMELAYOUT (WHERE FRAGMENT IS INSERTED)


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    //DATA INITIALIZATION
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //INFLATE THE VIEW
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragments_testing_fragment1,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
