package com.lighthousesignal.lsslib;

import java.util.Vector;

public interface ScanListener {

	/**
	 * The LSSService has attached to an LSSListener.
	 */
	static int MSG_LISTENER_ATTACHED = 1;

	/**
	 * The LSSService has stopped.
	 */
	static int MSG_SERVICE_STOPPED = 2;

	/**
	 * The service could not get a wifi lock.
	 */
	static int MSG_ERR_WIFI_NOT_AVAIL = 3;
	
	/**
	 * The LSSService has begun scanning for wifi networks.
	 */
	static int MSG_SCANS_STARTED = 4;

	/**
	 * The LSSService has completed scanning for wifi networks.
	 */
	static int MSG_SCANS_STOPPED = 5;

	public void onStatusChanged(int status);

	public void onWifiDataAvailable(Vector<WifiScanResult> data);

}
