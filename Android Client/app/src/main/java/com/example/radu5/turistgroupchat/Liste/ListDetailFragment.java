package com.example.radu5.turistgroupchat.Liste;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.radu5.turistgroupchat.IMainActivity;
import com.example.radu5.turistgroupchat.Model.List;
import com.example.radu5.turistgroupchat.Model.ListItem;
import com.example.radu5.turistgroupchat.R;
import com.example.radu5.turistgroupchat.Utils.ApiHelper;

import java.util.ArrayList;

/**
 * Created by radu5 on 3/29/2018.
 */

public class ListDetailFragment extends Fragment {

    //todo display continut lista
    private static final String TAG = "ListDetailFragment";

    //interface
    private IMainActivity mInterface;//tell it list has been selected and give the data

    //vars
    private ArrayList<ListItem> mItems=new ArrayList<>();
    private int list=0;
    private String listName;
    private ListItemsRecyclerViewAdapter adapter;

    //views
    private RecyclerView listItemsRecyclerView;
    private TextView txtListName;
    private FloatingActionButton fab;

    public static ListDetailFragment newInstance(int listId,String name) {

        Bundle args = new Bundle();
        args.putInt("listId",listId);//selected list
        args.putString("listName",name);
        ListDetailFragment fragment = new ListDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IMainActivity) {
            mInterface = (IMainActivity) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement IMainActivity");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        list=getArguments().getInt("listId",0);
        listName=getArguments().getString("listName","No Name");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_list_detail,container,false);

        listItemsRecyclerView=(RecyclerView)v.findViewById(R.id.recycler_view_listitems);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        listItemsRecyclerView.setLayoutManager(linearLayoutManager);
        listItemsRecyclerView.setHasFixedSize(true);
        listItemsRecyclerView.setItemViewCacheSize(20);
        listItemsRecyclerView.setDrawingCacheEnabled(true);
        listItemsRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
       // setupAdapter();

       // listItemsRecyclerView.setNestedScrollingEnabled(true);

        txtListName=v.findViewById(R.id.textListName);

        txtListName.setText(listName);

        fab=v.findViewById(R.id.fab);

        //todo SELECTED LIST, PASS DATA TO MAIN ACTIVITY
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mItems!=null&&mItems.size()>0) {
                    //pass data
                    mInterface.onListSelected(mItems);//todo CHANGE SELECTION NEXT TYPE WE GO TO MAP WE SEE DIS SELCTED LIST
                    fab.setEnabled(false);//DISABLE SO HE CANT SPAM
                }
            }
        });

        //start downloading the data
        new FetchLists().execute(list);
        return v;

    }
    private void setupAdapter() {
        if (isAdded()) {
            adapter=new ListItemsRecyclerViewAdapter(mItems,getContext());
            listItemsRecyclerView.setAdapter(adapter);
        }
    }


    //download once
    private class FetchLists extends AsyncTask<Integer,Void,ArrayList<ListItem>> {

        @Override
        protected ArrayList<ListItem> doInBackground(Integer... ints) {//city id
            return ApiHelper.getListItems(Integer.valueOf(ints[0]));
        }
        @Override
        protected void onPostExecute(ArrayList<ListItem> l) {
            if(l.isEmpty()){
                //EROARE LISTA NU ARE ELEMENTE O SA FAC MAI TARZIU SA FISEZ IN GALERIE DOAR LISTELE CARE AU ELEMENTE
                return;
            }
            else{
                //update
                mItems=l;
                setupAdapter();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //setHasOptionMenu(true) in on create, handle onOptionsItemSelected(MenuItem item) {
        super.onCreateOptionsMenu(menu, inflater);
    }
}
