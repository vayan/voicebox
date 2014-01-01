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
import android.util.Log;

public class AudioRecorder extends IntentService {
	private final IBinder _Binder = new LocalBinder();
	private MediaRecorder mRecord = null;
	private String Filename = null;
	static final int MSG_SAY_HELLO = 1;
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	
	public AudioRecorder() {
		super("AudioRecorderServiceu");
	}
	
	public class LocalBinder extends Binder {
	    public AudioRecorder getService() {
	        return AudioRecorder.this;
	    }
	}
	
	class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SAY_HELLO:
                    //Toast.makeText(getApplicationContext(), "hello!", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

	
	public static void onStartRecord() {
		
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
        stopSelf();
	}
	
	public void test() {
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
	
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(MainActivity.LOG_TAG, "Service binded");
		return _Binder;
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		
	}

}
