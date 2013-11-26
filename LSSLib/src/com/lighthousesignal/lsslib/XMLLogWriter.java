package com.lighthousesignal.lsslib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

public class XMLLogWriter {
	private static final String TAG = "LW";
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

	public static final String APPEND_PATH = "/sdcard/Wifisearcher2/";
	public static final String DEFAULT_NAME = "lastlog.txt";
	
	private static XMLLogWriter sInstance;
	public static XMLLogWriter instance() {
		return sInstance == null? sInstance = new XMLLogWriter() : sInstance;
	}
	public static void reset() {
		sInstance = null;
	}

	private File logFile;
	private String currentFile;
	private boolean logFlag;
	private BufferedWriter fStream;
	private boolean isOk = true;
	
	public XMLLogWriter() {
//		currentFile = "tmp_log" + System.currentTimeMillis() / 10000 + ".log";
		currentFile = DEFAULT_NAME;
		String path = APPEND_PATH + currentFile;
		logFile = new File(path);
		logFile.mkdirs();
		try {
			logFile.delete();
			logFile.createNewFile();
			fStream = new BufferedWriter(new FileWriter(logFile));
		} catch (Exception e) {
			e.printStackTrace();
			isOk = false;
		}
		String str = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<%s>\n", _logroot);
		write(str);
		logFlag = false;
	}

	public boolean isOk() {
		return isOk;
	}
	public String fileName() {
		return APPEND_PATH + currentFile;
	}
	
	private void write(String str) {
		try {
			//Log.v(TAG, str);
			fStream.write(str.replaceAll(",", "."));
			fStream.flush();
		} catch (Exception e) {
			e.printStackTrace();
			isOk = false;
		}
	}
	
	public void addToLog(Vector data) {
		String str = String.format("<%s>\n",_logtext);
		for (Object tmp: data) {
			str += String.format("<%s>%s;</%s>\n",_log, tmp.toString(), _log);
		}
		str += String.format("</%s>\n",_logtext);
		write(str);
	}

	public void addLocation(double latitude, double longitude) {
		String xml = String.format("<%s %s='%.6f' %s='%.6f' />\n",_loc,_lat,latitude,_lon,longitude);
		write(xml);
	}

	public void addImageName(String name, int b_id, Point pos) {
		String xml = String.format("<%s name=\'%s\' building_id=\'%d\' x=\'%.1f\' y=\'%.1f\' />\n",_image,name,b_id,pos.x,pos.y);
		write(xml);
	}

	public void addImage(int i_id, int b_id, Point pos) {
		String xml= String.format("<%s id=\'%d\' building_id=\'%d\' x=\'%.1f\' y=\'%.1f\' />\n",_image,i_id,b_id,pos.x,pos.y);
		write(xml);
	}

	public void addScanParams(int time, int totalScan) {
		String xml = String.format("<%s  %s='%d' %s='%d' />\n",_param,_scantime,time,_scancnt,totalScan);
		write(xml);
	}

	public void addLog(WifiScanResult res) {
		
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
		write(xml);
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
		write(xml.toString());
	}
	
	public void addUserInfo(Context ctx, String customer_id, String developer_id) {
		StringBuilder xml = new StringBuilder("<Customer_ID>");
		xml.append(customer_id);
		xml.append("</Customer_ID>\n");
		xml.append("<Developer_ID>");
		xml.append(developer_id);
		xml.append("</Developer_ID>\n");
		write(xml.toString());
	}

	public void addNotes(String notes){
		if (notes == null) 
			notes = "";
		String xml = String.format("<notes>%s</notes>\n",notes);
		write(xml);
	}

	public void endLog() {
		String str = String.format("</%s>",_logroot);
		write(str);
	}
	
	int countToString = 0;

	String[] testFiles = new String[] {
			 "/sdcard/Wifisearcher2/testlog1.xml",
	};
	
	public String toString() {
		String name = testFiles[(countToString++) % testFiles.length];
//		File fl = new File(name);
		File fl = logFile;
		StringBuilder builder = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fl));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString().replaceAll(",", ".");
	}

	public boolean saveLog(String name) {
		File newFile = new File(APPEND_PATH + name);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
			writer.write(toString());
			writer.flush();
			writer.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
