package com.example.jb.bluetooth;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.firebase.crash.FirebaseCrash;


public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;

    private ProgressBar spinner;



    Button scanButton;

    //A BluetoothDevice lets you create a connection with the respective device or query information about it, such as the name, address, class, and bonding state.
    public BluetoothDevice btDevice;


    // Ble scanner
    private BluetoothLeScanner mLEScanner;


    // Defines a scan period
    //40 seconds
    private static final long SCAN_PERIOD = 40000;

    //Each Handler instance is associated with a single thread and that thread's message queue.
    private Handler scanHandler = new Handler();

    Boolean is_scanning = false;

    Context context;

    Boolean deviceFound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        scanButton = (Button) findViewById(R.id.button);
        spinner = (ProgressBar) findViewById(R.id.spin);

        spinner.setVisibility(View.GONE);


        context = MainActivity.this;

        // Initializes Bluetooth adapter !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            // If no BLE
            new AlertDialog.Builder(context)
                    .setTitle("BLUETOOTH")
                    .setMessage("Please turn on your Phone and M100 Bluetooth")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }


    }



    public void scanningbtn(View v)
    {
        if(is_scanning == false)
        {
            spinner.setVisibility(View.VISIBLE);
            scanButton.setText("Scanning....");
            startScan.run();
            is_scanning = true;
        }

        else if(is_scanning == true)
        {
            scanHandler.removeCallbacks(stopScan);
            scanHandler.removeCallbacks(startScan);
            scanLeDevice(false);
            spinner.setVisibility(View.GONE);
            scanButton.setText("Start Scan");
            is_scanning = false;
        }

    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                mLEScanner.startScan(mScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }



    // FOR old phones and API 18 and API 19  Android 4.3 and 4.4
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String device_name = device.getName().toLowerCase();

                                if (device_name.contains("M100electromed") && !deviceFound) {
                                    // If device is close to object then allow Bonding
                                    // if(rssi > -45) {

                                    if (mBluetoothAdapter.isDiscovering()) {
                                        mBluetoothAdapter.cancelDiscovery();
                                    }

                                    complete_stop.run();

                                    Intent pass_value = new Intent(MainActivity.this, PasswordInput.class);
                                    pass_value.putExtra("ble_device", btDevice);
                                    goToNextActivity(pass_value, R.anim.fadein, R.anim.fadeout);
                                    finish();
                                } else if (device_name.contains("laird") && !deviceFound) {

                                    deviceFound = true;

                                    if (mBluetoothAdapter.isDiscovering()) {
                                        mBluetoothAdapter.cancelDiscovery();
                                    }

                                    complete_stop.run();
                                    Intent pass_value = new Intent(MainActivity.this, FirmwareUpdate.class);
                                    pass_value.putExtra("ble_device", btDevice);
                                    goToNextActivity(pass_value, R.anim.fadein, R.anim.fadeout);
                                    finish();
                                }

                            }catch (Exception e)
                            {
                                FirebaseCrash.report(new Exception("MainActivity  Old LE Scan: " + e.getMessage()));
                            }

                        }
                    });
                }
            };



    @SuppressLint("NewApi")
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            try {

                // Bluetooth Device
                btDevice = result.getDevice();


                // RSSI Value
                int rssi = result.getRssi();



                // Bonded means Bonded on this phone
                String device_name = result.getDevice().getName().toLowerCase();


                // Bonded = 12 , Bonding = 11  and None = 10
                //int bond_state = result.getDevice().getBondState();

                //Device_type_classic  = 1 , Device_type_dual = 3 , Device_type_LE= 2 , Device_type_unknown = 0
                // int gettype = result.getDevice().getType(); // 2 Device_type


                // MAKE SURE IT IS LOWER CASE OR ELSE IT WON'T MATCH
                if (device_name.contains("m100electromed") && !deviceFound)
                {
                    // If device is close to object then allow Bonding
                   // if(rssi > -45) {
                        deviceFound = true;

                        if(mBluetoothAdapter.isDiscovering())
                        {
                            mBluetoothAdapter.cancelDiscovery();
                        }

                        complete_stop.run();

                        Intent pass_value = new Intent(MainActivity.this,PasswordInput.class);
                        pass_value.putExtra("ble_device",btDevice);
                        goToNextActivity(pass_value, R.anim.fadein, R.anim.fadeout);
                        finish();
                }
                else if (device_name.contains("laird") && !deviceFound)
                {

                    deviceFound = true;

                    if(mBluetoothAdapter.isDiscovering())
                    {
                        mBluetoothAdapter.cancelDiscovery();
                    }

                    complete_stop.run();
                    Intent pass_value = new Intent(MainActivity.this,FirmwareUpdate.class);
                    pass_value.putExtra("ble_device",btDevice);
                    goToNextActivity(pass_value, R.anim.fadein, R.anim.fadeout);
                    finish();
                }



            } catch (Exception e)
            {
                FirebaseCrash.report(new Exception("Main Activity New LE Scan " + e.getMessage()));
            }

        }


    };


    private void goToNextActivity(Intent intent, int animationIn, int animationOut) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        //Call immediately after one of the flavors of startActivity(Intent) or finish() to
        // specify an explicit transition animation to perform next.
        overridePendingTransition(animationIn, animationOut);
    }


    private Runnable complete_stop = new Runnable() {
        @Override
        public void run() {
            scanHandler.removeCallbacks(startScan);
            scanLeDevice(false);
            scanHandler.removeCallbacks(stopScan);


            scanButton.setText("Start Scan");
            spinner.setVisibility(View.GONE);

        }
    };


    private Runnable stopScan = new Runnable() {
        @Override
        public void run() {
            spinner.setVisibility(View.GONE);
            scanLeDevice(false);
            scanHandler.postDelayed(startScan, 10);
        }
    };

    private Runnable startScan = new Runnable() {
        @Override
        public void run() {
            scanHandler.postDelayed(stopScan, 1000);
            spinner.setVisibility(View.VISIBLE);
            scanLeDevice(true);

            scanHandler.postDelayed(complete_stop, SCAN_PERIOD);
        }
    };



    @Override
    public void onDestroy() {
        super.onDestroy();

        // Stopping the handlers and Scanning Events
        complete_stop.run();
    }


    @Override
    protected void onRestart() {
        super.onRestart();

        Intent i = new Intent(MainActivity.this, SplashPage.class);
        startActivity(i);
        finish();
    }








}
