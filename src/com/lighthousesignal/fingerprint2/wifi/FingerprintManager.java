package com.lighthousesignal.fingerprint2.wifi;

import java.util.Vector;

import android.content.Context;

import com.lighthousesignal.fingerprint2.logs.LogWriter;
import com.lighthousesignal.lsslib.DeviceData;
import com.lighthousesignal.lsslib.ServiceManager;
import com.lighthousesignal.lsslib.WifiData;
import com.lighthousesignal.lsslib.XMLLogWriter;

public class FingerprintManager extends ServiceManager<FingerprintService> {

	private int mode = 0;

	private Vector<WifiData> collection;

	public FingerprintManager(Context context) {
		super(context, FingerprintService.class);
	}

	public void startScans() {
		startService(mode);
	}

	public void stopScans() {
		stopService();
	}

	// TODO
	// This should be called when the scan is finished
	// and the logs should be submitted
	// probably from map view
	public void finishScans() {
		// Get wifi data from service from this round
		collection = ((FingerprintService) mService).getCollectedData();

		// Make the fingerprint network task.
		// The network listener may be this,
		// or perhaps the mapView
		// NetworkTask task = new V1Util.fingerprintTask(listener, token,
		// collection, device);

		// TODO Execute task
	}

	public void incTryCount() {
		// TODO?
	}

	public Vector<WifiData> getWifiData() {
		return collection;
	}

	public void reset() {
		((FingerprintService) mService).clearCollectedData();
	}
}
