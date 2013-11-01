package com.lighthouse.fingerprint2.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.lighthouse.fingerprint2.R;

public class DataPersistence {

	public static final String PREF_FILE_NAME = "PrefFile";
	public static Context context = null;
	public Resources m_res = null;

	public DataPersistence(Context cont) {
		context = cont;
		m_res = context.getResources();
	}

	public String getServerName() {
		SharedPreferences preferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
		String addr = preferences.getString(m_res.getString(R.string.shared_preferences_name), null);
		if (addr == null || addr.equals("")) {
			addr = context.getString(R.string.default_url);
		}
		return addr;
	}
	

	public void setServerName(String name) {
		SharedPreferences settings = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(m_res.getString(R.string.shared_preferences_name), name);
		editor.commit();
	}
}
