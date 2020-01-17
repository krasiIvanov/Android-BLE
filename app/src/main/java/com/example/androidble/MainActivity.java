package com.example.androidble;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.example.androidble.bluetooth.Bluetooth;
import com.example.androidble.services.BluetoothLeScanService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;

    private Bluetooth bluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.e(TAG, "BLE is not supported on this device");
            finish();
        }

        bluetooth = new Bluetooth(this);

        if(checkAndRequestPermissions()){
            bluetooth.scanBleDevices();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT){
            if (resultCode == RESULT_OK){

            }
        }
    }

    //Application permission callback
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        boolean bluetooth_permission = (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED);
        boolean location_permission = (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        //startService(new Intent(getApplicationContext(), BluetoothService.class));
        if(location_permission && bluetooth_permission){
            bluetooth.scanBleDevices();
        }

    }

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeScanService.ACTION_GATT_CONNECTED.equals(action)) {

            } else if (BluetoothLeScanService.ACTION_GATT_DISCONNECTED.equals(action)) {

            } else if (BluetoothLeScanService.
                    ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the
                // user interface.
            } else if (BluetoothLeScanService.ACTION_DATA_AVAILABLE.equals(action)) {

            }
        }
    };

    private  boolean checkAndRequestPermissions() {

        int permissionBluetooth = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH);
        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (permissionBluetooth != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.BLUETOOTH);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),1);
            return false;
        }
        return true;
    }
}
