package com.lighthousesignal.fingerprint2.activities;

import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Window;
import com.lighthousesignal.fingerprint2.R;
import com.lighthousesignal.fingerprint2.fragments.MapListFragment.MapData;
import com.lighthousesignal.fingerprint2.logs.LogWriter;
import com.lighthousesignal.fingerprint2.logs.LogWriterSensors;
import com.lighthousesignal.fingerprint2.network.HttpLogSender;
import com.lighthousesignal.fingerprint2.network.INetworkTaskStatusListener;
import com.lighthousesignal.fingerprint2.network.NetworkManager;
import com.lighthousesignal.fingerprint2.network.NetworkResult;
import com.lighthousesignal.fingerprint2.network.NetworkTask;
import com.lighthousesignal.fingerprint2.utilities.AppLocationManager;
import com.lighthousesignal.fingerprint2.utilities.DataPersistence;
import com.lighthousesignal.fingerprint2.utilities.UiFactories;
import com.lighthousesignal.fingerprint2.views.MapView;
import com.lighthousesignal.fingerprint2.wifi.FingerprintManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class MapViewActivity extends Activity implements
		INetworkTaskStatusListener {

	private static final String LOG_TAG = "LSS F3 MapView";

	private static final int TAG_GET_POINTS = 2;

	/**
	 * network tag key
	 */
	private static final String TAG_KEY = "TAG";

	/**
	 * Tag of submit logs to a server
	 */
	private static final int TAG_LOG_SUBMIT = 1;

	protected static ImageLoader imageLoader;
	private DisplayImageOptions options;
	// private MapView mapView;
	private MapView mapView;
	private String imageUrl;
	private Bitmap loadedMap;
	protected MapData mData;
	// download or reload map
	private Boolean reloadMap;

	protected String mFilename;

	protected boolean mIsMapLoaded = false;

	protected ListView mLSearchResult;

	protected MapData mMapData;

	protected MapView mMapView;

	/**
	 * state of wifi access point scan completion
	 */
	protected boolean mScanCompleted = false;

	protected SensorManager mSensorManager;

	protected LinearLayout mViewport, mViewMap, mViewSummary;

	// private WifiSearcherAdapter mAdapter;
	private FingerprintManager mManager;

	private Bundle mBundle;

	private ConnectivityManager mConManager;

	// private WifiSnifferService mService;

	private int mWifiActiveNetwork = -1;

	private WifiManager mWifiManager;

	/**
	 * stores state of Mobile Internet connection before closing one
	 */
	private boolean mConnectionMobileEnabled;

	/**
	 * stores state of Wifi connection before closing one
	 */
	private boolean mConnectionWifiEnabled;

	private boolean mFlagScan = false;

	/**
	 * building info get from maplist
	 */
	private String map_info;

	/**
	 * Connection to Wi-Fi sniffer
	 * 
	 * protected ServiceConnection connection = new ServiceConnection() {
	 * 
	 * public void onServiceConnected(ComponentName className, IBinder binder) {
	 * mService = ((WifiSnifferService.ServiceBinder) binder).getService();
	 * mService.setDelegate(MapViewActivity.this); mService.mResults = new
	 * Vector<WifiScanResult>();
	 * 
	 * mAdapter.setData(mService == null ? null : mService.mResults);
	 * mAdapter.setTryCount(mService == null ? 0 : mService.mCount);
	 * 
	 * }
	 * 
	 * public void onServiceDisconnected(ComponentName className) {
	 * mService.stopScan(); mService.unregisterReceiver(mService.getOnScan());
	 * mService = null; } };
	 */

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_view);
		setImageLoaderOption();
		reloadMap = false;

		// mAdapter = new WifiSearcherAdapter(this);
		mManager = new FingerprintManager(this);

		// mBundle = getIntent().getExtras();

		// mMapData = (MapData) mBundle.get("data");

		/**
		 * Service
		 * 
		 * if (mService == null) { startService(new Intent(this,
		 * WifiSnifferService.class)); bindService(new Intent(this,
		 * WifiSnifferService.class), connection, 0); }
		 */

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		mConManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		Button button_scan_start = (Button) findViewById(R.id.button_scan_start);
		Button button_scan_stop = (Button) findViewById(R.id.button_scan_stop);
		Button button_scan_clear = (Button) findViewById(R.id.button_scan_clear);
		Button button_scan_save = (Button) findViewById(R.id.button_scan_save);

		TextView textView_map_info = (TextView) findViewById(R.id.map_info);

		if (savedInstanceState == null) {
			mBundle = getIntent().getExtras();
			if (mBundle == null) {
				map_info = null;
			} else {
				map_info = mBundle.getString("MAP_INFO");
			}
		} else {
			map_info = (String) savedInstanceState.getSerializable("MAP_INFO");
		}
		textView_map_info.setText("Map info: \n" + map_info);

		button_scan_start.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (loadedMap != null) {
					// mapView.setBitmap(loadedMap);
					mapView.startPaint(loadedMap);
					startScan();
					// mapView.setImageBitmap(loadedMap);
				}
			}
		});

		button_scan_stop.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (loadedMap != null) {
					mapView.stopPaint();
					stopScan();
				}
			}
		});

		button_scan_clear.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Perform action on click
				downloadMap(reloadMap);
			}
		});

		button_scan_save.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				saveScan();
				// Perform action on click
				// mapView.printMatrix(mapView.getDisplayMatrix());
			}
		});

		// mBundle = getIntent().getExtras();
		// mMapData = (MapData) mBundle.get("data");
		imageUrl = DataPersistence.getImgUrl(this);
		downloadMap(reloadMap);
		// setMapImage();
	}

	public void downloadMap(Boolean update) {
		// use ImageViewTouch lib to deal with image zooming and panning
		imageLoader = ImageLoader.getInstance();
		// if (loadedMap != null) {
		// mapView.setBitmap(loadedMap);
		// }
		mapView = (MapView) findViewById(R.id.map_image);
		// imageView = (ImageViewTouch) findViewById(R.id.map_image);
		mapView.setDisplayType(DisplayType.FIT_IF_BIGGER);
		imageLoader.displayImage(imageUrl, mapView, options,
				new SimpleImageLoadingListener() {
					@Override
					public void onLoadingStarted(String imageUri, View view) {
					}

					@Override
					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {
						String message = null;
						switch (failReason.getType()) { // fail type
						case IO_ERROR:
							message = "Input/Output error";
							break;
						case DECODING_ERROR:
							message = "Image can't be decoded";
							break;
						case NETWORK_DENIED:
							message = "Downloads are denied";
							break;
						case OUT_OF_MEMORY:
							message = "Out Of Memory error";
							break;
						case UNKNOWN:
							message = "Unknown error";
							break;
						}
						Toast.makeText(MapViewActivity.this, message,
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						if (!reloadMap) {
							loadedMap = loadedImage;
							reloadMap = true;
						}
					}
				});

	}

	/**
	 * Loads map
	 */
	public void loadMap() {
		/**
		 * Loading points
		 */
		Hashtable<String, String> hash = new Hashtable<String, String>(3);
		hash.put("imageId", Integer.valueOf(mData.imageId).toString());
		hash.put("token", DataPersistence.getToken(this));
		NetworkTask task = new NetworkTask(this,
				DataPersistence.getServerName(this), "/logs/pars/getpoint",
				false, hash, true);
		task.setTag(TAG_KEY, new Integer(TAG_GET_POINTS));
		NetworkManager.getInstance().addTask(task);
	}

	/**
	 * network task error
	 */
	public void nTaskErr(NetworkResult result) {
		// initTitleProgressBar(false);

		if (result.getResponceCode() == 401) {
			UiFactories.standardAlertDialog(this,
					getString(R.string.msg_error),
					getString(R.string.msg_alert_1), null);
		} else {
			UiFactories.standardAlertDialog(this,
					getString(R.string.msg_error),
					getString(R.string.msg_alert), null);
		}
	}

	/**
	 * network task success
	 */
	public void nTaskSucces(NetworkResult result) {
		try {
			XmlPullParser parser = XmlPullParserFactory.newInstance()
					.newPullParser();
			parser.setInput(new ByteArrayInputStream(result.getData()), "UTF-8");
			switch (((Integer) result.getTask().getTag(TAG_KEY)).intValue()) {
			// case TAG_LOG_SUBMIT:
			// break;
			case TAG_GET_POINTS:
				parser.nextTag();
				ArrayList<HashMap<String, String>> points = new ArrayList<HashMap<String, String>>();
				if (XmlPullParser.START_TAG == parser.getEventType()) {
					if (parser.getName().equalsIgnoreCase("images")) {
						while (parser.next() != XmlPullParser.END_DOCUMENT)
							if (parser.getEventType() == XmlPullParser.START_TAG
									&& parser.getName().equalsIgnoreCase("img")) {
								HashMap<String, String> data = new HashMap<String, String>();
								data.put("point_id", parser.getAttributeValue(
										null, "scan_point_id"));
								data.put("point_name", parser
										.getAttributeValue(null,
												"scan_point_name"));
								data.put("point_x", parser.getAttributeValue(
										null, "point_x"));
								data.put("point_y", parser.getAttributeValue(
										null, "point_y"));
								points.add(data);
							}
					}
				}

				// mMapView.setPoints(points);

				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * set options for image loader, mainly about cache issues
	 */
	private void setImageLoaderOption() {
		options = new DisplayImageOptions.Builder()
				// .showImageForEmptyUri(R.drawable.ic_empty)
				// .showImageOnFail(R.drawable.ic_error)
				.showImageForEmptyUri(R.drawable.ic_launcher)
				.showImageOnFail(R.drawable.ic_launcher)
				.resetViewBeforeLoading(true)
				.cacheInMemory(true)
				// cache to memory
				.cacheOnDisc(true)
				// cache to SD card
				.imageScaleType(ImageScaleType.EXACTLY)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.displayer(new FadeInBitmapDisplayer(300)).build();
	}

	private SensorEventListener mSensorListener = new SensorEventListener() {

		private static final int X = 0;

		private static final int Y = 1;

		private static final int Z = 2;
		private Float azimuth = null;
		private HashMap<Integer, ArrayList<Object[]>> data = new HashMap<Integer, ArrayList<Object[]>>();

		private float[] mOldValues = null;

		private Long timestamp;

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		public void onSensorChanged(SensorEvent event) {
			if (timestamp == null) {
				timestamp = System.currentTimeMillis();
			}

			if (!data.containsKey(event.sensor.getType())) {
				data.put(event.sensor.getType(), new ArrayList<Object[]>());
			}

			long sensorMilis = System.currentTimeMillis();

			if (Sensor.TYPE_MAGNETIC_FIELD == event.sensor.getType()
					&& azimuth != null) {

				AppLocationManager alm = AppLocationManager
						.getInstance(MapViewActivity.this);

				GeomagneticField field = new GeomagneticField(Double.valueOf(
						alm.getLatitude()).floatValue(), Double.valueOf(
						alm.getLongtitude()).floatValue(), Double.valueOf(
						alm.getAltitude()).floatValue(),
						System.currentTimeMillis());

				double trueHeading = azimuth + field.getDeclination();

				data.get(event.sensor.getType())
						.add(new Object[] { sensorMilis, event.values[0],
								event.values[1], event.values[2], trueHeading });
			} else if (Sensor.TYPE_ORIENTATION != event.sensor.getType()
					&& event.sensor.getType() != Sensor.TYPE_MAGNETIC_FIELD) {
				data.get(event.sensor.getType()).add(
						new Object[] { sensorMilis, event.values[0],
								event.values[1], event.values[2] });
			} else if (Sensor.TYPE_ORIENTATION == event.sensor.getType()
					&& event.sensor.getType() != Sensor.TYPE_MAGNETIC_FIELD) {
				azimuth = event.values[0];
				for (int i = 0; i < event.values.length; i++) {
					event.values[i] = (float) ((event.values[i] * Math.PI) / 180.0d);
				}
				event.values[X] -= 2 * Math.PI;
				float[] deltaRotationVector = null;

				if (mOldValues != null) {
					float axisX = event.values[X];
					float axisY = event.values[Y];
					float axisZ = event.values[Z];
					float dx = mOldValues[X] - axisX;
					float dy = mOldValues[Y] - axisY;
					float dz = mOldValues[Z] - axisZ;
					float omegaMagnitude = (float) Math.sqrt(dx * dx + dy * dy
							+ dz * dz);
					dx /= omegaMagnitude;
					dy /= omegaMagnitude;
					dz /= omegaMagnitude;
					deltaRotationVector = new float[4];
					float thetaOverTwo = omegaMagnitude / 2;
					float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
					float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
					deltaRotationVector[0] = sinThetaOverTwo * dx;
					deltaRotationVector[1] = sinThetaOverTwo * dy;
					deltaRotationVector[2] = sinThetaOverTwo * dz;
					deltaRotationVector[3] = cosThetaOverTwo;
					// is nan check
					deltaRotationVector[0] = Float
							.isNaN(deltaRotationVector[0]) ? 0
							: deltaRotationVector[0];
					deltaRotationVector[1] = Float
							.isNaN(deltaRotationVector[1]) ? 0
							: deltaRotationVector[1];
					deltaRotationVector[2] = Float
							.isNaN(deltaRotationVector[2]) ? 0
							: deltaRotationVector[2];
					deltaRotationVector[3] = Float
							.isNaN(deltaRotationVector[3]) ? 0
							: deltaRotationVector[3];
				}
				mOldValues = Arrays.copyOf(event.values, event.values.length);

				if (deltaRotationVector != null) {
					data.get(event.sensor.getType()).add(
							new Object[] { sensorMilis, event.values[0],
									event.values[1], event.values[2],
									deltaRotationVector[0],
									deltaRotationVector[1],
									deltaRotationVector[2],
									deltaRotationVector[3] });
				}

			}

			if (sensorMilis - timestamp > 1000) {
				writeToLogAndClearList();
			}
		}

		protected void writeToLogAndClearList() {

			timestamp = System.currentTimeMillis();
			LogWriterSensors.instance().write(data);
			data.clear();
		}
	};

	/**
	 * Shows alert to turn active wifi/mobile connections off
	 * 
	 * DO NOT TURN OFF FOR NOW
	 */
	public void alertActiveConnectionsTurnOff(final Runnable r) {

		// disableAllWifiNetworks();
		try {
			setMobileDataEnabled(this, false);
		} catch (Exception e1) {
			e1.printStackTrace();
			UiFactories.standardAlertDialog(this,
					getString(R.string.msg_alert),
					getString(R.string.msg_operation_failed), null);
		}

		if (r != null)
			r.run();

		// standardConfirmDialog(getString(R.string.msg_alert),
		// getString(R.string.msg_connections_turnoff), new OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// try {
		// setMobileDataEnabled(WifiSearcherActivity.this, false);
		// disableAllWifiNetworks();
		// if (r != null) {
		// r.run();
		// }
		// } catch (Exception e) {
		// standardAlertDialog(getString(R.string.msg_alert),
		// getString(R.string.msg_operation_failed), null);
		// }
		// }
		// }, new OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// if (r != null) {
		// r.run();
		// }
		// }
		// }, false);
	}

	/**
	 * Shows alert to turn active connections on
	 */
	public void alertActiveConnectionsTurnOn() {
		// Re enable data
		try {
			// if (mConnectionMobileEnabled)
			setMobileDataEnabled(MapViewActivity.this, true);

			// if (mConnectionWifiEnabled)
			// toggleWifiActiveConnection();

			mConnectionMobileEnabled = false;
			mConnectionWifiEnabled = false;

		} catch (Exception e) {
			UiFactories.standardAlertDialog(this,
					getString(R.string.msg_alert),
					getString(R.string.msg_operation_failed), null);
			Log.e(LOG_TAG, "error", e);
		}

		// standardConfirmDialog(getString(R.string.msg_alert),
		// getString(R.string.msg_connections_turnon), new OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// try {
		// if (mConnectionMobileEnabled)
		// setMobileDataEnabled(WifiSearcherActivity.this, true);
		//
		// if (mConnectionWifiEnabled)
		// toggleWifiActiveConnection();
		//
		// mConnectionMobileEnabled = false;
		// mConnectionWifiEnabled = false;
		//
		// } catch (Exception e) {
		// standardAlertDialog(getString(R.string.msg_alert),
		// getString(R.string.msg_operation_failed), null);
		// Log.e(LOG_TAG, "error", e);
		// }
		// }
		// }, null);
	}

	/**
	 * Clears scan
	 */
	public void clearScan() {
		/**
		 * Button controls states
		 */
		// mBtnStartScan.setEnabled(true);
		enableButtonsAfterScan(false);

		// mAdapter.reset();
		mManager.reset();

		mFilename = null;

		/**
		 * Reset info
		 */
		setScanInfo(0, 0, false);

		// mTimelapsedTv.setText("Time elapsed 0s");

		LogWriter.reset();
		LogWriterSensors.reset();

		// mMapView.resetMarkers();

		// mMapView.setFirstMarker();

		mFlagScan = false;

		mScanCompleted = false;
	}

	public void completeLogs() {
		if (!mScanCompleted) {
			// mService.stopScan(mMapData.imageId, mMapData.floorId,
			// mMapView.mMarker, mMapView.mMarker2);
			mScanCompleted = true;
		}
	}

	/**
	 * Buttons : Save, clear, submit scan
	 * 
	 * @param b
	 */
	public void enableButtonsAfterScan(boolean b) {
		// mBtnSaveScan.setEnabled(b);
		// mBtnClearScan.setEnabled(b);
		// mBtnSubmitScan.setEnabled(b);
	}

	/*
	 * public WifiSearcherAdapter getAdapter() { return mAdapter; }
	 */

	public void loadSummary() {
	}

	/**
	 * Summary Info
	 * 
	 * @param time
	 * @param readings
	 * @param x
	 * @param y
	 * @param map
	 * @param apCount
	 */
	public void setScanInfo(int readings, int apCount, boolean incTryCount) {
		// mNumberReadingsTv.setText(new
		// StringBuilder().append("Readings: ").append(readings));
		// mApCountTv.setText(new
		// StringBuilder().append("Total AP: ").append(apCount));

		if (incTryCount)
			// mAdapter.incTryCount();
			mManager.incTryCount();
	}

	public void startSensors() {
		mSensorManager.registerListener(mSensorListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(mSensorListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
				SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(mSensorListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
				SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(mSensorListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(mSensorListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void stopSensors() {
		try {
			mSensorManager.unregisterListener(mSensorListener);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Submits scan to sd card
	 */
	public void submitScan() {
		/**
		 * Submitting scan
		 */
		ArrayList<String> fileList = new ArrayList<String>();

		if (mFilename != null) {
			fileList.add(LogWriter.APPEND_PATH + mFilename + ".log");
		} else {
			fileList.add(LogWriter.APPEND_PATH + LogWriter.DEFAULT_NAME);
		}

		new HttpLogSender(this, DataPersistence.getServerName(this)
				+ getString(R.string.submit_log_url), fileList).setToken(
				DataPersistence.getToken(this)).execute();
	}

	/**
	 * Checks is mobile network connected
	 * 
	 * @return
	 */
	protected boolean isMobileInternetConnected() {
		NetworkInfo info = mConManager.getActiveNetworkInfo();
		return info != null && info.isConnectedOrConnecting()
				&& info.getType() == ConnectivityManager.TYPE_MOBILE;
	}

	/**
	 * Checks is wifi network connected
	 * 
	 * @return
	 */
	protected boolean isWiFiInternetConnected() {
		NetworkInfo info = mConManager.getActiveNetworkInfo();
		return info != null && info.isConnectedOrConnecting()
				&& info.getType() == ConnectivityManager.TYPE_WIFI;
	}

	@Override
	protected void onDestroy() {
		stopServices();
		super.onDestroy();
	}

	/**
	 * Starts scan
	 */
	protected void startScan() {
		// forgetAllWifiNetworks();
		// disableAllWifiNetworks();

		// enables scan
		if (isWifiAvailable()) {
			Runnable startScan = new Runnable() {

				@Override
				public void run() {
					// mService.startScan();
					mManager.startScans();
					mFlagScan = true;
				}
			};

			if (isWiFiInternetConnected()) {
				mConnectionWifiEnabled = true;
				mWifiActiveNetwork = mWifiManager.getConnectionInfo()
						.getNetworkId();
			}

			if (isMobileInternetConnected()) {
				mConnectionMobileEnabled = true;
			}

			if (mConnectionMobileEnabled || mConnectionWifiEnabled) {
				alertActiveConnectionsTurnOff(startScan);
			} else {
				startScan.run();
			}
		}
	}

	protected void stopScan() {
		stopSensors();
		// mService.pauseScan();
		mManager.stopScans();

		// Toast.makeText(this, R.string.msg_map_notification_points, 3000);

		enableButtonsAfterScan(true);

		if ((mConnectionMobileEnabled || mConnectionWifiEnabled)
				&& (mConManager.getActiveNetworkInfo() == null || !mConManager
						.getActiveNetworkInfo().isConnectedOrConnecting())) {
			alertActiveConnectionsTurnOn();
		}
	}

	/**
	 * Save scan data into log file
	 */
	public void saveScan() {
		final Dialog dialog = new Dialog(MapViewActivity.this);
		dialog.requestWindowFeature((int) Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_savescan);
		TextView log_file = (TextView) dialog.findViewById(R.id.text_log_name);
		Button btn_yes = (Button) dialog.findViewById(R.id.button_save_yes);
		Button btn_no = (Button) dialog.findViewById(R.id.button_save_no);
		final String log_name = getSaveCheckString();
		log_file.setText(log_name);
		final String log_filename = LogWriter.generateFilename() + map_info;

		btn_yes.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				LogWriter.instance().saveLog(log_filename + ".log");
				LogWriterSensors.instance().saveLog(log_filename + ".dev");
				dialog.dismiss();
				Toast.makeText(getApplicationContext(), "Data saved!",
						Toast.LENGTH_SHORT).show();
			}
		});
		btn_no.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
				Toast.makeText(getApplicationContext(), "Data not saved!",
						Toast.LENGTH_SHORT).show();
			}
		});
		dialog.show();
	}

	/**
	 * Stop service
	 */
	protected void stopServices() {
		/*
		 * try { if (mService != null) { mService.stopScan();
		 * mService.deInitWifi(); } unbindService(connection); stopService(new
		 * Intent(this, WifiSnifferService.class)); } catch (Exception e) {
		 * e.printStackTrace(); }
		 */
		mManager.stopScans();
		stopSensors();
	}

	/**
	 * Disables auto-connect to configured networks
	 */
	private void disableAllWifiNetworks() {
		for (WifiConfiguration conf : mWifiManager.getConfiguredNetworks()) {
			mWifiManager.disableNetwork(conf.networkId);
		}
	}

	private void forgetAllWifiNetworks() {
		for (WifiConfiguration conf : mWifiManager.getConfiguredNetworks()) {
			mWifiManager.removeNetwork(conf.networkId);
		}
	}

	/**
	 * Turn on/off mobile internet
	 * 
	 * @param context
	 * @param enabled
	 */
	private void setMobileDataEnabled(Context context, boolean enabled)
			throws Exception {
		final ConnectivityManager conman = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final Class<?> conmanClass = Class.forName(conman.getClass().getName());
		final Field iConnectivityManagerField = conmanClass
				.getDeclaredField("mService");
		iConnectivityManagerField.setAccessible(true);
		final Object iConnectivityManager = iConnectivityManagerField
				.get(conman);
		final Class<?> iConnectivityManagerClass = Class
				.forName(iConnectivityManager.getClass().getName());
		final Method setMobileDataEnabledMethod = iConnectivityManagerClass
				.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
		setMobileDataEnabledMethod.setAccessible(true);

		setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
	}

	/**
	 * Toggle active connection of Wifi
	 */
	private void toggleWifiActiveConnection() {
		WifiInfo connection = mWifiManager.getConnectionInfo();
		if (connection != null
				&& mConManager.getActiveNetworkInfo() != null
				&& mConManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI
				&& mConManager.getActiveNetworkInfo().isConnectedOrConnecting()) {
			// turn wifi connection off
			mWifiActiveNetwork = mWifiManager.getConnectionInfo()
					.getNetworkId();
			// mWifiManager.disableNetwork(mWifiActiveNetwork);
			Log.v(LOG_TAG, "disabled all configured networks");
		} else {
			// turn wifi connection on
			if (mWifiActiveNetwork != -1) {
				mWifiManager.enableNetwork(mWifiActiveNetwork, true);
				Log.v(LOG_TAG, "enabled wi-fi connection ");
			} else {
				Log.v(LOG_TAG, "nothing to enable ");
			}
		}
	}

	/**
	 * Is Wi-Fi enabled
	 * 
	 * @param context
	 * @return
	 */
	public boolean isWifiAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = null;
		if (connectivityManager != null) {
			networkInfo = connectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		}
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

	public String getSaveCheckString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(getString(R.string.check_save_1)).append("\n")
				.append(LogWriter.generateFilename()).append("-")
				.append(map_info).append("\n")
				.append(getString(R.string.check_save_2));
		return stringBuilder.toString();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is
		// present.
		getMenuInflater().inflate(R.menu.map_view, menu);
		return true;
	}
}
