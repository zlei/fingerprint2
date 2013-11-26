package com.lighthousesignal.lsslib.findpoint;

import java.util.ArrayList;
import java.util.Vector;

import com.lighthousesignal.lsslib.ApiUtil;

import com.lighthousesignal.lsslib.Fence;
import com.lighthousesignal.lsslib.Floor;
import com.lighthousesignal.lsslib.NetworkListener;
import com.lighthousesignal.lsslib.NetworkResult;
import com.lighthousesignal.lsslib.NetworkTask;
import com.lighthousesignal.lsslib.Offer;
import com.lighthousesignal.lsslib.ScanListener;
import com.lighthousesignal.lsslib.ScanService;
import com.lighthousesignal.lsslib.WifiScanResult;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;

/**
 * A service to scan for wifi networks and determine the location of the user.
 */
public class LSSService extends ScanService implements NetworkListener, LocationListener {
	
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
	
	/**
	 * If set to true, appends information about the users device to be processed along with other api requests.
	 */
	public static boolean ADD_DEVICE_INFO = false;
	
	/**
	 * If set to true, appends the developer and user id with api requests.
	 */
	public static boolean ADD_USER_INFO = false;
	
	/**
	 * If set to true, use gps data to determine if a user is inside a building.
	 * Also appends lat/lon to log.
	 * Requires permission android.permission.ACCESS_FINE_LOCATION
	 */
	public static boolean USE_GPS_DATA = false;
	
	protected final IBinder binder = new ServiceBinder();		//Object that receives interactions from listener
	protected static boolean running = false;
	protected static boolean scanning = false;
	
	//Used to access wifi
	protected WifiManager mWifiManager;
	protected WifiLock mWifiLock;
	
	//Holds wifi results as they arrive at broadcast receiver
	protected Vector<WifiScanResult> mResults;
	protected SharedPreferences mPreferences;
	
	//Schedules periodic scan-sets
	protected Handler mPeriodicHandler;
	
	//Receives updates
	protected static LSSListener mListener;
	
	//Api specific server communications
	protected ApiUtil mApiUtil;
	
	public static int currentFloorId = -1;
	
	//Used to identify feedback
	public int queryId = -1;
	
	//Array of floors by floor_id key
	protected static SparseArray<Floor> floors;
	
	//Array of offers by key offer_id
	protected SparseArray<Offer> offers;
	
	//Array of fences
	protected ArrayList<Fence> fences;
	
	//Keeps track of how long scans have been running.
	protected long mStartTime;
	protected long mEndTime;
	//Used to determine average of wifi scans
	protected int mCount;
	
	protected static LocationManager lm;
	protected static Location lastGpsLocation;
	
	public static int selected_floor = -1;
	
	@Override
	public void onCreate() {
		super.onCreate();
		running = true;
		
		mPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	
		floors = new SparseArray<Floor>();
		mApiUtil = new V1Util();
		mPeriodicHandler = new Handler();
		
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		if(USE_GPS_DATA) {
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, this);
		}
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
		
