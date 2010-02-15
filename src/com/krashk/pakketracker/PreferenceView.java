package com.krashk.pakketracker;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PreferenceView extends PreferenceActivity {
	
	/**	 Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	addPreferencesFromResource(R.layout.preferences);
    }

}
