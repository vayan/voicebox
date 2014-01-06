package com.vaya.voicebox;

import android.app.Dialog;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

public class NumberPickerPreference extends DialogPreference {
	    public NumberPickerPreference(Context context, AttributeSet attrs) {
	        super(context, attrs);
	     
	        setDialogLayoutResource(R.xml.dialog);
	        setPositiveButtonText(android.R.string.ok);
	        setNegativeButtonText(android.R.string.cancel);

	        NumberPicker np = (NumberPicker) Context.getApplicationContext().findViewById(R.id.numberPicker1);
	         np.setMaxValue(100); // max value 100
	         np.setMinValue(0);   // min value 0
	         np.setWrapSelectorWheel(false);
	         np.setOnValueChangedListener((OnValueChangeListener) this);
	        
	        setDialogIcon(null);
	        
	    }
	    
	}