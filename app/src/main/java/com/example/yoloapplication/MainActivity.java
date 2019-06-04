package com.example.yoloapplication;

import android.Manifest;
import android.annotation.SuppressLint;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yoloapplication.thread.StartProxyThread;

import org.json.JSONArray;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity implements WifiConnectorModel {
    Button packages, closePopupBtn, registration,forgetallBtn, disableButton1;
    WifiP2pManager mManager;
    ProgressBar spinner;
    WifiManager wifiManager;
    WifiReceiver wifiReceiver;
    int fl = 0;
    WiFiListAdapter wifiListAdapter;
    ListView wifiListView;
    List<ScanResult> wifiList;
    RecyclerView wifiRecyclerView;
    WifiViewHolder wifiViewHolder;
    static int flag = 0;
    String[] s1;
    private StartProxyThread proxyThread;
    Button scanWifiBtn, inet;
    private WifiListRvAdapter adapter;
    private WifiConnector wifiConnector;

    private static final String TAG = MainActivity.class.getSimpleName();

    Channel mChannel;
    BroadcastReceiver mReceiver, mShowlist = null;
    IntentFilter mIntentFilter, mmint = new IntentFilter();
    TextView status, ssi, pas, w;
    public static final String myprefs = "mysp";
    String s = "", p = "";
    Collection<WifiP2pDevice> l;
    int f = 0;
    LinearLayout linearLayout1;
    SharedPreferences sp;
    int[] imageArray;
    ImageView imageView1;
    String kioskId = "";

    //
//    @Override
//    public void onItemClick(int position) {
//        showWifiPasswordDialog(position);
//    }
    private void setLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
        }
    }
//    public void showWifiPasswordDialog(int position) {
//
//        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
//        View mView = getLayoutInflater().inflate(R.layout.dialog_connectwifi, null);
//
//        final EditText inputPassword = mView.findViewById(R.id.wifi_Password);
//        Button connectBtn = mView.findViewById(R.id.connect_Btn);
//        Button cancelBtn = mView.findViewById(R.id.cancel_Btn);
//
//        final TextView wifiSSID = mView.findViewById(R.id.wifi_SSID);
//
//        String newVar = wifiList.get(position).toString();
//        String[] s0 = newVar.split(",");
//        s1 = s0[0].split(": ");
//        wifiSSID.setText(s1[1]);
//
//        alert.setView(mView);
//
//        final AlertDialog alertDialog = alert.create();
//        alertDialog.setCanceledOnTouchOutside(false);
//
//        connectBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//
//                final String nameSSID = s1[1];
//                final String namePass = inputPassword.getText().toString();
//                connectToWifi(nameSSID, namePass);
//
//                alertDialog.dismiss();
//
//            }
//        });
//
//        cancelBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                alertDialog.dismiss();
//            }
//        });
//
//        alertDialog.show();
//
//    }

    //    public void connectToWifi(String ssid, String passphrase) {
//        WifiConfiguration wifiConfig = new WifiConfiguration();
//        wifiConfig.SSID = "\"" + ssid + "\"";
//        wifiConfig.priority = (getMaxConfigurationPriority(wifiManager) + 1);
//        wifiConfig.preSharedKey = "\"" + passphrase + "\"";
//        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//        wifiConfig.priority = getMaxConfigurationPriority(wifiManager);
//
//        int netId = wifiManager.addNetwork(wifiConfig);
//
//
//        wifiManager.disconnect();
//    }
//
//
//    private int getMaxConfigurationPriority(final WifiManager wifiManager) {
//        final List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
//        int maxPriority = 0;
//        for (final WifiConfiguration config : configurations) {
//            if (config.priority > maxPriority)
//                maxPriority = config.priority;
//        }
//
//        return maxPriority;
//    }
//
    private void setMobileDataEnabled(boolean enabled) {
        try {
            TelephonyManager telephonyService = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

            Method setMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("setDataEnabled", boolean.class);

            if (null != setMobileDataEnabledMethod) {
                setMobileDataEnabledMethod.invoke(telephonyService, enabled);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error setting mobile data state", ex);
        }
    }

    // below method returns true if mobile data is on and vice versa
    private boolean mobileDataEnabled(Context context) {
        boolean mobileDataEnabled = false; // Assume disabled
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            assert cm != null;
            Class cmClass = Class.forName(cm.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true); // Make the method callable
            // get the setting for "mobile data"
            mobileDataEnabled = (Boolean) method.invoke(cm);
        } catch (Exception e) {
            // Some problem accessible private API
            // TODO do whatever error handling you want here
        }
        return mobileDataEnabled;
    }

    WifiManager wifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
