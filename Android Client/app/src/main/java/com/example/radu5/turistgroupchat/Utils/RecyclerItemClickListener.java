package com.example.radu5.turistgroupchat.Utils;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by radu5 on 3/24/2018.
 */


//todo USED TO DETECT WHAT LIST HE LONGED CLICKED ON
public class RecyclerItemClickListener extends RecyclerView.SimpleOnItemTouchListener {
    private static final String TAG = "RecyclerItemClickListen";


    public interface OnRecyclerClickListener{
        //void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    private final OnRecyclerClickListener mListener;

    private final GestureDetectorCompat mGestureDetector;

    public RecyclerItemClickListener(Context context, final RecyclerView recyclerView, final OnRecyclerClickListener mListener) {
        this.mListener = mListener;
        mGestureDetector=new GestureDetectorCompat(context,new GestureDetector.SimpleOnGestureListener(){
          /*  @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Log.d(TAG, "onSingleTapUp: starts");
                View childView=recyclerView.findChildViewUnder(e.getX(),e.getY());
                if(childView!=null && mListener!=null){
                    Log.d(TAG, "onSingleTapUp: calling listener.onitemclick");
                    mListener.onItemClick(childView,recyclerView.getChildAdapterPosition(childView));
                }
                return true;
            }*/

            @Override
            public void onLongPress(MotionEvent e) {
                Log.d(TAG, "onLongPress: starts");
                View childView=recyclerView.findChildViewUnder(e.getX(),e.getY());
                if(childView!=null && mListener!=null){
                    Log.d(TAG, "onSingleTapUp: calling listener.onitemlongclick");
                    mListener.onItemLongClick(childView,recyclerView.getChildAdapterPosition(childView));
                }
            }
            //todo can override some of the other methods
        });

    }

    //todo Intercept touch events
    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        Log.d(TAG, "onInterceptTouchEvent: starts");
        if(mGestureDetector!=null){
            boolean result=mGestureDetector.onTouchEvent(e);
            Log.d(TAG, "onInterceptTouchEvent() returned :"+result);
            return result;

        }else{
            Log.d(TAG, "onInterceptTouchEvent() returned false ");
            return false;
        }


    }
}
