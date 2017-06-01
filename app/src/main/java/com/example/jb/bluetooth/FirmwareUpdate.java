package com.example.jb.bluetooth;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// Importing Package
import com.example.jb.bluetooth.bytes.ByteArray;
import com.google.firebase.crash.FirebaseCrash;


public class FirmwareUpdate extends AppCompatActivity {

    // Layout definitions
    private TextView consolevalue;
    private ScrollView scroll;
    private ProgressBar progress;
    private Button startbtn;
    private Boolean tobegin = false;

    // Bluetooth
    public BluetoothAdapter mBluetoothAdapter;
    public BluetoothLeScanner mLEScanner;
    public BluetoothDevice btDevice;

    public BluetoothGatt mGatt;

    Context context;


    //Class definition
    ByteArray SerialManager;

    // First 4  of 8 bytes
    int four_steps = 0;
    String to_send_bytes;
    int main_bytes = 0;
    String  dataToBeSend = "";
    char quote = '"';

    Boolean deviceFound =false;

    boolean is_final = false;

    private static final UUID CUSTOM_SERVICE = UUID.fromString("569a1101-b87f-490c-92cb-11ba5ea5167c");

    // For listening
    private static final UUID CHAR_LISTEN = UUID.fromString("569a2000-b87f-490c-92cb-11ba5ea5167c");
    // For writing
    private static final UUID CHAR_WRTIE = UUID.fromString("569a2001-b87f-490c-92cb-11ba5ea5167c");

