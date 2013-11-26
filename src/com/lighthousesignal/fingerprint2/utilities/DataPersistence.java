package com.lighthousesignal.fingerprint2.utilities;

import com.lighthousesignal.fingerprint2.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

public class DataPersistence {

	public static final String PREF_FILE_NAME = "PrefFile";
//	public static Context context = null;
//	public Resources m_res = null;
	
	public static final String PREF_FLOOR_ID = "floor_id";
	public static final String PREF_BUILDING_ID = "building_id";
	public static final String PREF_IMG_URL = "img_url";
	public static final String PREF_TOKEN = "token";

	public DataPersistence(Context cont) {
//		context = cont;
//		m_res = context.getResources();
	}

	public static String getServerName(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
		String addr = preferences.getString(context.getString(R.string.shared_preferences_name), null);
		if (addr == null || addr.equals("")) {
			addr = context.getString(R.string.default_url);
		}
		return addr;
	}
	
	public static void setServerName(Context context, String name) {
		getPrefs(context).edit().putString(context.getString(R.string.shared_preferences_name), name).commit();
	}
	
	public static String getFloorID(Context context) {
		return getPrefs(context).getString(PREF_FLOOR_ID, "");
	}
	
	public static void setFloorID(Context context, String floor_id) {
		getPrefs(context).edit().putString(PREF_FLOOR_ID, floor_id).commit();
	}
	
	public static void setBuildingID(Context context, String building_id) {
		getPrefs(context).edit().putString(PREF_BUILDING_ID, building_id).commit();
	}

	public static String getBuildingID(Context context) {
		return getPrefs(context).getString(PREF_BUILDING_ID, "");
	}

	public static void setImgUrl(Context context, String imageName) {
		String imageUrl = new StringBuilder()
				.append(getServerName(context) + context.getString(R.string.plans_url))
				.append(imageName).toString();
		getPrefs(context).edit().putString(PREF_IMG_URL, imageUrl).commit();
	}
	
	public static String getImgUrl(Context context) {
		return getPrefs(context).getString(PREF_IMG_URL, "");
	}
	
	public static String getToken(Context context) {
		return getPrefs(context).getString(PREF_TOKEN, "");
	}
	
	public static SharedPreferences getPrefs(Context context) {
		System.out.println("data persistance get prefs Context null? : " + (context == null));
		return context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
	}
}
