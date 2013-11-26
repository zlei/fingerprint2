package com.lighthousesignal.lsslib;

import java.util.Vector;

import android.content.Context;
import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.telephony.TelephonyManager;

public class XMLMaker {
	
	private String logtext = "";
	
	public static final String _logroot = "logroot";
	public static final String _log = "log";
	public static final String _lat = "lat";
	public static final String _lon = "lon";
	public static final String _loc = "loc";
	public static final String _logtext = "logtext";
	public static final String _image = "image";
	public static final String _name = "name";
	public static final String _param = "param";
	public static final String _scantime = "scantime";
	public static final String _scancnt = "scancnt";
	public static final String _stat = "stat";


	public XMLMaker() {
		logtext = new String();
		String str = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<%s>\n", _logroot);
		logtext += str;
	}
	
	public void addToLog(Vector data) {
		String str = String.format("<%s>\n",_logtext);
		for (Object tmp: data) {
			str += String.format("<%s>%s;</%s>\n",_log, tmp.toString(), _log);
		}
		str += String.format("</%s>\n",_logtext);
		logtext += str;
	}

	public void addLocation(Location loc) {
		if(loc == null) return;
		double latitude = loc.getLatitude();
		double longitude = loc.getLongitude();
		long time = loc.getTime();
		float accuracy = loc.getAccuracy();
		String provider = loc.getProvider();
		int numSats = loc.getExtras().getInt("satellites", -1);
		//String xml = String.format("<%s %s='%.6f' %s='%.6f' />\n",_loc,_lat,latitude,_lon,longitude);
		String xml = String.format("<%s %s='%f' %s='%f' %s='%f' %s='%d' %s='%s' %s='%d'/>\n",_loc,_lat,latitude,_lon,longitude,"accuracy",accuracy, "time", time, "provider", provider, "satellites",numSats);
	
		logtext += xml;
	}

	public void addImageName(String name, int b_id, Point pos) {
		String xml = String.format("<%s name=\'%s\' building_id=\'%d\' x=\'%.1f\' y=\'%.1f\' />\n",_image,name,b_id,pos.x,pos.y);
		logtext += xml;
	}

	public void addImage(int i_id, int b_id, Point pos) {
		String xml= String.format("<%s id=\'%d\' building_id=\'%d\' x=\'%.1f\' y=\'%.1f\' />\n",_image,i_id,b_id,pos.x,pos.y);
		logtext += xml;
	}

	public void addScanParams(int time, int totalScan) {
		String xml = String.format("<%s  %s='%d' %s='%d' />\n",_param,_scantime,time,_scancnt,totalScan);
		logtext += xml;
	}
	
	public void addCaptureTime(long time) {
		String xml = String.format("<CaptureTime>%s</CaptureTime>\n",time);
		logtext += xml;
	}
	
	public void addScanStatistics (Vector<WifiScanResult> res, int totalCnt){
		double yield = 0, noise;
		String xml = "";
		for(WifiScanResult stat : res) {
			yield = stat.getYield(totalCnt);
			noise = stat.getAverageNoise();
			if (noise == 0) 
				noise = -100;
			xml += String.format("<%s>%s;%.2f;%.1f;%.2f;%.2f;%.2f;</%s>\n",_stat,stat.getBSSID(),
					stat.getMeanRSSI(),stat.getMedianRSSI(),stat.getDeviationRSSI(), noise, yield, _stat);
		}
		logtext += xml;
	}

	public void addDeviceInfo(Context ctx) {
		String id = "";
		try {
			TelephonyManager telephonyManager = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
			id = telephonyManager.getDeviceId();
		} catch (Throwable t) {
		}
		StringBuilder xml= new StringBuilder("<device>\n");
		xml.append(String.format("<dev_id>%s</dev_id>\n", id));
		xml.append(String.format("<dev_model>%s</dev_model>\n", Build.DEVICE));
		xml.append(String.format("<dev_os>%s</dev_os>\n", "Android OS"));
		xml.append(String.format("<dev_name>%s</dev_name>\n", Build.MODEL));
		xml.append(String.format("<dev_version>%s</dev_version>\n", Build.VERSION.RELEASE));
		xml.append("</device>\n");
		logtext += xml.toString();
	}
	
	public void addUserInfo(Context ctx, String customer_id, String developer_id) {
		StringBuilder xml = new StringBuilder("<Customer_ID>");
		xml.append(customer_id);
		xml.append("</Customer_ID>\n");
		xml.append("<Developer_ID>");
		xml.append(developer_id);
		xml.append("</Developer_ID>\n");
		logtext += xml.toString();
	}

	public void addNotes(String notes){
		if (notes == null) 
			notes = "";
		String xml = String.format("<notes>%s</notes>\n",notes);
		logtext += xml;
	}

	public void endLog() {
		String str = String.format("</%s>",_logroot);
		logtext += str;
	}


	public String getString() {
		return logtext.replaceAll(",", ".");
	}
}
