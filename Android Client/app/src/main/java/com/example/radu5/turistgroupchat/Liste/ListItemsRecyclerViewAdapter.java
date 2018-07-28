package com.example.radu5.turistgroupchat.Liste;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.radu5.turistgroupchat.Model.ListItem;
import com.example.radu5.turistgroupchat.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by radu5 on 5/4/2018.
 */

public class ListItemsRecyclerViewAdapter extends RecyclerView.Adapter<ListItemsRecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "ListItemsRecyclerViewAd";

    private ArrayList<ListItem> mItems;
    private Context ctx;

    public ListItemsRecyclerViewAdapter(ArrayList<ListItem> mItems, Context ctx) {
        this.mItems = mItems;
        this.ctx = ctx;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_detail_element,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RequestOptions requestOptions = new RequestOptions()
                .fitCenter()//scale to fit entire image withing imageview
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_launcher_background);
        //todo DOWNLOAD IMAGE
        Glide.with(ctx)
                .load("http://raduhdd.asuscomm.com:3000"+mItems.get(position).getUrl())
                .apply(requestOptions)
                .into(holder.image);

        holder.name.setText(mItems.get(position).getNume());
        holder.description.setText(mItems.get(position).getDescriere());
        //holder.description.setMovementMethod(new ScrollingMovementMethod());//so description scrolls
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView name;
        private TextView description;


        public ViewHolder(View itemView){
            super(itemView);
            image=(ImageView) itemView.findViewById(R.id.image);
            name=(TextView)itemView.findViewById(R.id.listName);
            description=(TextView)itemView.findViewById(R.id.txtLocationDescription);
            description.setSelected(true);
        }
        public void bindDrawable(Drawable drawable) {
            image.setImageDrawable(drawable);
        }
    }
}
