package com.zhh;

import com.zhh.SmsSendService;
import com.zhh.SmsSendService.LocalBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {

	public static volatile boolean runFlag = false;
	EditText requestAddress;
	EditText sourcePhone;
	EditText targetPhone;
	Button saveBtn;
	Button runstopBtn;
	SharedPreferences sp;
	TelephonyManager tm;
	
	SmsSendService smsSendService;  

    private ServiceConnection mConnection = new ServiceConnection() {  
        public void onServiceConnected(ComponentName className,IBinder localBinder) {  
        	smsSendService = ((LocalBinder)localBinder).getService();
        }  
        public void onServiceDisconnected(ComponentName arg0) {  
        	smsSendService = null;  
        }  
    }; 
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sp = getSharedPreferences("AppleRaider", Context.MODE_PRIVATE);
		tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		requestAddress = (EditText)findViewById(R.id.request_address);
		sourcePhone = (EditText)findViewById(R.id.source_phone);
		targetPhone = (EditText)findViewById(R.id.target_phone);
		saveBtn = (Button)findViewById(R.id.save_btn);
		runstopBtn = (Button)findViewById(R.id.runstop_btn);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		String requestAddressStr = sp.getString("requestAddress", Content.requestAddressDefault);
		String sourcePhoneStr  = tm.getLine1Number();
		if(null == sourcePhoneStr){
			sourcePhoneStr = "";
		}
		sourcePhoneStr = sp.getString("sourcePhone", sourcePhoneStr);
		String targetPhoneStr = sp.getString("targetPhone", Content.targetPhoneDefault);
		requestAddress.setText(requestAddressStr);
		sourcePhone.setText(sourcePhoneStr);
		targetPhone.setText(targetPhoneStr);
		runstopBtn.setText(R.string.run);
		Intent intent = new Intent(this, SmsSendService.class);  
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(runFlag){
			runstopBtn.setText(R.string.stop);
		}else{
			runstopBtn.setText(R.string.run);
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		unbindService(mConnection);
	}

	public void saveConfig(View v) {
		String requestAddressStr = requestAddress.getText().toString();
		String sourcePhoneStr  = sourcePhone.getText().toString();
		String targetPhoneStr = targetPhone.getText().toString();
		Editor editor = sp.edit();
		editor.putString("requestAddress", requestAddressStr);
		editor.putString("sourcePhone", sourcePhoneStr);
		editor.putString("targetPhone", targetPhoneStr);
		editor.commit();
	}
	
	public synchronized void runOrStop(View v) {
		runFlag = !runFlag;
		if(runFlag){
			smsSendService.run();
			runstopBtn.setText(R.string.stop);
		}else{
			smsSendService.stop();
			runstopBtn.setText(R.string.run);
		}
	}
}
