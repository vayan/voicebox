package com.vaya.voicebox;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.vaya.voicebox.AudioRecorder.MessageProto;

public class MainActivity extends Activity {
	private Messenger msgService = null;
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	UpdateDuration upd = null;

	public static final String LOG_TAG = "VoiceBox";

	/*
	 * ******************
	 * UI Stuff
	 * ******************
	 */

	public void TouchStartRecord(View view) { 
		Log.d(MainActivity.LOG_TAG, "Start Record button hit");
		sendMsgServ(AudioRecorder.MSG_START_RECORD);
	}

	public void TouchStopRecord(View view) { 
		Log.d(MainActivity.LOG_TAG, "Stop Record button hit");
		sendMsgServ(AudioRecorder.MSG_STOP_RECORD);
	}

	private void toggleUiRecord(boolean recording) {
		TextView t =(TextView)findViewById(R.id.text_status); 
		Button btn_srt = (Button)findViewById(R.id.button_start);
		Button btn_stp = (Button)findViewById(R.id.button_stop);

		btn_srt.setEnabled(!recording);
		btn_stp.setEnabled(recording);
		if (recording) t.setText("Status : Recording");
		else t.setText("Status : Stopped Recording");
	}

	private void updateDuration(long t) {
		TextView txt =(TextView)findViewById(R.id.text_duration);
		long t_now = System.currentTimeMillis();
		long elapse = (t_now - t) / 1000;
		txt.setText("Duration : " + Long.toString(elapse) + "sec");
	}


	private class UpdateDuration extends AsyncTask<Long, Long, Long> {
		@Override
		protected Long doInBackground(Long... arg0) {
			while (true) {
				try {
					Thread.sleep(500); //up if lag 
				} catch (InterruptedException e) {
					Log.e(MainActivity.LOG_TAG, "doInBackground() Sleep failed, " + e.toString());
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
			Log.d(MainActivity.LOG_TAG, "onPostExecute() of async time update");
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



	/*
	 * ******************
	 * Messaging service <-> Activity
	 * ******************
	 */

	private void startService() {
		startService(new Intent(MainActivity.this, AudioRecorder.class));
		bindService(new Intent(this, AudioRecorder.class), mConnection, Context.BIND_AUTO_CREATE);
	}


	//Create connection between activity and the recording service
	private ServiceConnection mConnection  = new ServiceConnection() {
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
				Log.e(MainActivity.LOG_TAG, "onServiceConnected() crash : " + e.toString());  	
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			msgService = null;
			Log.d(MainActivity.LOG_TAG, "onServiceDisconnected() called");
		}
	};

	//Wrapper to send message to serv
	private void sendMsgServ(int msg) {
		if (msgService == null) {
			Log.e(MainActivity.LOG_TAG, "sendMsgServ() : msgService null");
			return;
		}
		try {
			msgService.send(Message.obtain(null,
					msg, msg, 0));
		} catch (RemoteException e) {
			Log.e(MainActivity.LOG_TAG, "sendMsgServ() : send failed, " + e.toString());
		}
	}

	//Handle incoming message from server
	class IncomingHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			Log.d(MainActivity.LOG_TAG, "handleMessage Acti : " + msg.toString());

			switch (msg.what) {
			case AudioRecorder.MSG_START_RECORD:
				Log.d(MainActivity.LOG_TAG, "Service say it started recording");
				sendMsgServ(AudioRecorder.MSG_TIME_START);
				toggleUiRecord(true);
				break;
			case AudioRecorder.MSG_STOP_RECORD:
				Log.d(MainActivity.LOG_TAG, "Service say it stopped recording"); 
				toggleUiRecord(false);
				if (upd != null) upd.cancel(true);
				break;
			case AudioRecorder.MSG_TIME_START:
				MessageProto val = (MessageProto) msg.obj;
				if (upd != null) upd.cancel(true);
				Log.d(MainActivity.LOG_TAG, "Service sending time start : "+ Long.toString(val.value));
				upd = new UpdateDuration();
				upd.execute(val.value, val.value, val.value);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}


	/*
	 * ******************
	 * Activity
	 * ******************
	 */

	public MainActivity() {
		Log.d(MainActivity.LOG_TAG, "Program Started");
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayShowTitleEnabled(false);
		startService();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (upd != null) upd.cancel(true);
		Log.d(MainActivity.LOG_TAG, "Pause MainActivity"); 
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(MainActivity.LOG_TAG, "Start MainActivity"); 
		startService();
		sendMsgServ(AudioRecorder.MSG_GET_STATUS);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (upd != null) upd.cancel(true);
		Log.d(MainActivity.LOG_TAG, "Stop MainActivity"); 
	}

	@Override
	protected void onResume() {
		Log.d(MainActivity.LOG_TAG, "Resume MainActivity");   
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
