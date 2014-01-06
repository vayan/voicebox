package com.vaya.voicebox;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;

public class SettingsActivity extends Activity  {

	public static final String LOG_TAG = "VoiceBoxSettings";
	public static final String activity_title = "Settings";
	public void updateTheme() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

		if (sharedPref.getBoolean("use_dark_theme", false)) setTheme(android.R.style.Theme_Holo);
		else setTheme(android.R.style.Theme_Holo_Light);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	public static class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			Log.d(LOG_TAG, "Create frag settings");
			addPreferencesFromResource(R.xml.settings);
		}

		@Override
		public void onPause() {
			super.onPause();
			getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onResume() {
			super.onResume();
			getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
			Log.d(LOG_TAG, "Settings changed ");
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(LOG_TAG, "Create activity settings");
		updateTheme();
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(activity_title);

		getFragmentManager().beginTransaction()
		.replace(android.R.id.content, new SettingsFragment())
		.commit();
	}
	
	
	/*public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

        Log.i("value is",""+newVal);

    }
	
	 public void show()
	    {
		  
	         final Dialog d = new Dialog(SettingsActivity.this);
	         d.setTitle("NumberPicker");
	         d.setContentView(R.xml.dialog);
	         Button b1 = (Button) d.findViewById(R.id.button1);
	         Button b2 = (Button) d.findViewById(R.id.button2);
	         final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
	         np.setMaxValue(100); // max value 100
	         np.setMinValue(0);   // min value 0
	         np.setWrapSelectorWheel(false);
	         np.setOnValueChangedListener((OnValueChangeListener) this);
	         b1.setOnClickListener(new OnClickListener()
	         {
	          @Override
	          public void onClick(View v) {
	              tv.setText(String.valueOf(np.getValue())); //set the value to textview
	              d.dismiss();
	           }    
	          });
	         b2.setOnClickListener(new OnClickListener()
	         {
	          @Override
	          public void onClick(View v) {
	              d.dismiss(); // dismiss the dialog
	           }    
	          });
	       d.show();


	    }*/
}

