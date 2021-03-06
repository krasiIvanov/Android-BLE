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
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.example.androidble.adapters.DevicesAdapter;
import com.example.androidble.bluetooth.Bluetooth;
import com.example.androidble.services.BluetoothLeScanService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements Bluetooth.BluetoothScanListener, DevicesAdapter.ItemClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;

    private List<BluetoothDevice>dataSet;

    private ProgressBar progressBar;
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

        //Check if bluetooth is turn on
        if (!bluetooth.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else{

            checkAndRequestPermissions();
        }


        bluetooth.setBluetoothScannerListener(this);
        progressBar = findViewById(R.id.progressBar);
        dataSet = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.device_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new DevicesAdapter(dataSet,this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);


    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(gattUpdateReceiver,new IntentFilter(BluetoothLeScanService.ACTION_GATT_CONNECTED));
        registerReceiver(gattUpdateReceiver,new IntentFilter(BluetoothLeScanService.ACTION_GATT_DISCONNECTED));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(gattUpdateReceiver);
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

            }
        }
    };
    //Check permissions and if not granted request them
    private boolean checkAndRequestPermissions() {

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
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStopScanning(Set<BluetoothDevice> devices) {
        progressBar.setVisibility(View.INVISIBLE);
        if(devices.size()>0){
            dataSet = new ArrayList<>(devices);
            adapter.updateDataSet(dataSet);
        }
    }

    @Override
    public void onItemClick(int position) {

        Intent profile = new Intent(MainActivity.this,DeviceProfileActivity.class);
        profile.putExtra("dev",dataSet.get(position));
        startActivity(profile);

    }
}
