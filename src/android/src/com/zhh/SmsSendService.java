package com.zhh;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

public class SmsSendService extends Service {

	public static volatile boolean overFlag = false;

	public static volatile boolean runFlag = false;

	SharedPreferences sp;
	SmsManager sms;

	Thread thread = new Thread() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (!overFlag) {
				if (runFlag) {
					execute();
				}
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	};

	private final IBinder binder = new LocalBinder();

	public class LocalBinder extends Binder {
		SmsSendService getService() {
			return SmsSendService.this;
		}
	}

	public IBinder onBind(Intent intent) {
		return binder;
	}

	public void execute() {
		String taskStr = getTask();
		if (StringUtils.isNotBlank(taskStr)) {

		}
	}

	public String getTask() {
		String result = null;
		String requestAddressStr = sp.getString("requestAddress", Content.requestAddressDefault);
		String sourcePhoneStr = sp.getString("sourcePhone", "");
		String uri = requestAddressStr + Content.getSendSmsTaskUri;

		HttpPost httpRequst = new HttpPost(uri);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("phone", sourcePhoneStr));
		try {
			httpRequst.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequst);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				HttpEntity httpEntity = httpResponse.getEntity();
				result = EntityUtils.toString(httpEntity);// È¡³öÓ¦´ð×Ö·û´®
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public void sendSms(String phone, String message) {
		PendingIntent sentPI = PendingIntent.getActivity(this, 0, new Intent("sms_sent"), 0);
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Log.i("====>", "Activity.RESULT_OK");
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Log.i("====>", "RESULT_ERROR_GENERIC_FAILURE");
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Log.i("====>", "RESULT_ERROR_NO_SERVICE");
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Log.i("====>", "RESULT_ERROR_NULL_PDU");
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Log.i("====>", "RESULT_ERROR_RADIO_OFF");
					break;
				}
			}
		}, new IntentFilter("sms_sent"));
		PendingIntent deliveredPI = PendingIntent.getActivity(this, 0, new Intent("sms_delivered"), 0);
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Log.i("====>", "RESULT_OK");
					break;
				case Activity.RESULT_CANCELED:
					Log.i("=====>", "RESULT_CANCELED");
					break;
				}
			}
		}, new IntentFilter("sms_delivered"));
		sms.sendTextMessage(phone, null, message, sentPI, deliveredPI);
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		sp = this.getSharedPreferences("AppleRaider", Context.MODE_PRIVATE);
		sms = SmsManager.getDefault();
		thread.start();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		overFlag = true;
		super.onDestroy();
	}

	public void run() {
		runFlag = true;
	}

	public void stop() {
		runFlag = false;
	}

}
