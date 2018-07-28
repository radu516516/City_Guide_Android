package com.example.radu5.turistgroupchat.Liste;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.radu5.turistgroupchat.Model.List;
import com.example.radu5.turistgroupchat.R;

import java.lang.reflect.Array;
import java.util.ArrayList;


/**
 * Created by radu5 on 5/3/2018.
 */

public class StaggeredListRecyclerViewAdapter extends RecyclerView.Adapter<StaggeredListRecyclerViewAdapter.ViewHolder>
{
    //todo can set fixed size

    private static final String TAG = "StaggeredListRecyclerVi";

    private ArrayList<List> lists;
    private Context ctx;

    public StaggeredListRecyclerViewAdapter(ArrayList<List> lists,Context ctx) {
        this.lists = lists;
        this.ctx=ctx;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_grid_idem,parent,false);
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
                .load("http://raduhdd.asuscomm.com:3000"+lists.get(position).getThumbnailUrl())
                .apply(requestOptions)
                .into(holder.image);

        holder.name.setText(lists.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }


    public void swap(ArrayList<List> lists){
        if(lists==null || lists.size()==0){
            return;
        }
        if(lists!=null &&lists.size()>0){
            this.lists.clear();
        }
        this.lists.addAll(lists);
        this.notifyItemRangeInserted(0,lists.size());
    }
    //when load another page
    public void clear(){
        int curSize=this.getItemCount();
        lists.clear();
        //notifyItemRangeRemoved(0,curSize);
        //notifyDataSetChanged();
    }
    //load new page
    void loadNewData(ArrayList<List> lists ){
        this.lists=lists;
       // this.notifyItemRangeInserted(0,lists.size());
        notifyDataSetChanged();
    }
    //get list from position
    public List getList(int position){
        return ((lists!=null)&&(lists.size()!=0) ? lists.get(position):null);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView name;


        public ViewHolder(View itemView){
            super(itemView);
            image=(ImageView) itemView.findViewById(R.id.image);
            name=(TextView)itemView.findViewById(R.id.listName);
        }
        public void bindDrawable(Drawable drawable) {
            image.setImageDrawable(drawable);
        }
    }
}
