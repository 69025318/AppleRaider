package com.zhh;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		MySharedPreferences spf = MySharedPreferences.getInstance(MainActivity.this);
	}

	public void saveConfig(View v) {

	}
	
	public void runOrStop(View v) {

	}
}
