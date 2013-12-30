package com.lighthousesignal.fingerprint2.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.lighthousesignal.fingerprint2.R;

public class SettingsActivity extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_general);

	}

}