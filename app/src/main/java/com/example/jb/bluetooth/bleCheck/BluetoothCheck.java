package com.example.jb.bluetooth.bleCheck;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;


public class BluetoothCheck {

    BluetoothAdapter mBluetoothAdapter;

    public Boolean isBluetoothOn(Context context){
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
             return false;
        }

         return true;
    }

}
