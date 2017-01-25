package com.example.traz.systemstatus;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class Wireless extends AppCompatActivity {


    Button wifistteing;
    TextView wifiname,wifispeed;

    Button voicesetting;
    TextView voicemain, voiceg,voicestr;

    Button bluesetting;
    TextView blueconnect,bluetype;

    TextView simmain,simstatus,simIMEI;

    Button back,refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wireless);

        //Set title
        setTitle("Wireless Info");


        //start declarations and onclick listeners
        wifistteing=(Button) findViewById(R.id.wifisettings);
        wifiname=(TextView) findViewById(R.id.wifissid);
        wifispeed=(TextView) findViewById(R.id.wifistrength);

        wifistteing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                startActivity(intent);
                doshit();
            }
        });

        voicesetting=(Button) findViewById(R.id.voicebutton);
        voiceg=(TextView) findViewById(R.id.VoiceGen);
        voicestr=(TextView) findViewById(R.id.voiceStr);

        voicesetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings$DataUsageSummaryActivity"));
                startActivity(intent);
                doshit();
            }
        });
        bluesetting=(Button) findViewById(R.id.BluetoothSettings);
        blueconnect=(TextView) findViewById(R.id.BluetoothConnectivity);
        bluetype=(TextView) findViewById(R.id.DevicePaired);
        bluetype.setVisibility(View.INVISIBLE);

        bluesetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
                doshit();
            }
        });

        simstatus=(TextView) findViewById(R.id.simStatus);
        simIMEI=(TextView) findViewById(R.id.SimIMEI);


        voicemain=(TextView) findViewById(R.id.Connection);
        simmain=(TextView) findViewById(R.id.SimMain);


        refresh=(Button) findViewById(R.id.Refresher);
        back=(Button) findViewById(R.id.BackFromWireless);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doshit();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        //end declerations
        //go to main thing
        doshit();


    }
    private void doshit() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(wifi.isConnectedOrConnecting())
        {
            WifiManager wifiManager = (WifiManager) getSystemService (Context.WIFI_SERVICE);
            WifiInfo info = wifiManager.getConnectionInfo();
            wifiname.setText(info.getSSID());
            wifiname.setTextColor(Color.BLACK);
            wifispeed.setVisibility(View.VISIBLE);
            if(wifi.isConnected())
            {
                wifispeed.setText(String.format("%d MBps",info.getLinkSpeed()));
                wifispeed.setTextColor(Color.BLACK);

            }
            else
            {
                wifispeed.setText("Connecting");
                wifispeed.setTextColor(Color.YELLOW);

            }


        }
        else
        {
            wifiname.setText("DISCONNECTED");
            wifiname.setTextColor(Color.RED);
            wifispeed.setVisibility(View.INVISIBLE);
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            blueconnect.setText("UNSUPPORTED");
            blueconnect.setTextColor(Color.RED);
            bluesetting.setVisibility(View.GONE);

        } else {

            blueconnect.setTextColor(Color.BLACK);
            if (bluetoothAdapter.isEnabled()) {
                blueconnect.setText("Enabled");

            } else {
                blueconnect.setText("Not Enabled");
            }
        }

        NetworkInfo wireless = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        // if has mobile network capabilities
        if (wireless!=null) {
            TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            int connecttype = telephonyManager.getNetworkType();//get primary?
            switch (connecttype) {//jesus christ there are 15 wireless types. fuck me.
                case 1:
                case 2:
                case 4:
                case 7:
                case 11:
                    voiceg.setTextColor(Color.YELLOW);
                    voiceg.setText("2G");
                    voicestr.setVisibility(View.VISIBLE);
                    break;
                case 3:
                case 5:
                case 6:
                case 8:
                case 9:
                case 10:
                case 12:
                case 14:
                case 15:
                    voiceg.setTextColor(Color.BLACK);
                    voiceg.setText("3G");
                    voicestr.setVisibility(View.VISIBLE);
                    break;
                case 13:
                    voiceg.setTextColor(Color.GREEN);
                    voiceg.setText("4G");
                    voicestr.setVisibility(View.VISIBLE);
                    break;

                case 0:
                    voiceg.setText("NONE");
                    voiceg.setTextColor(Color.RED);
                    voicestr.setVisibility(View.INVISIBLE);
                    break;

                default: //you know, in case I missed one
                    voiceg.setText("Unknown");
                    voiceg.setTextColor(Color.BLACK);
                    voicestr.setVisibility(View.VISIBLE);
                    break;
            }
            if (connecttype != 0) {
                //establish strength listener
                //might crash if lose connection while on screem
                Siglister phoneStateListener = new Siglister(voicestr);

                telephonyManager.listen(phoneStateListener,
                        PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            }

            switch (telephonyManager.getSimState())
            {
                case 0:
                    simstatus.setText("Error, try again later");
                    simstatus.setTextColor(Color.RED);
                    simIMEI.setVisibility(View.GONE);
                    break;
                case 1:
                    simstatus.setText("No SIM Detected");
                    simstatus.setTextColor(Color.RED);
                    simIMEI.setVisibility(View.GONE);
                    break;
                case 2:
                case 3:
                case 4:
                    simstatus.setText("SIM Locked");
                    simstatus.setTextColor(Color.YELLOW);
                    simIMEI.setVisibility(View.VISIBLE);
                    if(Build.VERSION.SDK_INT<23)
                        simIMEI.setText(telephonyManager.getDeviceId());
                    else
                        simIMEI.setText("23+ permissions are hard");
                    break;
                case 5:
                    simstatus.setText("OK");
                    simstatus.setTextColor(Color.BLACK);
                    simIMEI.setVisibility(View.VISIBLE);
                    if(Build.VERSION.SDK_INT<23)
                        simIMEI.setText(telephonyManager.getDeviceId());
                    else
                        simIMEI.setText("23+ permissions are hard");
                    break;
                default:
                    simstatus.setText("Error, try again later");
                    simstatus.setTextColor(Color.RED);
                    simIMEI.setVisibility(View.GONE);

            }



        }
        else{
            //set everything invis if not in device with mobile
            //I could probably just hide the layouts

            voicemain.setVisibility(View.INVISIBLE);
            voiceg.setVisibility(View.INVISIBLE);
            voicestr.setVisibility(View.INVISIBLE);
            voicesetting.setVisibility(View.INVISIBLE);

            simmain.setVisibility(View.INVISIBLE);
            simIMEI.setVisibility(View.INVISIBLE);
            simstatus.setVisibility(View.INVISIBLE);





        }



    }


}
