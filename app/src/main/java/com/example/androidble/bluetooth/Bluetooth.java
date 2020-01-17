package com.example.androidble.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.List;

public class Bluetooth {

    private static final String TAG = Bluetooth.class.getSimpleName();
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private Context context;
    private Handler handler;
    private boolean mIsScanning;
    private static BluetoothAdapter bluetoothAdapter;

    public Bluetooth(Context context) {
        this.context = context;
        setBluetoothAdapter(context);
        handler = new Handler();
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            Log.e(TAG,result.getDevice().getAddress());

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);

            Log.e(TAG,"Scan finish");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);

            Log.e(TAG,"Scan failed");

        }
    };

    public boolean isBluetoothEnabled(){
        return bluetoothAdapter.isEnabled();
    }

    public void scanBleDevices(){
        Log.e(TAG,"Scan for BLE devices");
        final BluetoothLeScanner leScanner = bluetoothAdapter.getBluetoothLeScanner();
        if(isBluetoothEnabled()){

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIsScanning = false;
                    leScanner.stopScan(scanCallback);
                }
            },SCAN_PERIOD);

            mIsScanning = true;
            leScanner.startScan(scanCallback);

        }else {

            mIsScanning = false;
            leScanner.stopScan(scanCallback);
        }

    }

    private void setBluetoothAdapter(Context context){

        BluetoothManager bluetoothManager = (BluetoothManager)context.
                getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            Bluetooth.bluetoothAdapter = bluetoothManager.getAdapter();
        }else{
            Log.e(TAG,"Bluetooth manager is null");
        }

    }



}
