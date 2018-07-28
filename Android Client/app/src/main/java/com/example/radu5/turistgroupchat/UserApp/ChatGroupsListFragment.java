package com.example.radu5.turistgroupchat.UserApp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.radu5.turistgroupchat.IMainActivity;
import com.example.radu5.turistgroupchat.Liste.ListUploadFragment;
import com.example.radu5.turistgroupchat.Liste.MyListeListFragment;
import com.example.radu5.turistgroupchat.Model.ChatGroup;
import com.example.radu5.turistgroupchat.R;
import com.example.radu5.turistgroupchat.Utils.ApiHelper;
import com.example.radu5.turistgroupchat.Utils.RecyclerItemClickListener;

import java.util.ArrayList;

/**
 * Created by radu5 on 5/27/2018.
 */

public class ChatGroupsListFragment extends Fragment implements RecyclerItemClickListener.OnRecyclerClickListener,GroupCreateDialog.GroupCreateDialogListener
{
    private static final String TAG = "ChatGroupsListFragment";

    //Vars
    int cityid=1;
    int userid=-1;
    private int page=1;
    private ArrayList<ChatGroup> chatGroups;
    private ChatGroupsAdapter adapter;
    private IMainActivity listener;
    LinearLayoutManager manager;
    ProgressBar progress;


    //views
    private FloatingActionButton fab;
    private SwipeRefreshLayout swipeContainer;
    private RecyclerView recyclerView;//display group chats
    Boolean isScrolling=false;
    int currentItems,totalItems,scrollOutItems;//getChild,getItemCount
    //scrolled out,cate sunt sus
    //cand visible items+scrolled out items=total items, we load more data

