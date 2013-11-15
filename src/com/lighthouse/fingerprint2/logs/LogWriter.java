package com.lighthouse.fingerprint2.logs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringEscapeUtils;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.lighthouse.fingerprint2.networks.WifiScanResult;

public class LogWriter {

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

	public static final String APPEND_PATH = "/sdcard/Fingerprint2/";
	public static final String DEFAULT_NAME = "wifi.log";

	public static final String NEWLINE = "\r\n";

	private static LogWriter sInstance;

	public static LogWriter instance() {
		return sInstance == null ? sInstance = new LogWriter() : sInstance;
	}

	public static void reset() {
		sInstance = null;
	}

	private File logFile;
	private String currentFile;
	private boolean logFlag;
	private BufferedWriter fStream;
	private boolean isOk = true;

	private int mSignalStrength = 0;

	public void setSignalStrength(int mSignalStrength) {
		this.mSignalStrength = mSignalStrength;
	}

	public LogWriter() {

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
		String str = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%n<%s>%n", _logroot);    
		write(str);
		openLog();
		logFlag = false;
	}

	public static void delete(String file) {
		new File(LogWriter.APPEND_PATH + file).delete();
	}

	public boolean isOk() {
		return isOk;
	}

	public String fileName() {
		return APPEND_PATH + currentFile;
	}

	private void write(String str) {
		try {
			fStream.write(str);
			fStream.flush();
		} catch (Exception e) {
			e.printStackTrace();
			isOk = false;
		}
	}

	public void openLog(){
		write(String.format("<%s>%n", _logtext));
	}

	public void closeLog(){
		write(String.format("</%s>%n", _logtext));
	}

	public synchronized void addLog(String str) {
		write( String.format("<%s>%s;</%s>%n", _log, str, _log));
	}

	public void addToLog(Vector data) {
		if (data != null) {
			String str = String.format("<%s>%n", _logtext);
			for (Object tmp : data) {
				str += String.format("<%s>%s;</%s>%n", _log, tmp.toString(), _log);
			}
			str += String.format("</%s>%n", _logtext);
			write(str);
		}
	}

	public void addLocation(double latitude, double longitude) {
		String xml =
				String.format("<%s %s='%.6f' %s='%.6f' />%n", _loc, _lat, latitude, _lon, longitude);
		write(xml);
	}

	public void addImageName(String name, int b_id, Point pos) {
		String xml =
				String.format("<%s name=\'%s\' building_id=\'%d\' x=\'%.1f\' y=\'%.1f\' />%n", _image,
						name, b_id, pos.x, pos.y);
		write(xml);
	}

	public void addImage(int i_id, int b_id, PointF pos, PointF pos2) {
		String xml =
				String.format("<%s id=\'%d\' building_id=\'%d\' x=\'%.1f\' y=\'%.1f\' x2=\'%.1f\' y2=\'%.1f\' />%n", _image, i_id,
						b_id, pos.x, pos.y, pos2.x, pos2.y);
		write(xml);
	} 

	public void addScanParams(int time, int totalScan) {
		String xml =
				String.format("<%s  %s='%d' %s='%d' />\n", _param, _scantime, time, _scancnt, totalScan);
		write(xml);
	}

	public void addLog(WifiScanResult res) { }

	public void addScanStatistics(Vector<WifiScanResult> res, int totalCnt) {
		double yield = 0, noise;
		String xml = "";
		for (WifiScanResult stat : res) {
			yield = stat.getYield(totalCnt);
			noise = stat.getAverageNoise();
			if (noise == 0)
				noise = -100;
			xml +=
					String.format("<%s>%s;%.2f;%.1f;%.2f;%.2f;%.2f;</%s>%n", _stat, stat.getBSSID(), stat
							.getMeanRSSI(), stat.getMedianRSSI(), stat.getDeviationRSSI(), noise, yield, _stat);
		}
		write(xml);
	}

