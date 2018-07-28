package com.example.radu5.turistgroupchat;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by radu5 on 3/26/2018.
 */

//todo Fereastra in care are loc chatul, si conexiunea la serverul de socket.io
//todo Exemplu socket-io-android are si TYPING,NOT TYPING ETC

public class GroupChatFragment extends Fragment
{
    private static final String TAG = "GroupChatFragment";

    private Socket mSocket;//client socket
    private String room=null;
    private int roomId;
    private String groupName="Group Name";
    private String name="radu";
    private Boolean isConnected=false;
    //views
    private TextView mSendMessage;
    private EditText mNewMessage;
    private TextView mGroupName;
    private RecyclerView mRecyclerView;
    //vars
    private Message mMessage;
    private List<Message> mMessages = new ArrayList<Message>();
    private RecyclerView.Adapter mAdapter;

    public static GroupChatFragment newInstance(int roomId,String name,String grp) {
        Bundle args = new Bundle();
        GroupChatFragment fragment = new GroupChatFragment();
        args.putInt("roomid",roomId);
        args.putString("name",name);
        args.putString("grp",grp);
        fragment.setArguments(args);
        return fragment;
    }
    //todo event fires first before creating fragment or any of its views
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mAdapter=new GroupChatRecyclerViewAdapter(context,mMessages);//make adapter
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Started");
        roomId=getArguments().getInt("roomid");
        name=getArguments().getString("name");
        groupName=getArguments().getString("grp");
    }
    //todo first connect
    public void connectSocket(){
        try{
            Manager manager=new Manager(new URI("http://raduhdd.asuscomm.com:3000"));
            mSocket = manager.socket("/chat-groups");
            mSocket.connect();
            Log.d(TAG, "connectSocket: CONNECTED TO SERVER");
            isConnected=true;
        }catch(Exception e){
            System.out.println(e);
        }
    }
    //todo then config socket events
    public void configSocketEvents(){
        mSocket.on(Socket.EVENT_CONNECT,onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT,onDisconnect );
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        //todo my events from server
        mSocket.on("socketID",onSocketId );
        mSocket.on("newMessage",onNewMessage);
        mSocket.on("updateUserList",onUpdateUserList );
        mSocket.on("newLocationMessage", onLocationMessage);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View view=inflater.inflate(R.layout.fragment_group_chat,container,false);
       mRecyclerView=view.findViewById(R.id.recycler_view);
       mSendMessage=view.findViewById(R.id.post_message);
       mNewMessage=view.findViewById(R.id.input_message);
       mGroupName=view.findViewById(R.id.txtGroupName);
       mGroupName.setText(groupName);

       //todo setup recycler view
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);

        //mSendMessage.setOnClickListener(this);
        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo attempt send the message
                //other chececks like mUsername==null return
                if(mSocket.connected()){
                    String message=mNewMessage.getText().toString().trim();
                    if(TextUtils.isEmpty(message)){
                        mNewMessage.requestFocus();
                        return;
                    }
                    mNewMessage.setText(" ");
                    addMessage(name,message);//display in the recycler view the message

                    JSONObject msg=new JSONObject();
                    try {
                        msg.put("text",message);
                        
                        //todo EMIT THE MESSAGE
                        mSocket.emit("createMessage", msg, new Ack() {
                            @Override
                            public void call(Object... args) {
                                //get awk from server
                                String a=String.valueOf(args[0]);
                                Log.d(TAG, "Emit Message :"+a);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        //todo NUMAI CAND E IN VIEW SUNT CONNECTAT LA SOCKET
        Log.d(TAG, "onCreateView: CONNECTING SOCKET!!!!!!");
        room="room"+roomId;//UNIQUE ROOM ID
        //todo Cand intra in fragmentul asta e intr-un chat room
        //todo 1. CONNECT SOCKET on create
        connectSocket();
        //todo setup events comming from server
        configSocketEvents();
        return view;
    }
    //todo THIS CALLS WHEN I REPLACE FRAGMENT, SO I DISCONNECT HERE OR WHEN APP IS DESTROYED
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: DISCONNECTING SOCKET!!!");
        if(mSocket!=null)
        {
            disconnectSocket();
        }
    }
    @Override
    public void onDestroy() {//todo called when parent activity destroyed aswell
        Log.d(TAG, "onDestroy: DISCONNECTING SOCKET!!!");
        super.onDestroy();
        if(mSocket!=null)
        {
            disconnectSocket();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: ");
    }

    public void disconnectSocket(){
        mSocket.disconnect();//todo disconnect on destroy
        mSocket.off(Socket.EVENT_CONNECT,onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT,onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR,onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT,onConnectError);
        mSocket.off("socketID",onSocketId );
        mSocket.off("newMessage",onNewMessage);
        mSocket.off("updateUserList",onUpdateUserList );
        mSocket.off("newLocationMessage", onLocationMessage);
    }

    //todo ADD MESSAGE TO VIEW
    private void addMessage(String username, String message) {
        mMessages.add(new Message(username,message));
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        mRecyclerView.smoothScrollToPosition(mMessages.size() - 1);
    }
    private void scrollToBottom() {
        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    //todo My Event listeners#####################################################
    private Emitter.Listener onConnect=new Emitter.Listener() {
        @Override
        public void call(Object... args) {//todo ARgs = data from server
            Log.d(TAG, "Client Connect to the server ");
            //todo join a room after
            JSONObject obj=new JSONObject();
            try {
                obj.put("name",name);
                obj.put("room",room);
                mSocket.emit("join", obj, new Ack() {//awk=callback if smthing went wrong
                    @Override
                    public void call(Object... args) {
                        //todo AWKNOLEGEMT FROM THE SERVER
                        if(args.length>0) {
                            JSONObject data = (JSONObject) args[0];
                            try {
                                Log.d(TAG, "Room Join: " + data.getString("error"));//eroare
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mSocket.disconnect();//could not join the room

                        }
                        //no error name and room good
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };
    private Emitter.Listener onDisconnect=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "Client Disconnected to the server ");
        }
    };
    private Emitter.Listener onConnectError=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "Client error connecting");
        }
    };
    private Emitter.Listener onSocketId=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data=(JSONObject)args[0];
            try {
                String id=data.getString("id");
                Log.d(TAG, "Your socket id on the server is : "+id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    private Emitter.Listener onNewMessage= new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d(TAG, "call: GOT MESSAGE!");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data=(JSONObject)args[0];
                    String username;
                    String message;
                    String createdAt;
                    try {
                        username=data.getString("from");
                        message=data.getString("text");
                        createdAt=data.getString("createdAt");
                        Log.d(TAG, "Message: "+username+":"+" "+message);
                        addMessage(username, message);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //todo add message to recyccler view
                }
            });
        }
    };
    //todo DONT NEED TO DO NOTTIN
    private Emitter.Listener onUpdateUserList=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //todo current users in room
            JSONArray data=(JSONArray)args[0];
            Log.d(TAG, "Update User List:"+data.toString());
            //update ui or smthing

        }
    };
    //todo LATER
    private Emitter.Listener onLocationMessage=new Emitter.Listener() {
        @Override
        public void call(Object... args) {

        }
    };
}
