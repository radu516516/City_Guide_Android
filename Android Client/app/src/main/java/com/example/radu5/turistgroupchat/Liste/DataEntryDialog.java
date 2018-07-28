package com.example.radu5.turistgroupchat.Liste;

import android.Manifest;
import android.app.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;


import com.example.radu5.turistgroupchat.Model.ListItem;
import com.example.radu5.turistgroupchat.R;
import com.example.radu5.turistgroupchat.Utils.PathUtil;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.net.URISyntaxException;

/**
 * Created by radu5 on 4/18/2018.
 */

public class DataEntryDialog extends DialogFragment {
    private int PICK_IMAGE_REQUEST = 100;//get image
    private int STORAGE_PERMISSION = 200;
    private int PLACE_PICKER_REQUEST = 300;//get place\
    private static final String TAG = "DataEntryDialog";

    private ListItem item=null;
    private EditText txtName;
    private EditText txtDescription;
    private EditText txtLocation;
    private EditText txtUri;

    public interface DataInputDialogLister{
        void onFinishImputDialog(ListItem item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        item=new ListItem();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.data_entry_dialog, container, false);

        Button btnOk = rootView.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: ");
                boolean okay=true;
                if(txtDescription.getText().toString().matches(""))
                {
                    okay=false;
                    //if its empty
                    txtDescription.setError("This field can not be blank");

                }
                if(txtName.getText().toString().matches(""))
                {
                    okay=false;
                    txtName.setError("This field can not be blank");

                }
                if(txtUri.getText().toString().matches(""))
                {
                    okay=false;
                    txtUri.setError("This field can not be blank");

                }
                if(txtLocation.getText().toString().matches(""))
                {
                    okay=false;
                    txtLocation.setError("This field can not be blank");

                }
                if(okay==false){
                    return;
                }

                //todo DATA OK
                item.setDescriere(txtDescription.getText().toString());
                item.setNume(txtName.getText().toString());
                //todo Send data back to containing fragmnet
                DataInputDialogLister listener = (DataInputDialogLister) getTargetFragment();
                listener.onFinishImputDialog(item);
                dismiss();

            }
        });
        Button btnCancel = rootView.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });


        //todo Select image
        ImageButton imgBtn = (ImageButton) rootView.findViewById(R.id.imageButton);
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            STORAGE_PERMISSION);
                } else {
                    //todo select image (when done onActivityResult)
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    photoPickerIntent.setType("image/*");
                    if (photoPickerIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivityForResult(photoPickerIntent, PICK_IMAGE_REQUEST);
                    }
                }
            }
        });

        //todo Select location
        ImageButton imgBtn2 = (ImageButton) rootView.findViewById(R.id.imageButton2);
        imgBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        txtUri=(EditText)view.findViewById(R.id.txtURL);
        txtLocation=(EditText)view.findViewById(R.id.txtLocatie);
        txtName=(EditText)view.findViewById(R.id.txtNume);
        txtDescription=(EditText)view.findViewById(R.id.txtDescriere);
    }

    //todo Starting activity to get image send result back
    //todo SELECT PHOTO AND LOCATION
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri returnUri = data.getData();//location of image
            txtUri.setText(returnUri.toString());//todo URI FILE PATH
            try {
                String filePath= PathUtil.getPath(getContext(),returnUri);
                txtUri.setText(filePath);//todo REAL FILE PATH
                Log.d(TAG, "onActivityResult: got file");
                item.setUrl(filePath);//path on disk
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        if(requestCode==PLACE_PICKER_REQUEST && resultCode==Activity.RESULT_OK){
            Place place = PlacePicker.getPlace(getActivity(),data);
            txtLocation.setText(place.getName());
            item.setLat(place.getLatLng().latitude);
            item.setLng(place.getLatLng().longitude);
            Log.d(TAG, "onActivityResult: got place");
        }
    }

}

