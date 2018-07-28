package com.example.radu5.turistgroupchat.AsyncTask;


/**
 * Created by radu5 on 4/23/2018.
 */
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

//todo GETS DATA AND RETURNS TO THE ACTIVITY

//todo WATCH ASYNC TASK LOADER

public class MyAsyncTaskLoader extends android.support.v4.content.AsyncTaskLoader<String> {
    public MyAsyncTaskLoader(@NonNull Context context) {
        super(context);
    }
    //todo ISNT AS STRONGLY TIED TO THE ACTIVITY LIFECYCLE LIKE ASYNCTASTK

    //todo WONT BE INTERRUPTED BY CONFIGURATION CHANGE ( DECY ASYNCTASK STILL DOWNLOADING ON CONIFG CHANGE)


    @Nullable
    @Override
    public String loadInBackground() {


        //return data from loader
        //in mai activity getSupportLoaderManager().initLoader(0,null,this).forceLoad() //implements
        return null;
    }
}
