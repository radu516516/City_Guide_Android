package com.example.radu5.turistgroupchat.UserApp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.radu5.turistgroupchat.Liste.DataEntryDialog;
import com.example.radu5.turistgroupchat.R;

/**
 * Created by radu5 on 5/28/2018.
 */

public class GroupCreateDialog  extends DialogFragment {

    private EditText txtName;
    private Spinner language;

    public interface GroupCreateDialogListener {
        void onFinishGroupCreate(String name, String language);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_group_create, container, false);

        Button btnOk = rootView.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean okay=true;
                if(txtName.getText().toString().matches(""))
                {
                    //if its empty
                    okay=false;
                    txtName.setError("This field can not be blank");

                }
                if(language.getSelectedItem()==null){
                    okay=false;
                    TextView errorText = (TextView)language.getSelectedView();
                    errorText.setError("");
                    errorText.setTextColor(Color.RED);//just to highlight that this is an error
                    errorText.setText("Must select a language!");//changes the selected item text to this
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new String[]{""});
                    language.setAdapter(adapter);
                }
                if(okay==false){
                    return;
                }
                GroupCreateDialogListener listener=(GroupCreateDialogListener)getTargetFragment();
                listener.onFinishGroupCreate(txtName.getText().toString(),language.getSelectedItem().toString());
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
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        txtName = (EditText) view.findViewById(R.id.txtNume);
        language = (Spinner) view.findViewById(R.id.spinnerLang);
    }
}
