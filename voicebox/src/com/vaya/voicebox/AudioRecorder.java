package com.vaya.voicebox;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/*
 * 
 * TODO : change the way service work, startservice === startrecord, stopservice mean stop record,
 * better but im lazy
 * 
 */

public class AudioRecorder extends Service {
	private MediaRecorder mRecord = null;
	private String Filename = null;
	private String Folder = "VoiceBox";
	private Messenger mClient = null;
	private boolean recording = false;
	private long StartRecord = 0;
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	public static final String LOG_TAG = "VoiceBoxService";

	/* =========
	 * SETTINGS
	 * =========
	 */
	private OnSharedPreferenceChangeListener listener = null;
	private SharedPreferences prefs = null;
	private Integer S_AudioFormat = 1;
	private String  S_Folder = "VoiceBox";
	private String 	S_FileName = "VoiceBox";


	/*==============================
	 * MAP FOR AUDIO FILE EXTENSION
	 * =============================
	 */
	private static final Map<Integer, String> file_extension;
	static
	{
		file_extension = new HashMap<Integer, String>();
		file_extension.put(MediaRecorder.OutputFormat.THREE_GPP, "3gp");
		file_extension.put(MediaRecorder.OutputFormat.MPEG_4, "mp4");
		file_extension.put(MediaRecorder.OutputFormat.AAC_ADTS, "aac");
		file_extension.put(MediaRecorder.OutputFormat.AMR_NB, "amr");
	}


	/*========================
	 * MSG VALUE FOR PROTOCOL
	 * =======================
	 */
	static final int MSG_REGISTER_CLIENT = 1;
	static final int MSG_START_RECORD = 11;
	static final int MSG_STOP_RECORD = 12;
	static final int MSG_GET_STATUS = 13;
	static final int MSG_TIME_START = 14;
	static final int MSG_SETTINGS_UPDATED = 15;

	/*==============
	 * NOTIFICATION
	 *==============
	 */
	private void CancelAllNotif() {
		NotificationManager mNotificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancelAll();
	}

	private NotificationManager notifUser(String Title, String text) {

		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.ic_launcher)
		.setOngoing(true)
		.setContentTitle(Title)
		.setContentText(text);
		Intent resultIntent = new Intent(this, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(11 , mBuilder.build());
		return mNotificationManager;
	}

	/*================================
	 * MESSAGING SERVICE <-> ACTIVITY
	 *================================
	 */
	public class MessageProto { //object to send to the activity for data
		public int type;
		public long value;

		public MessageProto(int t, long v) {
			type = t;
			value = v;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(LOG_TAG, "Service binded");
		return mMessenger.getBinder();
	}

	private void sendObjClient(int msg, long value) {
		try {
			mClient.send(Message.obtain(null,
					msg, new MessageProto(msg, value)));
		} catch (RemoteException e) {
			Log.e(LOG_TAG, "sendObjClient() : sendObjClient send() failed " + e.toString());
		}
	}

	private void sendMsgClient(int msg) {
		try {
			mClient.send(Message.obtain(null,
					msg, msg, 0));
		} catch (RemoteException e) {
			Log.e(LOG_TAG, "sendMsgClient() : sendMsgClient send() failed " + e.toString());
		}
	}

	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			Log.d(LOG_TAG, "handleMessage Service : " + msg.toString());
			switch (msg.what) {
			case MSG_REGISTER_CLIENT: //Register the client
				Log.d(LOG_TAG, "New Client");
				mClient = msg.replyTo; 
				break;
			case MSG_START_RECORD: //Receive record command
				StartRecord();
				break;
			case MSG_STOP_RECORD: //Receive stop record command
				StopRecord();
				break;
			case MSG_TIME_START: //Receive request for time start
				sendObjClient(MSG_TIME_START, StartRecord);
				break;
			case MSG_GET_STATUS: //Receive request status
				if (recording) sendMsgClient(MSG_START_RECORD);
				else sendMsgClient(MSG_STOP_RECORD);
				break;
			case MSG_SETTINGS_UPDATED: //Settings are updated
				UpdatePref();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}


	/*================
	 * AUDIO RECORDER
	 *================
	 */
	public void onStopRecord() {
		sendMsgClient(MSG_STOP_RECORD);
		recording = false;
		StartRecord = 0;
		CancelAllNotif();
		Log.d(LOG_TAG, "onStopRecord()");
	}

	public void onStartRecord() {
		sendMsgClient(MSG_START_RECORD);
		recording = true;
		StartRecord = System.currentTimeMillis();
		notifUser("Voice Recording", "You are currently recording audio");
		Log.d(LOG_TAG, "onStartRecord()");
	}

	public void SetFilename(String name) { 
		File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Folder+"/");
		dir.mkdir();

		Filename = Environment.getExternalStorageDirectory().getAbsolutePath();
		Filename += "/"+Folder+"/"+name+"."+file_extension.get(S_AudioFormat);
	}

	private String generateFileName() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd__HH-mm-ss");
		String currentDateandTime = sdf.format(new Date());

		Log.d(LOG_TAG, "Date " + currentDateandTime);
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
			Log.d(LOG_TAG, "StartRecord()");

		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed : " + e.getMessage());
		}
		mRecord.start();
		onStartRecord();
	}

	private void PauseRecord(){
		//TODO : lazy
	}

	private void StopRecord() {
		if (mRecord == null) {
			Log.e(LOG_TAG, "StopRecord() failed, no recording");
			return;
		}
		mRecord.stop();
		mRecord.release();
		mRecord = null;
		Log.d(LOG_TAG, "StopRecord()");
		onStopRecord();
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


	/*=========
	 * SERVICE
	 *=========
	 */
	public void UpdatePref() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		S_AudioFormat = Integer.parseInt((sharedPref.getString("set_format", "1")));
		S_Folder = sharedPref.getString("default_folder", "VoiceBox");
		S_FileName = sharedPref.getString("default_file_name", "VoiceBox");
		Log.d(LOG_TAG, "Pref Loaded file format is : " + S_AudioFormat);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		CancelAllNotif();
		Log.d(LOG_TAG, "Service started");

		//handle preference
		UpdatePref();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
				Log.d(LOG_TAG, "Preference changed"); 
				UpdatePref();
			}
		};
		prefs.registerOnSharedPreferenceChangeListener(listener);		
	}

	@Override
	public void onDestroy() {
		Log.d(LOG_TAG, "Service dead");
		CancelAllNotif();
		super.onDestroy();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(LOG_TAG, "Service unbinded");
		return super.onUnbind(intent);
	}
}
