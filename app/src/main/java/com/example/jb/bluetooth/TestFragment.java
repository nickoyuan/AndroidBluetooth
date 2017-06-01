package com.example.jb.bluetooth;


import android.app.LocalActivityManager;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import az.plainpie.PieView;
import az.plainpie.animation.PieAngleAnimation;


/**
 * A simple {@link Fragment} subclass.
 */
public class TestFragment extends Fragment {

    TabHost tabHost;

    LocalActivityManager mLocalActivityManager;

    BluetoothDevice selected_ble;



    TextView status_txt;
    TextView ble_date;
    ImageView status_circle;
    TextView blestatus;

    PieView batt_gauge;
    PieView ble_dial_status;


    List<Double> array_float = new ArrayList<>();

    List<String> ble_string = new ArrayList<>();

    Intent first_tab;
    View view;
    TabHost.TabSpec tab1;

    int progress = 0;

    boolean ble_status = false;


    public TestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_test_fragment, container, false);
       // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
       // setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle("M100 Status Tracker");





        // Bluetooth Device passed
        //Bundle bundle = this.getArguments();
        //selected_ble = bundle.getParcelable("ble_device");

        //progressDialog = ProgressDialog.show(getActivity(), "", "Loading..");


        // start listening for refresh local file list in
       // LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mYourBroadcastReceiver, new IntentFilter("ax.androidexample.mybroadcast"));

        // Might put this in a thread later on
        IntentFilter intentFilter = new IntentFilter("ax.androidexample.mybroadcast");
        getActivity().registerReceiver(mYourBroadcastReceiver, intentFilter);




        status_txt = (TextView) view.findViewById(R.id.ble_status_txts);
        status_txt.setText("M100 DISCONNECTED");

        ble_date = (TextView) view.findViewById(R.id.ble_date);
        ble_date.setText("-");

        blestatus = (TextView) view.findViewById(R.id.blestatus);
        blestatus.setText("Standby...");

        status_circle = (ImageView) view.findViewById(R.id.image_circle);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            status_circle.setImageDrawable(getResources().getDrawable(R.drawable.sis_circle, getActivity().getTheme()));
        } else {
            status_circle.setImageDrawable(getResources().getDrawable(R.drawable.sis_circle));
        }

        // Creating a Handler for a Runnable Thread
        //hand.postDelayed(run, 1000);




        PieView pieView = (PieView) view.findViewById(R.id.main_gauge);
        PieAngleAnimation main_dial= new PieAngleAnimation(pieView);
        main_dial.setDuration(5000);
        pieView.startAnimation(main_dial);
        pieView.setInnerText("99%");
        pieView.setPieInnerPadding(50);
        pieView.setInnerBackgroundColor(Color.rgb(252,252,252));
        pieView.setTextColor(Color.rgb(45,50,57));

        batt_gauge= (PieView) view.findViewById(R.id.batt_gauge);
        batt_gauge.setPercentageBackgroundColor(Color.RED);
        PieAngleAnimation batt_dial = new PieAngleAnimation(batt_gauge);
        batt_dial.setDuration(2000);
        batt_gauge.startAnimation(batt_dial);
        batt_gauge.setPieInnerPadding(20);
        batt_gauge.setInnerBackgroundColor(Color.rgb(252,252,252));
        batt_gauge.setTextColor(Color.rgb(45,50,57));

        ble_dial_status = (PieView) view.findViewById(R.id.blestatusdial);
        PieAngleAnimation ble_status_animation = new PieAngleAnimation(ble_dial_status);
        ble_status_animation.setDuration(5000);
        ble_dial_status.startAnimation(ble_status_animation);
        ble_dial_status.setInnerText("0%");
        ble_dial_status.setTextColor(Color.rgb(45,50,57));
        ble_dial_status.setInnerBackgroundColor(Color.rgb(252,252,252));
        ble_dial_status.setPercentageBackgroundColor(Color.rgb(255,127,39));


        //mChart = new LineChart(this);

        //mainlayout.addView(mChart);

        //array_float.add(0);


        tabHost = (TabHost) view.findViewById(android.R.id.tabhost);

        mLocalActivityManager = new LocalActivityManager(getActivity(), false);
        mLocalActivityManager.dispatchCreate(savedInstanceState);
        tabHost.setup(mLocalActivityManager);


        array_float.add(0.0);


        setupTabs();

        //RootActivity parentActivity;
      // parentActivity = (YourRootActivity) this.getParent();
       // parentActivity.setTitle(title);

        //ble_thread_background app = new ble_thread_background(handler,getActivity());
        //ble_thread_background.SecondThread threading = app.new SecondThread();

        // make sure thread will be killed if app is unexpectedly killed
       // threading.setDaemon(true);
        //threading.start();

        return view;
    }



    /*
                    status_txt.setText("M100 DISCONNECTED");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        status_circle.setImageDrawable(getResources().getDrawable(R.drawable.sis_circle, getActivity().getTheme()));
                    } else {
                        status_circle.setImageDrawable(getResources().getDrawable(R.drawable.sis_circle));

                    }
     */

    private BroadcastReceiver mYourBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                //  Intent called "status" will pass dc
                if(extras.containsKey("status")){

                        if(!ble_status)
                        {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                status_circle.setImageDrawable(getResources().getDrawable(R.drawable.sis_circle_connected, getActivity().getTheme()));
                            } else {
                                status_circle.setImageDrawable(getResources().getDrawable(R.drawable.sis_circle_connected));
                            }
                            status_txt.setText("M100 CONNECTED");
                            ble_status = true;
                        }

                        if(extras.getString("status").contains("Finish"))
                        {
                            progress = 100;
                            ble_dial_status.setPercentage(progress);
                            ble_dial_status.setPercentageBackgroundColor(Color.rgb(20,185,214));
                            blestatus.setText(extras.getString("status"));
                            progress = 0;
                        }
                        else if (extras.getString("status").contains("dc"))
                        {
                            status_txt.setText("M100 DISCONNECTED");
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                status_circle.setImageDrawable(getResources().getDrawable(R.drawable.sis_circle, getActivity().getTheme()));
                            } else {
                                status_circle.setImageDrawable(getResources().getDrawable(R.drawable.sis_circle));
                            }
                            // Changing ble_status to true;
                            ble_status = false;

                            progress = 0;
                            ble_dial_status.setPercentage(progress);
                            blestatus.setText("Standby...");
                        }
                        else
                        {
                            progress = progress + 20;
                            ble_dial_status.setPercentage(progress);
                            if(progress > 30) {
                                ble_dial_status.setPercentageBackgroundColor(Color.rgb(255,127,39));
                            }else{
                                ble_dial_status.setPercentageBackgroundColor(Color.RED);
                            }
                            blestatus.setText(extras.getString("status"));
                        }

                     }
                //  Intent called "Message" will pass graph_array
                else{
                    ble_date.setText(Date());
                    array_float.addAll((Collection<? extends Double>) extras.getSerializable("message"));

                    // Get BLE status
                    ble_string.addAll((Collection<? extends String>) extras.getSerializable("device_info"));

                    batt_gauge.setPercentage(Float.parseFloat(ble_string.get(0)));

                    status_txt.setText("M100 CONNECTED");

                    int currentTabId = tabHost.getCurrentTab();
                    tabHost.clearAllTabs();
                    setupTabs();
                    tabHost.setCurrentTab(currentTabId);


                }

            }
        }
    };





    @Override
    public void onResume() {
        super.onResume();

        // Might put this in a thread later on
        IntentFilter intentFilter = new IntentFilter("ax.androidexample.mybroadcast");
        getActivity().registerReceiver(mYourBroadcastReceiver, intentFilter);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getActivity().unregisterReceiver(mYourBroadcastReceiver);

    }

    public String Date() {
        DateFormat df = new SimpleDateFormat("h:mm a");
        String date = df.format(Calendar.getInstance().getTime());
        return date;
    }

    public void setupTabs()
    {



        tab1 = tabHost.newTabSpec("tab1");
        first_tab = new Intent(getActivity(),TabOneGraph.class);
        first_tab.putExtra("m", (Serializable) array_float);
        first_tab.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        tab1.setContent(first_tab);
        tab1.setIndicator("Tracking History");
        tabHost.addTab(tab1);

        Intent tab2_host;
        TabHost.TabSpec tab2 = tabHost.newTabSpec("tab2");
        tab2_host = new Intent(getActivity(),TabTwoGraph.class);
        tab2_host.putExtra("m", (Serializable) array_float);
        tab2_host.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        tab2.setContent(tab2_host);

        tab2.setIndicator("Statistics");
        tabHost.addTab(tab2);
    }

   /*
    Runnable run = new Runnable() {
        @Override public void run()
        {
            update_pie_chart();
        }
    };

    public void update_pie_chart() {





    }
  */


}
