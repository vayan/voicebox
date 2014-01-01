package com.vaya.voicebox;

import com.vaya.voicebox.AudioRecorder.LocalBinder;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.View;



public class MainActivity extends Activity {
	AudioRecorder mService;
	boolean mBound = false;


	public static final String LOG_TAG = "VoiceBox";
	
	
	public void ToggleRecord(View view) {
		 Log.d(MainActivity.LOG_TAG, "ToggleRecord() hit");
		 new Thread(new Runnable() {
		        public void run() {
		        	if (mBound) mService.test();
		        }
		    }).start(); 
	 }
	
	private ServiceConnection mConnection  = new ServiceConnection() {
		 @Override
		    public void onServiceConnected(ComponentName name, IBinder service) {
			 	LocalBinder binder = (LocalBinder) service;
			 	mService  = binder.getService();
			 	mBound = true;
		    	Log.d(MainActivity.LOG_TAG, "onServiceConnected() called");
		 }
		 

	    @Override
	    public void onServiceDisconnected(ComponentName arg0) {
	    	mBound = false;
	    	Log.d(MainActivity.LOG_TAG, "onServiceDisconnected() called");
	    }
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	    
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent(this, AudioRecorder.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
