package com.example.traz.systemstatus;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.widget.TextView;


public class Siglister extends PhoneStateListener  {
    Siglister(TextView but)
    {
        button=but;
    }
    public int signalStrengthValue;
    TextView button;

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
        if (signalStrength.isGsm()) {
            if (signalStrength.getGsmSignalStrength() != 99)
                signalStrengthValue = signalStrength.getGsmSignalStrength() * 2 - 113;
            else
                signalStrengthValue = signalStrength.getGsmSignalStrength();
        } else {
            signalStrengthValue = signalStrength.getCdmaDbm();
        }
        button.setText(String.valueOf(signalStrengthValue)+"dBm");
    }
}

