package com.example.jb.bluetooth;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import az.plainpie.PieView;
import az.plainpie.animation.PieAngleAnimation;

public class AlertPage extends AppCompatActivity {


    ListView lv;
    //Context context;

    ArrayList prgmName;


    public static int [] prgmImages={R.drawable.list_number,R.drawable.list_number_two,R.drawable.list_number_three,R.drawable.list_number_four,R.drawable.list_number_five,
            R.drawable.list_number_six, R.drawable.list_number_seven,R.drawable.list_number_eight,R.drawable.list_number_nine};

    public static String [] title={"CHECK DRESSING TAPE","CHECK ELECTRODES ARE WET","CHECK IF ELECTRODES ARE DISCOLORED",
            "SHAVE SKIN IF NECESSARY"," REPLACE SIS ELECTRODES"," CHECK HARNESS CONNECTIONS","CHECK FOR SKIN IRRITATION","CHECK MONITORING SETTING"," PERFORM HARNESS CHECK"};

    public static String [] description= {"Check that the dressing tape is holding the entire surface " +
            "of the electrodes in contact with skin or wound." +
            "Replace or add dressing tape if necessary.",
            // Second description
            "Re-moisten electrodes with distilled or tap water if necessary.",
            // Third
            "Replace electrodes if they are discolored (12-72 hours).",

            // fourth
            "Check if hair growth is lifting electrodes from skin." + "Re-shave skin if necessary.",

            // Five
            "If all other checks have been performed and OK," +
                    "there is a possible electrode fault. Replace both SIS electrodes",

            // Six
            "Check harness (connecting wire) has not unplugged." +
                    "Check harness is connected to the two electrodes and to the SIS machine.",

            // Seven
            "INFORMATION ONLY. Stimulation voltage has increased: " +
                    "Check under electrodes and dressing tape for skin irritation.",

            // eight
            "If monitoring set to MANUAL then reduce MONITORING setting." +
                    "Or set MONITORING to AUTO. [Click HERE]",

            // nine
            "Follow instructions to perform a harness check." +
                    "Notify manufacturer or supplier if harness is broken." +
                    "Replace harness if necessary." +
                    "[Click HERE]"};


    List<Float> array_float = new ArrayList<>();

    List<String> numbers = new ArrayList<String>();

    the_receive myReceiver;

    ImageView status_circle;
    TextView status_txt;
    TextView ble_date;

    CheckBox check_auto;
    CheckBox check_manual;
    boolean dbchecked = false;
    LinearLayout manual_bar;
    SeekBar seekbar;

    int count=0;

