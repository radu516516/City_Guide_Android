package com.example.radu5.turistgroupchat.Liste;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.radu5.turistgroupchat.Model.List;
import com.example.radu5.turistgroupchat.Model.ListItem;
import com.example.radu5.turistgroupchat.R;
import com.example.radu5.turistgroupchat.Utils.ApiHelper;
import com.example.radu5.turistgroupchat.Utils.RecyclerItemClickListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static android.content.ContentValues.TAG;

/**
 * Created by radu5 on 5/5/2018.
 */
//todo REMEMBER LATER SCALE DOWN IMAGES WHEN U UPLOAD SO THEY ARE NOT TOO BIGGO

//todo DO VERIFICATIONS


//todo FOR MULTIPART HTTP I USE OKHTTP LIBRARY CA MA COMPLICAM MULT

public class ListUploadFragment extends Fragment implements DataEntryDialog.DataInputDialogLister,CreateListDialog.CreateListDialogLister,RecyclerItemClickListener.OnRecyclerClickListener {

    private static final String TAG = "ListUploadFragment";
    //Vars
    private ArrayList<ListItem> mItems=new ArrayList<>();
    private List list=new List(-1,"","",1,-1,"");//list to be uploaded;
    RecyclerViewAdapter adapter;
    int cityid=1;//hardcoded
    int userid=1;//harcoded
    //todo Uploader id stiu din token ca ii trimit si token in header,token primesc la login

    //views
    private FloatingActionButton fab;
    private ImageButton btnUpload;
    private RecyclerView recyclerView;
    ProgressDialog progressDialog;

