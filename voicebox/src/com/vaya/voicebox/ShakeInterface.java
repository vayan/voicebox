package com.vaya.voicebox;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.FloatMath;
import android.util.Log;

public class ShakeInterface implements SensorEventListener {
	
	 //检测摇动相关变量
   private long initTime = 0;
   private long lastTime = 0;
   private long curTime = 0;
   private long duration = 0;
   /**
    * 上次检测时，各分量
    * */
   private float last_x = 0.0f,last_y = 0.0f,last_z = 0.0f, totalShake = 0.0f;
   /**
    * 本次晃动值
    * */
   private float shake = 0.0f;
   /**
    * 控制时间间隔
    * */
   private   int TimeInterval = 150;
   /**
    * 晃动阀值
    * */
   private  int shakeThreshold = 3000;
   private  boolean isRecoding = false;
   private SensorManager mSensorManager;  
	private ArrayList<OnShakeListener> mListeners;
   public ShakeInterface(Context context){
   	mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
   	mListeners = new ArrayList<OnShakeListener>();
   }
   /** 
    * 定义摇晃发生时的事件处理接口，需实现onShake方法 
    */  
   public interface OnShakeListener {  
       /** 
        * 当手机摇晃时被调用 
        */  
       void onShake();  
   }
   /** 
    * 注册OnShakeListener，当摇晃时接收通知 
    *  
    * @param listener 
    */  
   public void registerOnShakeListener(OnShakeListener listener) {  
       if (mListeners.contains(listener))  
           return;  
       mListeners.add(listener);  
   }
   /** 
    * 移除已经注册的OnShakeListener 
    *  
    * @param listener 
    */  
   public void unregisterOnShakeListener(OnShakeListener listener) {  
       mListeners.remove(listener);
   }
   /** 
    * 启动摇晃检测 
    */  
   public void start() {  
       if (mSensorManager == null) {  
           throw new UnsupportedOperationException();  
       }  
       Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);  
       if (sensor == null) {  
           throw new UnsupportedOperationException();  
       }  
       boolean success = mSensorManager.registerListener(this, sensor,SensorManager.SENSOR_DELAY_GAME);
       if (!success) {  
           throw new UnsupportedOperationException();  
       }else{
       	System.out.println("注册成功");
       }
   }  
   /** 
    * 停止摇晃检测 
    */  
   public void stop() {  
       if (mSensorManager != null)  
           mSensorManager.unregisterListener(this);  
   }  
	@Override
   public void onAccuracyChanged(Sensor sensor, int accuracy) {
           //什么也不干
   	System.out.println("精度发生变化");
   }

   //传感器数据变动事件
   @SuppressLint("FloatMath")
@Override
   public void onSensorChanged(SensorEvent event) {        
	
           //如果没有开始录音的话可以监听是否有摇动事件，如果有摇动事件可以开始录音
   	//获取加速度传感器的三个参数
   	float x = event.values[0];
   	float y = event.values[1];
   	float z = event.values[2];

   	//获取当前时刻的毫秒数
   	curTime = System.currentTimeMillis();
   	if(!isRecoding){
   	   //Log.d("tag", "hoho");
	    	//100毫秒检测一次
   		//System.out.println("开始变化,curtime:" + curTime +"lasttime:" + lastTime);
	    	if ((curTime - lastTime) > TimeInterval) {
	    		duration = (curTime - lastTime);
	    		// 看是不是刚开始晃动
	    		if (last_x == 0.0f && last_y == 0.0f && last_z == 0.0f) {
	    			//last_x、last_y、last_z同时为0时，表示刚刚开始记录
	    			initTime = System.currentTimeMillis();
	    		} else {
	    			//精确算法，各方向差值平方和开平方,单次晃动幅度
	    			shake = FloatMath.sqrt((x - last_x)*(x - last_x)+(y - last_y)*(y - last_y)+(z - last_z)*(z - last_z))/duration*10000;
	    		}
	    		//totalShake  += shake;
	    		if(shake >= shakeThreshold){
	    			//此处开始执行
	    			this.notifyListeners(); 
	    			initShake();
	    		}
	    		last_x = x;
		    	last_y = y;
		    	last_z = z;
		    	lastTime = curTime;
	    	}
	    	
   	}
   	
   }
   
   public void initShake() {

	   lastTime = 0;

	   duration = 0;

	   curTime = 0;

	   initTime = 0;

	   last_x = 0.0f;

	   last_y = 0.0f;

	   last_z = 0.0f;

	   shake = 0.0f;

	   totalShake = 0.0f;

	  }
   
   /** 
    * 当摇晃事件发生时，通知所有的listener 
    */
   private void notifyListeners() {  
       for (OnShakeListener listener : mListeners) {
       	System.out.println("你执行了？");
       	//isRecoding = true;
           listener.onShake();  
       }  
   }
	public int getTimeInterval() {
		return TimeInterval;
	}
	public void setTimeInterval(int timeInterval) {
		TimeInterval = timeInterval;
	}
	public int getShakeThreshold() {
		return shakeThreshold;
	}
	public void setShakeThreshold(int shakeThreshold) {
		this.shakeThreshold = shakeThreshold;
	}
	public boolean isRecoding() {
		return isRecoding;
	}
	public void setRecoding(boolean isRecoding) {
		this.isRecoding = isRecoding;
	}
   
}