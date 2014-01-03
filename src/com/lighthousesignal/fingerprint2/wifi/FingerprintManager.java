package com.lighthousesignal.fingerprint2.wifi;

import java.util.Vector;

import android.content.Context;
import android.util.Log;

import com.lighthousesignal.lsslib.DeviceData;
import com.lighthousesignal.lsslib.ServiceManager;
import com.lighthousesignal.lsslib.WifiData;

public class FingerprintManager extends ServiceManager<FingerprintService> {

	private int mode = 0;

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
		Vector<WifiData> collection = ((FingerprintService) mService)
				.getCollectedData();
		Log.d("collected data ", collection.get(0).toJSONArray().toString());

		// Get deviceData instance
		DeviceData device = DeviceData.getInstance();
		Log.d("device info", device.toJSON(mContext).toString());

		// Make the fingerprint network task.
		// The network listener may be this,
		// or perhaps the mapView
		// NetworkTask task = new V2Util.fingerprintTask(listener, token,
		// collection, device);

		// TODO Execute task
	}

	public void incTryCount() {
		// TODO?
	}

	public void reset() {
		((FingerprintService) mService).clearCollectedData();
	}
}
