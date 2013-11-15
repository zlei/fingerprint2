/*
 * For the full copyright and license information, please view the LICENSE file that was distributed
 * with this source code. (c) 2011
 */

package com.lighthouse.fingerprint2.networks;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PointF;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.lighthouse.fingerprint2.R;
import com.lighthouse.fingerprint2.activities.BasicActivity;
import com.lighthouse.fingerprint2.activities.MapViewActivity;
import com.lighthouse.fingerprint2.logs.ErrorLog;
import com.lighthouse.fingerprint2.logs.LogWriter;
import com.lighthouse.fingerprint2.logs.LogWriterSensors;
import com.lighthouse.fingerprint2.utilities.Radio;
import com.lighthouse.fingerprint2.utilities.Utils;

/**
 * Wi-Fi sniffer
 * 
 * @version SVN: $Id$
 */
public class WifiSnifferService extends Service {

	public class ServiceBinder extends Binder {
		public WifiSnifferService getService() {
			return WifiSnifferService.this;
		}
	}

	public int mApCount = 0;

	public int mCount = 0;

	public Vector<WifiScanResult> mResults;
	public int mScanTime = 10;
	public long mStartTime;
	public int mTime;
	protected AlertDialog dialog;
	protected boolean mConfigScanDeleteNotFound = false;
	protected long mEndTime;

	protected ProgressDialog mLoaderDialog;

	PhoneStateListener listener;

	protected boolean mScanInProgress = false;

	protected boolean mScanStopped = false;

	protected Timer mTimeTicker;

	public AtomicInteger mTimeElapsed = new AtomicInteger(0);

	protected WifiManager mWifiManager;
	protected TelephonyManager mTeleManager;

	protected List<ScanResult> newResults;

	MapViewActivity mDelegate;

	BroadcastReceiver onScan = new BroadcastReceiver() {

		public int counter = 0;

		public void onReceive(Context c, Intent i) {

			if (mScanStopped) {
				return;
			}

			Log.v(BasicActivity.LOG_TAG, "wifi results");

			if (mLoaderDialog.isShowing()) {
				mLoaderDialog.dismiss();
				initTicker();
			}

			if (mWifiManager == null) {
				Utils.errorFeedBack(WifiSnifferService.this,
						"WifiManager is null");
				return;
			}

			mWifiManager.startScan();

			newResults = mWifiManager.getScanResults(); // Returns a <list> of
			////////////////////wifi information//////////////
			Log.d("results", newResults.toString());

			counter++;

			collectData();

		}
	};

	private final IBinder binder = new ServiceBinder();

	private WifiLock mWifiLock;

	private Vector<String> scanData;

	/**
	 * Collects data
	 */
	public void collectData() {
		mScanInProgress = true;

		if (newResults != null) {

			@SuppressWarnings("unchecked")
			List<ScanResult> wifiResults = (List<ScanResult>) ((ArrayList<ScanResult>) newResults)
					.clone();

			try {
				mApCount = 0;

				for (ScanResult newResult : wifiResults) {

					LogWriter
							.instance()
							.addLog(new StringBuilder()
									.append(newResult.BSSID)
									.append(";")
									.append(newResult.SSID.replaceAll(
											"[^0-9a-zA-Z]", "_"))
									.append(";")
									.append(newResult.level)
									.append(";-100;")
									.append(newResult.frequency)
									.append(";")
									.append(Radio
											.getChannelFromFrequency(newResult.frequency))
									.append(";")
									.append(System.currentTimeMillis())
									.toString());

					boolean isResultAdded = false;
					for (WifiScanResult result : mResults) {
						if (result.getBSSID().equalsIgnoreCase(newResult.BSSID)) {
							result.add(newResult.level, -100);
							isResultAdded = true;
							break;
						}
					}
					if (!isResultAdded) {
						WifiScanResult res = new WifiScanResult(
								newResult.BSSID, newResult.SSID, ""
										+ newResult.frequency);
						res.add(newResult.level, -100);
						mResults.add(res);
					}

				}

				if (mConfigScanDeleteNotFound) {

					@SuppressWarnings("unchecked")
					Vector<WifiScanResult> mResultsClone = (Vector<WifiScanResult>) mResults
							.clone();

					/**
					 * Deleting wi-fi access point from a list
					 */
					for (WifiScanResult result : mResultsClone) {
						boolean found = false;
						for (ScanResult newResult : wifiResults) {
							if (result.getBSSID().equalsIgnoreCase(
									newResult.BSSID)) {
								found = true;
							}
						}

						if (!found) {
							mResults.remove(result);
							mDelegate.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									mDelegate.getAdapter()
											.notifyDataSetInvalidated();
								}
							});

						}
					}
				}

				mApCount = mResults.size();

				mCount++;

