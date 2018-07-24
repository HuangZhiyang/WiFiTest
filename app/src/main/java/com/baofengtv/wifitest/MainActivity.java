package com.baofengtv.wifitest;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
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
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "WiFiTest";
    private Button btnPlay;
    private Button btnStartIperf;
    private Button btnStopPlay;
    private TextView tvWiFiInfo;
    private ConnectivityManager mConnectivityManager;
    private WifiManager mWifiManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        initViews();
        initListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWiFiInfo();
        testNetworkSpeed();
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
        if(Build.VERSION.SDK_INT >= 23) {
            Network network = mConnectivityManager.getActiveNetwork();
            //LinkProperties linkProperties = mConnectivityManager.getLinkProperties(network);
            //linkProperties.getDnsServers();
            //linkProperties.getRoutes();
            //linkProperties.getLinkAddresses();
        }
        if(networkInfo!=null &&  networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {

            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            tvWiFiInfo.append("当前网络连接类型：WiFi \n");

            //network ID
            int networkId =  wifiInfo.getNetworkId();
            tvWiFiInfo.append("当前NetworkID:"+ networkId + "\n");
            //热点SSID信息
            tvWiFiInfo.append("连接热点:" + wifiInfo.getSSID() +  "\n MAC地址：" + wifiInfo.getBSSID() + "\n");
            //热点加密类型
            tvWiFiInfo.append("加密类型:"+ getWifiSecureType(networkId)+"\n");
            //TODO 获取路由器型号

            //获取Ip地址
            int ipAddr = wifiInfo.getIpAddress();

            tvWiFiInfo.append("IP地址:" + getIpString(ipAddr) + "\n");

           // LinkProperties =
            //获取网关地址
            DhcpInfo  dhcpInfo = mWifiManager.getDhcpInfo();
            tvWiFiInfo.append("网关地址:"+ getIpString(dhcpInfo.gateway) + "\n");
            tvWiFiInfo.append("" + getIpString(dhcpInfo.netmask) + "\n");
            tvWiFiInfo.append("" + getIpString(dhcpInfo.dns1) + "\n" );
            tvWiFiInfo.append("" + getIpString(dhcpInfo.dns2) + "\n" );
            tvWiFiInfo.append("" + dhcpInfo.leaseDuration + "\n" );
            tvWiFiInfo.append("" + getIpString(dhcpInfo.ipAddress) + "\n");
            //TODO 判断当前是静态IP还是动态获取IP 如果是静态IP的获取方式时，怎么处理？


            //int gateway =
            //链路层连接信息
            if(Build.VERSION.SDK_INT >= 21) {
                tvWiFiInfo.append("工作频率:" + wifiInfo.getFrequency() + "\n");
                tvWiFiInfo.append("信道"+ getWiFiFreqChannel(wifiInfo.getFrequency()) + "\n");
            }
            int linkSpeed = wifiInfo.getLinkSpeed();
            tvWiFiInfo.append("链接速率:" +  linkSpeed + "Mbps\n");
           //TODO 获取MCS和工作于HT40还是HT20;
            tvWiFiInfo.append("工作模式" + getWiFiHTMode(linkSpeed)+ "\n");
            tvWiFiInfo.append("RSSI：" + wifiInfo.getRssi() +"\n");

            //物理层信息：
            //WiFi MAC地址
             tvWiFiInfo.append("WiFi MAC地址：" + wifiInfo.getMacAddress() + "\n");
            //TODO WIFI模块型号

        }else{
            //WiFi此时未连接上
            //TODO 判断当前网络类型
            //TODO 当前WiFi是否已经打开
        }
        //TODO 获取附近热点信息

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
    private static final float[][]MCS={
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
            {57.8f,}
            {86.7f,}
            {115.6f,}
            {130.0f,}
            {144.4f,}

            {21.7f,}
            {43.3f,}
            {65.0f,}
            {86.7f,}
            {130.0f,}
            {173.3f,}
            {195.0f,}
            {216.7f,}


    };*/

    private String getWiFiHTMode(int speed){
        return "";
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

}
