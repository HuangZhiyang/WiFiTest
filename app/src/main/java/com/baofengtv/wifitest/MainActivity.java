package com.baofengtv.wifitest;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "WiFiTest";
    private Button btnPlay;
    private Button btnStartIperf;
    private Button btnStopPlay;
    private TextView tvWiFiInfo;
    private ConnectivityManager mConnectivityManager;
    private WifiManager mWifiManager;
    private PackageManager mPackageManager;
    private ActivityManager mActivityManager;
    private UsbManager mUsbManager;
    private boolean bScan=false;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        mActivityManager = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
        mPackageManager = getPackageManager();
        mUsbManager = (UsbManager)mContext.getSystemService(Context.USB_SERVICE);
        initViews();
        initListeners();
        initReceivers();

    }

    @Override
    protected void onResume() {
        super.onResume();
        getWiFiInfo();
        testNetworkSpeed();
    }

    private void initReceivers(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()){
                    case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                        List<ScanResult> scanResults = mWifiManager.getScanResults();
                       Log.e(TAG,"接收到WiFi 热点扫描结果" + scanResults.size());
                        for(int i =0 ;i< scanResults.size();i++) {
                            /*wifiInfoAppendLine(
                                    scanResults.get(i).SSID
                                            + scanResults.get(i).level
                                            + scanResults.get(i).channelWidth
                                            + scanResults.get(i).centerFreq0
                                            + scanResults.get(i).centerFreq1
                                            + scanResults.get(i).capabilities
                                            // + scanResults.get(i).operatorFriendlyName
                                            + scanResults.get(i).BSSID
                                            + scanResults.get(i).frequency
                            );*/
                            Log.e(TAG, scanResults.get(i).toString());
                        }
                        break;
                }

            }
        },intentFilter);
    }

    private void testNetworkSpeed(){
        //TODO 完成网络测速信息
        //TODO 局域网ping延时
        //TODO 互联网ping延时   百度、爱奇艺、暴风、讯飞、腾讯
    }


    private void getWiFiInfo(){

        tvWiFiInfo.setText("");
        //当前连接网络类型
        NetworkInfo networkInfo= mConnectivityManager.getActiveNetworkInfo();
        /*
        if(Build.VERSION.SDK_INT >= 23) {
            Network network = mConnectivityManager.getActiveNetwork();
            //LinkProperties linkProperties = mConnectivityManager.getLinkProperties(network);
            //linkProperties.getDnsServers();
            //linkProperties.getRoutes();
            //linkProperties.getLinkAddresses();
        }*/
        if(networkInfo!=null &&  networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                getConnectedWifiInfo();
        }else{
            //WiFi此时未连接上
            if(networkInfo!=null){
                switch (networkInfo.getType()){
                    case ConnectivityManager.TYPE_ETHERNET:
                        wifiInfoAppendLine("当前使用的网络类型为:有线网");
                        break;
                    case ConnectivityManager.TYPE_MOBILE:
                    case ConnectivityManager.TYPE_MOBILE_DUN:
                        wifiInfoAppendLine("当前使用的网络类型为:移动数据");
                        break;
                        default:
                            break;
                }
            }

            //TODO 判断一些
            //获取USB设备信息
            wifiInfoAppendLine("WiFi模块:" + getWifiModuleType());
            //TODO 当前WiFi是否已经打开
            int wifistate =mWifiManager.getWifiState();
            switch (wifistate){
                //当前WiFi已经打开
                case WifiManager.WIFI_STATE_ENABLED:
                    wifiInfoAppendLine("当前WiFi已经打开");
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    wifiInfoAppendLine("当前WiFi正在打开");
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    wifiInfoAppendLine("当前WiFi已经关闭");
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    wifiInfoAppendLine("当前WiFi正在关闭");
                    break;
            }
            if(wifistate == WifiManager.WIFI_STATE_ENABLED){
                wifiInfoAppendLine("开始扫描附近WiFi热点");
                bScan=true;
                mWifiManager.startScan();
            }

        }
        //TODO 获取附近热点信息

    }

    private void getConnectedWifiInfo(){

        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        wifiInfoAppendLine("当前网络连接类型：WiFi");
        //network ID
        int networkId =  wifiInfo.getNetworkId();
        wifiInfoAppendLine("当前NetworkID:"+ networkId);
        //热点SSID信息
        wifiInfoAppendLine("连接热点:" + wifiInfo.getSSID() +  "\n MAC地址：" + wifiInfo.getBSSID());
        //热点加密类型
        wifiInfoAppendLine("加密类型:"+ getWifiSecureType(networkId));
        //TODO 获取路由器型号

        //获取Ip地址
        int ipAddr = wifiInfo.getIpAddress();

        wifiInfoAppendLine("IP地址:" + getIpString(ipAddr));
        // LinkProperties =
        //获取网关地址
        DhcpInfo  dhcpInfo = mWifiManager.getDhcpInfo();
        wifiInfoAppendLine("网关地址:"+ getIpString(dhcpInfo.gateway));
        wifiInfoAppendLine("子网掩码：" + getIpString(dhcpInfo.netmask));
        wifiInfoAppendLine("DNS1:" + getIpString(dhcpInfo.dns1));
        wifiInfoAppendLine("DNS2:" + getIpString(dhcpInfo.dns2));
        wifiInfoAppendLine("租约时间:" + dhcpInfo.leaseDuration);
        wifiInfoAppendLine("IP地址：" + getIpString(dhcpInfo.ipAddress));
        //TODO 判断当前是静态IP还是动态获取IP 如果是静态IP的获取方式时，怎么处理？

        //int gateway =
        //链路层连接信息
        if(Build.VERSION.SDK_INT >= 21) {
            wifiInfoAppendLine("工作频率:" + wifiInfo.getFrequency());
            wifiInfoAppendLine("信道"+ getWiFiFreqChannel(wifiInfo.getFrequency()));
        }
        int linkSpeed = wifiInfo.getLinkSpeed();
        wifiInfoAppendLine("链接速率:" +  linkSpeed + "Mbps");
        //TODO 获取MCS和工作于HT40还是HT20;
        wifiInfoAppendLine("工作模式:" + getWiFiHTMode(linkSpeed));
        wifiInfoAppendLine("RSSI：" + wifiInfo.getRssi());
        //物理层信息：
        //WiFi MAC地址
        //wifiInfoAppendLine("WiFi MAC地址：" + wifiInfo.getMacAddress());
        //TODO WIFI模块型号
        wifiInfoAppendLine("WiFi模块:" + getWifiModuleType());

        mWifiManager.startScan();
    }


    private String  getWifiModuleType(){
        String wifiModuleType="";

        HashMap<String,UsbDevice> usbDeviceHashMap  = mUsbManager.getDeviceList();
        if(usbDeviceHashMap != null ){
            Iterator<UsbDevice> it = usbDeviceHashMap.values().iterator();
            while(it.hasNext()){
                UsbDevice device = (UsbDevice)it.next();
                switch (device.getVendorId()){
                    case 0x0bda: {
                            int productId = device.getProductId();
                            if(productId == 0x818c ){
                                wifiModuleType += "RTL8192EU + RTW8761AW ";
                            }  else if (productId == 0x818b) {
                                wifiModuleType += "RTL8192EU ";
                            }  else if (productId == 0x818b ||  productId == 0x0179 ) {
                                wifiModuleType += "RTL8188ETV ";
                            } else{
                                wifiModuleType += "RTK WIFI ";
                            }
                        }
                        break;
                    case  0x0e8d:{
                            wifiModuleType += "MTK WIFI ";
                        }
                        break;
                    case 0x0a5c: {
                        wifiModuleType += "BROADCOM WIFI ";
                    }
                        break;
                    default:
                        break;
                }
            }
        }
        return wifiModuleType;
    }

    private String getIpString(int ipAddr) {
        StringBuffer ipBuf = new StringBuffer();
        ipBuf.append(ipAddr & 0xff).append('.').
                append((ipAddr >>>= 8) & 0xff).append('.').
                append((ipAddr >>>= 8) & 0xff).append('.').
                append((ipAddr >>>= 8) & 0xff);

        return ipBuf.toString();
    }

    private String getWifiSecureType(int networkId){
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
            for(WifiConfiguration config:configs){
                if(config.networkId ==  networkId){
                   // tvWiFiInfo.append("config.BSSID" + config.BSSID + '\n');
                  //  tvWiFiInfo.append("config.SSID" + config.SSID + '\n');
                    return getSecurity(config);
                }
            }
            return "";
    }

    String getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            return "PSK";
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) ||
                config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            return "EAP";
        }
        return (config.wepKeys[0] != null) ? "WEP" : "NONE";
    }

    private int getWiFiFreqChannel(int frequecy){
       return frequecy > 5000 ? (frequecy-5000)/5 : (frequecy-2407)/5;
    };


