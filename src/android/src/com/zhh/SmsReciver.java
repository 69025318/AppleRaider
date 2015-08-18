package com.zhh;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsReciver extends BroadcastReceiver {

    private static final String TAG = "SmsReciver";


    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Object[] object = (Object[]) bundle.get("pdus");
        SmsMessage sms[] = new SmsMessage[object.length];
        String content="";
        for (int i = 0; i < object.length; i++) {
            sms[0] = SmsMessage.createFromPdu((byte[]) object[i]);
//            Log.e(TAG, "来自" + sms[i].getDisplayOriginatingAddress() + " 的消息是：" + sms[i].getDisplayMessageBody());
             content= sms[i].getDisplayMessageBody();
        }

        for (int j = 0; j < content.length(); j++) {
            if (content.charAt(j) > 47 && content.charAt(j) < 58 &&
                    (content.length() > j + 5) && (content.charAt(j + 1) > 47 &&
                    content.charAt(j + 1) < 58) && (content.charAt(j + 2) > 47 &&
                    content.charAt(j + 2) < 58) && (content.charAt(j + 3) > 47 &&
                    content.charAt(j + 3) < 58) && (content.charAt(j + 4) > 47 &&
                    content.charAt(j + 4) < 58) && (content.charAt(j + 5) > 47 &&
                    content.charAt(j + 5) < 58)) {
                /*该判定只对六位数字的验证码有效*/
                Intent intent1=new Intent("receiverVerifyCode");
                intent1.putExtra("verifyCode",content.substring(j-1,j+5));
                context.sendBroadcast(intent1);
            }
        }
        //终止广播，在这里我们可以稍微处理，根据用户输入的号码可以实现短信防火墙。
//        abortBroadcast();
    }
}
