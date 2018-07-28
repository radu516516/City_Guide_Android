package com.example.radu5.turistgroupchat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by radu5 on 3/26/2018.
 */

public class GroupChatRecyclerViewAdapter extends RecyclerView.Adapter<GroupChatRecyclerViewAdapter.ViewHolder>{
    private static final String TAG = "GroupChatRecyclerViewAd";

    //vars
    private List<Message> mMessages = new ArrayList<Message>();
    private Context mContext;
    private int[] mUsernameColors;


    //todo interfata comunicare cu main activity


    public GroupChatRecyclerViewAdapter(Context context,List<Message> messages){
        this.mContext=context;
        this.mMessages=messages;
        mUsernameColors = context.getResources().getIntArray(R.array.username_colors);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //todo maybe INFLATE VIEWS BASED ON TYPES OF MESSAGES , LIKE LOG, ACTIOn,WELCOME ETC U GET ME?
        //todo Watch Socket Io Example
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

            final Message message = mMessages.get(position);
           // Log.d(TAG, "onBindViewHolder: "+message.getMessage());


            holder.setMessage(message.getMessage());
            holder.setUsername(message.getUsername());

    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView mUsernameView;
        private TextView mMessageView;

        public ViewHolder(View itemView){
            super(itemView);
            mUsernameView=(TextView)itemView.findViewById(R.id.username);
            mMessageView = (TextView) itemView.findViewById(R.id.message);

        }
        public void setUsername(String username) {
            if (null == mUsernameView) return;
            mUsernameView.setText(username);
            mUsernameView.setTextColor(getUsernameColor(username));
        }

        public void setMessage(String message) {
            if (null == mMessageView) return;
            mMessageView.setText(message);
        }
        private int getUsernameColor(String username) {
            int hash = 7;
            for (int i = 0, len = username.length(); i < len; i++) {
                hash = username.codePointAt(i) + (hash << 5) - hash;
            }
            int index = Math.abs(hash % mUsernameColors.length);
            return mUsernameColors[index];
        }

    }

}
