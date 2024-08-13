package com.example.flutter_wifi_direct_java_test;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.plugin.common.MethodChannel;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "com.example.wifi_direct";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        requestLocationPermission();

        new MethodChannel(getFlutterEngine().getDartExecutor().getBinaryMessenger(), CHANNEL)
            .setMethodCallHandler(
                (call, result) -> {
                    if (call.method.equals("discoverPeers")) {
                        discoverPeers(result);
                    } else if (call.method.equals("connect")) {
                        String address = call.argument("address");
                        connectToDevice(address, result);
                    } else {
                        result.notImplemented();
                    }
                }
            );
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void discoverPeers(MethodChannel.Result result) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            result.error("PERMISSION_DENIED", "Location permission is required", null);
            return;
        }

        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("@@@@@@@@@@@@@@@@@@@ WiFiDirect", "Discovery started successfully.");
                manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peerList) {
                        List<String> peers = new ArrayList<>();
                        for (WifiP2pDevice device : peerList.getDeviceList()) {
                          System.out.println("@@@@@@@@@@@@@@@@@@@@@@ device : " +  device);
                          peers.add(device.deviceName + " - " + device.deviceAddress);
                        }
                        result.success(peers);
                    }
                });
            }

            @Override
            public void onFailure(int reason) {
                Log.e("@@@@@@@@@@@@@@@@@@@@@ WiFiDirect", "Discovery failed with reason: " + reason);
                result.error("@@@@@@@@@@@@@@@@@@@@@@@ DISCOVERY_FAILED", "Discovery failed with reason: " + reason, null);
            }
        });
    }

    private void connectToDevice(String deviceAddress, MethodChannel.Result result) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = deviceAddress;

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("WiFiDirect", "Connection successful.");
                result.success(null);
            }

            @Override
            public void onFailure(int reason) {
                Log.e("WiFiDirect", "Connection failed with reason: " + reason);
                result.error("CONNECTION_FAILED", "Connection failed with reason: " + reason, null);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("WiFiDirect", "Location permission granted.");
            } else {
                Log.e("WiFiDirect", "Location permission denied.");
            }
        }
    }
}