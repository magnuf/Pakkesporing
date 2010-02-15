package com.krashk.pakketracker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class PreferenceView extends PreferenceActivity {
	
	/**	 Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	addPreferencesFromResource(R.layout.preferences);
    	
    	ListPreference intervalPref = (ListPreference)findPreference(getString(R.string.intervalVal));
		intervalPref.setEnabled(((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getBackgroundDataSetting());
		
		intervalPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				return true;
			}
		});
		
		CheckBoxPreference notificationsPref = (CheckBoxPreference)findPreference(getString(R.string.notificationpref));
		
		notificationsPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference arg0, Object arg1) {
				return true;
			}
		});
		
		CheckBoxPreference updatePref = (CheckBoxPreference)findPreference(getString(R.string.updatePref));
		
		updatePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference arg0, Object arg1) {
				return true;
			}
		});
    }

}
