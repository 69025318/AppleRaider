package com.zhh;

import org.apache.commons.lang3.StringUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsReciver extends BroadcastReceiver {

    private static final String TAG = "SmsReciver";

    @Override
    public void onReceive(Context context, Intent intent) {
    	SharedPreferences sp = context.getSharedPreferences("AppleRaider", Context.MODE_PRIVATE);
    	String targetPhoneStr = sp.getString("targetPhone", Content.targetPhoneDefault);
    	Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        String phone;
        String message;
                
        if(bundle != null){
            Object[] pdus = (Object[])bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            for(int i = 0; i < msgs.length; i++){
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                phone = msgs[i].getOriginatingAddress();
                if(StringUtils.equals(targetPhoneStr, phone)){
                    message = msgs[i].getMessageBody();
                }
                
            }
        }
    }
}
