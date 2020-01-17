package com.example.androidble;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.androidble.adapters.DevicesAdapter;
import com.example.androidble.bluetooth.Bluetooth;
import com.example.androidble.services.BluetoothLeScanService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements Bluetooth.BluetoothScanListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;

    private List<BluetoothDevice>dataSet;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private DevicesAdapter adapter;
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

        if (!bluetooth.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else{
            checkAndRequestPermissions();
        }

        bluetooth.setBluetoothScannerListener(this);

        dataSet = new ArrayList<>();
        recyclerView = findViewById(R.id.device_list);
        layoutManager = new LinearLayoutManager(this);
        adapter = new DevicesAdapter(dataSet);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_appbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.action_search){
            bluetooth.scanBleDevices();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT){
            if (resultCode == RESULT_OK){
                checkAndRequestPermissions();
            }
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

    @Override
    public void onStartScanning() {

    }

    @Override
    public void onStopScanning(Set<BluetoothDevice> devices) {
        List<BluetoothDevice> list = new ArrayList<>();
        list.addAll(devices);
        adapter.updateDatase(list);
    }
}
