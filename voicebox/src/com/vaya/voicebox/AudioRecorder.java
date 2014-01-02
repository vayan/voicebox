package com.vaya.voicebox;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	private String Folder = "VoiceBox";
	private Messenger mClient = null;
	private boolean recording = false;
	private long StartRecord = 0;
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	/*
	 * MSG VALUE FOR PROTOCOL
	 */
	
	static final int MSG_SAY_HELLO = 1; 
	static final int MSG_REGISTER_CLIENT = 1;
	static final int MSG_SET_VALUE = 3;
	static final int MSG_START_RECORD = 11;
	static final int MSG_STOP_RECORD = 12;
	static final int MSG_GET_STATUS = 13;
	static final int MSG_TIME_START = 14;

	
	/*
	 * ******************
	 * Messaging Service <-> Activity
	 * ******************
	 */
	
	//object to send to the activity for data
	public class MessageProto {
		public int type;
		public long value;
		
		public MessageProto(int t, long v) {
			type = t;
			value = v;
		}
	}


	@Override
	public IBinder onBind(Intent intent) {
		Log.d(MainActivity.LOG_TAG, "Service binded");
		return mMessenger.getBinder();
	}
	
	public long getTimeStartRecord() {
		return StartRecord;
	}

	
	private void sendObjClient(int msg, long value) {
		try {
			mClient.send(Message.obtain(null,
					msg, new MessageProto(msg, value)));
		} catch (RemoteException e) {
		}
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
				//test();
				StartRecord();
				break;
			case MSG_STOP_RECORD: //Receive stop record command
				StopRecord();
				break;
			case MSG_TIME_START: //Receive request for time start
				sendObjClient(MSG_TIME_START, getTimeStartRecord());
				break;
			case MSG_GET_STATUS: //Receive request status
				if (recording) sendMsgClient(MSG_START_RECORD);
				else sendMsgClient(MSG_STOP_RECORD);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	
	/*
	 * ******************
	 * Audio Recorder
	 * ******************
	 */

	
	public void onStopRecord() {
		sendMsgClient(MSG_STOP_RECORD);
		recording = false;
		StartRecord = 0;
	}


	public void onStartRecord() {
		sendMsgClient(MSG_START_RECORD);
		recording = true;
		StartRecord = System.currentTimeMillis();
	}


	public void SetFilename(String name) { 
		File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Folder+"/");
		dir.mkdir();

		Filename = Environment.getExternalStorageDirectory().getAbsolutePath();
		Filename += "/"+Folder+"/"+name+".3gp";
	}


	private String generateFileName() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd__HH-mm-ss");
		String currentDateandTime = sdf.format(new Date());

		Log.d(MainActivity.LOG_TAG, "Date " + currentDateandTime);

		return currentDateandTime;
	}


	private void StartRecord() {
		SetFilename(generateFileName());
		mRecord = new MediaRecorder();
		mRecord.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecord.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecord.setOutputFile(Filename);
		mRecord.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		try {
			mRecord.prepare();
			Log.d(MainActivity.LOG_TAG, "StartRecord()");

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
		Log.d(MainActivity.LOG_TAG, "StopRecord()");
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

	

	/*
	 * ******************
	 * Service
	 * ******************
	 */
	
	public AudioRecorder() {
		super("AudioRecorder");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

	}

}
