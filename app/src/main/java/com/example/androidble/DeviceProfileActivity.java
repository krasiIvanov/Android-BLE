package com.example.androidble;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.androidble.adapters.DeviceDataAdapter;
import com.example.androidble.models.CharacteristicViewModel;
import com.example.androidble.models.ServiceViewModel;
import com.example.androidble.services.BluetoothLeScanService;
import com.example.androidble.utils.GattCharacteristics;
import com.example.androidble.utils.GattServices;
import com.example.androidble.utils.OnItemClick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeviceProfileActivity extends AppCompatActivity implements OnItemClick {

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private BluetoothDevice device;

    private RecyclerView recyclerView;
    private DeviceDataAdapter adapter;

    private List<ServiceViewModel>dataSet = new ArrayList<>();
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_profile);

        device = getIntent().getParcelableExtra("dev");

        recyclerView = findViewById(R.id.device_profile_recycle_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new DeviceDataAdapter(dataSet,this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        if(device != null){
            Intent intent = new Intent(this,BluetoothLeScanService.class);
            intent.setAction("conn");
            intent.putExtra("dev",device);
            if(Build.VERSION.SDK_INT == Build.VERSION_CODES.O){
                startForegroundService(intent);
            }else{
                startService(intent);
            }
        }
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

            if(BluetoothLeScanService.ACTION_GATT_CONNECTED.equals(action)){



            }else if (BluetoothLeScanService.ACTION_GATT_DISCONNECTED.equals(action)) {

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
        String name = null;
        String unknownServiceString = "Unknown service";
        String unknownCharaString = "Unknown characteristic";

        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();

        List<ServiceViewModel>serviceViewModels = new ArrayList<>();

        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

            uuid = gattService.getUuid().toString();
            name = GattServices.lookup(uuid, unknownServiceString);

            ServiceViewModel serviceViewModel = new ServiceViewModel(name,uuid);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);

                uuid = gattCharacteristic.getUuid().toString();
                name = GattCharacteristics.lookup(uuid, unknownCharaString);

                CharacteristicViewModel characteristicViewModel = new CharacteristicViewModel(name,uuid);
                serviceViewModel.addCharacteristic(characteristicViewModel);

            }
            mGattCharacteristics.add(charas);
            serviceViewModels.add(serviceViewModel);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        dataSet = serviceViewModels;
        adapter.updateDataSet(dataSet);

    }

    @Override
    public void onItemClicked(int position) {

        Intent intent = new Intent(this,ServiceActivity.class);
        intent.putParcelableArrayListExtra("char",mGattCharacteristics.get(position));
        intent.putExtra("serviceUuid",dataSet.get(position).getUuid());
        startActivity(intent);
    }
}