package com.lighthousesignal.lsslib;
import java.util.Vector;
import java.util.List;

import android.net.wifi.ScanResult;

public class Util {
	public static Vector<WifiScanResult> addResults(Vector<WifiScanResult> oldData, List<ScanResult> newData){
		for(ScanResult newResult: newData){
			boolean isResultAdded = false;
			for(WifiScanResult result: oldData){
				if(result.getBSSID().equalsIgnoreCase(newResult.BSSID)) {
					result.add(newResult.level, -100);
					isResultAdded = true;
					break;
				}
			}
			
			if(!isResultAdded) {
				WifiScanResult res = new WifiScanResult(newResult.BSSID, newResult.SSID, ""+newResult.frequency);
				res.add(newResult.level, -100);
				oldData.add(res);
			}
		}
		return oldData;
	}
}
