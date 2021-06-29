package com.ubicompproject.SARCAapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity{


    // ---------- SET UP ---------- //
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;

    TextView tvBluetoothStatus, incomingMessages, tvSignal, tvFoundList, tvTitleComms, tvTitleMessages, tvHumitureLog, tvGPSLog, tvCO2TVOCLog;
    ImageView ivSignal, ivTest, ivLogo;
    Button btnBluetoothOn, btnBluetoothOff, btnBluetoothDiscoverable, btnBluetoothDiscover, btnStartConnection, btnSend, btnHumitureDataDialog, btnGPSData, btnCO2TVOC;
    EditText etSend;
    String mDateStamp, mHumiture, mCO2TVOC, mIMU1, mIMU2, mIMU3, mIMU4, mSen, mTemperature, mHumidity, mCO2, mTVOC, mGPS1, mGPS2, mAltSpeed, mDirection, mMagno, mAccel, mGyro, mInternalTemp, mPressure;
    List<String> listReceivedData = new ArrayList<>();

    List<String> listDateStamps = new ArrayList<>();
    List<String> listHumitureRecordings = new ArrayList<>();
    List<String> listCO2TVOCRecordings = new ArrayList<>();
    List<String> listLatLongRecordings = new ArrayList<>();
    List<String> listAltSpeedRecordings = new ArrayList<>();
    List<String> listDirectionRecordings = new ArrayList<>();
    List<String> listMagnetometerRecordings = new ArrayList<>();
    List<String> listAccelerometerRecordings = new ArrayList<>();
    List<String> listGyroRecordings = new ArrayList<>();
    List<String> listIntTempPressure = new ArrayList<>();

    BluetoothAdapter mBluetoothAdapter;
    BluetoothConnectionService mBluetoothConnection;
    BluetoothDevice mBTDevice;
    Set<BluetoothDevice> mDevices = new HashSet<BluetoothDevice>();//This will be a set of bluetooth device objects.

    List<String> listFoundDevices; //List of found bluetooth devices as a string list
    ArrayAdapter<String> mArrayAdapter;  //Adapter for adding devices to the list view
    ListView lvFoundDevices; //Will display the found devices
    StringBuilder messages;
    StringBuilder humiture;
    StringBuilder mGPS;
    StringBuilder mCO2Tvoc;

    public ArrayList<BluetoothDevice> mBTDevices;
    private final UUID insecureRandomUUID = UUID.fromString("f22fbd56-631e-442c-8992-78f961e103f6");

    AlertDialog.Builder alertDialogBuilder;

    // ---------- RECEIVERS ---------- //
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (mDevices.contains(foundDevice)){
                    mToast("Device discovery has finished");
                    mBluetoothAdapter.cancelDiscovery();
                }
                else if (!mDevices.contains(foundDevice)){
                    mDevices.add(foundDevice);  //Device gets added to the devices set
                    mBTDevices.add(foundDevice);
                    listFoundDevices.add("\t" + foundDevice.getName() + "\n\tAddress: " + foundDevice.getAddress()); //This is a list of the devices as a string
                    mArrayAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage"); //This string holds the data that has just come in.
            listReceivedData = Arrays.asList(text.split("#"));
            try {
                mDateStamp = listReceivedData.get(0); // "02/06/2021 07:51:29"
                mHumiture = listReceivedData.get(1); // "Temperature: 55.2F, 21.4C \n Humidity: 57.4%"
                mCO2TVOC = listReceivedData.get(2); // "eCO2 = 451 ppm \n TVOC = 1 ppb"
                mGPS1 = listReceivedData.get(3); // "Lat: 0.0 Long 0.0"
                mGPS2 = listReceivedData.get(4); // "Altitude: nan Speed nan (m/s) / nan (mph)"
                mIMU1 = listReceivedData.get(5); // "Direction:"
                mIMU2 = listReceivedData.get(6); // "Magnetometer: 292.41*"
                mIMU3 = listReceivedData.get(7); // "Accelerometer: x = -7.18, y = 0.04"
                mIMU4 = listReceivedData.get(8); // "Gyroscope: x = 22.33, y = -28.29, z = -3.43"
                mSen = listReceivedData.get(9); // "Internal Temperature = 29.0 C \n Pressure = 101141.46 Pa"
            } catch (IndexOutOfBoundsException e){
                System.out.println(e);
            }

            listDateStamps.add(mDateStamp + "\n");
            listHumitureRecordings.add(mHumiture + "\n");
            listCO2TVOCRecordings.add(mCO2TVOC + "\n");
            listLatLongRecordings.add(mGPS1 + "\n");
            listAltSpeedRecordings.add(mGPS2 + "\n");
            listDirectionRecordings.add(mIMU1 + "\n");
            listMagnetometerRecordings.add(mIMU2 + "\n");
            listAccelerometerRecordings.add(mIMU3 + "\n");
            listGyroRecordings.add(mIMU4 + "\n");
            listIntTempPressure.add(mSen + "\n");

            Collections.reverse(listReceivedData);

            messages.delete(0, messages.length());
            humiture.delete(0, humiture.length());
            mGPS.delete(0, mGPS.length());
            mCO2Tvoc.delete(0, mCO2Tvoc.length());

            for (String item: listReceivedData){
                messages.insert(0, item + "\n");
            }
            incomingMessages.setText(messages);

            for (String item: listHumitureRecordings){
                humiture.append(item);
            }

            for (String item: listLatLongRecordings){
                mGPS.append(item);
            }

            for (String item: listCO2TVOCRecordings){
                mCO2Tvoc.append(item);
            }
        }
    };



    // ---------- ON CREATE ---------- //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //Sets the content view to the activity_main layout.



        // LINK THE VIEWS AND BUTTONS TO THE WIDGETS IN THE LAY OUT FILE VIA THEIR ID
        tvBluetoothStatus = findViewById(R.id.tvBluetoothStatus);
        tvSignal = findViewById(R.id.tv_signal);
        tvFoundList = findViewById(R.id.tv_title_foundlist);
        tvTitleComms = findViewById(R.id.tv_title_comms);
        tvTitleMessages = findViewById(R.id.tv_title_message);
        ivSignal = findViewById(R.id.iv_signal);
        btnBluetoothOn = findViewById(R.id.btnBluetoothOn);
        btnBluetoothOff = findViewById(R.id.btnBluetoothOff);
        btnBluetoothDiscoverable = findViewById(R.id.btnBluetoothDiscoverable);
        btnBluetoothDiscover = findViewById(R.id.btnBluetoothDiscover);
        btnStartConnection = findViewById(R.id.startconnection);

        tvHumitureLog = findViewById(R.id.tv_log_humiture);
        tvGPSLog = findViewById(R.id.tv_log_GPS);
        tvCO2TVOCLog = findViewById(R.id.tv_log_CO2TVOC);

        //Send dialog
        btnSend = findViewById(R.id.btnSend);
        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Send Message");
        alertDialogBuilder.setMessage("Please enter the message you wish to send: ");
        alertDialogBuilder.setCancelable(true);
        etSend = new EditText(this);
        alertDialogBuilder.setView(etSend);
        alertDialogBuilder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                byte[] bytes = etSend.getText().toString().getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(bytes);
                etSend.setText("");
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", null);
        AlertDialog alertDialog = alertDialogBuilder.create();

        //Humiture dialog
        btnHumitureDataDialog = findViewById(R.id.btn_humiture_dialog);
        humiture = new StringBuilder();

        btnGPSData = findViewById(R.id.btn_GPS_dialog);
        mGPS = new StringBuilder();

        btnCO2TVOC = findViewById(R.id.btn_CO2_dialog);
        mCO2Tvoc = new StringBuilder();




        // LINK THE ADAPTERS AND LISTS AND VIEWS
        listFoundDevices = new ArrayList<>(); //String list of devices
        mArrayAdapter = new ArrayAdapter<>(this, R.layout.list_view_layout, listFoundDevices);
        lvFoundDevices = findViewById(R.id.lv_found_devices);  //Link to the list view in the layout
        lvFoundDevices.setAdapter(mArrayAdapter); //link the view and the adapter together

        // CONSTRUCT AN EMPTY LIST
        mBTDevices = new ArrayList<>();

        // GET THE BLUETOOTH ADAPTER
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // DEFAULT TO NO SIGNAL ON START
        ivSignal.setImageResource(R.drawable.ic_signal_lost);
        tvSignal.setText("Signal Strength: Not Connected");

        // MESSAGES FROM AND TO PI
        incomingMessages = findViewById(R.id.incomingMessages);
        messages = new StringBuilder();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));

        // CHECK IF BLUETOOTH IS AVAILABLE AND REPORT BACK
        if (mBluetoothAdapter == null){
            tvBluetoothStatus.setText("Bluetooth adapter is not available!");
        }

        // ON CLICK LISTENER FOR THE LIST VIEW
        lvFoundDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                tvTitleComms.setVisibility(View.VISIBLE);
                btnStartConnection.setVisibility(View.VISIBLE);
                //mToast(mArrayAdapter.getItem(position));
                mBluetoothAdapter.cancelDiscovery();
                String connectAddress = mArrayAdapter.getItem(i);
                String[] splitName = connectAddress.split("Address:");
                splitName[0] = splitName[0].trim();
                splitName[1] = splitName[1].trim();
                String deviceName = splitName[0];

                mToast("Trying to connect to: " + deviceName);

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    try {
                        mBTDevices.get(i).createBond();
                        mBTDevice = mBTDevices.get(i);
                        mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);

                        mToast("Successfully connected to: " + deviceName);
                        tvSignal.setText("Connected to " + mBTDevice.getName());
                        ivSignal.setImageResource(R.drawable.ic_signal_good);  //TODO How do we pick up when a connection has been lost?
                    }catch(Exception e){
                        mToast("Could not connect to: " + deviceName);
                    }
                }
            }
        });

        // BLUETOOTH ON BUTTON
        btnBluetoothOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBluetoothAdapter.isEnabled()){
                    mToast("Turning Bluetooth On");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                } else {
                    mToast("Bluetooth is already on");
                }
            }
        });

        //BLUETOOTH OFF BUTTON
        btnBluetoothOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter.isEnabled()){
                    mBluetoothAdapter.disable();
                    mToast("Turning off Bluetooth");
                } else {
                    mToast("Bluetooth is already off");
                }

            }
        });

        // MAKE DEVICE DISCOVERABLE BUTTON
        btnBluetoothDiscoverable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBluetoothAdapter.isDiscovering()){
                    mToast("Making device discoverable");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(intent, REQUEST_DISCOVER_BT);
                } else {
                    mToast("Device already discoverable");
                }
            }
        });


        //FIND DEVICES
        btnBluetoothDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentFilter discoverDevices = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                tvFoundList.setVisibility(View.VISIBLE);
                lvFoundDevices.setVisibility(View.VISIBLE);
                if (mBluetoothAdapter.isEnabled()){
                    if (!listFoundDevices.isEmpty()){
                        mToast("Restarting device discovery");
                        mBluetoothAdapter.cancelDiscovery();
                        listFoundDevices.clear();
                        mDevices.clear();
                        mBTDevices.clear();
                        mArrayAdapter.notifyDataSetChanged();
                        checkPermissionBluetooth();
                        mBluetoothAdapter.startDiscovery();
                        registerReceiver(mBroadcastReceiver, discoverDevices);

                    }
                    else if (listFoundDevices.isEmpty()){
                        mToast("Starting device discovery");
                        checkPermissionBluetooth();
                        mBluetoothAdapter.cancelDiscovery();
                        mBluetoothAdapter.startDiscovery();
                        registerReceiver(mBroadcastReceiver, discoverDevices);
                    }
                } else {
                    mToast("Bluetooth is not enabled");
                }
            }
        });

        // SET UP CLICK LISTENER FOR START CONNECTION BUTTON (STARTS THE RFCOM SERVICE AND ATTEMPTS TO ESTABLISH A CONNECTION)
        btnStartConnection.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) { //TODO We need some kind of catch here in case device not paired?
                tvFoundList.setVisibility(View.GONE);
                lvFoundDevices.setVisibility(View.GONE);
                tvTitleComms.setVisibility(View.GONE);
                btnStartConnection.setVisibility(View.GONE);
                btnSend.setVisibility(View.VISIBLE);
                tvTitleMessages.setVisibility(View.VISIBLE);
                incomingMessages.setVisibility(View.VISIBLE);
                try {
                    startConnection();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        // SET UP CLICK LISTENER FOR THE SEND BUTTON, FOR ONCE TEXT IS SENT
        btnSend.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                alertDialog.show();
            }
        });


        btnHumitureDataDialog.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                humiture.delete(0, humiture.length());
                for (String item: listHumitureRecordings){
                    humiture.append(item + "\n");
                }
                tvHumitureLog.setText(humiture);
            }
        });

        btnGPSData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGPS.delete(0, mGPS.length());
                for (String item: listLatLongRecordings){
                    mGPS.append(item + "\n");
                }
                tvGPSLog.setText(mGPS);
            }
        });

        btnCO2TVOC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCO2Tvoc.delete(0, mCO2Tvoc.length());
                for (String item: listCO2TVOCRecordings){
                    mCO2Tvoc.append(item + "\n");
                }
                tvCO2TVOCLog.setText(mCO2Tvoc);
            }
        });

    } //END OF ON CREATE;


    // ---------- CREATE METHOD FOR STARTING CONNECTION ---------- //
    public void startConnection(){
        startBTConnection(mBTDevice,insecureRandomUUID);
    }

    // ---------- START RFCOM SERVICE ---------- //
    public void startBTConnection(BluetoothDevice device, UUID uuid){
        mToast("Starting RFCOM Bluetooth Connection");
        mBluetoothConnection.startClient(mBTDevice, uuid);
    }




    // ---------- UNREGISTER RECEIVERS ON DESTROY ---------- //
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }




    // ---------- CHECK WHICH PERMISSION IS REQUIRED DEPENDING ON ANDROID VERSION ---------- //
    private void checkPermissionBluetooth() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }
        } else {
            mToast("checkPermissionBluetooth: No need to check permission.  SDK version < LOLLIPOP");
        }
    }




    // ---------- ON ACTIVITY RESULT FOR REQUEST ENABLE BT ---------- //
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK){
//                    ivBluetooth.setImageResource(R.drawable.ic_action_on);
                    mToast("Bluetooth is on");
                } else {
                    mToast("Could not turn on bluetooth");
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }




    // ---------- SET UP EASY TO USE TOAST ---------- //
    private void mToast (String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }


}