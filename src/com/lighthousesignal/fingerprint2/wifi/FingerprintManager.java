package com.lighthousesignal.fingerprint2.wifi;

import java.util.Vector;

import android.content.Context;
import android.util.Log;

import com.lighthousesignal.lsslib.ScanListener;
import com.lighthousesignal.lsslib.ServiceManager;
import com.lighthousesignal.lsslib.WifiScanResult;

public class FingerprintManager extends ServiceManager implements ScanListener {

	private int mode = 0;
	
	public FingerprintManager(Context context) {
		super(context);
	}

	public void startScans() {
		startService(mode);
	}
	
	public void stopScans() {
		stopService();
	}
	
	public void incTryCount() {
		//TODO
	}
	
	public void reset() {
		//TODO
	}

	@Override
	public void onStatusChanged(int status) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onWifiDataAvailable(Vector<WifiScanResult> data) {
		// TODO Auto-generated method stub
		Log.d("LSS FingerprintManger", "Wifi data available.");
		for(WifiScanResult r : data) {
			Log.d("LSS FingerprintManger", "\t\t " + r.getSSID());
		}
	}
}
