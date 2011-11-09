package com.squidgle.philly;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity 
{
//	SharedPreferences mPrefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
//		mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	}
}