/*
    private static final float[][]HT20_MCS_80211N = new float[][] {
            {6.5f, 7.2f},
            {13.0f, 14.4f},
            {19.5f, 21.7f},
            {26.0f, 28.9f},
            {39.9f, 43.3f},
            {52.0f, 57.8f},
            {58.5f, 65.0f},
            {65.0f, 72.2f},

            {13.0f, 14.4f},
            {26.0f, 28.9f},
            {39.0f, 43.3f},
            {52.0f, 57.8f},
            {78.0f, 86.7f},
            {104.0f, 115.6f},
            {117.0f, 130.0f},
            {130.0f, 144.4f},
    };

    private static final float[][]HT40_MCS_80211N = new float[][] {
            {13.5f, 15.0f},
            {27.0f, 30.0f},
            {40.5f, 45.0f},
            {54.0f, 60.0f},
            {81.0f, 90.0f},
            {108.0f, 120.0f},
            {121.5f, 135.0f},
            {135.0f, 150,0f},

            {27.0f, 30.0f},
            {13.0f, 14.4f},
            {54.0f, 60.0f},
            {81.0f, 90.0f},
            {108.0f, 120.0f},
            {162.0f, 180.0f},
            {216.0f, 240.0f},
            {243.0f, 270.0f},
            {270.0f, 300.0f},
    };
*/
    private static final float[][]HT20_MCS_80211AC = new float[][] {
            {6.5f, 7.2f},
            {13.0f, 14.4f},
            {19.5f, 21.7f},
            {26.0f, 28.9f},
            {39.9f, 43.3f},
            {52.0f, 57.8f},
            {58.5f, 65.0f},
            {65.0f, 72.2f},

            {13.0f, 14.4f},
            {26.0f, 28.9f},
            {39.0f, 43.3f},
            {52.0f, 57.8f},
            {78.0f, 86.7f}, //2x2 16QAM 3/4码率,  1x1 256QAM 3/4码率
            {104.0f, 115.6f},
            {117.0f, 130.0f},
            {130.0f, 144.4f},
            {156.0f, 173.4f},//2x2  256QAM 3/4码率
    };

    private static final float[][]HT40_MCS_80211AC = new float[][] {
            {13.5f, 15.0f},
            {27.0f, 30.0f},
            {40.5f, 45.0f},
            {54.0f, 60.0f},
            {81.0f, 90.0f},
            {108.0f, 120.0f},
            {121.5f, 135.0f},
            {135.0f, 150,0f},
            {162.0f, 180.0f},//256QAM 3/4
            {180.0f, 200.0f},//256QAM 5/6

            {27.0f, 30.0f},
            {13.0f, 14.4f},
            {54.0f, 60.0f},
            {81.0f, 90.0f},
            {108.0f, 120.0f},
            {162.0f, 180.0f},
            {216.0f, 240.0f},
            {243.0f, 270.0f},
            {270.0f, 300.0f},
            {324.0f, 360.0f}, //256QAM 3/4
            {360.0f, 400.0f},//256QAM  5/6
    };

    private static final float[][]HT80_MCS_80211AC = new float[][] {
            {29.3f, 32.5f},
            {58.5f, 65.0f},
            {87.8f, 97.5f},
            {117.0f,130.0f},
            {175.5f, 195.0f},
            {234.0f, 260.0f},
            {263.3f, 292.5f},
            {292.5f, 325,0f},
            {351.0f, 390.0f},//256QAM 3/4
            {390.0f, 433.3f},//256QAM 5/6

            {58.6f, 65.0f},
            {117.0f, 130.0f},
            {175.6f, 195.0f},
            {234.0f, 260.0f},
            {351.0f, 390.0f},
            {468.0f, 520.0f},
            {526.6f, 585.0f},
            {585.0f, 650.0f},
            {702.0f, 780.0f},  //256QAM 3/4
            {780.0f, 866.6f},  //256QAM 5/6
    };



    private String getWiFiHTMode(int speed){
        int i;
        int j;
        String mode="";
        for(i=0;i<HT20_MCS_80211AC.length;i++){
            for( j=0;j<2;j++){
                if(speed == (int)HT20_MCS_80211AC[i][j]){
                    mode = mode + "HT20_"+ i + "_"+(j==0? "GI800ns":"GI400ns" )+" ";
                }
            }
        }
        for(i=0;i<HT40_MCS_80211AC.length;i++){
            for( j=0;j<2;j++){
                if(speed == (int)HT40_MCS_80211AC[i][j]){
                    mode = mode + "HT20_"+ i + "_"+ (j==0? "GI800ns":"GI400ns" )+" ";
                }
            }
        }

        for(i=0;i<HT80_MCS_80211AC.length;i++){
            for( j=0;j<2;j++){
                if(speed == (int)HT80_MCS_80211AC[i][j]){
                    mode = mode + "HT80_"+ i + "_"+(j==0? "GI800ns":"GI400ns" )+" ";
                }
            }
        }
        return mode;
    }

    private final void pingIpAddr(String ipaddress) {
        try {
            // This is hardcoded IP addr. This is for testing purposes.
            // We would need to get rid of this before release.
            String ipAddress = "74.125.47.104";
            Process p = Runtime.getRuntime().exec("ping -c 1 " + ipAddress);
            int status = p.waitFor();
            if (status == 0) {
               // mPingIpAddrResult = "Pass";
            } else {
             //   mPingIpAddrResult = "Fail: IP addr not reachable";
            }
        } catch (IOException e) {
            //mPingIpAddrResult = "Fail: IOException";
        } catch (InterruptedException e) {
           // mPingIpAddrResult = "Fail: InterruptedException";
        }
    }

    private final void pingHostname(String hostName) {
        try {
            Process p = Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int status = p.waitFor();
            if (status == 0) {
              //  mPingHostnameResult = "Pass";
            } else {
              //  mPingHostnameResult = "Fail: Host unreachable";
            }
        } catch (UnknownHostException e) {
           // mPingHostnameResult = "Fail: Unknown Host";
        } catch (IOException e) {
          //  mPingHostnameResult= "Fail: IOException";
        } catch (InterruptedException e) {
          //  mPingHostnameResult = "Fail: InterruptedException";
        }
    }


    private void  initViews(){
        btnPlay = findViewById(R.id.button_play);
        btnStartIperf = findViewById(R.id.button_start_iperf);
        btnStopPlay = findViewById(R.id.button_stop);
        tvWiFiInfo = findViewById(R.id.textWiFiInfo);
    }

    private void initListeners(){
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, MusicService.class);
                startService(intent);
            }
        });

        btnStopPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,MusicService.class);
                stopService(intent);
            }
        });


        btnStartIperf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent();
                   intent.setClassName("com.magicandroidapps.iperf","com.magicandroidapps.iperf.iperf");
                    //ComponentName componentName = new ComponentName("com.magicandroidapps.iperf","com.magicandroidapps.iperf.iperf");
                    //intent.setComponent(componentName);
                   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }catch (Exception e){
                    Log.e(TAG,e.toString());
                }
            }
        });


    }

    private void wifiInfoAppendLine(String str){
        tvWiFiInfo.append(str + "\n");
    }
}