    public static ChatGroupsListFragment newInstance(int cityid,int userid) {
        Bundle args = new Bundle();
        ChatGroupsListFragment fragment = new ChatGroupsListFragment();
        Log.d(TAG, "newInstance: userid:"+userid);
        args.putInt("userid",userid);
        args.putInt("cityid",cityid);
        fragment.setArguments(args);
        return fragment;
    }

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
        chatGroups=new ArrayList<>();
        cityid=getArguments().getInt("cityid");
        userid=getArguments().getInt("userid");
        Log.d(TAG, "onCreate: userid:"+userid);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_chat_groups_list,container,false);
        fab=v.findViewById(R.id.fab_creategroup);
        recyclerView=(RecyclerView)v.findViewById(R.id.recycler_view_chatGroups);
        progress=(ProgressBar)v.findViewById(R.id.progress);
        manager=new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(),recyclerView,this));
        setupAdapter();
        //todo for infinite scrolling
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState== AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    isScrolling=true;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                currentItems=manager.getChildCount();//cate in view
                totalItems=manager.getItemCount();
                scrollOutItems=manager.findFirstVisibleItemPosition();

                if(isScrolling&& currentItems+scrollOutItems==totalItems){
                    //data fetch
                    Log.d(TAG, "onScrolled: FETHINCG MORE DATA");

                    isScrolling=false;
                    progress.setVisibility(View.VISIBLE);
                    new FetchMoreGroups().execute(page,cityid);//todo in fecthData progresbar.visibiliti(true),si cand se termina gone
                }
            }
        });
        
        swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "onRefresh: REFRESHIND");
                page=1;
                chatGroups.clear();
                adapter.notifyDataSetChanged();
                //recyclerView.scrollToPosition(0);
                new FetchMoreGroups().execute(page,cityid);
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo CREATE GROUP DIALOG
                Log.d(TAG, "onClick: userid:"+userid);
                if(userid==-1){
                    Toast.makeText(getContext(),"You are not allowed to create a chat group!",Toast.LENGTH_LONG).show();
                    return;
                }
                GroupCreateDialog groupCreateDialog=new GroupCreateDialog();
                groupCreateDialog.setTargetFragment(ChatGroupsListFragment.this,200);
                groupCreateDialog.show(getFragmentManager(),"group_create_dialog");
                //Custom dialog, implement listener
            }
        });
        
        new FetchMoreGroups().execute(page,cityid);
        return v;
    }

    private void setupAdapter(){
        if(isAdded()){
            adapter=new ChatGroupsAdapter(chatGroups,getContext());
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
        //todo Long click on group
        int id=chatGroups.get(position).getId();
        Toast.makeText(getContext(),"Long tap at position "+position,Toast.LENGTH_SHORT).show();
        listener.onChatGroupSelected(id,chatGroups.get(position).getName());
        //todo start chatting fragment
    }

    //todo Upload group
    @Override
    public void onFinishGroupCreate(String name, String language) {
        //Upload ting , Prespiration ting
        String token= PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString("token","n/a");
        Log.d(TAG, "onFinishGroupCreate: Name:"+name+" Language:"+language+" Token:"+token);
        if(token.equals("n/a"))
        {
            //no tokenm, no login :O
            Toast.makeText(getContext(),"You have no login token!",Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            //Upload DIS B //start asynctasker :O
            Log.d(TAG, "onFinishGroupCreate: UPLOADING GROUPS");
            new UploadGroup().execute(name,language,String.valueOf(cityid),token);
        }

    }

    public class ChatGroupsAdapter extends RecyclerView.Adapter<ChatGroupsAdapter.ViewHolder>{
        private ArrayList<ChatGroup> mItems;
        private Context ctx;

        public ChatGroupsAdapter(ArrayList<ChatGroup> mItems, Context ctx) {
            this.mItems = mItems;
            this.ctx = ctx;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_group,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.name.setText(mItems.get(position).getName()+mItems.get(position).getLanguage());

            if(mItems.get(position).getLanguage().equals("RO"))
            {
                Glide.with(ctx).load(R.drawable.ro).into(holder.image);
            }
            else if(mItems.get(position).getLanguage().equals("ENG")){
                Glide.with(ctx).load(R.drawable.eng).into(holder.image);
            }
            else{
                Glide.with(ctx).load(R.drawable.ic_launcher_background).into(holder.image);
            }
        }
        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView image;
            private TextView name;

            public ViewHolder(View itemView){
                super(itemView);
                image=(ImageView) itemView.findViewById(R.id.imgGroupLanguage);
                name=(TextView)itemView.findViewById(R.id.txtGroupName);
            }
        }
    }

    public int getImage(String imageName) {
        int drawableResourceId = this.getResources().getIdentifier(imageName,"drawable",getActivity().getPackageName());

        return drawableResourceId;
    }
    public class FetchMoreGroups extends AsyncTask<Integer,Void,ArrayList<ChatGroup>>
    {
        @Override//pagina city id
        protected ArrayList<ChatGroup> doInBackground(Integer... ints) {
            return ApiHelper.getChatGroups(Integer.valueOf(ints[0]),Integer.valueOf(ints[1]));
        }

        @Override
        protected void onPostExecute(ArrayList<ChatGroup> c) {
            swipeContainer.setRefreshing(false);
            progress.setVisibility(View.INVISIBLE);
            if(c.isEmpty()){
                //nu a downloadat nimic
                //page ramane lafel
                Log.d(TAG, "onPostExecute: NO MORE GROUOPS PAGE:"+page);
               // page--;//fetch last page again
                return;
            }
            else{
                Log.d(TAG, "onPostExecute: GOT GROUPS:"+c.size()+" PAGE:"+page);
                page++;//next page
                int cursize=adapter.getItemCount();
                chatGroups.addAll(c);
                adapter.notifyItemRangeInserted(cursize,c.size());
            }

        }
        
        
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: DESTROYED ! BAM");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: ");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        //getActivity().getSupportFragmentManager().popBackStack();
        //Disconnect from socket,
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        //Connect to socket,
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: ");

        //todo AICI INCHID SOCKET CONNECTION DISCONNNEC
    }
    
    //upload group
    public class UploadGroup extends AsyncTask<String,Void,String> {
        @Override//pagina city id
        protected String doInBackground(String... s) {
            return ApiHelper.createGroup(s[0], s[1], Integer.valueOf(s[2]), s[3]);
        }

        @Override
        protected void onPostExecute(String msg) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
        }
    }
}
