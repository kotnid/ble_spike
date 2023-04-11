package com.example.tkt_ble_spike;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothDevice device;
    private BluetoothGatt bluetoothGatt;
    private String deviceAddress = "A8:E2:C1:9B:07:89";
    public static String RX_CHAR = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E";
    private boolean connect_stat = false;
    public static  UUID RX_SERVICE_UUID;
    public static  UUID RX_CHAR_UUID ;
    public static final UUID TX_CHAR_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");

    public String deviceName = "robot";

    TextView statusText;
    View upBtn;
    View downBtn;
    View leftBtn;
    View rightBtn;
    View shootBtn;
    TextView shootText;

    boolean[] click_stat = {false, false, false, false};

    int REQUEST_BLUETOOTH_PERMISSIONS = 127;
    int REQUEST_CONNECTION_PERMISSIONS = 128;
    int REQUEST_COARSE_LOCATION = 129;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(getWindow().FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
//        toastmsg(""+Build.VERSION.SDK_INT);

        // UI
        statusText = findViewById(R.id.connection);
        upBtn = findViewById(R.id.rectangle_1);
        downBtn = findViewById(R.id.rectangle_3);
        leftBtn = findViewById(R.id.rectangle_4);
        rightBtn = findViewById(R.id.rectangle_5);
        shootBtn = findViewById(R.id.shoot_butto);
        shootText = findViewById(R.id.shoot);
        statusText.setText("Bluetooth is not connected");

        // Initialize ble
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        checkPermission();
//        search();
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
                        if(click_stat[0])sendmsg("5");
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
                        if(click_stat[1])sendmsg("5");
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
                        if(click_stat[2])sendmsg("5");
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
                        if(click_stat[3])sendmsg("5");
                        click_stat[3] = false;
                        break;
                }
                return true;
            }
        });

        shootBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setEnabled(false);
                sendmsg("6");
                shootText.setTextColor(0xFF404040);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.setEnabled(true);
                        shootText.setTextColor(0xff000000);
                    }
                }, 2000);
            }
        });
    }

    public void checkPermission(){
        ActivityResultLauncher<Intent> enableBtLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != Activity.RESULT_OK) {
                        toastmsg("Error : permission denied");
                    }
                });
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtLauncher.launch(enableBtIntent);
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            enableBtLauncher.launch(intent);
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= 31) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_SCAN},
                    REQUEST_BLUETOOTH_PERMISSIONS);
            toastmsg("Request permission");
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                bluetoothLeScanner.startScan(scanCallback);
                search();
            } else {
                toastmsg("Error : permission not granted");
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void search(){
        toastmsg("Start scan");
        Log.d("GATT" , "Start scan");
        bluetoothLeScanner.startScan(scanCallback);
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice foundDevice = result.getDevice();
            toastmsg("Device found:"+foundDevice.getName()+" MAC:"+foundDevice.getAddress());
            Log.d("GATT" , "Device found:"+foundDevice.getName()+" MAC:"+foundDevice.getAddress());

            if (foundDevice.getName() != null && foundDevice.getBondState() != BluetoothDevice.BOND_BONDED && foundDevice.getName().contains(deviceName)){
                deviceAddress = foundDevice.getAddress();
                bluetoothLeScanner.stopScan(scanCallback);
                connect();
            }
        }
    };

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
            if (newState == BluetoothProfile.STATE_DISCONNECTED){
                Log.d("GATT", "Disconnected");
//                bluetoothGatt.close();
                connect_stat = false;
                statusText.setText("Bluetooth is not connected");
                search();
            }
            else if (BluetoothGatt.GATT_SUCCESS == status) {
                Log.d("GATT", "Connected");
                gatt.discoverServices();
                bluetoothGatt = gatt;
                connect_stat = true;
                statusText.setText("Bluetooth is connected to LEGO Spike Prime");
            } else {
                Log.d("GATT", "Disconnected");
//                bluetoothGatt.close();
                connect_stat = false;
                statusText.setText("Bluetooth is not connected");
                search();
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> services = gatt.getServices();
                for (BluetoothGattService service : services) {
                    Log.d("GATT", "Service UUID: " + service.getUuid().toString());
                    RX_SERVICE_UUID = service.getUuid();
                    List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                    for (BluetoothGattCharacteristic characteristic : characteristics) {
                        Log.d("GATT", "Characteristic UUID: " + characteristic.getUuid().toString());
                        RX_CHAR_UUID = characteristic.getUuid();
                    }
                }
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
            UUID TMP_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
            UUID TMP_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
            BluetoothGattCharacteristic characteristic = bluetoothGatt.getService(TMP_SERVICE_UUID).getCharacteristic(TMP_CHAR_UUID);
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