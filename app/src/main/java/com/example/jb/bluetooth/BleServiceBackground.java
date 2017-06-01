package com.example.jb.bluetooth;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;

import com.google.firebase.crash.FirebaseCrash;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BleServiceBackground extends Service {


    private static final UUID CUSTOM_SERVICE = UUID.fromString("0000abcd-0000-1000-8000-00805f9b34fb");
    private static final UUID FLOAT_CHAR = UUID.fromString("0000ef23-0000-1000-8000-00805f9b34fb");

    // Device info SERVICES AND CHAR
    private static final UUID INFO_SERVICE = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    private static final UUID SERIAL_CHAR = UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb");
    private static final UUID hardware_CHAR = UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb");
    private static final UUID software_CHAR = UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb");

    private static final UUID battery_char = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb");

    List<String> device_info = new ArrayList<>();
    List<BluetoothGattCharacteristic> BLEGATTS = new ArrayList<>();


    private int mConnectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    BluetoothAdapter mBluetoothAdapter;


    private BluetoothGatt mGatt;

    Thread thread;


    Boolean once = true;


    BleServiceBackground BleServiceBackground;


    // Data received after Read
    List<Double> graph_array = new ArrayList<>();

    // Handler for re-connecting
    Handler reconnect_handle = new Handler();


    Boolean notify_newdata = false;


    Handler timeout_handle;
    Runnable run_timeout;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 10 = MAXIMUM Priority
     //   android.os.Process.setThreadPriority(10);

        timeout_handle = new Handler();

        ble_gatt_connect_first_time();


    }



    public void  timeout(){
        Intent retIntent = new Intent();
        retIntent.setAction("ax.androidexample.mybroadcast");
        retIntent.putExtra("status", "dc");
        sendBroadcast(retIntent);
    }

    private void ble_gatt_connect_first_time(){

        if (mBluetoothAdapter == null || mGatt == null) {

        } else {

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {

                    mGatt.close();

                }
            });

            mGatt = null;
        }



        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // No need to Check if bluetooth, the splash screen checks it for us.

        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
        mBluetoothAdapter.cancelDiscovery();




        if (bondedDevices.size() > 0) {

            for (BluetoothDevice device : bondedDevices) {

                // This works
                //mGatt = device.connectGatt(this, false, gattCallback);

                // Try this new method of just finding bonding word
                //match = device.getName().toString().contains("electromed");
                //second_match = device.getName().toString().contains("bond");

                Boolean match = device.getName().contains("M100electromed");

                // If Device is found in the Bonded Library, try to connect !!!
                if (match) {


                    final BluetoothDevice ble = device;


                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            mGatt = ble.connectGatt(BleServiceBackground, false, gattCallback);
                            Boolean refresh = refreshDeviceCache(mGatt);

                            if (!refresh) {
                                // Show Error or do error checking
                            }
                            mConnectionState = STATE_CONNECTING;

                        }
                    });


                    break;
                }

            }

        }


    }



    public boolean refreshDeviceCache(BluetoothGatt gatt) {
        try {

            Method localMethod = mGatt.getClass().getMethod("refresh", new Class[0]);

            if (localMethod != null) {

                boolean bool = ((Boolean) localMethod.invoke(mGatt, new Object[0])).booleanValue();
                return bool;
            }
        } catch (Exception localException) {

            // Crash Report on FireBase
            return false;
        }
        return false;
    }

    public void auto_connect() {
        // Do this after 5 Seconds



                // Our post delayed Handler
                reconnect_handle.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            if (mGatt != null && mConnectionState == STATE_DISCONNECTED) {
                                Handler main_handler = new Handler(Looper.getMainLooper());
                                main_handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mGatt.connect();
                                    }
                                });

                            }

                        } catch (Exception e) {
                            FirebaseCrash.report(new Exception("Ble Service Background: Auto Connect problem: " + e.getMessage()));
                        }
                    }
                }, 5000);

    }


    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        /// this will get called when a device connects or disconnects
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            switch (newState) {


                case BluetoothProfile.STATE_CONNECTED:

                    mConnectionState = STATE_CONNECTED;


                    Intent connectintent = new Intent();
                    connectintent.setAction("ax.androidexample.mybroadcast");
                    connectintent.putExtra("status", "Reading M100, Please wait..");
                    sendBroadcast(connectintent);

                    // Status error  129, Make sure to wait a few seconds before discover Services
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        FirebaseCrash.report(new Exception("Thread sleep failed in Ble Service: " + e.getMessage()));
                    }

                    timeout_handle.removeCallbacks(run_timeout);

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            boolean test = mGatt.discoverServices();
                            if (test) {

                            } else {
                                disconnect();
                            }

                        }
                    });
                    break;

                //The connection failed and that it is necessary to connect again if we want to communicate with the device.
                case BluetoothProfile.STATE_DISCONNECTED:

                    // Disconnect mGatt
                    disconnect();
                    mConnectionState = STATE_DISCONNECTED;

                    // Graph array > 0 and Notify new data = false
                    if (graph_array.size() > 0 && (!notify_newdata)) {
                        // Send Graph Data notifications
                        notifcations_finish();
                    }

                        notify_newdata = false;

                        // Try connect again.
                        auto_connect();

                    timeout_handle.postDelayed(run_timeout = new Runnable() {
                        @Override
                        public void run() {
                            timeout();
                        }
                        // 30 seconds
                    }, 40000);

                    break;


                case BluetoothProfile.STATE_DISCONNECTING:
                    disconnect();
                default:

            }

        }

        // this will get called after the client initiates a BluetoothGatt.discoverServices() call
        //As previously, the call is asynchronous and when the discovering is completed,the following method needs to be called
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
            {
                    Intent retIntent = new Intent();
                    retIntent.setAction("ax.androidexample.mybroadcast");
                    retIntent.putExtra("status", "Reading M100 measurement");
                    sendBroadcast(retIntent);

                    readnextsensor(mGatt);
            }
            } else if (status == BluetoothGatt.GATT_FAILURE) {
                // Find out
                disconnect();

            } else if (status == BluetoothGatt.GATT_CONNECTION_CONGESTED) {
                // Find out
                disconnect();
            }
            // If it's status 133
            else if (status == 133){

                ble_gatt_connect_first_time();


            }
            // If it's status 129
            else if (status == 129 ){

                Intent retIntent = new Intent();
                retIntent.setAction("ax.androidexample.mybroadcast");
                retIntent.putExtra("status", "First Time Adjusting M100 to User...wait.");
                sendBroadcast(retIntent);

                // If status 129 then disconnect();
                notify_newdata = true;
                disconnect();
            }
            // A new Status error
            else{
                // Send this error message to Firebase
                disconnect();
            }

        }


        // Reading the function to sensor
        private void readnextsensor(BluetoothGatt gatt) {


            BluetoothGattCharacteristic characteristic;

            // one that will receive notifications
            characteristic = mGatt.getService(CUSTOM_SERVICE).getCharacteristic(FLOAT_CHAR);
            BLEGATTS.add(characteristic);

            characteristic = mGatt.getService(INFO_SERVICE).getCharacteristic(hardware_CHAR);
            BLEGATTS.add(characteristic);

            characteristic = mGatt.getService(INFO_SERVICE).getCharacteristic(software_CHAR);
            BLEGATTS.add(characteristic);

            characteristic = mGatt.getService(INFO_SERVICE).getCharacteristic(SERIAL_CHAR);
            BLEGATTS.add(characteristic);

            characteristic = mGatt.getService(INFO_SERVICE).getCharacteristic(battery_char);
            gatt.readCharacteristic(characteristic);
            BLEGATTS.add(characteristic);

        }

        public void requestChar(BluetoothGatt gatt) {
            gatt.readCharacteristic(BLEGATTS.get(BLEGATTS.size() - 1));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            if (status == 0) {

                byte[] data = characteristic.getValue();


                if (BLEGATTS.size() != 1) {

                    String s = new String(data);

                    device_info.add(s);

                    BLEGATTS.remove(BLEGATTS.get(BLEGATTS.size() - 1));

                    requestChar(gatt);
                } else {

                    BLEGATTS.clear();
                    byte[] values;


                    for (int i = 0; i < data.length; i++) {

                        values = Arrays.copyOfRange(data, i, i + 2);

                        Short f_short = ByteBuffer.wrap(values).order(ByteOrder.LITTLE_ENDIAN).getShort();

                        double final_val = parseSFLOATtoDouble(f_short);

                        graph_array.add(final_val);

                        i = i + 1;
                    }

                    gatt.setCharacteristicNotification(characteristic, true);
                    List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors(); //find the descriptors on the characteristic
                    BluetoothGattDescriptor descriptor = descriptors.get(0); //get the right descriptor for setting notifications
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mGatt.writeDescriptor(descriptor); //apply these changes to the ble chip to tell it we are ready for the data


                    final BluetoothGattCharacteristic found_char = characteristic;

                    reconnect_handle.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            mGatt.setCharacteristicNotification(found_char, false);
                            List<BluetoothGattDescriptor> disable_descriptor = found_char.getDescriptors();
                            BluetoothGattDescriptor gatt_disable = disable_descriptor.get(0); //get the right descriptor for setting notifications
                            gatt_disable.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                            mGatt.writeDescriptor(gatt_disable);


                            notifcations_finish();
                            notify_newdata = true;

                            Intent retIntent = new Intent();
                            retIntent.setAction("ax.androidexample.mybroadcast");
                            retIntent.putExtra("status", "Finish! M100 re-measuring in 30sec");
                            sendBroadcast(retIntent);

                        }
                    }, 5000);

                }


            } else {
                disconnect();
            }


        }


        // After changing the Notification Values
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            byte[] data = characteristic.getValue();


            if (data != null) {
                byte[] values;


                for (int i = 0; i < data.length; i++) {

                    values = Arrays.copyOfRange(data, i, i + 2);

                    Short f_short = ByteBuffer.wrap(values).order(ByteOrder.LITTLE_ENDIAN).getShort();

                    final double final_val = parseSFLOATtoDouble(f_short);

                    graph_array.add(final_val);

                    i = i + 1;
                }
            } else {

            }


        }


        public void notifcations_finish() {

            try {
                // Send the data out
                Intent retIntent = new Intent();
                retIntent.setAction("ax.androidexample.mybroadcast");
                retIntent.putExtra("message", (Serializable) graph_array);
                retIntent.putExtra("device_info", (Serializable) device_info);
                sendBroadcast(retIntent);

                // Clear all the data from graph_array
                graph_array.clear();
            } catch (Exception e) {

                FirebaseCrash.report(new Exception("Ble background Service, notifications_finish() failed " + e.getMessage()));

                thread.interrupt();
                stopSelf();
            }


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

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {

                    if (mBluetoothAdapter == null || mGatt == null) {

                        return;
                    }
                    mGatt.disconnect();
                }
            });
        }


    };

    // This is where you start a service
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        //Boolean  start_firmware_mode = intent.getBooleanExtra("Bootmode", false);

        //thread = new Thread(new Thethread());
        // make sure thread will be killed if app is unexpectedly killed
        //thread.setDaemon(true);
        // thread.start();

        //  return super.onStartCommand(intent, flags, startId);


        // This mode makes sense for things that will be explicitly started
        // and stopped to run for arbitrary periods of time, such as a service performing background music playback
        //return START_STICKY;
        //there are no new start intents to deliver to it, then take the service out of the started state and don't recreate
        return START_NOT_STICKY;
    }

    // This is where you destroy a service
    @Override
    public void onDestroy() {
        //Toast.makeText(backgroud_service.this,"service destroyed", Toast.LENGTH_LONG).show();

        final BluetoothGattCharacteristic characteristic = mGatt.getService(CUSTOM_SERVICE).getCharacteristic(FLOAT_CHAR);
        List<BluetoothGattDescriptor> disable_descriptor = characteristic.getDescriptors();
        BluetoothGattDescriptor gatt_disable = disable_descriptor.get(0); //get the right descriptor for setting notifications
        gatt_disable.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);


        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                if (mBluetoothAdapter == null || mGatt == null) {

                } else {
                    mGatt.close();
                }
                mGatt = null;
            }
        });

        // If the thread is still alive, it's still blocking on the methodcall, try stopping it
        //If this thread is blocked in an I/O operation upon an interruptible channel then the channel will be closed
        //thread.interrupt();

        handler.removeCallbacksAndMessages(null);

        // Stop Service
        stopSelf();
    }


    public double parseSFLOATtoDouble(short value) {
        // NaN
        if (value == 0x07FF) {
            return Double.NaN;
        }
        // NRes (not at this resolution)
        else if (value == 0x0800) {
            return Double.NaN;
        }
        // +INF
        else if (value == 0x07FE) {
            return Double.POSITIVE_INFINITY;
        }
        // -INF
        else if (value == 0x0802) {
            return Double.NEGATIVE_INFINITY;
        }
        // Reserved
        else if (value == 0x0801) {
            return Double.NaN;
        } else {
            return ((double) getMantissa(value)) * Math.pow(10, getExponent(value));
        }
    }

    public short getExponent(short value) {
        if (value < 0) { // if exponent should be negative
            return (byte) (((value >> 12) & 0x0F) | 0xF0);
        }

        short test = (short) ((value >> 12) & 0x0F);
        return test;
    }

    public short getMantissa(short value) {
        if ((value & 0x0800) != 0) { // if mantissa should be negative
            return (short) ((value & 0x0FFF) | 0xF000);
        }

        short test = (short) (value & 0x0FFF);
        return test;
    }


}
