package com.lichard49.btheadsetcontroller;

import android.support.v7.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private TextView outputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        outputText = findViewById(R.id.output_text);

        BluetoothAdapter.getDefaultAdapter().getProfileProxy(this, new BluetoothProfile.ServiceListener() {
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (profile == BluetoothProfile.HEADSET) {
                    mBluetoothHeadset = (BluetoothHeadset) proxy;
                    BluetoothDevice device = mBluetoothHeadset.getConnectedDevices().get(0);
                    UUID uuid = device.getUuids()[0].getUuid();
                    try {
                        System.out.println("Connecting to " + device.getName());
                        final BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuid);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    socket.connect();
                                    System.out.println("Connected: " + socket.isConnected());

                                    socket.getOutputStream().write("\r\n+CIEV: 2,1\r\n".getBytes());
                                    socket.getOutputStream().flush();

                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    socket.getOutputStream().write("\r\n+CIEV: 2,0\r\n".getBytes());
                                    socket.getOutputStream().flush();

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            public void onServiceDisconnected(int profile) {
                if (profile == BluetoothProfile.HEADSET) {
                    mBluetoothHeadset = null;
                }
            }
        }, BluetoothProfile.HEADSET);

        new Thread(new Runnable() {
            @Override
            public void run() {

                //Get the text file
                File file = new File("/sdcard/btsnoop_hci.log");

                try {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    reader.skip(file.length());

                    String forward = ".*02 \\w\\w \\w\\w 0c \\w\\w \\w\\w \\w\\w \\w\\w \\w\\w \\w\\w \\w\\w \\w\\w \\w\\w \\w\\w \\w\\w 4b.*";
                    String backward = ".*02 \\w\\w \\w\\w 0c \\w\\w \\w\\w \\w\\w \\w\\w \\w\\w \\w\\w \\w\\w \\w\\w \\w\\w \\w\\w \\w\\w 4c.*";
                    String play = ".*02 \\w\\w \\w\\w 0c \\w\\w \\w\\w \\w\\w \\w\\w \\w\\w \\w\\w \\w\\w \\w\\w \\w\\w \\w\\w \\w\\w 44.*";

                    while (true) {
                        if (reader.ready()) {
                            byte[] line = reader.readLine().getBytes();
                            String lineString = bytesToHexString(line);

                            if (lineString.matches(forward)) {
                                System.out.println("forward button!!!!");
                            } else if (lineString.matches(backward)) {
                                System.out.println("backward button!!!!");
                            } else if (lineString.matches(play)) {
                                System.out.println("play button!!!!");
                            } else {
                                System.out.println(lineString);
                            }
                        }
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    BluetoothHeadset mBluetoothHeadset;

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for(byte b : bytes){
            sb.append(String.format("%02x ", b&0xff));
        }
        return sb.toString();
    }
}
