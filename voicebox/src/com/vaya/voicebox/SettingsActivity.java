package com.vaya.voicebox;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

public class SettingsActivity extends Activity  {
	
	public static final String LOG_TAG = "VoiceBoxSettings";
	
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

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
	}
}