    public static ListUploadFragment newInstance(int cid,int uid) {

        Bundle args = new Bundle();

        ListUploadFragment fragment = new ListUploadFragment();
        args.putInt("cityid",cid);
        args.putInt("userid",uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
        //setRetainInstance(true);
        cityid=getArguments().getInt("cityid");
        userid=getArguments().getInt("cityid");

    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_list_upload,container,false);

        fab=v.findViewById(R.id.fab_add);

        recyclerView=(RecyclerView)v.findViewById(R.id.recycler_view_listitemsupload);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(),recyclerView,this));

        //todo SELECTED LIST, PASS DATA TO MAIN ACTIVITY
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //todo Add location to recycler view
                DialogFragment dialog=new DataEntryDialog();
                dialog.setTargetFragment(ListUploadFragment.this,300);
                dialog.show(getFragmentManager(),"upload_dialog");
            }
        });
        //todo CHECK IF EVERYTHING OK
        //todo ASK TO NAME LIST,  POST LIST, GET ID IF OK, UPLOAD ALL THE ELEMENTS TO THAT LIST ID, BEGONE
        btnUpload=v.findViewById(R.id.btn_upload);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //todo DO CHECKS(nr of elements min ,max) etc

                //UPLOAD LOCATION AND LIST


               /*MultipartBody.Builder buildernew = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("listName", "testList1")
                        .addFormDataPart("listDesc", "testDescription");

                for (int i = 0; i < mItems.size(); i++) {
                    File f = new File(mItems.get(i).getUrl());
                    if (f.exists()) {
                        MediaType MEDIA_TYPE = mItems.get(i).getUrl().endsWith("png") ?
                                MediaType.parse("image/png") : MediaType.parse("image/jpeg");
                        Log.d(TAG, "onClick: exists");
                        buildernew.addFormDataPart("file" + i, "file" + i + ".png", RequestBody.create(MEDIA_TYPE, f));
                    }

                }
                Request request = new Request.Builder()
                        .url("http://raduhdd.asuscomm.com:3000/api/lists/createList")
                        .post(buildernew.build())
                        .build();*/

               // uploadAlbumImage(1,mItems);

                //todo Alert Dialog To enter list name and description and confirm list creation


                //todo WHEN DIS IS DONE , LIST STARTS UPLOADING TO THE SERVER
                CreateListDialog dialog=new CreateListDialog();
                dialog.setTargetFragment(ListUploadFragment.this,200);
                dialog.show(getFragmentManager(),"upload_list_dialog");
            }


        });
        //btnUpload.setEnabled(false);
        setupAdapter();
        return v;
    }


    private void setupAdapter() {
        if (isAdded()) {
            adapter=new RecyclerViewAdapter(mItems,getContext());
            recyclerView.setAdapter(adapter);

        }
    }

    //todo Got Data from imput dialog
    @Override
    public void onFinishImputDialog(ListItem item) {
            //todo ADD TO ARRAYLIST, NOTIFY RECYCLERVIEW ITEM INSERTED
        Log.d(TAG, "onFinishImputDialog: GOT ITEM:"+item.getNume());
        int curSize = adapter.getItemCount();
        mItems.add(item);
        adapter.notifyItemInserted(curSize);
        recyclerView.scrollToPosition(curSize);

    }
    //todo PRESSED UPLOAD ,ENTERED LIST NAME and DESCRIPTION , NOW UPLOAD EVERYTHING
    @Override
    public void onDialogPositiveClick(String name, String description) {

        //todo THE UPLOAD
        //todo METHOD 1 SEND BIG MULTIPART REQUEST ALL THE DATA AT ONCE
        Log.d(TAG, "onDialogPositiveClick: "+name+" "+description);
        //uploadList(name,description,mItems.get(0).getUrl());

        //todo DO CHECKS IF LIST CONTAINS AT LEAST 3 ELEMENTS
        int curSize = adapter.getItemCount();
        if(curSize<3){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("List must contain at least 3 locations")
                    .setTitle("Upload Failed");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            TextView messageView = (TextView)dialog.findViewById(android.R.id.message);
            messageView.setGravity(Gravity.CENTER);
        }
        else{
            uploadWholeListAtOnce(name,description,mItems);
        }
    }
    //todo one item at a time
    public void uploadList(String name,String desc,String fileUrl){
        progressDialog=new ProgressDialog(getContext());
        progressDialog.setTitle("Uploading");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
                File f=new File(fileUrl);
                if(f.exists()) {
                    /** Changing Media Type whether JPEG or PNG **/
                    MediaType MEDIA_TYPE =fileUrl.endsWith("png") ?
                            MediaType.parse("image/png") : MediaType.parse("image/jpeg");

                    OkHttpClient clinet=new OkHttpClient();
                    RequestBody file_body=RequestBody.create(MEDIA_TYPE,f);
                    Log.d(TAG, "run: Gto file");

                    MultipartBody.Builder multipartBuilder=new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("type",MEDIA_TYPE.toString())
                            .addFormDataPart("name",name)
                            .addFormDataPart("description",desc)
                            .addFormDataPart("image",fileUrl.substring(fileUrl.lastIndexOf("/")+1),file_body);

                    Request request=new Request.Builder()
                            .url("http://raduhdd.asuscomm.com:3000/api/lists")
                            .post(multipartBuilder.build())
                            .build();
                    Log.d(TAG, "run: send request");
                    try {
                        Response response=clinet.newCall(request).execute();
                        if(!response.isSuccessful()){
                            Log.d(TAG, "run: Not succes");
                            throw new IOException("Error :"+response);
                        }
                        progressDialog.dismiss();

                        //todo DESTROY FRAGMENT/CLEAR LIST
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
    }

    //todo UPLOADS WHOLE LIST AT ONCE, TRIMITE TOATE POZELE, DATELE DE ODATA
    public  void uploadWholeListAtOnce(String listName,String listDesc, ArrayList<ListItem> listItems) {
            MultipartBody.Builder  multipartBuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("listName",listName)
                    .addFormDataPart("listDesc",listDesc)
                    .addFormDataPart("cityId",String.valueOf(cityid))
                    .addFormDataPart("userId",String.valueOf(userid));

            int length = listItems.size();
            int noOfImageToSend = 0;
            for(int i = 0; i < length; i++) {
                /**
                 * Getting Photo Caption and URL
                 */
                ListItem item=listItems.get(i);
                String photoUrl=item.getUrl();
                String itemDesc=item.getDescriere();
                String itemName=item.getNume();
                double lat=item.getLat();
                double lng=item.getLng();

                File sourceFile = new File(photoUrl);

                if(sourceFile.exists()) {
                    /** Changing Media Type whether JPEG or PNG **/
                    MediaType MEDIA_TYPE = photoUrl.endsWith("png") ?
                            MediaType.parse("image/png") : MediaType.parse("image/jpeg");

                    /** Adding in MultipartBuilder **/
                    multipartBuilder.addFormDataPart("listItemName", itemName);//todo aveam +i la fiecare da asa fac sa fie array
                    multipartBuilder.addFormDataPart("listItemDesc", itemDesc);
                    multipartBuilder.addFormDataPart("listItemLat",String.valueOf(lat));
                    multipartBuilder.addFormDataPart("listItemLng",String.valueOf(lng));
                    multipartBuilder.addFormDataPart("listItemImages", sourceFile.getName(), RequestBody.create(MEDIA_TYPE, sourceFile));
                    /** Counting No Of Images **/
                    noOfImageToSend++;
                }
            }
            RequestBody requestBody = multipartBuilder
                    .addFormDataPart("nrImages", String.valueOf(noOfImageToSend))
                    .build();

            Request request = new Request.Builder()
                    .url("http://raduhdd.asuscomm.com:3000/api/lists/createList/upload")// .addHeader("authorization", "Bearer " + Credentials.getAuthToken(mContext))
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            progressDialog=new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading");
            progressDialog.setMessage("Please wait...");
            progressDialog.show();


            //todo AFTER UPLOAD ATTEMPT
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    progressDialog.dismiss();
                    e.printStackTrace();
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    progressDialog.dismiss();
                    try (ResponseBody responseBody = response.body()) {
                        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                        try {
                            String responseData = response.body().string();
                            JSONObject json = new JSONObject(responseData);
                            final String message = json.getString("message");
                            if(message.equals("good")){
                                Log.i(TAG, "onResponse: "+message);
                                //todo UPLOAD SUCCES GO BACK NOW
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                         getActivity().onBackPressed();//todo CLOSE FRAGMENT GO BACK
                                    }
                                });
                            }
                            else{//todo UPLOAD NOT SUCCES DISPLAY THE PROBLEM TO THE USER
                                final String error=json.getString("error");
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setMessage(error)
                                                .setTitle("Upload Failed");
                                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                // User clicked OK button
                                                dialog.dismiss();
                                            }
                                        });
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                        TextView messageView = (TextView)dialog.findViewById(android.R.id.message);
                                        messageView.setGravity(Gravity.CENTER);
                                    }
                                });

                            }
                        } catch (JSONException e) {

                        }
                        //String responseStr = response.body().string();
                        //Log.i(TAG, "responseStr : "+responseStr );
                    }
                }
            });
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
       // ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
    }

    //todo For delete element
    @Override
    public void onItemLongClick(View view, int position) {
        Toast.makeText(getContext(),"Long tap at position "+position,Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete Item");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                //remove item at position
                mItems.remove(position);
                adapter.notifyItemRemoved(position);

            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();


    }


    //todo RECYCLERVIEW ADAPTER
    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

        private static final String TAG = "ListItemsRecyclerViewAd";

        private ArrayList<ListItem> mItems;
        private Context ctx;

        public RecyclerViewAdapter(ArrayList<ListItem> mItems, Context ctx) {
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
                    .fitCenter()//scale to fit entire image withing imageview //not needed daca pun options bine la imageview
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_launcher_background);
            Glide.with(ctx)
                    .load(mItems.get(position).getUrl())
                    .apply(requestOptions)
                    .into(holder.image);

            holder.name.setText(mItems.get(position).getNume());
            holder.description.setText(mItems.get(position).getDescriere());
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
        }
    }
}
