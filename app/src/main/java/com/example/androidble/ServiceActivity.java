package com.example.androidble;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.androidble.bluetooth.Bluetooth;
import com.example.androidble.services.BluetoothLeScanService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServiceActivity extends AppCompatActivity {

    private static  final String TAG = ServiceActivity.class.getSimpleName();

    private Button writeBtn;
    private Button readBtn;
    private Button subscribeBtn;
    private EditText editText;
    private TextView readable;
    private TextView writable;
    private TextView notifications;

    private BluetoothGattCharacteristic characteristic;
    private String serviceUuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);


        characteristic = getIntent().getParcelableExtra("char");
        serviceUuid = getIntent().getStringExtra("serviceUuid");

        if(characteristic == null && serviceUuid == null){
            Log.e(TAG, "Characteristic not found ");
            finish();
        }

        readable = findViewById(R.id.readable);
        writable = findViewById(R.id.writable);
        notifications = findViewById(R.id.notification);

        subscribeBtn =findViewById(R.id.subscribe_btn);

        boolean isReadable = isCharacteristicReadable(characteristic);
        boolean isWritable = isCharacteristicWritable(characteristic);
        boolean isNotifiable = isCharacteristicNotifiable(characteristic);

        readable.setText("Readable: " + isReadable);
        writable.setText("Writable: " + isWritable);
        notifications.setText("Notifiable: " + isNotifiable);

        subscribeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isCharacteristicNotifiable(characteristic)){
                    BluetoothLeScanService.setSubscribeCharacteristic(UUID.fromString(serviceUuid),characteristic,true);
                }
            }
        });

        writeBtn = findViewById(R.id.write_btn);

        writeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isCharacteristicWritable(characteristic)){

                    if(BluetoothLeScanService.writeCharacteristic(UUID.fromString(serviceUuid),characteristic)){
                        Log.e(TAG,"write");
                    }else{
                        Log.e(TAG,"Write fail");
                    }

                }

            }
        });

        readBtn = findViewById(R.id.read_btn);

        readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BluetoothLeScanService.readCharacteristic(UUID.fromString(serviceUuid),characteristic);

            }
        });
    }


    //Check if property is writable

    public boolean isCharacteristicWritable(BluetoothGattCharacteristic pChar) {
        return (pChar.getProperties() & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0;
    }

    //Check if property is readable
    public boolean isCharacteristicReadable(BluetoothGattCharacteristic pChar) {
        return ((pChar.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0);
    }


    //Check if  supports notification
    public boolean isCharacteristicNotifiable(BluetoothGattCharacteristic pChar) {
        return (pChar.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
    }
}
