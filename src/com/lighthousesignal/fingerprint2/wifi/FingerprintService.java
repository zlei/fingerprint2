package com.lighthousesignal.fingerprint2.wifi;

import java.util.Vector;

import android.util.Log;

import com.lighthousesignal.lsslib.ScanService;
import com.lighthousesignal.lsslib.WifiData;

public class FingerprintService extends ScanService {

	private Vector<WifiData> mWifiData = new Vector<WifiData>();

	@Override
	protected void onStatusChanged(int status) {
		Log.d("LSS FingerprintService", "New status " + status);
	}

	@Override
	protected void onWifiDataAvailable(WifiData data) {
		mWifiData.add(data);
	}

	public Vector<WifiData> getCollectedData() {
		return mWifiData;
	}

	public void clearCollectedData() {
		mWifiData.clear();
	}
}
