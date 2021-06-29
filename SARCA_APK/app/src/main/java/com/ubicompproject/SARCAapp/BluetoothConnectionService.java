package com.ubicompproject.SARCAapp;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


/**----------------------------------------------------------------------
 *
 *                   START OF BLUETOOTH CONNECTION CLASS
 *
 * ----------------------------------------------------------------------**/

public class BluetoothConnectionService{
    private static final String appName = "SARCA";
    private final UUID insecureRandomUUID = UUID.fromString("f22fbd56-631e-442c-8992-78f961e103f6");
    private AcceptThread mInsecureAcceptThread;
    private BluetoothDevice mmDevice;
    private ConnectThread mConnectThread;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;
    private ConnectedThread mConnectedThread;
    Context mContext;
    BluetoothAdapter mBluetoothAdapter;

    // SET BLUETOOTH ADAPTER AND CONTEXT (MESSAGES)
    public BluetoothConnectionService(Context context){
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }

    // WHEN THE 'START CONNECTION' BUTTON IS PRESSED, STARTS THIS PROCESS AND WAITS FOR AN INCOMING CONNECTION.
    // THIS IS BYPASSED IN OUR CASE, AS THE SERVER IS HOSTED ON THE PI. BUT IT CAN BE USED TO CONNECT MORE THAN ONE DEVICE.
    // i.e. start the connection on one android device, and then start the connection on another, and then it is possible to send messages
    // to both through the editText on the activity_main
    private class AcceptThread extends Thread {

        // Create the local server socket (RFCOM)
        final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;
            // Create a new listening server port
            try{
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(appName, insecureRandomUUID); // tries to open a temporary service on rfcom channel
            }catch (IOException e){
                e.printStackTrace();
            }
            mmServerSocket = tmp; // if it succeeds, the temporary port is assigned to mmServerSocket as the main process
        }
        public void run(){
            BluetoothSocket socket = null;
            try {
                socket = mmServerSocket.accept(); // automatically accept the connection if one is made
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(socket != null){
                connected(socket, mmDevice);
            }
        }
        public void cancel(){
            try {
                mmServerSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }


    /**-------------------------------------------------------------------------------------------------
     *
     *      THIS RUNS WHILST ATTEMPTING TO CONNECT TO AN 'ALREADY ON' SERVICE ON THE RFCOM CHANNEL.
    *                    Either a connection will be made, or it will fail.
     *
     * -------------------------------------------------------------------------------------------------**/

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        public ConnectThread(BluetoothDevice device, UUID uuid) {
            mmDevice = device;
            deviceUUID = uuid;
        }
        public void run() {
            BluetoothSocket tmp = null;
            // Get a bluetooth socket for connection with device
            try {
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
            // Always cancel discovery mode as it will slow down connection time.
            mBluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    mmSocket.close();

                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            connected(mmSocket, mmDevice);
        }
        public void cancel(){
            try{
                mmSocket.close();
            }catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    // This is going to start the accept thread (automatically accept any incoming RFCOM requests).
    public synchronized void start() {

        // Cancel any thread and attempt to create a new one if already running.
        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if(mInsecureAcceptThread != null){
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    // Accept thread starts and waits for connection.
    // Connect thread starts and attempts to make a connection with other devices.
    public void startClient(BluetoothDevice device, UUID uuid){
        // Init process diaglog.
        mProgressDialog = ProgressDialog.show(mContext,"Connecting Bluetooth", "Please wait...", true);
        try {
            mConnectThread = new ConnectThread(device, uuid);
            mConnectThread.start();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket){
            mmSocket = socket;
            InputStream tmpIn = null; // Assign input and outputs to temporary values
            OutputStream tmpOut = null;

            // Dismiss the dialog box
            try {
                mProgressDialog.dismiss();
            }catch(NullPointerException e){
                e.printStackTrace();
            }

            try {
                tmpIn = mmSocket.getInputStream(); // If a successful connection has been made, assign to non-temp variables
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInStream = tmpIn; // Assigned to non-temp here
            mmOutStream = tmpOut;
        }

        /**
         * THIS IS WHERE THE MESSAGE THAT IS RECEIVED OR SENT IS PROCESSED
         */
        public void run(){
            byte[] buffer = new byte[1024]; // Buffer store for data stream
            int bytes; // Returned from read

            // Keep listening for new information while running, until an error occurs
            while (true) {
                // Read from input steam
                try {
                    bytes = mmInStream.read(buffer); //Puts the incoming message into the read buffer (set above)
                    String incomingMessage = new String(buffer, 0, bytes); // Formats the data in the buffer into string

                    Intent incomingMessageIntent = new Intent("incomingMessage"); // New intent with the focus of the incomingMessage (act of receiving data)
                    incomingMessageIntent.putExtra("theMessage", incomingMessage); // Add to the end, theMessage (actual transmitted data)
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent); // Use local broadcast to send this to the activity_main

                } catch (IOException e) {
                    e.printStackTrace();
                    // If there is an issue with the connection. Want to break the loop
                    break;
                }
            }
        }

        // Call this to send data to the remote device
        public void write(byte[] bytes){
            String text = new String(bytes, Charset.defaultCharset());
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /* CALL THIS TO SHUTDOWN THE CONNECTION */
        public void cancel(){
            try {
                mmSocket.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    // Start the thread to manage the connection and perform trasnmission of data
    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice){
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    // Write to the Connected Thread in an asynchronous manner
    public void write(byte[] out){
        //Create temporary object
        ConnectedThread r;
        r = mConnectedThread;
        mConnectedThread.write(out);
    }
}