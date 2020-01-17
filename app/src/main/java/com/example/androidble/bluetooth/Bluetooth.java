package com.example.androidble.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Bluetooth {

    private static final String TAG = Bluetooth.class.getSimpleName();
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private Set<BluetoothDevice> devicesSet = new HashSet<>();
    private Context context;
    private Handler handler;
    private boolean mIsScanning;
    private static BluetoothAdapter bluetoothAdapter;

    private BluetoothScanListener bluetoothScanListener ;

    public Bluetooth(Context context) {
        this.context = context;
        setBluetoothAdapter(context);
        handler = new Handler();
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            devicesSet.add(result.getDevice());
            Log.e(TAG,result.getDevice().getAddress());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);

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
        if(bluetoothScanListener != null){
            bluetoothScanListener.onStartScanning();
        }
        if(isBluetoothEnabled()){

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIsScanning = false;
                    leScanner.stopScan(scanCallback);
                    Log.e(TAG,"Scan finish");
                    if(bluetoothScanListener != null){
                        bluetoothScanListener.onStopScanning(devicesSet);
                    }
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

    public void setBluetoothScannerListener(BluetoothScanListener listener){
        bluetoothScanListener = listener;
    }


    public interface BluetoothScanListener{
        void onStartScanning();
        void onStopScanning(Set<BluetoothDevice>devices);
    }

}
