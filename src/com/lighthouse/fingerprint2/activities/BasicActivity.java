package com.lighthouse.fingerprint2.activities;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lighthouse.fingerprint2.R;
import com.lighthouse.fingerprint2.logs.ErrorLog;
import com.lighthouse.fingerprint2.logs.LogWriter;
import com.lighthouse.fingerprint2.logs.LogWriterSensors;
import com.lighthouse.fingerprint2.networks.AppLocationManager;
import com.lighthouse.fingerprint2.utilities.DataPersistence;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class BasicActivity extends Activity {
	protected SharedPreferences mPreferences;
	public static final String TAG_KEY = "TAG_KEY";
	public static final String LOG_TAG = "FINGERPRINT2";
	static protected WeakReference<AppLocationManager> mLocManager;
	public static String PREF_LOGIN_TOKEN = "login_token";

	public static String PREF_LOGIN_USERNAME = "login_username";

	public static String PREF_LOGIN_PASS = "login_password";

	public static String PREF_CUSTOMER_ID = "customer_id";

	public static String PREF_DEVELOPER_ID = "developer_id";

	public static String PREF_BUILDING_ID = "";

	public static String PREF_FLOOR_ID = "";

	public static String PREF_IMG_URL = "";

	public static final int INTENT_LOGIN_CODE = 100;

	protected String mFilename;

	public SharedPreferences getLocalPreferences() {
		return mPreferences;
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		// Create global configuration and initialize ImageLoader with this
		// configuration
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext()).build();
		ImageLoader.getInstance().init(config);
	}

	/**
	 * Gets Location Manager
	 * 
	 * @return
	 */
	public AppLocationManager getLocationManager() {
		if (mLocManager == null || mLocManager.get() == null) {
			mLocManager = new WeakReference<AppLocationManager>(
					new AppLocationManager(this, mPreferences));
		}
		return mLocManager.get();
	}

	/**
	 * Saves login token
	 * 
	 * @param token
	 */
	public void saveToken(String token) {
		mPreferences.edit().putString(PREF_LOGIN_TOKEN, token).commit();
	}

	/**
	 * Saves login information
	 * 
	 * @param token
	 * @param username
	 * @param pass
	 */
	public void saveToken(String token, String username, String pass) {
		mPreferences.edit().putString(PREF_LOGIN_TOKEN, token)
				.putString(PREF_LOGIN_USERNAME, username)
				.putString(PREF_LOGIN_PASS, pass).commit();
	}

	/**
	 * Gets token
	 */
	public String getToken() {
		return mPreferences.getString(PREF_LOGIN_TOKEN, "");
	}

	/**
	 * Has token
	 * 
	 * @return
	 */
	public boolean hasToken() {
		return mPreferences.contains(PREF_LOGIN_TOKEN);
	}

	public void eraseToken() {
		if (mPreferences.contains(PREF_LOGIN_TOKEN)) {
			mPreferences.edit().remove(PREF_LOGIN_TOKEN).commit();
		}
	}

	// save current building id
	public void saveBuildingID(String building_id) {
		mPreferences.edit().putString(PREF_BUILDING_ID, building_id).commit();
	}

	/**
	 * get current building id
	 */
	public String getBuildingID() {
		return mPreferences.getString(PREF_BUILDING_ID, "");
	}

	/**
	 * save current floor id
	 */
	public void saveFloorID(String floor_id) {
		mPreferences.edit().putString(PREF_FLOOR_ID, floor_id).commit();
	}

	/**
	 * get current floor id
	 */
	public String getFloorID() {
		return mPreferences.getString(PREF_FLOOR_ID, "");
	}

	/**
	 * save current img url
	 */
	public void saveImgUrl(String imageName) {
		DataPersistence d = new DataPersistence(this);
		String imageUrl = new StringBuilder()
				.append(d.getServerName() + getString(R.string.plans_url))
				.append(imageName).toString();
		mPreferences.edit().putString(PREF_IMG_URL, imageUrl).commit();
	}

	/**
	 * get current img url
	 */
	public String getImgUrl() {
		return mPreferences.getString(PREF_IMG_URL, "");
	}

	/**
	 * Internet connection is available or not
	 * 
	 * @return
	 */
	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm.getActiveNetworkInfo() == null) {
			return false;
		}
		return cm.getActiveNetworkInfo().isConnectedOrConnecting();
	}

	/**
	 * Standard Confirm Dialog
	 * 
	 * @param title
	 * @param message
	 * @param onClickListener
	 * @return
	 */
	public AlertDialog standardConfirmDialog(String title, String message,
			DialogInterface.OnClickListener onClickListenerPositive,
			DialogInterface.OnClickListener onClickListenerNegative,
			boolean cancelable) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle(title);
		alertDialog.setPositiveButton("YES", onClickListenerPositive);
		alertDialog.setNegativeButton("NO", onClickListenerNegative);
		alertDialog.setCancelable(cancelable);
		alertDialog.setMessage(message);
		alertDialog.show();

		return alertDialog.create();
	}

	/**
	 * Standard Alert Message Dialog
	 * 
	 * @param title
	 * @param message
	 * @param onClickListener
	 * @return
	 */
	public AlertDialog standardAlertDialog(String title, String message,
			DialogInterface.OnClickListener onClickListener) {
		try {
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle(title);
			alertDialog.setButton("OK", onClickListener);
			alertDialog.setMessage(message);

			alertDialog.show();

			return alertDialog;
		} catch (Exception e) {
			// sometime it loses current window
			return null;
		}
	}

	public AlertDialog segmentNameDailog(String title, final Context context,
			final String existingFilename, final MapViewActivity activity,
			final View row, final String[] files, final int files_index) {
		final AlertDialog.Builder alert = new AlertDialog.Builder(context);

		final LinearLayout view = new LinearLayout(context);
		final TextView datestamp = new TextView(context);
		final EditText segnum = new EditText(context);
		segnum.setInputType(InputType.TYPE_CLASS_NUMBER);
		final Spinner segmode = new Spinner(context);
		final EditText nameinput = new EditText(context);

		List<String> items = new ArrayList<String>();
		items.add("orig");
		items.add("mod");
		items.add("missing");
		items.add("new");
		items.add("a");
		items.add("b");
		items.add("c");
		items.add("d");
		items.add("e");
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, items);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		segmode.setAdapter(dataAdapter);
		segnum.setHint("#");
		nameinput.setHint("Name");
		TextView splitter = new TextView(this);
		splitter.setText("-");

		if (existingFilename == null) {
			datestamp.setText(LogWriter.generateFilename() + "-");
			segmode.setSelection(0);
		} else {
			String[] dat;
			if (existingFilename.contains(".")) {
				dat = existingFilename.substring(0,
						existingFilename.indexOf('.')).split("-");
			} else {
				dat = existingFilename.split("-");
			}
			datestamp.setText(dat[0] + "-");
			segnum.setText(dat[1].replaceAll("\\D+", ""));
			String mode = dat[1].replaceAll("[^A-Za-z]+", "");
			segmode.setSelection((items.indexOf(mode) == -1) ? 0 : items
					.indexOf(mode));
			nameinput.setText(dat[2]);
		}
		if (getResources().getConfiguration().orientation == 1) {
			LinearLayout horizontal = new LinearLayout(context);
			LinearLayout vertical = new LinearLayout(context);
			horizontal.setOrientation(LinearLayout.HORIZONTAL);
			vertical.setOrientation(LinearLayout.VERTICAL);

			horizontal.addView(datestamp);
			horizontal.addView(segnum);
			horizontal.addView(segmode);
			horizontal.addView(splitter);

			vertical.addView(horizontal);
			vertical.addView(nameinput);
			view.addView(vertical);

		} else {
			view.addView(datestamp);
			view.addView(segnum);
			view.addView(segmode);
			view.addView(splitter);
			view.addView(nameinput);
		}

		alert.setView(view);
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				try {
					String value = "";
					String num = segnum.getText().toString().trim();
					String mode = segmode.getSelectedItem().toString().trim();
					String name = nameinput.getText().toString().trim()
							.replaceAll("[^0-9a-zA-Z]", "_");

					if (num.isEmpty()) {
						standardAlertDialog("Warning",
								"Please provide a segment # and save again.",
								null);
						num = "0";
					}

					if (name == "")
						name = "noname";
					if (mode == "orig")
						mode = "";

					value = datestamp.getText().toString().trim() + num + mode
							+ "-" + name;

					if (existingFilename == null) {
						// Update wifisearcheractivity if applicable
						if (activity != null)
							activity.mFilename = value;
						LogWriter.instance().saveLog(value + ".log");
						LogWriterSensors.instance().saveLog(value + ".dev");
					} else {

						// Move file
						String oldfilename = existingFilename;
						if (existingFilename.contains(".")) {
							oldfilename = existingFilename.substring(0,
									existingFilename.indexOf("."));
						}
						File oldlogfile = new File(LogWriter.APPEND_PATH + "/"
								+ oldfilename + ".log");
						File newlogfile = new File(LogWriter.APPEND_PATH + "/"
								+ value + ".log");

						boolean logsuccess = oldlogfile.renameTo(newlogfile);

						File olddevfile = new File(LogWriter.APPEND_PATH + "/"
								+ oldfilename + ".dev");
						File newdevfile = new File(LogWriter.APPEND_PATH + "/"
								+ value + ".dev");

						boolean devsuccess = olddevfile.renameTo(newdevfile);

						if (devsuccess && logsuccess) {
							Toast.makeText(
									getApplicationContext(),
									"Renamed to "
											+ newlogfile.getAbsolutePath(),
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(getApplicationContext(),
									"Error renaming file.", Toast.LENGTH_SHORT)
									.show();
						}

						// Update sent flags
						int currentFlags = MainMenuActivity.getSentFlags(
								existingFilename, context);
						MainMenuActivity.setSentFlags(existingFilename, 0,
								context);
						MainMenuActivity.setSentFlags(value + ".log",
								currentFlags, context);

						if (activity != null)
							activity.mFilename = value;
						if (row != null) {
							String[] sent_mode = { "", "(s) ", "(e) ", "(s+e) " };
							files[files_index] = value + ".log";
							((TextView) row).setText(sent_mode[currentFlags]
									+ value + ".log");
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					ErrorLog.e(e);
					Log.e("Error", "Unknown error");
					standardAlertDialog(getString(R.string.msg_error),
							getString(R.string.msg_error), null);
				}
			}
		});
		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
					}
				});
		return alert.create();
	}

	/**
	 * Is Wi-Fi enabled
	 * 
	 * @param context
	 * @return
	 */
	public boolean isWifiAvailable() {
		// Log.d("Fingerprint", "Checking if wifi is available.");
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = null;
		if (connectivityManager != null) {
			// Log.d("Fingerprint", "Connectivity Manager is not null");
			networkInfo = connectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		}
		// Log.d("Fingerprint", "networkinfo null? " + (networkInfo == null));
		// Log.d("Fingerprint", "detailedstate " +
		// (NetworkInfo.DetailedState)networkInfo.getDetailedState());
		// Log.d("Fingerprint", "state " +
		// (NetworkInfo.State)networkInfo.getState());
		// Log.d("Fingerprint", "extrainfo " + networkInfo.getExtraInfo());
		// Log.d("Fingerprint", "isavailable" + networkInfo.isAvailable());
		return networkInfo == null ? false : networkInfo.isAvailable();
	}

	/**
	 * Is gps enabled
	 * 
	 * @return
	 */
	public boolean isGpsEnabled() {
		String provider = Settings.Secure.getString(getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

		return provider.contains("gps");
	}

	// safe long to int convert
	public static int safeLongToInt(long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(l
					+ " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}
}