    int number_counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_main_page);


        // CIRCLE OF THE CONNECTED IN LINEAR LAYOUT
        status_circle = (ImageView) findViewById(R.id.image_circle);

        ble_date = (TextView) findViewById(R.id.ble_date);
        ble_date.setText("unknown");

        status_txt = (TextView) findViewById(R.id.ble_status_txts);
        status_txt.setText("M100 DISCONNECTED");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            status_circle.setImageDrawable(getResources().getDrawable(R.drawable.sis_circle,getTheme()));
        } else {
            status_circle.setImageDrawable(getResources().getDrawable(R.drawable.sis_circle));
        }


        lv=(ListView) findViewById(R.id.alert_listview);
        lv.setAdapter(new CustomListAdapter(this, prgmImages,title,description));


        Intent intent = getIntent();
        if(intent.hasExtra("m")){
            array_float.addAll((Collection<? extends Float>) intent.getSerializableExtra("m"));
        }
        else {
            array_float.add((float) 0);
        }



        myReceiver = new the_receive();
        IntentFilter intentFilter = new IntentFilter("ax.androidexample.mybroadcast");
        registerReceiver(myReceiver, intentFilter);


        PieView pieView = (PieView) findViewById(R.id.improv_gauge);
        PieAngleAnimation animation = new PieAngleAnimation(pieView);
        animation.setDuration(3000);
        pieView.startAnimation(animation);
        pieView.setInnerText("60%");
        pieView.setPieInnerPadding(50);
        pieView.setInnerBackgroundColor(Color.rgb(252,252,252));
        pieView.setPercentageBackgroundColor(Color.rgb(255,127,39));
        pieView.setTextColor(Color.rgb(45,50,57));

      //  ProgressBar pro = (ProgressBar) findViewById(R.id.pro);
     //   pro.setProgress(0);
      //  pro.setMax(10);



        lv.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {



            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TextView txt = (TextView) view.findViewById(R.id.title);
                String message = txt.getText().toString();

                if (message.contains("HARNESS"))
                {
                    showharness(AlertPage.this);
                }

                else if(message.contains("MONITORING"))
                {
                   showmonitoring(AlertPage.this);
                }


            }


        });

    }


    public void showmonitoring(Activity activity)
    {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog_alert);


        seekbar = (SeekBar) dialog.findViewById(R.id.seekBar2);
        check_auto = (CheckBox) dialog.findViewById(R.id.checkBox_auto);
        check_manual = (CheckBox) dialog.findViewById(R.id.checkBox_manual);
        manual_bar = (LinearLayout) dialog.findViewById(R.id.manual_bar);

        auto_check();

        check_auto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(dbchecked == false)
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

                if(dbchecked == true)
                {
                    manual_check();
                }
                else {
                    check_manual.setChecked(true);
                }


            }
        });

        Button cancelbtn = (Button) dialog.findViewById(R.id.btn_cancel);
        cancelbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });

        Button savebtn = (Button) dialog.findViewById(R.id.btn_ok);
        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                Toast.makeText(AlertPage.this, "Saved Changes will take affect in next 10 Seconds",
                        Toast.LENGTH_LONG).show();
            }
        });

        dialog.show();

    }

    public void auto_check()
    {
        check_auto.setChecked(true);
        check_manual.setChecked(false);
        dbchecked = true;

        manual_bar.setVisibility(View.GONE);
        seekbar.setVisibility(View.GONE);
    }


    public void manual_check()
    {
        check_auto.setChecked(false);
        check_manual.setChecked(true);
        dbchecked = false;


        manual_bar.setVisibility(View.VISIBLE);
        seekbar.setVisibility(View.VISIBLE);
    }

    private void showharness(Activity activity)
    {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.harness_check_layout);

        Button close = (Button) dialog.findViewById(R.id.closebtn);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });

        Button cancelbtn = (Button) dialog.findViewById(R.id.cancelbtn);
        cancelbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });

        String [] inst_txt={"POWER OFF THE M100 DEVICE","FOLLOW 2.1 INSTRUCTION MANUAL OF M100",
                "POWER ON THE M100 DEVICE",
                " HOLDING THE TWO GOLD Banana PLUGS",
                 "READ MAIN WINDOW OF DISPLAY"};

       String [] description = {" ",
               "READ CONNECTION OF SIS ELECTRODE HARNESS TO M100\n" + "WARNING: Do not connect electrodes to the harness.",
               " ",
               " Hold the two gold 'banana plugs' at the ends of the black and red wires\n" +
               "of the electrode harness in contact with one another for a maximum of\n" +
               "55 seconds; make sure the contact between the 'banana plugs' is\n" +
               "continuous and do not touch the banana plugs with your fingers or any\n" +
               "other object. ",
               "If R = SC is shown in the Main Window of the display the harness is OK.\n" +
               "If R = OL is shown in the Main Window of the display the harness is broken." };

        ListView instructions = (ListView) dialog.findViewById(R.id.harness_list);
        instructions.setAdapter(new CustomListAdapter(this, prgmImages,inst_txt,description));
        dialog.show();
    }


    public class the_receive extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                //  Intent called "status" will pass dc
                if(extras.containsKey("status")){


                    status_txt.setText("M100 DISCONNECTED");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        status_circle.setImageDrawable(getResources().getDrawable(R.drawable.sis_circle, getTheme()));
                    } else {
                        status_circle.setImageDrawable(getResources().getDrawable(R.drawable.sis_circle));

                    }


                }
                //  Intent called "Message" will pass graph_array
                else{
                    List<Float> array_new_list = new ArrayList<>();

                    status_txt.setText("M100 CONNECTED");
                    // ble_date.setText(Date());
                    array_new_list.addAll((Collection<? extends Float>) extras.getSerializable("message"));
                    //  status_txt.setText("STATUS: CONNECTED");

                    DateFormat df = new SimpleDateFormat("h:mm a");
                    String date = df.format(Calendar.getInstance().getTime());

                    ble_date.setText(date);




                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        status_circle.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_online, getTheme()));
                    } else {
                        status_circle.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_online));
                    }


                }
            }
        }
    };




    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(myReceiver);
        //stopService(new Intent(testService.this,MyService.class));

    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter("ax.androidexample.mybroadcast");
        registerReceiver(myReceiver, intentFilter);

    }

    @Override
    public void onPause() {
        super.onPause();

        //unregisterReceiver(myReceiver);
    }



}
