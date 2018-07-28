package com.example.radu5.turistgroupchat.UserApp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.radu5.turistgroupchat.Model.ListItem;
import com.example.radu5.turistgroupchat.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by radu5 on 4/28/2018.
 */

public class SelectedListRecyclerViewAdapter  extends RecyclerView.Adapter<SelectedListRecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "SelectedListRecyclerVie";

    private ArrayList<ListItem> listItems;

    private Context mContext;

    //Listener
    private OnItemClickListener listener;
    //Listener interface
    public interface OnItemClickListener{
        void onItemClick(ListItem i);
    }
    //Method that alows parent to define the listener
    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener=listener;
    }


    public SelectedListRecyclerViewAdapter(ArrayList<ListItem> items,Context context){
        this.listItems=items;
        this.mContext=context;
    }


    //Inflates each layout
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_location,parent,false);
        return new ViewHolder(v);
    }

    //Populating data into the item thorugh the holder
    //DATA ATTACHED TO LIST ITEM
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: ");

        RequestOptions requestOptions = new RequestOptions()
                .fitCenter()//scale to fit entire image withing imageview
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_launcher_background);
        //todo DOWNLOAD IMAGE
        Glide.with(mContext)
                .asBitmap()
                .load("http://raduhdd.asuscomm.com:3000"+listItems.get(position).getUrl())
                .apply(requestOptions)
                .into(holder.image);

        holder.txtLocationName.setText(listItems.get(position).getNume());
        holder.txtLocationDescription.setText(listItems.get(position).getDescriere());

       /* holder.btnVisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext,"Button Clicked:"+position+" :"+listItems.get(position).getNume(),Toast.LENGTH_SHORT).show();
            }
        });*/
        holder.txtLocationDescription.setMovementMethod(new ScrollingMovementMethod());//so description scrolls
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    //each of the view, cache views for fast acces
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private CircleImageView image;
        private TextView txtLocationName;
        private TextView txtLocationDescription;
        private Button btnVisit;


        public ViewHolder(View itemView){
            super(itemView);
            image=(CircleImageView)itemView.findViewById(R.id.image);
            txtLocationName=(TextView)itemView.findViewById(R.id.txtLocationName);
            txtLocationDescription=(TextView)itemView.findViewById(R.id.txtLocationDescription);
            btnVisit=(Button)itemView.findViewById(R.id.btnVisit);


            btnVisit.setOnClickListener(this);
            //Merge si asa

        }

        @Override
        public void onClick(View v) {
            int position=getAdapterPosition();
            Toast.makeText(mContext,"Button Clicked:"+position+" :"+listItems.get(position).getNume(),Toast.LENGTH_SHORT).show();

            //fire vent
            if(listener!=null){
                listener.onItemClick(listItems.get(position));
            }

        }
    }
}
