package com.example.tkt_ble_spike;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothDevice device;
    private BluetoothGatt bluetoothGatt;
    private String deviceAddress = "A8:E2:C1:9B:07:89";
    private boolean connect_stat = false;
    public static final UUID RX_SERVICE_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");


    TextView statusText;
    View upBtn;
    View downBtn;
    View leftBtn;
    View rightBtn;

    boolean[] click_stat = {false, false, false, false};

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(getWindow().FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);


        // UI
        statusText = findViewById(R.id.connection);
        upBtn = findViewById(R.id.rectangle_1);
        downBtn = findViewById(R.id.rectangle_3);
        leftBtn = findViewById(R.id.rectangle_4);
        rightBtn = findViewById(R.id.rectangle_5);
        statusText.setText("Bluetooth is not connected");

        // Initialize ble
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        // Connect spike
        connect();



        // Set btn listener
        upBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        for (int i = 0; i < click_stat.length; i++) {
                            click_stat[i] = false;
                        }
                        sendmsg("1");
                        click_stat[0] = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        if(!click_stat[0])sendmsg("5");
                        click_stat[0] = false;
                        break;
                }
                return true;
            }
        });

        downBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        for (int i = 0; i < click_stat.length; i++) {
                            click_stat[i] = false;
                        }
                        sendmsg("2");
                        click_stat[1] = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        if(!click_stat[1])sendmsg("5");
                        click_stat[1] = false;
                        break;
                }
                return true;
            }
        });

        leftBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        for (int i = 0; i < click_stat.length; i++) {
                            click_stat[i] = false;
                        }
                        sendmsg("3");
                        click_stat[2] = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        if(!click_stat[2])sendmsg("5");
                        click_stat[2] = false;
                        break;
                }
                return true;
            }
        });

        rightBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        for (int i = 0; i < click_stat.length; i++) {
                            click_stat[i] = false;
                        }
                        sendmsg("4");
                        click_stat[3] = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        if(!click_stat[3])sendmsg("5");
                        click_stat[3] = false;
                        break;
                }
                return true;
            }
        });
    }

    // Connect to ble device
    @SuppressLint("MissingPermission")
    public void connect(){
        device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        bluetoothGatt = device.connectGatt(this , false , GattCallBack);
        Log.d("GATT", "Attempt to connect:"+deviceAddress);
    }

    private final BluetoothGattCallback GattCallBack = new BluetoothGattCallback() {
        // Connect / Disconnect
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (BluetoothGatt.GATT_SUCCESS == status) {
                Log.d("GATT", "Connected");
                gatt.discoverServices();
                bluetoothGatt = gatt;
                connect_stat = true;
                statusText.setText("Bluetooth is connected to LEGO Spike Prime");
            } else {
                Log.d("GATT", "Disconnected");
                bluetoothGatt.close();
                connect_stat = false;
                statusText.setText("Bluetooth is not connected");
            }
        }

        // Receive msg
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] data = characteristic.getValue();
            String message = new String(data);
            Log.d("GATT", "Rx:"+message);
            sendmsg("dllm");
        }
    };

    public void sendmsg(String msg){
        if(connect_stat){
            BluetoothGattCharacteristic characteristic = bluetoothGatt.getService(RX_SERVICE_UUID).getCharacteristic(RX_CHAR_UUID);
            byte[] byteArrray = msg.getBytes();
            characteristic.setValue(byteArrray);
            @SuppressLint("MissingPermission") boolean stat = bluetoothGatt.writeCharacteristic(characteristic);
            Log.d("GATT", "Tx:"+msg+" "+stat);
        }else{
            toastmsg("Not connected!");
            //search()
        }
    }

    public void toastmsg(String msg){
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
    }
}