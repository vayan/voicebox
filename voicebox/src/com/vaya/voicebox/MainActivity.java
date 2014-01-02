package com.vaya.voicebox;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	private Messenger msgService = null;
	boolean mBound = false;
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	
	public static final String LOG_TAG = "VoiceBox"; //TAG TO USE FOR ALL DEBUG
	
	public MainActivity() {
		Log.d(MainActivity.LOG_TAG, "Program Started");
	}
		
	public void TouchStartRecord(View view) { 
		 Log.d(MainActivity.LOG_TAG, "Start Record button hit");
		 sendMsgServ(AudioRecorder.MSG_START_RECORD);
	}
	
	public void TouchStopRecord(View view) { 
		 Log.d(MainActivity.LOG_TAG, "Stop Record button hit");
		 sendMsgServ(AudioRecorder.MSG_STOP_RECORD);
	}
	
	private void toggleUiRecord(boolean recording) {
		TextView t =(TextView)findViewById(R.id.textView1); 
		Button btn_srt = (Button)findViewById(R.id.button_start);
		Button btn_stp = (Button)findViewById(R.id.button_stop);
		
		btn_srt.setEnabled(!recording);
		btn_stp.setEnabled(recording);
		if (recording) {
			 t.setText("Recording");
		} else {
            t.setText("Stopped Recording");
		}
	}
	
	
	//Create connection between activity and the recording service
	private ServiceConnection mConnection  = new ServiceConnection() {
		 @Override
		    public void onServiceConnected(ComponentName name, IBinder service) {
			 	mBound = true;
		    	Log.d(MainActivity.LOG_TAG, "onServiceConnected() called");  	
		    	msgService = new Messenger(service);
		    	try {
			    	Message msg = Message.obtain(null,
			    			AudioRecorder.MSG_REGISTER_CLIENT);
	                msg.replyTo = mMessenger;
	                msgService.send(msg);
	                msg = Message.obtain(null,
	                		AudioRecorder.MSG_SET_VALUE, this.hashCode(), 0);
		    	} catch (RemoteException e) {
			    	Log.e(MainActivity.LOG_TAG, "onServiceConnected() crash : " + e.toString());  	
                }
		 }
		 
	    @Override
	    public void onServiceDisconnected(ComponentName arg0) {
	    	mBound = false;
	    	msgService = null;
	    	Log.d(MainActivity.LOG_TAG, "onServiceDisconnected() called");
	    }
	};
	
	//Wrapper to send message to serv
	private void sendMsgServ(int msg) {
		try {
			msgService.send(Message.obtain(null,
            		msg, msg, 0));
        } catch (RemoteException e) {
        }
	}
	
	//Handle incoming message from server
	class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
        	
    		
        	Log.d(MainActivity.LOG_TAG, "handleMessage Acti : " + msg.toString());
            
			switch (msg.what) {
            case AudioRecorder.MSG_SAY_HELLO:
            	Log.d(MainActivity.LOG_TAG, "Service say hello");
            	break;
            case AudioRecorder.MSG_START_RECORD:
            	Log.d(MainActivity.LOG_TAG, "Service say it started recording");    
            	toggleUiRecord(true);
            	break;
            case AudioRecorder.MSG_STOP_RECORD:
            	Log.d(MainActivity.LOG_TAG, "Service say it stopped recording");   
            	toggleUiRecord(false);
            	break;
            default:
                super.handleMessage(msg);
            }
        }
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	    
	}
	
	@Override
	protected void onPause() {
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
