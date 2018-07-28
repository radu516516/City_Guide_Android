package com.example.radu5.turistgroupchat.PostDataTest;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.radu5.turistgroupchat.R;
import com.example.radu5.turistgroupchat.Utils.ExifUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import okhttp3.MultipartBody;

import static android.app.Activity.RESULT_OK;

/**
 * Created by radu5 on 3/29/2018.
 */
//todo Aplication Arhitecture with fragments no more than 2 or 3 Fragments on screen at one time

//todo !!! RESON WHY USE FRAGMENTS
    //todo ALLWAYS USE FRAGMENTS

public class UploadImageFragment extends Fragment
{
    private static final String TAG = "UploadImageFragment";
    private int PICK_IMAGE_REQUEST=1;
    private int STORAGE_PERMISSION=2;
    private static final int maxWidth=640;
    private static final int maxHeight=480;
    //todo Comunicare Intre Fragment , via host activity
    public interface OnImageUploadedListener{
        public void onImageUploaded(String image);
    }
    private OnImageUploadedListener listener;
    //date si views
    private ImageView image;
    private Button button;

    //todo Attaching arguments to a fragment
    public static UploadImageFragment newInstance(UUID listID) {
        //todo Ataching args to fragment must be done after its created and before added to an activity
        //todo When Activity needs instance of this fragment, it calls the newInstance rather than constructur
        Bundle args = new Bundle();

        args.putSerializable("list_id",listID);
        args.putInt("testInt",5);
        //todo ACTIVITY should call new instance when it needs to create a fragment like this instead of constructor
        //todo then i get get the arguments in on create

        UploadImageFragment fragment = new UploadImageFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //todo get listener from host activity to communicate
        /*if (context instanceof OnImageUploadedListener) {
            listener = (OnImageUploadedListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implemenet MyListFragment.OnItemSelectedListener");
        }*/
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //todo INTENTS = async mesasges app components to request funct from other component

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_image_upload,container,false);

        image=(ImageView)v.findViewById(R.id.imageView);
        button=(Button)v.findViewById(R.id.buttonUpload);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo pick image to upload
                if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            STORAGE_PERMISSION);
                }
                else{
                    //todo select image
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    photoPickerIntent.setType("image/*");
                    if (photoPickerIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivityForResult(photoPickerIntent, PICK_IMAGE_REQUEST);
                    }
                }
            }
        });
       // button.setEnabled(false);

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //todo After activities on create ran

        //todo PASSING PARAMETERS TO FRAGMENTS
        Bundle bundle = getArguments();
        if (bundle != null) {
            //setText(bundle.getString("link"));
        }
    }

    @Override
    public void onResume() {//todo Safest place to update a fragments view
        super.onResume();
        //update ui
    }

    //todo On image selected
    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //handle result
        if(requestCode==PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null){

            Thread t=new Thread(new Runnable() {
                @Override
                public void run() {


                    Uri returnUri = data.getData();
                    try {


                        //todo from net
              /*  BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(returnUri), null,options);

                //...Now You have the 'bitmap' to rotate....
                //...Rotate the bitmap to its original Orientation...
                Bitmap bitmapNew = ImageOrientation.modifyOrientation(getContext().getApplicationContext(),bitmap,returnUri);
                bitmapNew=scaleBitmap(bitmapNew);
                //...After Rotation set the image to Image View...
                image.setImageBitmap(bitmapNew);*/

                        //todo REZIZE IMAGE,


                        Log.d(TAG, "onActivityResult: " + returnUri);

                         Bitmap  bitmapImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), returnUri);
                        // bitmapImage = Bitmap.createScaledBitmap(bitmapImage,(int)(bitmapImage.getWidth()*0.5), (int)(bitmapImage.getHeight()*0.5), true);
                        bitmapImage = scaleBitmap(bitmapImage);
                        Log.d(TAG, "onActivityResult: ORIENTATION " + getOrientation(getContext(), returnUri));

                        File myFile = new File(returnUri.getPath());
                        getCameraPhotoOrientation(myFile.getAbsolutePath());
                        final Bitmap bitmap=bitmapImage;
                        //todo PROBLEMA IMAGINEA VINE ROTITA
                        //Bitmap orientedBitmap= ExifUtil.rotateBitmap(myFile.getAbsolutePath(),bitmapImage);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                image.setImageBitmap(bitmap);
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                });
                t.start();




        }
    }
    //todo scale bitmap
    private Bitmap scaleBitmap(Bitmap bm) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        Log.v(TAG, "Width and height are " + width + "--" + height);

        if (width > height) {
            // landscape
            float ratio = (float) width / maxWidth;
            width = maxWidth;
            height = (int)(height / ratio);
        } else if (height > width) {
            // portrait
            float ratio = (float) height / 640;//todo era maxWidth before i changed sa fie mai square
            height = maxHeight;
            width = (int)(width / ratio);
        } else {
            // square
            height = maxHeight;
            width = maxWidth;
        }

        Log.v(TAG, "after scaling Width and height are " + width + "--" + height);

        bm = Bitmap.createScaledBitmap(bm, width, height, true);
        return bm;
    }
    //todo Some images rotated in image view

    public static int getCameraPhotoOrientation(String imageFilePath) {
        int rotate = 0;
        try {

            ExifInterface exif;

            exif = new ExifInterface(imageFilePath);
            String exifOrientation = exif
                    .getAttribute(ExifInterface.TAG_ORIENTATION);
            Log.d(TAG,"Orientare :"+ exifOrientation);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rotate;
    }
    public static int getOrientation(Context context, Uri photoUri) {
    /* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }




}
