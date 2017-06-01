package com.example.jb.bluetooth;


import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class EmsFragment extends Fragment {


    public EmsFragment() {
        // Required empty public constructor
    }

    CheckBox check_auto;
    CheckBox check_manual;

    Boolean ischecked = true;

    TextView manualmodetxt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_ems_fragment, container, false);



        check_auto = (CheckBox) view.findViewById(R.id.checkBox_auto);
        check_manual = (CheckBox) view.findViewById(R.id.checkBox_manual);

        manualmodetxt = (TextView) view.findViewById(R.id.txtdetails);

        auto_check();

        check_auto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ischecked == false)
                {
                    auto_check();

                }
                else {
                    check_auto.setChecked(true);
                }

            }
        });


        check_manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ischecked == true)
                {
                    manual_check();
                   showDialog(getActivity());

                }
                else {
                    check_manual.setChecked(true);
                }


            }
        });


        return view;
    }

    public void showDialog(Activity activity){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog);

        final SeekBar sbBetVal = (SeekBar)dialog.findViewById(R.id.seekBar2);
        sbBetVal.setMax(3);
        sbBetVal.setProgress(3);


        Button cancel_d = (Button) dialog.findViewById(R.id.btn_cancel);
        cancel_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                auto_check();

            }
        });

        Button select_d = (Button) dialog.findViewById(R.id.btn_ok);
        select_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                manual_check();

                int value = sbBetVal.getProgress() + 1;
                String value_str = Integer.toString(value);
                manualmodetxt.setText("Sensitivity Selected: " + value_str);
            }
        });


        dialog.show();
    }



    public void auto_check()
    {
        check_auto.setChecked(true);
        check_manual.setChecked(false);
        ischecked = true;

        manualmodetxt.setVisibility(View.GONE);
    }

    public void manual_check()
    {
        check_auto.setChecked(false);
        check_manual.setChecked(true);
        ischecked = false;


        manualmodetxt.setVisibility(View.VISIBLE);
    }


}
