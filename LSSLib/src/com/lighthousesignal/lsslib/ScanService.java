package com.lighthousesignal.lsslib;
import java.util.List;
import java.util.Vector;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;

/**
 * A service to scan for wifi networks
 */
public class ScanService extends Service {
	
	/**
	 * The service will scan for networks and request location updates periodically.
	 */
	public static final int MODE_CONTINUOUS = 0;
	
	/**
	 * The service will only determine a location once, until it is restarted.
	 */
	public static final int MODE_SINGLE = 1;
	
	/**
	 * Not yet implemented
	 */
	public static final int MODE_AUTOMATIC = 2;
	
	public static final int DEFAULT_SCAN_MODE = 0;
	public static final int DEFAULT_SCAN_DURATION = 6;
	public static final int DEFAULT_SCAN_PERIOD = 10;

	//Object that receives interactions from listener
	protected final IBinder binder = new ServiceBinder();		
	protected static boolean running = false;
	protected static boolean scanning = false;

	//Listener
	protected ScanListener mListener;
	
	//Used to access wifi
	protected WifiManager mWifiManager;
	protected WifiLock mWifiLock;
	
	//Holds wifi results as they arrive at broadcast receiver
	protected Vector<WifiScanResult> mResults;
	protected SharedPreferences mPreferences;
	
	//Schedules periodic scan-sets
	protected Handler mPeriodicHandler;
	
	//Keeps track of how long scans have been running.
	protected long mStartTime;
	protected long mEndTime;

	//Used to determine average of wifi scans
	protected int mCount;
	
	@Override
	public void onCreate() {
		super.onCreate();
		running = true;
		
		mPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	
		mPeriodicHandler = new Handler();
		
	}
	
	@Override
	public void onDestroy() {
		mListener.onStatusChanged(ScanListener.MSG_SERVICE_STOPPED);
		running = false;
		
		try{
			unregisterReceiver(scanResultsAvailable);
		}catch(IllegalArgumentException ex){
			/*not registered*/;
		}
		mPeriodicHandler.removeCallbacks(doScans);
		
		super.onDestroy();
	}
	
	/**
	 * Is the service running?
	 * @return The state of the ScanService
	 */
	public static boolean isRunning(){
		return running;
	}
	
	/**
	 * @param listener ScanListener to receive updates.
	 */
	public void setListener(ScanListener listener) {
		mListener = listener;
		mListener.onStatusChanged(ScanListener.MSG_LISTENER_ATTACHED);
	}
	
	/**
	 * @return The ScanListener
	 */
	public ScanListener getListener(){
		return mListener;
	}

	/**
	 * @return The current scan mode of the ScanService. See ScanService MODEs.
	 */
	public int getScanMode() {
		return mPreferences.getInt("scan_mode", DEFAULT_SCAN_MODE);
	}
	
	/**
	 * The ScanService will scan for a certain time before sending the results.
	 * The results obtained over this time are averaged to improve accuracy.
	 * @return The duration (seconds) of a single set of scans.
	 */
	public int getScanDuration() {
		return mPreferences.getInt("scan_duration", DEFAULT_SCAN_DURATION);
	}
	
	/**
	 * @return The seconds to wait in between sets of scans. (In continuous mode)
	 */
	public int getScanPeriod() {
		System.out.println("Scan period is " + mPreferences.getInt("scan_period", DEFAULT_SCAN_PERIOD));
		return mPreferences.getInt("scan_period", DEFAULT_SCAN_PERIOD);
	}
	
	/*
	 * This is called by the periodic handler in certain scan modes to run another set of scans.
	 */
	protected Runnable doScans = new Runnable() {
		public void run(){
			startScans();
		}
	};
	
	/*
	 * Prepares for a set of scans.
	 * -Resets data structures holding wifi results from last time.
	 * -Requests a wifi lock.
	 * -Sets up IntentFilter to receive results.
	 * -Prepares variables for timing.
	 * -Starts one scan.
	 * 	(On completion of scan, scanResultsAvailable.onReceive() will be called)
	 */
	public void startScans() {
		mListener.onStatusChanged(ScanListener.MSG_SCANS_STARTED);
		
		mCount=0;

		if(mResults == null){
			mResults = new Vector<WifiScanResult>();
		}else{
			mResults.clear();
		}
		
		mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		mWifiLock = mWifiManager.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, "LOCK");
		
		if(mWifiLock != null){
			mWifiLock.acquire();
		}else{
			mListener.onStatusChanged(ScanListener.MSG_ERR_WIFI_NOT_AVAIL);
			return;
		}
		
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		registerReceiver(scanResultsAvailable, mFilter);
		
		mStartTime = System.currentTimeMillis();
		scanning = true;
		mWifiManager.startScan();
	}
	
	/*
	 * Called on the completion of a set of scans.
	 * -Calls findPoint()
	 * -Sets up the periodic handler based on the scan mode.
	 */
	protected void finishScans() {
		scanning = false;
		mListener.onStatusChanged(ScanListener.MSG_SCANS_STOPPED);
		mListener.onWifiDataAvailable(mResults);
		if(getScanMode() == MODE_CONTINUOUS){
			mPeriodicHandler.postDelayed(doScans, getScanPeriod() * 1000);
		}
	}

	/*
	 * This BroadcastReceiver handles completed wifi scans.
	 */
	protected BroadcastReceiver scanResultsAvailable = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(scanning){ //Is this ours or system's scan

				if(mWifiManager == null){
					mListener.onStatusChanged(ScanListener.MSG_ERR_WIFI_NOT_AVAIL);
				}
				
				List<ScanResult> newResults = mWifiManager.getScanResults();
				mResults = Util.addResults(mResults, newResults);
				
				//If any networks were found, increase the scan count counter;
				if(newResults.size() > 0) mCount++;
				
				if((System.currentTimeMillis() - mStartTime) < (long)(getScanDuration()*1000)){
					mWifiManager.startScan();
				}else{
					mEndTime = System.currentTimeMillis();
										
					finishScans();
				}
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	/*
	 * Used to bind with an activity.
	 */
	public class ServiceBinder extends Binder {
		public ScanService getService() {
			return ScanService.this;
		}
	}
}
