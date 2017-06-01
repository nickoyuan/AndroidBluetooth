package com.example.jb.bluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;

public class PasswordInput extends AppCompatActivity {

    BluetoothDevice bluetoothDevice;


    TextView begintxt;



    private Handler scanHandler = new Handler();

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_input);


        Bundle extra = getIntent().getExtras();

        //  High Priority Pairing Request.
        // The ACTION_PAIRING_REQUEST is an ordered intent broadcast that you can intercept before the System Does.
        IntentFilter pairingRequestFilter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        pairingRequestFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
        registerReceiver(mPairingRequestRecevier, pairingRequestFilter);

        if(extra!=null)
        {
            bluetoothDevice = extra.getParcelable("ble_device");

        }
        else{

            // ERROR HAS OCCURED, NO BLUETOOTH DEVICE FOUND !
        }


        begintxt = (TextView) findViewById(R.id.begintxt);




        // Broadcast Receiver is use to listen for any changes in the state of the Bluetooth Adapter
        IntentFilter filter = new IntentFilter(bluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mReceiver, filter);


        begintxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                setDialog();

                bluetoothDevice.createBond();



            }
        });


    }


    // ACTION PAIRING REQUEST - RECEIVER
    private final BroadcastReceiver mPairingRequestRecevier = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            //Broadcast Action: This intent is used to broadcast PAIRING REQUEST
            if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(intent.getAction()))
            {
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int type = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);

                //The user will be prompted to enter a pin or an app will enter a pin for user
                if (type == BluetoothDevice.PAIRING_VARIANT_PIN)
                {

                   // byte[] bytes = pass_text.getBytes();
                    //bluetoothDevice.setPin(bytes);

                    // Clear BroadCast
                    abortBroadcast();

                }
                else
                {

                }
            }
        }
    };


    // BONDING STATE CHANGED  - RECEIVER
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();


            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);

                switch(state){
                    case BluetoothDevice.BOND_BONDING:

                        start_timer.run();

                        break;

                    case BluetoothDevice.BOND_BONDED:

                        // Removing both Runnable Timers ! Called from Bond_Bonding.....
                        scanHandler.removeCallbacks(start_timer);
                        scanHandler.removeCallbacks(stopScan);

                        dialog.dismiss();

                        LoadingScreenThread async_dialog = new LoadingScreenThread(PasswordInput.this);
                        async_dialog.execute();


                        bonded_good_toast_msg();


                         // No need to unregister since finish() is called. OnDestroy() will also be called
                        //unregisterReceiver(mReceiver);
                        //unregisterReceiver(mPairingRequestRecevier);

                        // Start Nav Bar activity
                        //Intent i = new Intent(PasswordInput.this, NavBar.class);
                        //startActivity(i);

                        break;

                    case BluetoothDevice.BOND_NONE:

                        // Removing both Runnable Timers ! Called from Bond_Bonding.....
                        scanHandler.removeCallbacks(start_timer);
                        scanHandler.removeCallbacks(stopScan);

                         bondingfailed_toastmsg();

                        break;

                }
            }
        }
    };


    public void setDialog()
    {
        dialog = ProgressDialog.show(this, "", "Loading..");
    }

  public void bondingfailed_toastmsg()
  {
      Toast.makeText(this, "M100 cannot be found ! Please turn bluetooth back on", Toast.LENGTH_LONG).show();
      dialog.dismiss();
  }


    public void bonded_good_toast_msg()
    {
        Toast.makeText(this, "Device successfully connected", Toast.LENGTH_LONG).show();
    }




    private Runnable stopScan = new Runnable() {
        @Override
        public void run() {

            try {
                bluetoothDevice.getClass().getMethod("cancelPairingUserInput").invoke(bluetoothDevice);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            scanHandler.removeCallbacks(start_timer);
            scanHandler.removeCallbacks(stopScan);
            dialog.dismiss();
        }
    };

    private Runnable start_timer = new Runnable() {
        @Override
        public void run() {
            scanHandler.postDelayed(stopScan, 15000);

        }
    };


    // Do nothing if Back Button is pressed
    // Requires API level 5 or higher
    @Override
    public void onBackPressed() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister broadcast listeners
        unregisterReceiver(mReceiver);
        unregisterReceiver(mPairingRequestRecevier);
    }


    @Override
    protected void onRestart() {
        super.onRestart();

        Intent i = new Intent(PasswordInput.this, SplashPage.class);
        startActivity(i);
        finish();
    }


}