				mDelegate.runOnUiThread(new Runnable() {
					public void run() {
						mDelegate.setScanInfo(mCount, mApCount, true);
					}
				});
			} catch (Exception e) {
				ErrorLog.e(e);

				Log.e(BasicActivity.LOG_TAG, "error", e);

				e.printStackTrace();

				stopScan();

				mDelegate.runOnUiThread(new Runnable() {
					public void run() {
						mDelegate.standardAlertDialog(mDelegate
								.getString(R.string.msg_error), mDelegate
								.getString(R.string.msg_alert),
								null);
					}
				});
			}
		}

	}

	public void deinitTicker() {
		mTimeTicker.cancel();
	}

	public void deInitWifi() {
		// try {
		// unregisterReceiver(onScan);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	public BroadcastReceiver getOnScan() {
		return onScan;
	}

	public WifiManager getWifiManager() {
		return mWifiManager;
	}

	/*
	 * Inits time countings
	 */
	public void initTicker() {
		mTimeTicker = new Timer("Ticker");

		mTimeTicker.scheduleAtFixedRate(new TimerTask() {

			public void run() {
				mDelegate.runOnUiThread(new Runnable() {
					@Override
					public void run() {
/**						mDelegate.mTimelapsedTv.setText(new StringBuilder()
								.append("Time elapsed ")
								.append(Integer.valueOf(
										mTimeElapsed.incrementAndGet())
										.toString()).append("s"));
*/					}
				});
			}
		}, 0, 1000);
	}

	public void initWifi() {
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mWifiLock = mWifiManager.createWifiLock(
				WifiManager.WIFI_MODE_SCAN_ONLY, "LOCK");
		if (mWifiLock != null)
			mWifiLock.acquire();
		if (mWifiManager == null) {
			Utils.errorFeedBack(this, "WifiManager unavailable");
			return;
		}
		System.out.println("Start Scan");
		IntentFilter i = new IntentFilter();
		i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		registerReceiver(onScan, i);
	}

	/*
	 * public void initCellTowers() { mTeleManager = (TelephonyManager)
	 * getSystemService(Context.TELEPHONY_SERVICE); if (mTeleManager == null) {
	 * Utils.errorFeedBack(this, "TelephonyManager unavailable"); return; }
	 * 
	 * listener = new PhoneStateListener() {
	 * 
	 * @Override public void onSignalStrengthsChanged(SignalStrength
	 * signalStrength) {
	 * 
	 * int rss = -113 + 2*signalStrength.getCdmaDbm();
	 * LogWriter.instance().setSignalStrength(rss); Log.i("Call Signal!",
	 * "strength = " + rss);
	 * 
	 * super.onSignalStrengthsChanged(signalStrength); } };
	 * 
	 * mTeleManager.listen(listener, PhoneStateListener.LISTEN_SIGNAL_STRENGTH);
	 * }
	 */

	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onDestroy() {
		deInitWifi();
		super.onDestroy();
	}

	public void pauseScan() {
		mScanStopped = true;
		deinitTicker();
	}

	public void resetStatistics() {

		LogWriter.reset();
		LogWriterSensors.reset();

		mCount = 0;
		mTime = 0;
		scanData = new Vector<String>();
		mTimeElapsed.set(0);
		if (mResults == null)
			mResults = new Vector<WifiScanResult>();
		else
			mResults.clear();
	}

	public void setDelegate(MapViewActivity activity) {
		mDelegate = activity;
	}

	public void startScan() {

		mScanStopped = false;

		initWifi();
		// TODO: Ale
		// initCellTowers();

		mWifiManager.startScan();

		mDelegate.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mLoaderDialog = new ProgressDialog(mDelegate);
				mLoaderDialog.setMessage(mDelegate
						.getString(R.string.msg_dialog_wifi_scanning));
				mLoaderDialog.setCancelable(false);
				mLoaderDialog.show();
			}
		});

		resetStatistics();

		if (!LogWriter.instance().isOk()) {
			Utils.errorFeedBack(this, "please insert SD card");
			return;
		}

		mDelegate.startSensors();

	}

	/**
	 * Stops scans
	 */
	public void stopScan() {
		stopScan(-1, -1, null, null);
	}

	/**
	 * Stops scan
	 * 
	 * @param imageId
	 * @param buildingId
	 * @param pos
	 */
	public void stopScan(int imageId, int buildingId, PointF pos, PointF pos2) {
		try {
			mScanInProgress = false;

			LogWriter.instance().closeLog();
			LogWriter.instance().addScanParams(mTimeElapsed.get(), mCount);
			LogWriter.instance().addScanStatistics(mResults, mCount);
			LogWriter.instance().addLocation(
					mDelegate.getLocationManager().getLatitude(),
					mDelegate.getLocationManager().getLongtitude());
			LogWriter.instance().addDeviceInfo(WifiSnifferService.this);
			LogWriter.instance().addCustomerId(
					mDelegate.getLocalPreferences().getString(
							BasicActivity.PREF_CUSTOMER_ID, ""));
			LogWriter.instance().addDeveveloperId(
					mDelegate.getLocalPreferences().getString(
							BasicActivity.PREF_DEVELOPER_ID, ""));

			// TODO:
			// LogWriter.instance().addCellTowersInfo(WifiSnifferService.this);

			if (!(imageId == -1 && buildingId == -1 && pos == null))
				LogWriter.instance().addImage(imageId, buildingId, pos, pos2);
			LogWriter.instance().endLog();
			LogWriterSensors.instance().endLog();

		} catch (Exception e) {
			ErrorLog.e(e);
			e.printStackTrace();
		}
	}
}