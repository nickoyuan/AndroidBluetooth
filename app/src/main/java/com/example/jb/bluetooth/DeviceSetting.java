package com.example.jb.bluetooth;


import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceSetting extends Fragment {

    private BluetoothAdapter mBluetoothAdapter;

    public DeviceSetting() {
        // Required empty public constructor
    }

    List<String> info_list = new ArrayList<>();

    // TextView
    TextView s_num_txt;
    TextView h_num_text;
    TextView soft_num_text;

    Button btn_change;
    Button firmwarebtn;

    ProgressBar progress;
    TextView txt_status;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_device_setting, container, false);

        s_num_txt = (TextView) view.findViewById(R.id.s_num_txt);
        h_num_text = (TextView) view.findViewById(R.id.h_num_text);
        soft_num_text = (TextView) view.findViewById(R.id.soft_num_text);

        progress = (ProgressBar) view.findViewById(R.id.progress_status);
        progress.setVisibility(View.INVISIBLE);

        txt_status= (TextView) view.findViewById(R.id.txt_status);
        txt_status.setVisibility(View.INVISIBLE);


        btn_change  = (Button) view.findViewById(R.id.changingble);
        firmwarebtn = (Button) view.findViewById(R.id.firmwarebtn);

        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setMessage("Warning !! Selecting new M100 will remove current M100");
                //alertDialogBuilder.setMessage("Are you Sure you want to Change ??");
                alertDialogBuilder.setPositiveButton("Continue",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                                BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
                                mBluetoothAdapter = bluetoothManager.getAdapter();
                                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();


                                // Try this one
                                if (pairedDevices.size() > 0) {
                                    for (BluetoothDevice device : pairedDevices) {


                                        if(device.getName().contains("electromed")) {
                                            try {
                                                Method m = device.getClass().getMethod("removeBond", (Class[]) null);
                                                m.invoke(device, (Object[]) null);
                                            } catch (Exception e) {

                                                getActivity().stopService(new Intent(getActivity(), BleServiceBackground.class));
                                                Intent i = new Intent(getActivity(), MainActivity.class);
                                                startActivity(i);
                                                getActivity().finish();
                                            }
                                        }
                                    }
                                }

                                getActivity().stopService(new Intent(getActivity(), BleServiceBackground.class));
                                Intent i = new Intent(getActivity(), MainActivity.class);
                                startActivity(i);
                                getActivity().finish();
                            }
                        });

                    alertDialogBuilder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });


        firmwarebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                   if(h_num_text.getText().toString().toLowerCase().contains("wait"))
                   {
                       new AlertDialog.Builder(getActivity())
                               .setTitle("Please wait ")
                               .setMessage("Waiting for M100 information")
                               .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                   public void onClick(DialogInterface dialog, int which) {

                                   }
                               })
                               .setIcon(android.R.drawable.ic_dialog_alert)
                               .show();
                   }
                   // if current device version is 1.0, then it's up to date
                   else if(h_num_text.getText().toString().equals("1.0"))
                   {
                       new AlertDialog.Builder(getActivity())
                               .setTitle("M100 up to date")
                               .setMessage("Newest M100 is already installed")
                               .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                   public void onClick(DialogInterface dialog, int which) {

                                   }
                               })
                               .setIcon(android.R.drawable.ic_dialog_alert)
                               .show();
                   }
                   // Else Convert to 1.0
                  else{
                       AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                       alertDialogBuilder.setMessage("Warning!! Only change Firmware if instructed");
                       alertDialogBuilder.setPositiveButton("Continue",
                               new DialogInterface.OnClickListener() {
                                   @Override
                                   public void onClick(DialogInterface arg0, int arg1) {

                                       // Stop Service
                                       getActivity().stopService(new Intent(getActivity(), BleServiceBackground.class));

                                       // Run your Firmware Class
                                       Intent i = new Intent(getActivity(), FirmwareUpdate.class);
                                       startActivity(i);
                                       getActivity().finish();

                                   }
                               });

                       alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {

                           }
                       });
                       AlertDialog alertDialog = alertDialogBuilder.create();
                       alertDialog.show();
                   }
            }
        });



        // Might put this in a thread later on
        IntentFilter intentFilter = new IntentFilter("ax.androidexample.mybroadcast");
        getActivity().registerReceiver(mYourBroadcastReceiver, intentFilter);




        return view;
    }



    private BroadcastReceiver mYourBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                //  Intent called "status" will pass dc
                if(extras.containsKey("status")){

                    if(extras.getString("status").contains("dc"))
                    {
                        progress.setVisibility(View.INVISIBLE);
                        txt_status.setVisibility(View.INVISIBLE);
                    }
                    else if(!extras.getString("status").contains("Finish")) {
                        progress.setVisibility(View.VISIBLE);
                        txt_status.setVisibility(View.VISIBLE);
                    }
                }
                //  Intent called "Message" will pass graph_array
                else{

                    progress.setVisibility(View.INVISIBLE);
                    txt_status.setVisibility(View.INVISIBLE);

                    info_list.addAll((Collection<? extends String>) extras.getSerializable("device_info"));
                    // Serial, Software and Hardware
                    s_num_txt.setText(info_list.get(1));
                    soft_num_text.setText(info_list.get(2));
                    h_num_text.setText(info_list.get(3));

                }
            }
        }
    };



}
