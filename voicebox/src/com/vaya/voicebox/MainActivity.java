package com.vaya.voicebox;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.vaya.voicebox.AudioRecorder.MessageProto;

public class MainActivity extends Activity {
	private Messenger msgService = null;
	private final Messenger mMessenger = new Messenger(new IncomingHandler());
	private UpdateDuration upd = null;

	public static final String LOG_TAG = "VoiceBox_Main";

	/*==========
	 * UI STUFF
	 *==========
	 */
	public void updateTheme() { 
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

		if (sharedPref.getBoolean("use_dark_theme", false)) setTheme(android.R.style.Theme_Holo);
		else setTheme(android.R.style.Theme_Holo_Light);
	}

	public void TouchStartRecord(View view) {
		Log.d(LOG_TAG, "Start Record button hit");
		sendMsgServ(AudioRecorder.MSG_START_RECORD);
	}

	public void TouchStopRecord(View view) { 
		Log.d(LOG_TAG, "Stop Record button hit");
		sendMsgServ(AudioRecorder.MSG_STOP_RECORD);
	}


	private void toggleUiRecord(boolean recording) { 
		//toggle the ui between record mode and stop

		TextView t =(TextView)findViewById(R.id.text_status); 
		ImageButton btn_srt = (ImageButton)findViewById(R.id.button_start);
		ImageButton btn_stp = (ImageButton)findViewById(R.id.button_stop);

		btn_srt.setEnabled(!recording);
		btn_stp.setEnabled(recording);
		if (recording){ 
			t.setText("Status : Recording");
			btn_srt.setImageResource(R.drawable.record_button_dark);
			btn_stp.setImageResource(R.drawable.stop_button);
		}
		else {
			t.setText("Status : Stopped Recording");
			btn_srt.setImageResource(R.drawable.record_button);
			btn_stp.setImageResource(R.drawable.stop_button_dark);
		}
	}

	private void updateDuration(long t) {
		TextView txt =(TextView)findViewById(R.id.text_duration);
		long t_now = System.currentTimeMillis();
		long elapse = (t_now - t) / 1000;
		txt.setText("Duration : " + Long.toString(elapse) + "sec");
	}


	private class UpdateDuration extends AsyncTask<Long, Long, Long> { 
		//async task to update the ui showing recording time

		@Override
		protected Long doInBackground(Long... arg0) {
			while (true) { //just do nothing..
				try {
					Thread.sleep(200); //up if lag 
				} catch (InterruptedException e) {
					Log.e(LOG_TAG, "doInBackground() Sleep failed, " + e.toString());
					e.printStackTrace();
				}
				publishProgress(arg0);
				if (isCancelled()) break;
			}
			return arg0[0];
		}
		protected void onProgressUpdate(Long... progress) {
			updateDuration(progress[0]);
		}
		protected void onPostExecute(Long result) {
			Log.d(LOG_TAG, "onPostExecute() of async time update");
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items

		switch (item.getItemId()) {
		case R.id.action_settings:
			OpenSettings();
			return true;
		case R.id.action_filelist:
			OpenFileList();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void OpenFileList() {
		startActivity(new Intent(MainActivity.this, FileListActivity.class));
	}

	private void OpenSettings() {
		startActivity(new Intent(MainActivity.this, SettingsActivity.class));
	}



	/*================================
	 * MESSAGING SERVICE <-> ACTIVITY
	 *================================
	 */
	private void startService() {
		startService(new Intent(MainActivity.this, AudioRecorder.class));
		bindService(new Intent(this, AudioRecorder.class), mConnection, Context.BIND_AUTO_CREATE);
	}

	private ServiceConnection mConnection  = new ServiceConnection() {
		//Create connection between activity and the recording service

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(MainActivity.LOG_TAG, "onServiceConnected() called");  	
			msgService = new Messenger(service);
			try {
				Message msg = Message.obtain(null, AudioRecorder.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				msgService.send(msg);
				sendMsgServ(AudioRecorder.MSG_GET_STATUS);
			} catch (RemoteException e) {
				Log.e(LOG_TAG, "onServiceConnected() crash : " + e.toString());  	
			}
		}
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			msgService = null;
			Log.d(LOG_TAG, "onServiceDisconnected() called");
		}
	};


	private void sendMsgServ(int msg) {
		//Wrapper to send message to service

		if (msgService == null) {
			Log.e(LOG_TAG, "sendMsgServ() : msgService null");
			return;
		}
		try {
			msgService.send(Message.obtain(null,
					msg, msg, 0));
		} catch (RemoteException e) {
			Log.e(LOG_TAG, "sendMsgServ() : send failed, " + e.toString());
		}
	}

	class IncomingHandler extends Handler {
		//Handle incoming message from service

		@Override
		public void handleMessage(Message msg) {
			Log.d(LOG_TAG, "Message incoming : " + msg.toString());
			switch (msg.what) {
			case AudioRecorder.MSG_START_RECORD:
				Log.d(LOG_TAG, "Service say it started recording");
				sendMsgServ(AudioRecorder.MSG_TIME_START);
				toggleUiRecord(true);
				break;
			case AudioRecorder.MSG_STOP_RECORD:
				Log.d(LOG_TAG, "Service say it stopped recording"); 
				toggleUiRecord(false);
				if (upd != null) upd.cancel(true);
				break;
			case AudioRecorder.MSG_TIME_START:
				MessageProto val = (MessageProto) msg.obj;
				Log.d(LOG_TAG, "Service sending time start : "+ Long.toString(val.value));
				if (upd != null) upd.cancel(true);
				upd = new UpdateDuration();
				upd.execute(val.value, val.value, val.value);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	/*==========
	 * ACTIVITY
	 *==========
	 */
	public MainActivity() {
		Log.d(LOG_TAG, "Program Started");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		updateTheme();
		setContentView(R.layout.activity_main);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		startService(); //create or rebind if already created   
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (upd != null) upd.cancel(true); //kill the async task
		Log.d(LOG_TAG, "Pause MainActivity"); 
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(LOG_TAG, "Start MainActivity"); 
		startService();
		sendMsgServ(AudioRecorder.MSG_GET_STATUS); //get recording data from service
	}

	@Override
	protected void onStop() {
		super.onStop();

		//if set to true stopped recording when app quit
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);	
		if (sharedPref.getBoolean("stop_record_quit", false)) sendMsgServ(AudioRecorder.MSG_STOP_RECORD);

		if (upd != null) upd.cancel(true);
		Log.d(LOG_TAG, "Stop MainActivity"); 
	}

	@Override
	protected void onResume() {
		Log.d(LOG_TAG, "Resume MainActivity");   
		super.onResume();		
		startService();
		sendMsgServ(AudioRecorder.MSG_GET_STATUS);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
}
