package com.example.yoloapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Viram on 5/13/2019.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    class Node {
        String ip;


        Node(String ip) {
            this.ip = ip;

        }

        @Override
        public String toString() {
            return ip;
        }
    }

    private WifiP2pManager mManager;
    private Channel mChannel;
    private Activity mActivity;
    private boolean isWifiP2pEnabled;
    String host;
    ArrayList<Node> listNote = new ArrayList<>();
String textResult;
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
                                       Activity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
        this.isWifiP2pEnabled = false;
    }

    private void readAddresses(String host) {

        BufferedReader bufferedReader = null;

        try {
            bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted.length >= 4) {
                    String ip = splitted[0];
                    String mac = splitted[3];
                    Log.d("mac",""+ip);
                    Log.d("mac",""+mac);
                    if (mac.equals(host)) {

                        Node thisNode = new Node(ip);
                        listNote.add(thisNode);
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                setIsWifiP2pEnabled(true);

            } else {
                setIsWifiP2pEnabled(false);
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // The peer list has changed! We should probably do something about
            // that.

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {


textResult="";
listNote.clear();
                    mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {

                        @Override
                        public void onConnectionInfoAvailable(final WifiP2pInfo info) {
                            if (info.isGroupOwner) {
                                mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {

                                    @Override
                                    public void onGroupInfoAvailable(WifiP2pGroup group) {
                                        //This is the size you want
                                        Collection<WifiP2pDevice> peerList = group.getClientList();

                                        ArrayList<WifiP2pDevice> list = new ArrayList<WifiP2pDevice>(peerList);

                                        int i;
                                        for (i = 0; i < list.size(); i = i + 1) {
                                            host = list.get(i).deviceAddress;
                                            Log.d("hostmac", "" + host);
                                        }
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                readAddresses(host);
                                                textResult = "";
                                                int j;
                                                for (j = 0; j < listNote.size(); j++) {

                                                    textResult = listNote.get(j).toString();

                                                }
                                                Log.d("ip", "" + textResult);
                                                if (!textResult.equals("")) {
//                                                    do {
//                                                        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//                                                        connected[0] = activeNetworkInfo != null && activeNetworkInfo.isConnected();
//                                                    } while (!connected[0]);
                                                    Intent intent = new Intent(context, WebviewPackages.class);
                                                    intent.putExtra("ip", textResult);
                                                    context.startActivity(intent);
                                                }
                                            }
                                        }, 1000);


                                    }
                                });
                            }
                        }
                    });

                // Connection state changed! We should probably do something about
                // that.


        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

        }
    }

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

}