		if(USE_GPS_DATA){
			lm.removeUpdates(this);
		}
		super.onDestroy();
	}
	
	/**
	 * Is the service running?
	 * @return The state of the LSSService
	 */
	public static boolean isRunning(){
		return running;
	}
	
	/**
	 * The LSSService will return location, map, and status updates to this LSSListener.
	 * @param listener LSSListener to receive updates.
	 */
	public void setListener(LSSListener listener) {
		mListener = listener;
		mListener.onStatusChanged(ScanListener.MSG_LISTENER_ATTACHED);
	}
	
	/**
	 * @return The LSSListener
	 */
	public ScanListener getListener(){
		return mListener;
	}

	/**
	 * @return The current scan mode of the LSSService. See LSSService MODEs.
	 */
	public int getScanMode() {
		return mPreferences.getInt("scan_mode", DEFAULT_SCAN_MODE);
	}
	
	/**
	 * The LSSService will scan for a certain time before sending the results.
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
	
	/**
	 * The stored login token is updated after a successful 'login' NetworkTask.
	 * @return The login token
	 */
	public String getLoginToken(){
		return mPreferences.getString("login_token", "notoken");
	}
	
	/**
	 * Tell the LSSService what floors are applicable to it.
	 * This is usually called by an Api task.
	 * @param floors The array of floor objects.  See Floor class.
	 */
	public void setFloors(SparseArray<Floor> floors){
		LSSService.floors = floors;
		mListener.onStatusChanged(LSSListener.MSG_FLOORS_AVAIL);
	}
	
	/**
	 * @return The floors that the service is aware of.
	 */
	public SparseArray<Floor> getFloors(){
		if(floors == null){
			return new SparseArray<Floor>();
		}
		return floors;
	}

	/**
	 * Used by Api tasks to update service of available offers
	 * @param offers New sparse array of offers
	 */
	public void setOffers(SparseArray<Offer> offers){
		this.offers = offers;
	}
	
	/**
	 * @return The Offers that the service is aware of.
	 */
	public SparseArray<Offer> getOffers(){
		if(offers == null){
			return new SparseArray<Offer>();
		}
		return offers;
	}
	
	/**
	 * Tell the LSSService the fences that are currently available.
	 * Called by an Api task.
	 * @param fences ArrayList of Fence objects.
	 */
	public void setFences(ArrayList<Fence> fences){
		this.fences = fences;
	}
	
	/**
	 * @return The Fences that the service is aware of.
	 */
	public ArrayList<Fence> getFences(){
		if(fences == null){
			return new ArrayList<Fence>();
		}
		return fences;
	}
	
	/**
	 * Changes the current floor.
	 * If the map image does not exist, it is downloaded.
	 * @param floorId The id (not floor#) of the requested Floor.
	 */
	public void setFloor(int floorId){
		if(floors.get(floorId) != null){	//Is this a valid floor id
			currentFloorId = floorId;
			if(floors.get(floorId).getMapImage() == null){
				downloadMapImage();
			}else{
				mListener.onNewMapAvailable(floors.get(floorId).getMapImage());
			}
		}
	}

	/*
	 * Called on the completion of a set of scans.
	 * -Calls findPoint()
	 * -Sets up the periodic handler based on the scan mode.
	 */
	protected void finishScans() {
		scanning = false;
		mListener.onStatusChanged(LSSListener.MSG_SCANS_STARTED);
		findPoint();
		if(getScanMode() == MODE_CONTINUOUS){
			mPeriodicHandler.postDelayed(doScans, getScanPeriod() * 1000);
		}
	}
	
	/*
	 * Creates and executes a ApiUtil.findPointTask()
	 */
	@SuppressLint("NewApi")
	public void findPoint(){
		int time = (int)((mEndTime - mStartTime)/1000);
				
		mListener.onStatusChanged(LSSListener.MSG_SENDING_DATA);
		NetworkTask fpt = mApiUtil.findPointTask(this,getLoginToken(),mResults,mCount,time,currentFloorId, getBaseContext());
		mListener.onStatusChanged(LSSListener.MSG_SENDING_DATA);
		
		if (android.os.Build.VERSION.SDK_INT >= 11) {
		    // Use APIs supported by API level 11 (Android 3.0) and up
			fpt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			fpt.execute();
		}
		
	}
	
	/*
	 * Creates and executes a ApiTask.downloadMapTask()
	 */
	@SuppressLint("NewApi")
	public void downloadMapImage(){
		if(mPreferences.getBoolean("download_maps", true)){
			NetworkTask mapTask = mApiUtil.downloadMapTask(this, getLoginToken(), floors.get(currentFloorId).getImagePath(), currentFloorId);
			if (android.os.Build.VERSION.SDK_INT >= 11) {
			    // Use APIs supported by API level 11 (Android 3.0) and up
				mapTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} else {
				mapTask.execute();
			}
		}
	}
	
	/*
	 * Called by a completed NetworkTask.
	 */
	public void onTaskSuccess(NetworkResult result) {
		switch(result.getResultType()){
			case FIND_POINT:
				mListener.onStatusChanged(LSSListener.MSG_RECEIVED_DATA);
				mApiUtil.processFindPointResponse(this, result.getData(), result.getQueryId());
				if(getScanMode() == MODE_SINGLE)LSSService.running = false;
				break;
			case GET_IMAGE:
	    		try {
	    			byte[] data = result.getData();
	    			result.setData(null);
	    			byte[] byteArrayForBitmap = new byte[16*1024]; 
	    			BitmapFactory.Options opt = new BitmapFactory.Options(); 
					opt.inTempStorage =  byteArrayForBitmap; 
					Bitmap mapImage = BitmapFactory.decodeByteArray(data, 0, data.length, opt);
					
					floors.get(result.getQueryId()).setMapImage(mapImage);
					
					if(mListener != null)mListener.onNewMapAvailable(mapImage);
					data = null;
					byteArrayForBitmap = null;					
				} catch (OutOfMemoryError ex) {
					//System.out.println("Out of memory.");
				}
	    		break;
		}
	}

	/*
	 * Called when a NetworkTask finishes with an error.
	 */
	public void onTaskError(NetworkResult result) {
		Log.d("LSSService", "onTaskError");
		if(result != null) {
			Log.d("LSSService", "response code " + result.getResponseCode());
		}
		//System.out.println("Netowk error: " + result.getException() + "\nCode: " + result.getResponseCode());
	}

	public ApiUtil getApi() {
		return mApiUtil;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	/*
	 * Used to bind with an activity.
	 */
	public class ServiceBinder extends Binder {
		public LSSService getService() {
			return LSSService.this;
		}
	}

	/*
	 * GPS
	 */
	@Override
	public void onLocationChanged(Location loc) {
		//System.out.println("New gps data");
		//System.out.println(loc.getAccuracy() + " " + loc.getLatitude() + " " + loc.getLongitude());
		lastGpsLocation = loc;
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	
	public static Location getLastGpsLocation() {
		return lastGpsLocation;
	}
}
