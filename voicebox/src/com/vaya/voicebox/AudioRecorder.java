package com.vaya.voicebox;

import java.io.IOException;

import android.app.IntentService;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class AudioRecorder extends IntentService {
	private MediaRecorder mRecord = null;
	private String Filename = null;
	private Messenger mClient = null;
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	/*
	 * MSG VALUE FOR PROTOCOL
	 */
	static final int MSG_SAY_HELLO = 1; 
	static final int MSG_REGISTER_CLIENT = 1;
	static final int MSG_SET_VALUE = 3;
	static final int MSG_START_RECORD = 11;
	static final int MSG_STOP_RECORD = 12;

	public AudioRecorder() {
		super("AudioRecorder");
	}

	private void sendMsgClient(int msg) {
		try {
			mClient.send(Message.obtain(null,
					msg, msg, 0));
		} catch (RemoteException e) {
		}
	}

	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			Log.d(MainActivity.LOG_TAG, "handleMessage Service : " + msg.toString());
			switch (msg.what) {
			case MSG_REGISTER_CLIENT: //Register the client
				Log.d(MainActivity.LOG_TAG, "New Client");
				mClient = msg.replyTo; 
				sendMsgClient(MSG_SAY_HELLO);
				break;
			case MSG_START_RECORD: //Receive record command
				test();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	public void onStopRecord() {
		sendMsgClient(MSG_STOP_RECORD);
	}


	public void onStartRecord() {
		sendMsgClient(MSG_START_RECORD);
	}


	public void SetFilename(String name) { 
		Filename = Environment.getExternalStorageDirectory().getAbsolutePath();
		Filename += "/"+name+".3gp";
	}


	private void StartRecord() {
		SetFilename("testtesttest11");
		mRecord = new MediaRecorder();
		mRecord.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecord.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecord.setOutputFile(Filename);
		mRecord.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		try {
			mRecord.prepare();
			Log.d(MainActivity.LOG_TAG, "StartRecord() go");

		} catch (IOException e) {
			Log.e(MainActivity.LOG_TAG, "prepare() failed : " + e.getMessage());
		}

		mRecord.start();
		onStartRecord();
	}

	private void PauseRecord(){

	}

	private void StopRecord() {
		if (mRecord == null) {
			Log.e(MainActivity.LOG_TAG, "StopRecord() failed, no recording");
			return;
		}
		mRecord.stop();
		mRecord.release();
		mRecord = null;
		Log.d(MainActivity.LOG_TAG, "StopRecord() go");
		onStopRecord();
		stopSelf();
	}

	public void test() { //Record test for debug purpose
		new Thread(new Runnable() {
			public void run() {
				StartRecord();
				long endTime = System.currentTimeMillis() + 5*1000;
				while (System.currentTimeMillis() < endTime) {
					synchronized (this) {
						try {
							wait(endTime - System.currentTimeMillis());
						} catch (Exception e) {
						}
					}
				}
				StopRecord();
			}
		}).start(); 
	}


	@Override
	public IBinder onBind(Intent intent) {
		Log.d(MainActivity.LOG_TAG, "Service binded");
		return mMessenger.getBinder();
	}


	@Override
	protected void onHandleIntent(Intent intent) {

	}

}
