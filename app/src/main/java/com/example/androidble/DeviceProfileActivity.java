package com.example.androidble;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.example.androidble.services.BluetoothLeScanService;
import com.example.androidble.utils.GattCharacteristics;
import com.example.androidble.utils.GattServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeviceProfileActivity extends AppCompatActivity {

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_profile);

    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(gattUpdateReceiver,new IntentFilter(BluetoothLeScanService.ACTION_GATT_DISCONNECTED));
        registerReceiver(gattUpdateReceiver,new IntentFilter(BluetoothLeScanService.ACTION_GATT_SERVICES_DISCOVERED));
        registerReceiver(gattUpdateReceiver,new IntentFilter(BluetoothLeScanService.ACTION_DATA_AVAILABLE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(gattUpdateReceiver);
    }

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
             if (BluetoothLeScanService.ACTION_GATT_DISCONNECTED.equals(action)) {

            } else if (BluetoothLeScanService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the
                // user interface.
                 List<BluetoothGattService> services =BluetoothLeScanService.getServices();

                 if(services != null){
                     getGattData(services);
                 }
                 Log.e("test","test");

            } else if (BluetoothLeScanService.ACTION_DATA_AVAILABLE.equals(action)) {

            }
        }
    };

    private void getGattData(List<BluetoothGattService> gattServices) {

        if (gattServices == null) {
            return;
        }

        String uuid = null;
        String unknownServiceString = "Unknown service";
        String unknownCharaString = "Unknown characteristic";

        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();

            currentServiceData.put(LIST_NAME, GattServices.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);

            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME, GattCharacteristics.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }
}