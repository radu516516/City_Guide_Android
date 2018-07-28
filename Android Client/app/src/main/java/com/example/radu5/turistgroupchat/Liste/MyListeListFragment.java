package com.example.radu5.turistgroupchat.Liste;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.radu5.turistgroupchat.IMainActivity;
import com.example.radu5.turistgroupchat.Model.List;
import com.example.radu5.turistgroupchat.R;
import com.example.radu5.turistgroupchat.Utils.ApiHelper;
import com.example.radu5.turistgroupchat.Utils.RecyclerItemClickListener;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by radu5 on 3/29/2018.
 */


public class MyListeListFragment extends Fragment implements RecyclerItemClickListener.OnRecyclerClickListener   {

    private static final String TAG = "MyListeListFragment";


    //Listeners and interfaces
    //todo Comunicare on click to hosting activity sa afiseze liste Detail
    private IMainActivity listener;


    //vars
    private ArrayList<List> mItems=new ArrayList<>();
    private int page=1;
    private int maxPage=-1;
    private int cityId=1;//todo Hardcoded
    private StaggeredListRecyclerViewAdapter adapter;
    private int selectedListId=-1;//will give to main activity through interface
    FetchLists fetchLists=null;

    //Views
    private RecyclerView listsRecyclerView;
    private ImageButton btnNext;
    private ImageButton btnPrev;
    private TextView txtPage;
    private TextView txtCity;
    private SwipeRefreshLayout swipeContainer;


    public static MyListeListFragment newInstance(int id) {
        Bundle args = new Bundle();
        MyListeListFragment fragment = new MyListeListFragment();
        args.putInt("cityid",id);
        fragment.setArguments(args);
        return fragment;
    }


    //todo NO LRU CACHE FOR NOW OR DISK CACHE
    //todo NO HANDLER TRHEAD BACKGROUND DOWNLOADER FOR NOW

    //fragment methods
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IMainActivity) {
            listener = (IMainActivity) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement IMainActivity");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);//todo IDK ABOUT DIS
        //Start data downloading
        cityId=getArguments().getInt("cityid");

    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_lists_gallery, container, false);
        listsRecyclerView=v.findViewById(R.id.recycler_view_list);
        StaggeredGridLayoutManager staggeredGridLayoutManager=new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        listsRecyclerView.setLayoutManager(staggeredGridLayoutManager);
       // listsRecyclerView.setHasFixedSize(true);//are fixed sized
        //listsRecyclerView.setItemViewCacheSize(20);
        //listsRecyclerView.setDrawingCacheEnabled(true);
       // mPhotoRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);*/
        //todo CLICK LISTENER LOAD NEW FRAGMENT
        listsRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(),listsRecyclerView,this));

        //refresh
        swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(fetchLists!=null)
                {
                    fetchLists.cancel(true);
                }
                fetchLists=new FetchLists();
                fetchLists.execute(page,cityId);
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        //ADAPTER
        setupAdapter();

        //rest of views
        btnNext=v.findViewById(R.id.imageButtonNext);
        btnPrev=v.findViewById(R.id.imageButtonPrev);
        txtPage=v.findViewById(R.id.textPage);
        txtCity=v.findViewById(R.id.textCity);

        //Load next page
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(!btnPrev.isEnabled()){
                    btnPrev.setEnabled(true);
                }
                page++;//pana cand empty
                //txtPage.setText("Page: "+page+"/"+maxPage);
               // fetchLists.cancel(true);
                if(fetchLists!=null)
                {
                    fetchLists.cancel(true);
                }
                fetchLists=new FetchLists();
                fetchLists.execute(page,cityId);
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(page==1){
                    btnPrev.setEnabled(false);
                    btnNext.setEnabled(true);
                }
                else{
                    if(!btnNext.isEnabled()){
                        btnNext.setEnabled(true);
                        page--;
                    }
                    page--;
                    if(fetchLists!=null)
                    {
                        fetchLists.cancel(true);
                    }
                  //  fetchLists.cancel(true);
                    //txtPage.setText("Page: "+page+"/"+maxPage);
                    fetchLists=new FetchLists();
                    fetchLists.execute(page,cityId);
                }
            }
        });
        //first time download
        new FetchLists().execute(page,cityId);//first time download page 1
        return v;
    }

    private void setupAdapter() {
        if (isAdded()) {
            adapter=new StaggeredListRecyclerViewAdapter(mItems,getContext());
            listsRecyclerView.setAdapter(adapter);
         //   swipeContainer.setRefreshing(false);
        }
    }


    //Events
    //todo LONG CLICKED LIST ITEM
    @Override
    public void onItemLongClick(View view, int position) {
        selectedListId=mItems.get(position).getId();
        Toast.makeText(getContext(),"Long tap at position "+position,Toast.LENGTH_SHORT).show();
        //Tell Main Activity To Load ListDetail Fragment and give id of selected list
        //listener.onListItemSelected(String.valueOf(selectedListId));
        listener.onListItemLongClicked(selectedListId,mItems.get(position).getTitle());//todo TELL MAIN ACTIVITY TO LOAD CONTINUT LISTA
    }

    //asynctask
    //todo DOWNLOAD NEW LISTS
    private class FetchLists extends AsyncTask<Integer,Void,ArrayList<List>> {

        @Override
        protected ArrayList<List> doInBackground(Integer... ints) {//city id

            return ApiHelper.getListeTuristice(Integer.valueOf(ints[0]),Integer.valueOf(ints[1]),MyListeListFragment.this);//page and city
        }
        @Override
        protected void onPostExecute(ArrayList<List> l) {
            swipeContainer.setRefreshing(false);
            if(l.isEmpty()){
                //page prea mare
                page--;
                btnNext.setEnabled(false);
                return;
            }
            else{
                //update;
                mItems=l;
               // setupAdapter();
                adapter.clear();
                adapter.loadNewData(mItems);
                //adapter.notifyDataSetChanged();
                listsRecyclerView.smoothScrollToPosition(0);
                txtPage.setText("Page: "+page+"/"+maxPage);
            }
        }

        @Override
        protected void onCancelled() {
            Log.d(TAG, "onCancelled: ");
            super.onCancelled();
        }
    }

    //todo Set dis from asynctask
    public void setMaxPage(int maxPage) {
        if(maxPage!=-1){
            this.maxPage = maxPage;//setez odata
        }
    }
    //life cycle stuff
}
