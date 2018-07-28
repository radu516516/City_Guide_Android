package com.example.radu5.turistgroupchat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.radu5.turistgroupchat.Liste.ListDetailFragment;
import com.example.radu5.turistgroupchat.Liste.ListUploadFragment;
import com.example.radu5.turistgroupchat.Liste.MyListeListFragment;
import com.example.radu5.turistgroupchat.Locatr.LocationTrackingFragment;
import com.example.radu5.turistgroupchat.Map.MyMapFragment;
import com.example.radu5.turistgroupchat.Model.ChatGroup;
import com.example.radu5.turistgroupchat.Model.ListItem;
import com.example.radu5.turistgroupchat.PathFinding.BusRoute;
import com.example.radu5.turistgroupchat.PathFinding.BusStation;
import com.example.radu5.turistgroupchat.PathFinding.PathFinder;
import com.example.radu5.turistgroupchat.PostDataTest.UploadImageFragment;
import com.example.radu5.turistgroupchat.UserApp.ChatGroupsListFragment;
import com.example.radu5.turistgroupchat.UserApp.HomeMapFragment;
import com.example.radu5.turistgroupchat.UserApp.UserRegisterActivity;
import com.example.radu5.turistgroupchat.Utils.ApiHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements IMainActivity, NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    private static final int LIST_DETAIL_FRAGMENT = 3;
    private static final int LIST_GALLERY_FRAGMENT = 2;
    private static final int MAP_FRAGMENT = 1;

    //fragments
    private ListDetailFragment listDetailFragment;
    private MyListeListFragment listGalleryFragment;
    private HomeMapFragment homeMapFragment;
    private ListUploadFragment listUploadFragment;
    private ChatGroupsListFragment chatGroupsListFragment;



    //vars
    private ArrayList<ListItem> mSelectedList = null;//todo SE SCHIMBA CONSTANT , DAU CA PARAMETRU CANT CONSTRUIESC MAIN MAP FRAGMENT
    private int cityId = 1;//WE NEED TO GET THIS DATA AT THE START ALSO USER ID IF THERE IS DEFAULT CONSANTA
    private int userId = -1;//DEFAULT GUEST=-1
    private String userName="guest";
    private FusedLocationProviderClient fusedLocationProviderClient;

    //views
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //todo Path Finding test

        //get user id
        userId = getIntent().getIntExtra("id", -1);
        if(getIntent().getStringExtra("name")==null){
            userName="guest";
        }
        else{
            userName=getIntent().getStringExtra("name");
        }

        Log.d(TAG, "onCreate: USER ID:" + userId);

        //get city he is in

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Task location = fusedLocationProviderClient.getLastLocation();
        location.addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Location currentLocation = (Location) task.getResult();
                    if(currentLocation!=null){
                        LatLng latLng1 = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                       // Log.d(TAG, "onComplete: got device location:"+currentLocation.get);
                        Geocoder geocoder=new Geocoder(MainActivity.this, Locale.ENGLISH);
                        StringBuilder builder = new StringBuilder();
                        try {
                            List<Address> address = geocoder.getFromLocation(latLng1.latitude, latLng1.longitude, 1);
                            //This is the complete address.
                            Log.d(TAG, "onComplete:  City:"+address.get(0).getLocality());

                            //todo get id of city from database
                            new getCity().execute(address.get(0).getLocality());//get city


                        } catch (IOException e) {

                        }
                        catch (NullPointerException e) {

                        }

                    }
                    else{//set default to dis
                        cityId=1;
                        init();
                    }

                }
                else{
                    //default use city 1

                }

            }
        });

        drawerLayout=findViewById(R.id.drawerlayout);
        navigationView=findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
       // init();

    }

    //start with list fragment
    private void init(){
            listGalleryFragment=MyListeListFragment.newInstance(cityId);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_content_frame, listGalleryFragment, "Gallery");
            //transaction.addToBackStack("Gallery");
            //todo dont add first one to backstack to fix white screen problem
            transaction.commit();
    }

    //todo navigation
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        //handle navigation item view clicks here
        switch(item.getItemId()){
            case R.id.nav_home:{
                homeMapFragment=HomeMapFragment.newInstance(mSelectedList,cityId);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main_content_frame,homeMapFragment, "HomeMap");
                transaction.addToBackStack("HomeMap");
                transaction.commit();
                item.setChecked(true);
                break;
            }
            case R.id.nav_liste:{
                init();
                item.setChecked(true);
                break;
            }
            case R.id.nav_upload:{
                if(userId!=-1) {
                    listUploadFragment = ListUploadFragment.newInstance(cityId, userId);
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.main_content_frame, listUploadFragment, "Upload");
                    transaction.addToBackStack("Upload");
                    transaction.commit();
                    //item.setChecked(true);
                }
                else{
                    Toast.makeText(this,"You are not allowed to create a list!",Toast.LENGTH_LONG).show();
                }
                break;
            }
            case R.id.nav_groups:{
                Log.d(TAG, "onNavigationItemSelected: userId:"+userId);
                chatGroupsListFragment=ChatGroupsListFragment.newInstance(cityId,userId);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main_content_frame,chatGroupsListFragment, "ChatGroups");
                transaction.addToBackStack("ChatGroups");
                transaction.commit();
                //item.setChecked(true);
                break;
            }
        }
        drawerLayout.closeDrawers();
        return false;
    }



    //todo EVENIMENTE CARE VIN DE LA FRAGMENTE
    @Override
    public void onListItemLongClicked(int listId,String ListName) {
        Log.d(TAG, "onListItemLongClicked: LIST SELECTED :"+listId);
        //DISPLAY LIST DETAIL FRAGMENT
        listDetailFragment=(ListDetailFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder);

        if(listDetailFragment!=null){
            //its available
            //call a method to update its view, we in 2 pane mode
        }
        else{
            //swap frags
            listDetailFragment=ListDetailFragment.newInstance(listId,ListName);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_content_frame,listDetailFragment, "ListDetail");
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    //todo A selectat o lista pe care vrea sa o viziteze
    @Override
    public void onListSelected(ArrayList<ListItem> list) {
        //todo remember to handle configuration changeges
        mSelectedList=list;//a schimbat selectia
        Log.d(TAG, "onListSelected: LIST ITEMS SELECTED");
    }

    //todo A selectat un chat group
    @Override
    public void onChatGroupSelected(int groupid,String grp) {
            GroupChatFragment groupChatFragment=GroupChatFragment.newInstance(groupid,userName,grp);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_content_frame,groupChatFragment, "chatFragment");
            transaction.addToBackStack(null);
            transaction.commit();
    }


    private class getCity extends AsyncTask<String,Void,Integer> {

        @Override
        protected Integer doInBackground(String... strings) {
            //return ApiHelper.busLogin(strings[0].toString(),strings[1].toString());

            return  ApiHelper.getCityId(strings[0]);
        }
        @Override
        protected void onPostExecute(Integer integer) {

            if(integer==-1){
                //didnt get city //default

            }
            else{
                Log.d(TAG, "onPostExecute: Got city Id:"+integer);
                init();
            }
        }
    }


}
