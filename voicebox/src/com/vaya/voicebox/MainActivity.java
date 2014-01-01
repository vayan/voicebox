package com.vaya.voicebox;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	
	public static final String LOG_TAG = "VoiceBox";
	
	public void ToggleRecord(View view) {
		 Log.d(MainActivity.LOG_TAG, "ToggleRecord() hit");
		 Intent intent = new Intent(this, AudioRecorder.class);
		 startService(intent);
	 }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
