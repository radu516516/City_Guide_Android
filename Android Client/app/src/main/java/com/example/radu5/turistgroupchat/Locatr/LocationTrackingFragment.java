package com.example.radu5.turistgroupchat.Locatr;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.radu5.turistgroupchat.Model.BusRoute;
import com.example.radu5.turistgroupchat.R;
import com.example.radu5.turistgroupchat.Utils.ApiHelper;
import com.google.android.gms.common.api.Api;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by radu5 on 4/19/2018.
 */

//Fragment pt soferi de autobuze prin care trimit locatia la server

    //todo THIS BUS SOCKET IS ONLY in 1 ROOM
public class LocationTrackingFragment extends Fragment {
    private static final String TAG = "LocationTrackingFragmen";
    private Socket mSocket;//Socket of bus
    private String trackingRoomName;//Can switch(EX 48 dus , 48 intors different rooms)
    private String busName="CT-05-RDT";//
    private Boolean isConnected=false;
    private String roomName="1_48_0";//HardcoddedForNow

    //views
    private Button btn;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new FetchBusRouteDetails().execute(String.valueOf(5));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Connect socket
        connectSocket();
        //Setup Events
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_location_tracking,container,false);
        btn=v.findViewById(R.id.btnSendLocation);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               // changeTraseu(roomName,"1_48_1");

                //todo emit location message
                JSONObject msg=new JSONObject();
                try {
                    msg.put("latitude",Math.random()%100+1);
                    msg.put("longitude",Math.random()%100+1);
                    mSocket.emit("busMoved", msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        return v;
    }

    public void connectSocket(){
        try{
            Manager manager=new Manager(new URI("http://raduhdd.asuscomm.com:3000"));
           // mSocket= IO.socket("http://raduhdd.asuscomm.com:3000");
            mSocket = manager.socket("/bus-tracking");//conenct to namespace
            configSocketEvents();
            mSocket.connect();
            isConnected=true;
            Log.d(TAG, "connectSocket: CONNECTED TO SERVER");
        }catch(Exception e){
            System.out.println(e);
        }
    }
    public void configSocketEvents(){
        mSocket.on(Socket.EVENT_CONNECT,onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT,onDisconnect );
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        //todo my events from server
        mSocket.on("socketID",onSocketId );
        mSocket.on("serverMessage",onServerMessage);
        mSocket.on("busMoved",onUpdateLocation);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.off(Socket.EVENT_CONNECT,onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT,onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR,onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT,onConnectError);
        mSocket.off("socketID",onSocketId );
        mSocket.off("serverMessage",onServerMessage);
        mSocket.off("busMoved",onUpdateLocation);
        mSocket.disconnect();
    }


    //Socket event listeners
    private Emitter.Listener onConnect=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "onConnect: Client connected to server");
            JSONObject obj=new JSONObject();
            //todo Conectare la camera in care isi trimite pozitia
            try {
                obj.put("busName",busName);
                obj.put("room",roomName);
                obj.put("type","bus");
                mSocket.emit("joinBusTrackingRoom", obj);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private Emitter.Listener onDisconnect=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "onDisconnect: Client Disconnected to the server");
        }
    };
    private Emitter.Listener onConnectError=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "Client error connecting");
            //todo it keeps trying to reconnect to infinity
            //mSocket.disconnect();

        }
    };
    private Emitter.Listener onSocketId=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data=(JSONObject)args[0];
            try {
                String id=data.getString("id");
                Log.d(TAG, "SocketID: Your socket id on the server is : "+id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    //todo Message From server
    private Emitter.Listener onServerMessage= new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

                    JSONObject data=(JSONObject)args[0];
                    String username;
                    String message;
                    String createdAt;
                    try {
                        username=data.getString("from");
                        message=data.getString("text");
                        createdAt=data.getString("createdAt");
                        Log.d(TAG, "ServerMessage: "+username+":"+" "+message+" "+createdAt);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
        }
    };
    //todo Got location from a bus din acelasi room in care esti
    private Emitter.Listener onUpdateLocation=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //Got an update from some bus
            JSONObject data=(JSONObject)args[0];
            String busName;
            String lat;
            String lng;
            //todo IDENTIFIC UNIC UN AUTOBUZ PRIN BUS_NAME AKA NR INMATRICULARE UNIC
            //todo hasmap de markere si vad pe care il updatez
            try {
                busName=data.getString("from");
                lat=data.getString("latitude");
                lng=data.getString("longitude");
                Log.d(TAG, "Update Bus Location: "+busName+":"+" "+lat+","+lng);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };



    //Traseu dus -> traseu intors different rooms
    public void changeTraseu(String oldRoomName,String newRoomName){

        JSONObject obj=new JSONObject();
        try {
            obj.put("room",oldRoomName);
            mSocket.emit("leaveBusTrackingRoom", obj, new Ack() {
                @Override
                public void call(Object... args) {
                    //Bus has left the old room and can now join a new one
                    String a=String.valueOf(args[0]);
                    Log.d(TAG, "leaveBusTrackingRoom:  "+a);
                    //JOIN NEW ROOM NOW
                    JSONObject obj=new JSONObject();

                    try {
                        obj.put("busName",busName);
                        obj.put("room",newRoomName);
                        obj.put("type","bus");
                        mSocket.emit("joinBusTrackingRoom", obj);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //todo async task to get details about(Could use asynctaskLoader)
    private class FetchBusRouteDetails extends AsyncTask<String,Void,BusRoute>{

        @Override
        protected BusRoute doInBackground(String... string) {
            ApiHelper.busLogin("CT-01-RAT","1234");
            return ApiHelper.getBusRounte(Integer.valueOf(string[0]));
        }

        @Override
        protected void onPostExecute(BusRoute busRoute) {
            if(busRoute==null){
                //nu s-a downlaodat
                return;
            }
            Toast.makeText(getActivity(),"Downloaded",Toast.LENGTH_SHORT).show();
        }
    }



}