    int response = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firmware_update);

        // For Displaying Alert Dialogs
        context = FirmwareUpdate.this;

        consolevalue = (TextView) findViewById(R.id.consolevalue);
        scroll = (ScrollView) findViewById(R.id.scrollViewVspOut);
        progress = (ProgressBar) findViewById(R.id.consoleprogress);
        startbtn = (Button) findViewById(R.id.startbtn);


        SerialManager = new ByteArray();

        progress.setProgress(0);
        progress.setMax(1300);
        progress.setIndeterminate(false);
        progress.getProgressDrawable().setColorFilter(
                Color.parseColor("#339FC6"), android.graphics.PorterDuff.Mode.SRC_IN);


        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();


        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Toast.makeText(FirmwareUpdate.this, "Please turn on your bluetooth ! ", Toast.LENGTH_LONG).show();
            finish();
        }




        // IF this came from MainActivity
        Bundle extra = getIntent().getExtras();
        if(extra!=null)
        {
            btDevice = extra.getParcelable("ble_device");

            startbtn.setText("Just a moment...");
            startbtn.setEnabled(false);

            // Clear Console Value Text
            consolevalue.setText("Please wait...");

            deleteBond();

            if(mBluetoothAdapter.isDiscovering())
            {
                mBluetoothAdapter.cancelDiscovery();
            }
            mBluetoothAdapter.cancelDiscovery();

            mGatt = btDevice.connectGatt(FirmwareUpdate.this, true, gattCallback);
            Boolean refresh = refreshDeviceCache(mGatt);

            if(refresh == false)
            {
                // Show Error or do error checking
            }
        }
        else{
            showDialog(FirmwareUpdate.this);
        }



    }


    public void deleteBond(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // Clear bonding
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {

                if(device.getName().contains("M100electromed")) {
                    try {
                        Method m = device.getClass().getMethod("removeBond", (Class[]) null);
                        m.invoke(device, (Object[]) null);
                    } catch (Exception e) {

                    }
                }
            }
        }
    }



    public void showDialog(Activity activity){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.firmware_dialog);



        // GO BACK
        TextView cancel_d = (TextView) dialog.findViewById(R.id.btncancel);
        cancel_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();

                // Restart Nav Bar Activity
               // Intent i = new Intent(FirmwareUpdate.this, SplashPage.class);
                // startActivity(i);
                finish();

            }
        });

        // CONTINUE
        TextView agree = (TextView) dialog.findViewById(R.id.btnstart);
        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                //stopservice();

            }
        });

        dialog.show();
    }

    /*
    public void stopservice()
    {
        stopService(new Intent(this, BleServiceBackground.class));
    }
    */



    public void start(View v)
    {

       if(!tobegin) {

               startbtn.setText("Just a moment...");
               startbtn.setEnabled(false);

               // Clear Console Value Text
               consolevalue.setText(" ");


               consolevalue.setText(">");
               consolevalue.append("Starting up Firmware download.....");
               consolevalue.append("\n");
               consolevalue.append(">DO NOT TURN OFF PHONE OR M100....");
               consolevalue.append("\n");

               consolevalue.append(">HOLD M100 BUTTON DOWN FOR 10 SECONDS");
               consolevalue.append("\n");

               // Initializes Bluetooth adapter.
               final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
               mBluetoothAdapter = bluetoothManager.getAdapter();


               if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {

                   new AlertDialog.Builder(context)
                           .setTitle("BLUETOOTH")
                           .setMessage("Please turn on your Phone's Bluetooth")
                           .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int which) {


                                   // Reset to MAIN ACTIVITY
                                   Intent i = getBaseContext().getPackageManager()
                                           .getLaunchIntentForPackage(getBaseContext().getPackageName());
                                   i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                   startActivity(i);
                                   finish();

                               }
                           })
                           .setIcon(android.R.drawable.ic_dialog_alert)
                           .show();
               }

           // Deletes the Bond
           deleteBond();


           final Handler handler = new Handler();
           handler.postDelayed(new Runnable() {
               @Override
               public void run() {
                   // Do something after 5s = 5000ms
                   consolevalue.append(">");
                   consolevalue.append("Searching for M100 Device.....");
                   consolevalue.append("\n");

                   // Start Scan
                   scanLeDevice(true);

               }
           }, 8000);
       }


        else{

           startbtn.setText("Do not turn off..");
           startbtn.setEnabled(false);

           progress.setProgress(1);

           consolevalue.append(">");
           consolevalue.append("Sending Data 1/500");
           consolevalue.append("\n");

           BluetoothGattCharacteristic first_char;
           first_char = mGatt.getService(CUSTOM_SERVICE).getCharacteristic(CHAR_WRTIE);
           first_char.setValue("at\r");
           mGatt.writeCharacteristic(first_char);


       }

    }

    public boolean refreshDeviceCache(BluetoothGatt gatt)
    {
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);

            if (localMethod != null) {

                boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                return bool;
            }
        }
        catch (Exception localException) {
              FirebaseCrash.report(new Exception("Firmware Update, Bond Delete Failed: " + localException.getMessage()));
              return false;
        }
        return false;
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

                                if (device_name.contains("laird") && device_name.contains("bl652") && !deviceFound) {
                                    deviceFound = true;
                                    scanLeDevice(false);

                                    if (mBluetoothAdapter.isDiscovering()) {
                                        mBluetoothAdapter.cancelDiscovery();
                                    }
                                    mBluetoothAdapter.cancelDiscovery();

                                    btDevice = device;

                                    mGatt = btDevice.connectGatt(FirmwareUpdate.this, true, gattCallback);
                                    Boolean refresh = refreshDeviceCache(mGatt);

                                    if (refresh == false) {
                                        // Show Error or do error checking
                                    }

                                }
                            }catch (Exception e)
                            {
                                FirebaseCrash.report(new Exception("Firmware update Old Le Scan Callback Failed! : " + e.getMessage()));
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

                int rssi = result.getRssi();
                // Bonded means Bonded on this phone
                String device_name = result.getDevice().getName().toLowerCase();


                // Bonded = 12 , Bonding = 11  and None = 10
                //int bond_state = result.getDevice().getBondState();

                //Device_type_classic  = 1 , Device_type_dual = 3 , Device_type_LE= 2 , Device_type_unknown = 0
                // int gettype = result.getDevice().getType(); // 2 Device_type


                if (device_name.contains("laird") && device_name.contains("bl652") && !deviceFound)
                {
                    deviceFound = true;
                    scanLeDevice(false);

                    if(mBluetoothAdapter.isDiscovering())
                    {
                        mBluetoothAdapter.cancelDiscovery();
                    }
                    mBluetoothAdapter.cancelDiscovery();

                    btDevice = result.getDevice();

                    mGatt = btDevice.connectGatt(FirmwareUpdate.this, true, gattCallback);
                    Boolean refresh = refreshDeviceCache(mGatt);

                    if(refresh == false)
                    {
                        // Show Error or do error checking
                    }

                }


            } catch (Exception e)
            {
                FirebaseCrash.report(new Exception("FU -New LE Scan has failed : " + e.getMessage()));
            }

        }
    };



    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        /// this will get called when a device connects or disconnects
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:


                    runOnUiThread(new Runnable() {
                        public void run() {
                    consolevalue.setText(">");
                    consolevalue.append("M100 Found, Please wait...");
                    consolevalue.append("\n");
                      }

                    });

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        FirebaseCrash.report(new Exception("Firmware Update: thread sleep" + e.getMessage()));
                    }

                    gatt.discoverServices();

                    break;

                //The connection failed and that it is necessary to connect again if we want to communicate with the device.
                case BluetoothProfile.STATE_DISCONNECTED:

                    break;

                case BluetoothProfile.STATE_DISCONNECTING:
                    disconnect();

                default:
                    //  Log.e("gattCallback", "STATE_OTHER");
            }

        }

        //// this will get called after the client initiates a BluetoothGatt.discoverServices() call
        //As previously, the call is asynchronous and when the discovering is completed,the following method needs to be called
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
            {
                    readnextsensor(mGatt);
            }

            } else if (status == BluetoothGatt.GATT_FAILURE) {
                // Find out
                reSubmitScan();

            } else if (status == BluetoothGatt.GATT_CONNECTION_CONGESTED) {

                // Find out
                reSubmitScan();

            }
            // If it's status 133
            else if (status == 133){
                reSubmitScan();
                FirebaseCrash.report(new Exception("Firmware Update Status 133"));
            }
            // If it's status 129
            else if (status == 129 ){
                reSubmitScan();
                FirebaseCrash.report(new Exception("Firmware Update Status 129"));
            }
            // A new Status error
            else{
                // Send this error message to Firebase
                reSubmitScan();
                FirebaseCrash.report(new Exception("Firmware Update Status Unknown in Serivce Discovered"));
            }

        }

        public void reSubmitScan(){

            runOnUiThread(new Runnable() {
                public void run() {

                    consolevalue.setText(">");
                    consolevalue.append("ER1 Too Many Apps opened at current moment, Memory is full");
                    consolevalue.append("\n");

                    consolevalue.setText(">");
                    consolevalue.append("ER2 Phone detecting too many Interference!");
                    consolevalue.append("\n");

                    consolevalue.setText(">");
                    consolevalue.append("Reset APP and M100");
                    consolevalue.append("\n");

                }

            });
        }


        // Characteristic Value of CD-CC-8C-3F    CD-CC-0C-40

        // Reading the function to sensor
        private void readnextsensor(BluetoothGatt gatt){



            //BluetoothGattCharacteristic modem;
            //modem = gatt.getService(CUSTOM_SERVICE).getCharacteristic(VSP_CHAR_MODEM_OUT);
            //gatt.setCharacteristicNotification(modem, true);

            // BluetoothGattCharacteristic modemin;
            //modemin = gatt.getService(CUSTOM_SERVICE).getCharacteristic(VSP_CHAR_MODEM_IN);
            //gatt.setCharacteristicNotification(modemin, true);

            // For listening
            BluetoothGattCharacteristic listen_char;
            listen_char = mGatt.getService(CUSTOM_SERVICE).getCharacteristic(CHAR_LISTEN);
            mGatt.setCharacteristicNotification(listen_char, true);

            List<BluetoothGattDescriptor> descriptors = listen_char.getDescriptors(); //find the descriptors on the characteristic

            BluetoothGattDescriptor descriptor = descriptors.get(0); //get the right descriptor for setting notifications
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mGatt.writeDescriptor(descriptor); //apply these changes to the ble chip to tell it we are ready for the data


            runOnUiThread(new Runnable() {
                public void run() {
                    tobegin = true;
                    consolevalue.setText(">");
                    consolevalue.append("Ready to Start, Please Click START NOW to Begin");
                    consolevalue.append("\n");
                    startbtn.setText("Start Now");
                    startbtn.setEnabled(true);
                }

            });

        }


        public void submit()
        {


            if(response == 0)
            {
                runOnUiThread(new Runnable() {
                    public void run() {
                        consolevalue.append(">");
                        consolevalue.append("Reloading new files...");
                        consolevalue.append("\n");
                        scroll.smoothScrollTo(0, consolevalue.getBottom());
                    }
                });

                dataToBeSend  = "at+del "  + quote + "$autorun$" + quote + "+" + "\r";
            }
            else if (response == 1){

                runOnUiThread(new Runnable() {
                    public void run() {
                        progress.setProgress(3);
                        consolevalue.append(">");
                        consolevalue.append("Loading new files...This may take some time");
                        consolevalue.append("\n");

                        consolevalue.append(">");
                        consolevalue.append("Do not touch M100 or Phone.....");
                        consolevalue.append("\n");
                        scroll.smoothScrollTo(0, consolevalue.getBottom());
                    }
                });

                dataToBeSend  = "at+fow "  + quote + "$autorun$" + quote + "\r";
            }

            // After deleting and initiating the file. It's sending FWRH time
            else{
                runOnUiThread(new Runnable() {
                    public void run() {
                         // IF OUR TEXT VIEW IS GETTING FULL
                        if( (response%2000) == 0)
                        {
                            consolevalue.setText("");
                        }
                        consolevalue.append(">");
                        consolevalue.append("Configuration of Main Files...." + response + " of data sent");
                        consolevalue.append("\n");

                        consolevalue.append(">");
                        consolevalue.append("Do not touch M100 or Phone.....");
                        consolevalue.append("\n");
                        scroll.smoothScrollTo(0, consolevalue.getBottom());
                    }
                });


                // Four steps starts at (0) 0-8  (1) 8-16  (2) 16-24  (3) 24-32
                if(four_steps > 3)
                {
                    four_steps = 0;
                    // Moving forward in the array until the end....  array[0] to array[100] etc...
                    main_bytes = main_bytes + 1;
                }

                // IF it's not the last bit
                if(SerialManager.length_of_array(main_bytes) == 32)
                {
                    to_send_bytes = SerialManager.bytes_to_return(main_bytes,four_steps);

                    four_steps = four_steps + 1;
                }
                // If it is the last bit
                else{

                    four_steps=0;
                    is_final = true;
                    to_send_bytes = "8910F724";
                }


                dataToBeSend  = "at+FWRH "  + quote + to_send_bytes + quote + " \r";
             }

            // Setting the Progress BAR  Every time we send data
            runOnUiThread(new Runnable() {
                public void run() {
                    response = response + 1;
                    progress.setProgress(response);
                }
            });

            writechar();
        }


        public void final_values(){

            to_send_bytes = SerialManager.final_bytes(four_steps);
            dataToBeSend  = "at+FWRH "  + quote + to_send_bytes + quote + " \r";
            //scroll.smoothScrollTo(0, consolevalue.getBottom());

            four_steps = four_steps + 1;
            writechar();
        }


        public void writechar()
        {
            BluetoothGattCharacteristic ssd;
            ssd = mGatt.getService(CUSTOM_SERVICE).getCharacteristic(CHAR_WRTIE);
            ssd.setValue(dataToBeSend);
            mGatt.writeCharacteristic(ssd);
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            try {
                String st = characteristic.getStringValue(0);

                st = st.replace("\n", "");

                st = st.replace("\r", "");


                if (st.equals("00")) {

                    runOnUiThread(new Runnable() {
                        public void run() {
                            consolevalue.append(">");
                            consolevalue.append("Data sent Success!");
                            consolevalue.append("\n");
                            scroll.smoothScrollTo(0, consolevalue.getBottom());
                        }
                    });

                    if (is_final == false) {
                        submit();
                    }
                    else{

                        if(four_steps == 10)
                        {

                            // Reset M100
                            //device_reset();

                            // closing mGATT
                            mGatt.close();
                            mGatt = null;

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    consolevalue.append(">");
                                    consolevalue.append("All Successful, M100 firmware has being Updated...");
                                    consolevalue.append("\n");

                                    consolevalue.append(">");
                                    consolevalue.append("Please restart M100... Restarting your APP in 10 seconds");
                                    consolevalue.append("\n");
                                    scroll.smoothScrollTo(0, consolevalue.getBottom());



                                    // Reset Phone Device
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                         public void run() {
                                                    // Do something after 5s = 5000ms
                                                    Intent i = getBaseContext().getPackageManager()
                                                            .getLaunchIntentForPackage(getBaseContext().getPackageName());
                                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    startActivity(i);
                                                    finish();
                                                }
                                         }, 10000);

                                }
                            });
                        }
                        else if(four_steps > 1)
                        {
                            end_values();

                        }
                        else {
                           final_values();
                        }


                    }



                }
            }catch (Exception e)
            {
                FirebaseCrash.report(new Exception("Firmware Update: On Characteristic Changed" + e.getMessage()));
            }
        }


        public void device_reset(){

            dataToBeSend  = "at+run" + quote + "$autorun$" + quote + "\r";
            writechar();
        }

        public void end_values(){

            dataToBeSend  = "at+FCL" + "\r";
            runOnUiThread(new Runnable() {
                public void run() {
                    consolevalue.append(">");
                    consolevalue.append("Finalizing all files on M100...");
                    consolevalue.append("\n");

                    scroll.smoothScrollTo(0, consolevalue.getBottom());
                }

            });
            four_steps = 10;

            writechar();
        }

        /**
         * Disconnects an existing connection or cancel a pending connection. The
         * disconnection result is reported asynchronously through the
         * BluetoothGattCallback >>
         * onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)
         * callback.
         */

        //Disconnects an established connection, or cancels a connection attempt currently in progress.
        // Just disconnects the Current GATT connection.
        public void disconnect() {
            if (mBluetoothAdapter == null || mGatt == null) {

                return;
            }
            mGatt.disconnect();
        }


        // Only if you want to get rid of all mGATT information.
        public void close() {
            if (mGatt == null) {
                return;
            }
            mGatt.close();
            mGatt = null;
        }

    };


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBluetoothAdapter == null || mGatt == null) {

        } else {
            mGatt.close();
        }
        mGatt = null;
    }

    @Override
    public void onBackPressed() {


    }



}