	public void addDeviceInfo(Context ctx) {
		String id = "";
		try {
			TelephonyManager telephonyManager =
					(TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
			id = telephonyManager.getDeviceId();
		} catch (Throwable t) {
		}
		StringBuilder xml = new StringBuilder(String.format("<device>%n"));
		xml.append(String.format("<dev_id>%s</dev_id>%n", id));
		xml.append(String.format("<dev_model>%s</dev_model>%n", Build.DEVICE));
		xml.append(String.format("<dev_os>%s</dev_os>%n", "Android OS"));
		xml.append(String.format("<dev_name>%s</dev_name>%n", Build.MODEL));
		xml.append(String.format("<dev_version>%s</dev_version>%n", Build.VERSION.RELEASE));
		xml.append(String.format("</device>%n"));
		write(xml.toString());
	}
	
	public void addCustomerId(String customerId) {		
		write(String.format("<Customer_ID>%s</Customer_ID>%n", StringEscapeUtils.escapeXml(customerId)));
	}
	
	public void addDeveveloperId(String developerId) {
		write(String.format("<Developer_ID>%s</Developer_ID>%n", StringEscapeUtils.escapeXml(developerId)));
	}

	public void addSignalStrength (int signal) {
		mSignalStrength = signal;
	}

	public void addCellTowersInfo(Context ctx) {

		TelephonyManager telephonyManager =
				(TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);

		if (telephonyManager != null) {
			
			StringBuilder xml = new StringBuilder(String.format("<cell_towers>%n"));
			
			int callState = telephonyManager.getCallState();
			xml.append(String.format("<call_state>%s</call_state>%n", callState));
			
			String networkOperator = telephonyManager.getNetworkOperator();
			xml.append(String.format("<network_operator_id>%s</network_operator_id>%n", networkOperator));
			
			String networkOperatorName = telephonyManager.getNetworkOperatorName();
			xml.append(String.format("<network_operator_name>%s</network_operator_name>%n", networkOperatorName));
			
			//int phoneType = telephonyManager.getPhoneType();
			//String countryIso = telephonyManager.getNetworkCountryIso();
			
			CellLocation cell = telephonyManager.getCellLocation();

			if (cell instanceof GsmCellLocation) {
				GsmCellLocation loc = ((GsmCellLocation)cell);
				int cid = loc.getCid();
				int lac = loc.getLac();
				int psc = loc.getPsc();
				//Log.i("Location", "cid = " + cid + ", lac = " + lac + ", psc = " + psc);
				xml.append(String.format("<gsm_cell_id>%s</gsm_cell_id>%n", cid));
				xml.append(String.format("<gsm_location_area_code>%s</gsm_location_area_code>%n", lac));
				
				if (psc != -1) {
					xml.append(String.format("<primary_scrambling_code>%s</primary_scrambling_code>%n", psc));
				}
				
			} else if (cell instanceof CdmaCellLocation) {
				CdmaCellLocation loc = ((CdmaCellLocation)cell);
				int stationId = loc.getBaseStationId();
				int lon = loc.getBaseStationLongitude();
				int lat = loc.getBaseStationLatitude();
				int networkId = loc.getNetworkId();
				int systemId = loc.getSystemId();
				
				xml.append(String.format("<cdma_base_station_id>%s</cdma_base_station_id>%n", stationId));
				xml.append(String.format("<cdma_base_station_longitude>%s</cdma_base_station_longitude>%n", lon));
				xml.append(String.format("<cdma_base_station_latitude>%s</cdma_base_station_latitude>%n", lat));
				xml.append(String.format("<cdma_network_id>%s</cdma_network_id>%n", networkId));
				xml.append(String.format("<cdma_system_id>%s</cdma_system_id>%n", systemId));
				
				//Log.i("Location", "lon = " + lon + ", lat = " + lat);
			}

			List<NeighboringCellInfo> neig = telephonyManager.getNeighboringCellInfo();

			//Construct the string
			String s = "";
			int rss = 0;
			int cid = 0;
			for (NeighboringCellInfo nci : neig) {
				cid = nci.getCid();
				rss = -113 + 2*nci.getRssi();
				s += "Cell ID: " + Integer.toString(cid) + "     Signal Power (dBm): " + Integer.toString(rss) + "\n";
				//Log.i("CELL", s);
			}

			if (mSignalStrength != 0) {
				xml.append(String.format("<signal_strength>%s</signal_strength>%n", mSignalStrength));
			}
			
			xml.append(String.format("</cell_towers>%n"));

			//Log.i("XML", xml.toString());
			write(xml.toString());
		}
	}

	public void addNotes(String notes) {
		if (notes == null)
			notes = "";
		String xml = String.format("<notes>%s</notes>%n", notes);
		write(xml);
	}

	public void endLog() {
		String str = String.format("</%s>", _logroot);
		write(str);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(logFile));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(NEWLINE);
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
			Log.i("SaveLog", toString());
			writer.write(toString());
			writer.flush();
			writer.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static String generateFilename() {

		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String filename = formatter.format(Calendar.getInstance().getTime());
		return filename;
	}

	public static String readFile(String path) throws IOException {
		FileInputStream stream = new FileInputStream(new File(path));
		try {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			/* Instead of using default, pass in a decoder. */
			return Charset.defaultCharset().decode(bb).toString();
		} finally {
			stream.close();
		}
	}
}