Bundle b=getIntent().getExtras();
if(b!=null)
{
    kioskId=b.getString("kioskId");
    Log.d("kioskId",kioskId);
}

        imageArray = new int[]{R.drawable.bimg1, R.drawable.bimg2,
                R.drawable.bimg3,R.drawable.bimg6,R.drawable.bimg9,R.drawable.bimg10, R.drawable.bimg11, R.drawable.bimg14,
                R.drawable.bimg15};
        View decorView = getWindow().getDecorView();
// Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        assert wifi != null;
        if (!wifi.isWifiEnabled()) {
            wifi.setWifiEnabled(true);

        }
        imageView1 = findViewById(R.id.imageView1);
        packages = findViewById(R.id.packages);
        registration = findViewById(R.id.registration);
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            int i = 0;

            public void run() {
                imageView1.setBackgroundResource(imageArray[i]);
                i++;
                if (i > imageArray.length - 1) {
                    i = 0;
                }
                handler.postDelayed(this, 3000);
            }
        };
        handler.postDelayed(runnable, 10);

        sp = getApplicationContext().getSharedPreferences(myprefs, Context.MODE_PRIVATE);
        proxyThread = new StartProxyThread();
        scanWifiBtn = findViewById(R.id.scanWifi);
        w = findViewById(R.id.wstatus);


        setLocationPermission();
        scanWifiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                f = 1;
                createWifiConnectorObject();
            }
        });


        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        packages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if (!mWifi.isConnected()) {
                    Toast.makeText(MainActivity.this,"Please Connect to Wifi...",Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent=new Intent(MainActivity.this,WebviewPackages.class);
                intent.putExtra("kioskId",kioskId);
                startActivity(intent);
            }
        });
registration.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mWifi.isConnected()) {
            Toast.makeText(MainActivity.this,"Please Connect to Wifi...",Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent=new Intent(MainActivity.this,WebviewRegistration.class);
        intent.putExtra("kioskId",kioskId);
        startActivity(intent);
    }
});
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

// status bar is hidden, so hide that too if necessary.


    }

    @Override
    public void onResume() {
        super.onResume();

        mShowlist = new ShowWifiListReceiver(wifiConnector);
        registerReceiver(mShowlist, mmint);

    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver(mShowlist);

    }


    @Override
    public void createWifiConnectorObject() {
        wifiConnector = new WifiConnector(this);
        wifiConnector.setLog(true);
        wifiConnector.registerWifiStateListener(new WifiStateListener() {
            @Override
            public void onStateChange(int wifiState) {

            }

            @Override
            public void onWifiEnabled() {

            }

            @Override
            public void onWifiEnabling() {

            }

            @Override
            public void onWifiDisabling() {

            }

            @Override
            public void onWifiDisabled() {

            }
        });


//        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    wifiConnector.enableWifi();
//                } else {
//                    wifiConnector.disableWifi();
//                }
//            }
//        });
        if (f == 1) {
            if (!wifi.isWifiEnabled()) {
                wifi.setWifiEnabled(true);
            }
            mmint.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            View layout = layoutInflater.inflate(R.layout.popup, null);
            final PopupWindow popup = new PopupWindow(layout, 500, 450);

            //scanWifiBtn.setEnabled(false);
            if (fl == 0) {


                //instantiate the popup.xml layout file


                popup.showAtLocation(findViewById(R.id.rl), Gravity.CENTER, 0, 0);

                fl = 1;
            }
            spinner = (ProgressBar) layout.findViewById(R.id.progressBar);
            spinner.setVisibility(View.VISIBLE);
            closePopupBtn = (Button) layout.findViewById(R.id.closePopupBtn);
            forgetallBtn=layout.findViewById(R.id.forgetbutton);
            wifiRecyclerView = layout.findViewById(R.id.wifiRv);
forgetallBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {

    }
});
            closePopupBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

