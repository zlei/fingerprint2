package com.lighthousesignal.lsslib.findpoint;
import java.util.Vector;

import com.lighthousesignal.lsslib.ScanListener;

import com.lighthousesignal.lsslib.WifiScanResult;

import android.graphics.Bitmap;
import android.location.Location;

/**
 * An LSSListener receives location, map, and status updates from a LSSService
 */
public class LSSListener implements ScanListener {
	
	/**
	 * The service successfully logged into the server.
	 */
	static int MSG_LOGIN_SUCCESS = 20;
	
	/**
	 * The service could not log into the server.
	 */
	static int MSG_LOGIN_FAIL = 21;
	
	/**
	 * The service logged out successfully.
	 */
	static int MSG_LOGOUT_SUCCESS = 22;
	
	/**
	 * There was an error when logging out.
	 */
	static int MSG_LOGOUT_FAIL = 23;
	
	/**
	 * The service is sending results to the server.
	 */
	static int MSG_SENDING_DATA = 24;
	
	/**
	 * The service has received information from the server.
	 */
	static int MSG_RECEIVED_DATA = 25;
	
	/**
	 * The LSSService is aware of a new position.
	 */
	static int MSG_NEW_POSITION = 26;
	
	/**
	 * The user has available offers.
	 */
	static int MSG_OFFERS_AVAIL = 27;
	
	/**
	 * The user is within a geofence.
	 */
	static int MSG_FENCES_AVAIL = 28;
	
	/**
	 * There is more than one floor available.
	 */
	static int MSG_FLOORS_AVAIL = 29;
	
	/**
	 * A location could not be determined.
	 */
	static int MSG_NO_FIX_AVAIL = 30;
	
	/**
	 * This method is called when a new location has been determined.
	 * 
	 * @param loc The new location.
	 */
    public void onLocationChanged(Location loc) { }
    
    /**
     * This method is called when a new map has been downloaded.
     * 
     * @param map The new bitmap.
     */
    public void onNewMapAvailable(Bitmap map) { }
    
    /**
     * This method is called when the status of the LSSService or LSSManager has changed.
     * 
     * @param code The message id. (See static int MSG_* above)
     */
    public void onStatusChanged(int code) { }

	public void onWifiDataAvailable(Vector<WifiScanResult> wifiData) { }
}
