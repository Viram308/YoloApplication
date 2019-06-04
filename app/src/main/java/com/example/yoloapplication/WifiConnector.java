package com.example.yoloapplication;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class WifiConnector {
    public static final String TAG = WifiConnector.class.getName();
    private boolean logOrNot = false;
    private Context context;
    private WifiConfiguration wifiConfiguration;
    private WifiManager wifiManager;
    private WifiStateListener wifiStateListener;
    private ConnectionResultListener connectionResultListener;
    private ShowWifiListener showWifiListListener;
    private RemoveWifiListener removeWifiListener;
    public IntentFilter wifiStateFilter;
    private IntentFilter chooseWifiFilter;
    private IntentFilter showWifiListFilter;
    public WifiStateReceiver wifiStateReceiver;
    public WifiConnectionReceiver wifiConnectionReceiver;
    public ShowWifiListReceiver showWifiListReceiver;

    private static List<WifiConfiguration> confList;


    public static final int SUCCESS_CONNECTION = 2500;
    public static final int AUTHENTICATION_ERROR = 2501;
    public static final int NOT_FOUND_ERROR = 2502;
    public static final int SAME_NETWORK = 2503;
    public static final int ERROR_STILL_CONNECTED_TO = 2504;
    public static final int UNKOWN_ERROR = 2505;

    /**
     * codes for searching wifi
     */
    public static final int WIFI_NETWORKS_SUCCESS_FOUND = 2600;
    public static final int NO_WIFI_NETWORKS = 2601;

    /**
     * WIFI SECURITY TYPES
     */
    private static final String SECURITY_WEP = "WEP";
    private static final String SECURITY_WPA = "WPA";
    private static final String SECURITY_PSK = "PSK";
    private static final String SECURITY_EAP = "EAP";

    /**
     * for setting wifi access point security type
     */
    public static final String SECURITY_NONE = "NONE";

    /**
     * String value for current connected Wi-Fi network
     */
    private String currentWifiSSID = null;

    /**
     * Static value to be acceded from anywhere
     */
    public static String CURRENT_WIFI = null;

    /**
     * String value for current connected Wi-Fi network
     */
    private String currentWifiBSSID = null;

    @Deprecated
    public WifiConnector(Context context, boolean enableWifi) {
        this.context = context;
        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (enableWifi) {
            enableWifi();
        }
    }

    public WifiConnector(Context context) {
        this.context = context;
        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

//    public WifiConnector(Context context, ScanResult scanResult, String password) {
//        this.context = context;
//        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        setWifiConfiguration(scanResult.SSID, scanResult.BSSID, getWifiSecurityType(scanResult), password);
//    }
//
//    public WifiConnector(WifiConfiguration wifiConfiguration, Context context) {
//        this.wifiConfiguration = wifiConfiguration;
//        this.context = context;
//        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//    }
//
//    public WifiConnector(Context context, String SSID, String BSSID, String securityType, String password) {
//        this.context = context;
//        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        setWifiConfiguration(SSID, BSSID, securityType, password);
//    }

    public void setScanResult(ScanResult scanResult, String password) {
        setWifiConfiguration(scanResult.SSID, scanResult.BSSID, getWifiSecurityType(scanResult), password);
    }

    public void setWifiConfiguration(String SSID, String BSSID, String securityType, String password) {
        this.wifiConfiguration = new WifiConfiguration();
        this.wifiConfiguration.SSID = SSID;
        this.wifiConfiguration.BSSID = BSSID;
        if (securityType.equals(SECURITY_NONE)) {
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else {
            wifiConfiguration.preSharedKey = ssidFormat(password);
            wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA); // For WPA
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN); // For WPA2
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiManager.enableNetwork(wifiManager.addNetwork(wifiConfiguration),true);
            wifiManager.saveConfiguration();

        }
    }

    // region Receivers


    public WifiConnector registerWifiStateListener(WifiStateListener wifiStateListener) {
        createWifiStateBroadcast();
        this.wifiStateListener = wifiStateListener;
        return this;
    }

    public void unregisterWifiStateListener() {
        try {
            context.getApplicationContext().unregisterReceiver(wifiStateReceiver);
        } catch (Exception e) {
            wifiLog("Error unregistering Wifi State Listener because may be it was never registered");
        }
    }

    WifiStateListener getWifiStateListener() {
        return wifiStateListener;
    }


//    public WifiConnector registerWifiConnectionListener(ConnectionResultListener connectionResultListener) {
//        createWifiConnectionBroadcastListener();
//        this.connectionResultListener = connectionResultListener;
//        return this;
//    }


    public synchronized void unregisterWifiConnectionListener() {
        try {
            context.getApplicationContext().unregisterReceiver(this.wifiConnectionReceiver);
        } catch (Exception e) {
            wifiLog("Error unregistering Wifi Connection Listener because may be it was never registered");
        }
    }

    /**
     * @return the current {@link #connectionResultListener} object
     */
    ConnectionResultListener getConnectionResultListener() {
        return connectionResultListener;
    }


//    public WifiConnector registerShowWifiListListener(ShowWifiListener showWifiListener) {
//        createShowWifiListBroadcastListener();
//        this.showWifiListListener = showWifiListener;
//        return this;
//    }


    public synchronized void unregisterShowWifiListListener() {
        try {
            this.showWifiListListener = null;
            context.getApplicationContext().unregisterReceiver(showWifiListReceiver);
        } catch (Exception e) {
            wifiLog("Error unregistering Wifi List Listener because may be it was never registered ");
        }
    }

    ShowWifiListener getShowWifiListListener() {
        return showWifiListListener;
    }


//    public WifiConnector registerWifiRemoveListener(RemoveWifiListener removeWifiListener) {
//        createWifiStateBroadcast();
//        this.removeWifiListener = removeWifiListener;
//        return this;
//    }

//    public synchronized void unregisterWifiRemoveListener() {
//        try {
//            context.getApplicationContext().unregisterReceiver(wifiStateReceiver);
//        } catch (Exception e) {
//            wifiLog("Error unregistering Wifi Remove Listener because may be it was never registered");
//        }
//    }

//    public synchronized void unregisterReceivers(Object... broadcastReceivers) {
//        wifiLog("Unregistering wifi listener(s)");
//        for (int i = 0; i < broadcastReceivers.length; i++) {
//
//            try {
//                context.getApplicationContext().unregisterReceiver((BroadcastReceiver) broadcastReceivers[i]);
//            } catch (Exception e) {
//                wifiLog("Error unregistering broadcast " + i + " because may be it was never registered");
//            }
//
//        }
//    }

    // endregion

    public WifiConnector enableWifi() {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            wifiLog("Wifi enabled, determining current connected wifi network if there is one...");
            setInitWifiInformation();
        } else {
            wifiLog("Wifi is already enable...");
        }
        return this;
    }

    /**
     * Just for having information about a current network if connected when wifi is enabled.
     */
    private void setInitWifiInformation() {
        connectionResultListener = new ConnectionResultListener() {
            @Override
            public void successfulConnect(String SSID) {

            }

            @Override
            public void errorConnect(int codeReason) {

            }

            @Override
            public void onStateChange(SupplicantState supplicantState) {

            }
        };
        createWifiConnectionBroadcastListener();
    }

    private void createWifiStateBroadcast() {
        wifiStateFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        wifiStateReceiver = new WifiStateReceiver(this);
        try {
            context.getApplicationContext().registerReceiver(wifiStateReceiver, wifiStateFilter);
        } catch (Exception e) {
            wifiLog("Exception on registering broadcast for listening Wifi State: " + e.toString());
        }
    }

    /**
     * For disabling wifi
     * If you want to listen wifi states, should call {@link #unregisterWifiStateListener()} and wait for
     * callback to update User Interface on your application
     */
    public void disableWifi() {
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        } else {
            wifiLog("Wifi is not enable...");
        }
    }

    /**
     * For knowing if wifi is enabled
     *
     * @return true if wifi is enabled
     */
    public boolean isWifiEnbled() {
        return wifiManager.isWifiEnabled();
    }

    private void setCurrentWifiInfo() {
        setCurrentWifiSSID(wifiManager.getConnectionInfo().getSSID());
        setCurrentWifiBSSID(wifiManager.getConnectionInfo().getBSSID());
    }

    public String getCurrentWifiSSID() {
        return currentWifiSSID;
    }

    public void setCurrentWifiSSID(String currentWifiSSID) {
        this.currentWifiSSID = currentWifiSSID;
        CURRENT_WIFI = getCurrentWifiSSID();
    }

    public String getCurrentWifiBSSID() {
        return currentWifiBSSID;
    }

    public void setCurrentWifiBSSID(String currentWifiBSSID) {
        this.currentWifiBSSID = currentWifiBSSID;
    }

    public boolean setPriority(int priority) {
        try {
            this.wifiConfiguration.priority = priority;
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public WifiManager getWifiManager() {
        return wifiManager;
    }

    public void setWifiManager(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    private void createWifiConnectionBroadcastListener() {
        chooseWifiFilter = new IntentFilter();
        chooseWifiFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        wifiConnectionReceiver = new WifiConnectionReceiver(this);
        try {
            context.getApplicationContext().registerReceiver(wifiConnectionReceiver, chooseWifiFilter);
        } catch (Exception e) {
            wifiLog("Register broadcast error (Choose): " + e.toString());
        }
    }

    private void createShowWifiListBroadcastListener() {
        showWifiListFilter = new IntentFilter();
        showWifiListFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        showWifiListReceiver = new ShowWifiListReceiver(this);
        try {
            context.getApplicationContext().getApplicationContext().registerReceiver(showWifiListReceiver, showWifiListFilter);
        } catch (Exception e) {
            wifiLog("Register broadcast error (ShowWifi): " + e.toString());
        }
    }

    /**
     * Tries to scan available wifi networks. After {@link ShowWifiListReceiver} gets results (or not), it automatically  unregister the
     * current broadcast listener so is not necessary to explicitly call {@link #unregisterShowWifiListListener()}.
     *
     * @param showWifiListener interface that will give the results
     */
    public void showWifiList(ShowWifiListener showWifiListener) {
        this.showWifiListListener = showWifiListener;
        wifiLog("show wifi list");
        createShowWifiListBroadcastListener();
        scanWifiNetworks();
    }

    private void scanWifiNetworks() {
        wifiManager.startScan();
    }

    public boolean isAlreadyConnected(String BSSID) {
        ConnectivityManager connManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getActiveNetworkInfo();

        wifiLog("isAlreadyConnected: " + wifiManager.getConnectionInfo().getBSSID() + " " + BSSID);

        if (mWifi != null && mWifi.getType() == ConnectivityManager.TYPE_WIFI && mWifi.isConnected()) {
            isConnectedToBSSID(BSSID);
        } else {
            wifiLog("getActiveNetwork - NetworkInfo is null");
        }
        return false;
    }

    public boolean isConnectedToBSSID(String BSSID) {
        if (wifiManager.getConnectionInfo().getBSSID() != null &&
                wifiManager.getConnectionInfo().getBSSID().equals(BSSID)) {
            wifiLog("Already connected to: " + wifiManager.getConnectionInfo().getSSID() +
                    "  BSSID: " + wifiManager.getConnectionInfo().getBSSID() + "  " + BSSID);
            return true;
        }
        return false;
    }

    /**
     * Tries to connect to specific wifi set on the constructor.
     *
     * @param connectionResultListener with methods of success and error
     */
    public void connectToWifi(ConnectionResultListener connectionResultListener) {
        this.connectionResultListener = connectionResultListener;
        if (isConnectedToBSSID(wifiConfiguration.BSSID)) {
            connectionResultListener.errorConnect(SAME_NETWORK);
        } else {
            if (wifiManager.getConnectionInfo().getBSSID() != null) {
                setCurrentWifiSSID(wifiManager.getConnectionInfo().getSSID());
                setCurrentWifiBSSID(wifiManager.getConnectionInfo().getBSSID());
                wifiLog("Already connected to: " + wifiManager.getConnectionInfo().getSSID() + " " +
                        "Now trying to connect to " + wifiConfiguration.SSID);

            }
            connectToWifi();
        }
    }

    public void setConnectionResultListener(ConnectionResultListener connectionResultListener) {
        this.connectionResultListener = connectionResultListener;
        createWifiConnectionBroadcastListener();
    }

    /**
     * Tries to connect to specific wifi network set on the constructor.
     * <strong>Remember: you must be register {@link #connectionResultListener} object before.</strong>
     */
    public void connectToWifi() {
        if (isConnectedToBSSID(wifiConfiguration.BSSID)) {
            connectionResultListener.errorConnect(SAME_NETWORK);
        } else {
            if (wifiManager.getConnectionInfo().getBSSID() != null) {
                setCurrentWifiSSID(wifiManager.getConnectionInfo().getSSID());
                setCurrentWifiBSSID(wifiManager.getConnectionInfo().getBSSID());
                wifiLog("Already connected to: " + wifiManager.getConnectionInfo().getSSID() + " " +
                        "Now trying to connect to " + wifiConfiguration.SSID);
            }
            connectToWifiAccesPoint();
        }
    }

    /**
     * Allows to connect to specific Wifi access point.
     * <ul><li>Tries to get network id</li></ul>
     * <ul><li>if network id = -1 add network configuration</li></ul>
     *
     * @return boolean value if connection was successfully completed
     */
    private boolean connectToWifiAccesPoint() {
        createWifiConnectionBroadcastListener();
        int networkId = getNetworkId(wifiConfiguration.SSID);
        wifiLog("network id found: " + networkId);
        if (networkId == -1) {
            networkId = wifiManager.addNetwork(wifiConfiguration);
            wifiLog("networkId now: " + networkId);
        }
        return enableNetwork(networkId);
    }

    /**
     * Search network id by given SSID.
     * If network is totally new, it returns -1.
     *
     * @param SSID name of wifi network
     * @return wifi network id
     */
    private int getNetworkId(String SSID) {
        confList = wifiManager.getConfiguredNetworks();
        if (confList != null && confList.size() > 0) {
            for (WifiConfiguration existingConfig : confList) {
                if (trimQuotes(existingConfig.SSID).equals(trimQuotes(SSID))) {
                    return existingConfig.networkId;
                }
            }
        }
        return -1;
    }

    private boolean enableNetwork(int networkId) {
        if (networkId == -1) {
            wifiLog("So networkId still -1, there was an error... may be authentication?");
            connectionResultListener.errorConnect(AUTHENTICATION_ERROR);
            unregisterWifiConnectionListener();
            return false;
        }
        return connectWifiManager(networkId);
    }

    private boolean connectWifiManager(int networkId) {
        wifiManager.disconnect();
        return wifiManager.enableNetwork(networkId, true);
    }

    public static String ssidFormat(String str) {
        if (!str.isEmpty()) {
            return "\"" + str + "\"";
        }
        return str;
    }

    private static String trimQuotes(String str) {
        if (!str.isEmpty()) {
            return str.replaceAll("^\"*", "").replaceAll("\"*$", "");
        }
        return str;
    }

    public void removeCurrentWifiNetwork(RemoveWifiListener removeWifiListener) {
        this.removeWifiListener = removeWifiListener;
        removeWifiNetwork(getCurrentWifiSSID(), getCurrentWifiBSSID());
    }

    public void removeWifiNetwork(WifiConfiguration wifiConfiguration, RemoveWifiListener removeWifiListener) {
        this.removeWifiListener = removeWifiListener;
        removeWifiNetwork(wifiConfiguration.SSID, wifiConfiguration.BSSID);
    }

    public void removeWifiNetwork(String SSID, String BSSID, RemoveWifiListener removeWifiListener) {
        this.removeWifiListener = removeWifiListener;
        removeWifiNetwork(SSID, BSSID);
    }

    public void removeWifiNetwork(ScanResult scanResult, RemoveWifiListener removeWifiListener) {
        this.removeWifiListener = removeWifiListener;
        removeWifiNetwork(scanResult.SSID, scanResult.BSSID);
    }

    // TODO show reason to remove failure!
    public void removeWifiNetwork(String SSID, String BSSID) {
        Log.d("info","SSID :- "+SSID+"cureent"+getCurrentWifiSSID());
        List<WifiConfiguration> list1 = wifiManager.getConfiguredNetworks();
        if (list1 != null && list1.size() > 0) {
            Log.d("info","SSID :- "+SSID+"cureent"+getCurrentWifiSSID());

            for (WifiConfiguration i : list1) {
                try {
                    Log.d("info","SSID :- "+i.SSID+"cureent");

                        wifiManager.removeNetwork(wifiManager.getConnectionInfo().getNetworkId());
                        wifiManager.saveConfiguration();

                } catch (NullPointerException e) {
                    wifiLog("Exception on removing wifi network: " + e.toString());
                }
            }
        } else {
            wifiLog("Empty Wifi List");
            removeWifiListener.onWifiNetworkRemoveError();
        }
    }

    /**
     * ForgetNetwork is a method that will only works if app is signed and run as system.
     * It will look for "forget" hidden method on WifiManager class.
     *
     * @param wifiManager current wifiManager
     * @param i           the wifiConfigured network to delete
     * @hide
     */
    public void forgetWifiNetwork(WifiManager wifiManager, WifiConfiguration i) {
        try {
            Method[] methods = wifiManager.getClass().getDeclaredMethods();
            Method forgetMEthod = null;
            for (Method method : methods) {
                if (method.getName().contains("forget")) {
                    forgetMEthod = method;
                    forgetMEthod.invoke(wifiManager, i.networkId, null);
                    wifiLog("Forgotten network " + i.SSID);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            wifiLog("Exception: " + e.toString());
        }
    }

    /**
     * Similar to {@link #forgetWifiNetwork(WifiManager, WifiConfiguration)} but this will run with any application
     * installed as user app and will only delete wifi configurations created by its own.
     *
     * @return true if delete configuration was successful
     */
    boolean deleteWifiConf() {
        try {
            confList = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration i : confList) {
                if (i.SSID != null && i.SSID.equals(ssidFormat(wifiConfiguration.SSID))) {
                    wifiLog("Deleting wifi configuration: " + i.SSID);
                    wifiManager.removeNetwork(i.networkId);
                    return wifiManager.saveConfiguration();
                }
            }
        } catch (Exception ignored) {
            return false;
        }
        return false;
    }

    public void setLog(boolean log) {
        this.logOrNot = log;
    }

    public boolean isLogOrNot() {
        return logOrNot;
    }

    public static String getWifiSecurityType(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("WPA")) {
            return SECURITY_WPA;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }

    private void wifiLog(String text) {
        if (logOrNot) Log.d(TAG, "WifiConnector: " + text);
    }

}
