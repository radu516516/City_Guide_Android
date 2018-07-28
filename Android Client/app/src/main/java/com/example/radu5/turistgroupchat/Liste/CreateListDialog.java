package com.example.radu5.turistgroupchat.Liste;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.radu5.turistgroupchat.Model.ListItem;
import com.example.radu5.turistgroupchat.R;

/**
 * Created by radu5 on 5/11/2018.
 */

public class CreateListDialog extends DialogFragment {

    public interface CreateListDialogLister{
        void  onDialogPositiveClick(String name,String description);
    }
    CreateListDialogLister listener;

    public CreateListDialog(){

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater=getActivity().getLayoutInflater();
        View view=inflater.inflate(R.layout.dialog_add_list,null);
        EditText name=view.findViewById(R.id.nameTxt);
        EditText desc=view.findViewById(R.id.descTxt);
        builder.setView(view);
        builder.setMessage("Create List")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //start upload , give data back\(

                        if(name.getText().toString().matches(""))
                        {
                            //if its empty
                            name.setError("This field can not be blank");
                            return;
                        }
                        listener=(CreateListDialogLister)getTargetFragment();
                        listener.onDialogPositiveClick(name.getText().toString(),desc.getText().toString());
                        dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        CreateListDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}