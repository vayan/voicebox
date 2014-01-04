package com.vaya.voicebox;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingsActivity extends Activity  {
	
	public static final String LOG_TAG = "VoiceBoxSettings";
	
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
	    actionBar.setDisplayShowTitleEnabled(false);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
	}
}
