package com.example.realtimestream;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.net.rtp.AudioCodec;
import android.net.rtp.AudioGroup;
import android.net.rtp.AudioStream;
import androidx.core.app.ActivityCompat;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


/**
 *
 *  objectives:
 *  stream voice on the phone over the network to a PC running ubuntu.
 *  this will be used to test the audio latency
 *
 *  steps:
 *  1. set ip address for android and for ubuntu (192.168.86.229)
 *  2. build the app and install
 *  3. @ubuntu open VLC and choose Media from tab. choose open network stream
 *     enter a network URL: rtp://192.168.86.229:12345
 *
 *  bugs?
 *  I can't hear the audio.. but I see the spectrum
 *
 *  watch out for wifi ssid!
 *
 */

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 1000;

    private AudioGroup audioGroup;
    private AudioStream audioStream;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        if(Build.VERSION.SDK_INT >= 23) {
            String[] permissions = {
                    Manifest.permission.RECORD_AUDIO,
            };
            checkPermission(permissions, REQUEST_CODE);
        }
    }

    public void checkPermission(final String[] permissions,final int request_code){
        ActivityCompat.requestPermissions(this, permissions, request_code);
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            InetAddress localAddress = getLocalAddress();
            InetAddress receiverAddress = InetAddress.getByName("192.168.86.229");

            audioStream = new AudioStream(localAddress);
            audioStream.setCodec(AudioCodec.PCMU);
            audioStream.setMode(AudioStream.MODE_SEND_ONLY);

            audioStream.associate(receiverAddress, 12345);
            audioGroup = new AudioGroup();
            audioGroup.setMode(AudioGroup.MODE_NORMAL);
            audioStream.join(audioGroup);
        } catch(Exception e) {

        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(audioGroup != null) {
            audioGroup.clear();
            audioGroup = null;
        }
        if(audioStream != null) {
            audioStream.release();
            audioStream = null;
        }
    }

    private static InetAddress getLocalAddress() throws SocketException {
        Enumeration<NetworkInterface> netifs
                = NetworkInterface.getNetworkInterfaces();
        while(netifs.hasMoreElements()) {
            NetworkInterface netif = netifs.nextElement();
            for(InterfaceAddress ifAddr : netif.getInterfaceAddresses()) {
                InetAddress a = ifAddr.getAddress();
                if(a != null && !a.isLoopbackAddress() && a instanceof Inet4Address){
                    return a;
                }
            }
        }
        return null;
    }
}