/*
 * Created by Jose Flavio on 2/10/18 4:33 PM.
 * Copyright (c) 2017 JoseFlavio.
 * All rights reserved.
 */

package com.example.yoloapplication;

import android.app.Dialog;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

/**
 * ConnectToWifiDialog
 *
 * @author Jose Flavio - jflavio90@gmail.com
 * @since 10/2/17
 */
public class ConnectToWifiDialog extends Dialog implements View.OnClickListener {

    private TextView wifiName;
    private TextView wifiSecurity;
    private EditText pass;
    private Button connect;
    private ScanResult scanResult;
    Context contex;
    int flag = 0;
    private DialogListener dialogListener;

    public ConnectToWifiDialog(@NonNull Context context, ScanResult scanResult) {
        super(context);
        contex = context;
        this.scanResult = scanResult;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_connectwifi);
        wifiName = findViewById(R.id.dialog_wifiname);
        wifiSecurity = findViewById(R.id.dialog_security);
        pass = findViewById(R.id.dialog_et);
        connect = findViewById(R.id.dialog_btn);
        connect.setOnClickListener(this);
        fillData();
    }

    private void fillData() {
        wifiName.setText(scanResult.SSID);
        WifiManager wifiManager;


        wifiManager = (WifiManager) contex.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String sec = WifiConnector.getWifiSecurityType(scanResult);

        assert wifiManager != null;
        List<WifiConfiguration> list1 = wifiManager.getConfiguredNetworks();
        if (list1 != null && list1.size() > 0) {
            for (WifiConfiguration i : list1) {
                try {
                    String ss = "\"" + scanResult.SSID + "\"";
                    Log.d("ss", "" + ss);
                    if (i.SSID.equals(ss))
                        flag = 1;


                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }

        if (WifiConnector.SECURITY_NONE.equals(sec) || flag == 1) {
            pass.setVisibility(View.INVISIBLE);
        } else {
            pass.setVisibility(View.VISIBLE);
        }
        wifiSecurity.setText(sec);
    }

    public void setConnectButtonListener(DialogListener listener) {
        this.dialogListener = listener;
    }

    @Override
    public void onClick(View v) {
        this.dialogListener.onConnectClicked(scanResult, pass.getText().toString());
        dismiss();
    }

    interface DialogListener {
        void onConnectClicked(ScanResult scanResult, String password);
    }
}