//                    scanWifiBtn.setEnabled(true);
                    popup.dismiss();
                    fl = 0;
                }
            });

            onWifiEnabled();

            adapter = new WifiListRvAdapter(wifiConnector, wifiManager, new WifiListRvAdapter.WifiItemListener() {
                @Override
                public void onWifiItemClicked(ScanResult scanResult) {
                    openConnectDialog(scanResult);
                }

                @Override
                public void onWifiItemLongClick(ScanResult scanResult) {
                    disconnectFromAccessPoint(scanResult);
                }
            });
            LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
            wifiRecyclerView.setLayoutManager(layoutManager);

            wifiRecyclerView.setItemAnimator(new DefaultItemAnimator());

            wifiRecyclerView.setAdapter(adapter);
            wifiRecyclerView.setHasFixedSize(true);

        }


    }

    public void openConnectDialog(ScanResult scanResult) {
        ConnectToWifiDialog dialog = new ConnectToWifiDialog(MainActivity.this, scanResult);
        dialog.setConnectButtonListener(new ConnectToWifiDialog.DialogListener() {
            @Override
            public void onConnectClicked(ScanResult scanResult, String password) {
                w.setVisibility(View.VISIBLE);
                connectToWifiAccessPoint(scanResult, password);
            }
        });
        dialog.show();
    }

    private void onWifiEnabled() {

        if (permisionLocationOn()) {
            //instantiate popup window

//            wifiList = wifiManager.getScanResults();

            //display the popup window
            scanForWifiNetworks();
            //close the popup window on button click
//            wifiListAdapter = new WiFiListAdapter(getApplicationContext(), wifiList);


//            wifiListAdapter.setOnClick(MainActivity.this);


        } else {
            checkLocationTurnOn();
        }
    }

    private void onWifiDisabled() {

//        adapter.setScanResultList(new ArrayList<ScanResult>());
    }

    private Boolean permisionLocationOn() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    private Boolean checkLocationTurnOn() {
        boolean onLocation = true;
        boolean permissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissionGranted) {
            LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            boolean gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!gps_enabled) {
                onLocation = false;
                android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(new ContextThemeWrapper(this, R.style.Theme_AppCompat_Dialog));
                //android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(this);
                dialog.setMessage("Please turn on your location");
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                });
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
                dialog.show();
            }
        }
        return onLocation;
    }

    @Override
    public void scanForWifiNetworks() {
        wifiConnector.showWifiList(new ShowWifiListener() {
            @Override
            public void onNetworksFound(WifiManager wifiManager, List<ScanResult> wifiScanResult) {
                if (wifiScanResult.size() > 0) {
                    spinner.setVisibility(View.GONE);
                    adapter.setScanResultList(wifiScanResult);
                }
            }

            @Override
            public void onNetworksFound(JSONArray wifiList) {

            }

            @Override
            public void errorSearchingNetworks(int errorCode) {
                Toast.makeText(MainActivity.this, "Error on getting wifi list, error code: " + errorCode, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void connectToWifiAccessPoint(final ScanResult scanResult, String password) {
        this.wifiConnector.setScanResult(scanResult, password);
        this.wifiConnector.setLog(true);
        this.wifiConnector.connectToWifi(new ConnectionResultListener() {
            @Override
            public void successfulConnect(String SSID) {


                w.setVisibility(View.INVISIBLE);
                Toast.makeText(MainActivity.this, "You are connected to " + scanResult.SSID + "!!", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();

            }

            @Override
            public void errorConnect(int codeReason) {
                Toast.makeText(MainActivity.this, "Error on connecting to wifi: " + scanResult.SSID + "\nError code: " + codeReason,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStateChange(SupplicantState supplicantState) {

            }
        });
    }

    @Override
    public void disconnectFromAccessPoint(ScanResult scanResult) {
        wifiConnector.removeWifiNetwork(wifiConnector.getCurrentWifiSSID(), "");
    }

    @Override
    public void destroyWifiConnectorListeners() {

    }
}
