package com.example.androidble.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.androidble.bluetooth.Bluetooth;
import com.example.androidble.utils.GattDescriptors;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class BluetoothLeScanService extends Service{

    private final static String TAG = BluetoothLeScanService.class.getSimpleName();
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;


    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_BATTERY_SERVICE =
            UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");



    private BluetoothDevice device;
    private static BluetoothGatt bluetoothGatt;
    private int connectionState = STATE_DISCONNECTED;

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1,buildNotification());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent != null){
            if(intent.getAction().equals("conn") && connectionState != STATE_CONNECTED){
                device = intent.getParcelableExtra("dev");
                bluetoothGatt = device.connectGatt(getApplicationContext(),true,gattCallback,2);
            }
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Various callback methods defined by the BLE API.
    private final BluetoothGattCallback gattCallback =
            new BluetoothGattCallback() {

                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    String intentAction;
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        intentAction = ACTION_GATT_CONNECTED;
                        connectionState = STATE_CONNECTED;
                        broadcastUpdate(intentAction);
                        Log.e(TAG, "Connected to GATT server.");
                        Log.e(TAG, "Attempting to start service discovery");
                        bluetoothGatt.discoverServices();

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                            intentAction = ACTION_GATT_DISCONNECTED;
                            connectionState = STATE_DISCONNECTED;
                            Log.e(TAG, "Disconnected from GATT server.");
                            broadcastUpdate(intentAction);
                    }
                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

                    } else {
                        Log.e(TAG, "onServicesDiscovered received: " + status);
                    }
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                    }
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    if(status == BluetoothGatt.GATT_SUCCESS){
                        Log.e(TAG,"Characteristic write");
                    }
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

                    Log.e(TAG,"Characteristic changed");

                }

                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    if(status == BluetoothGatt.GATT_SUCCESS){
                        Log.e(TAG,"On description write");
                    }
                }
            };


    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        if (UUID_BATTERY_SERVICE.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" +
                        stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    private Notification buildNotification(){

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), "ID")
                .setContentTitle("BLE")
                .setContentText("Test")
                .setPriority(2)
                .build();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "ID",
                    "BLE TEST",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if(manager != null)
                manager.createNotificationChannel(serviceChannel);
        }

        return notification;
    }

    public static List<BluetoothGattService> getServices(){
        if(bluetoothGatt == null){
            return null;
        }
        return bluetoothGatt.getServices();
    }

    public static void setSubscribeCharacteristic(BluetoothGattCharacteristic characteristic,boolean enable){

        if(bluetoothGatt == null){
            Log.e(TAG,"Gatt is null");
            return;
        }

        bluetoothGatt.setCharacteristicNotification(characteristic,enable);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(GattDescriptors.CLIENT_CHARACTERISTIC_CONFIG));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);

    }

    public static boolean writeCharacteristic(UUID serviceUuid,BluetoothGattCharacteristic charac){
        //check mBluetoothGatt is available
        if (bluetoothGatt == null) {
            Log.e(TAG, "lost connection");
            return false;
        }
        BluetoothGattService Service = bluetoothGatt.getService(serviceUuid);
        if (Service == null) {
            Log.e(TAG, "service not found!");
            return false;
        }

        if (charac == null) {
            Log.e(TAG, "char not found!");
            return false;
        }

        //byte[] b = text.getBytes(StandardCharsets.UTF_8)

        byte[] value = new byte[1];

        value[0] = (byte) (21 & 0xFF);
        charac.setValue(value);
        boolean status = bluetoothGatt.writeCharacteristic(charac);
        return status;
    }

    public static void closeConnection(){
        if(bluetoothGatt == null){
            Log.e(TAG,"Gatt is null");
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;

    }

}
