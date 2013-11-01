package com.lighthouse.fingerprint2.logs;

import android.hardware.Sensor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class LogWriterSensors {
	private static LogWriterSensors sInstance;
	public static final String APPEND_PATH = "/sdcard/WifiSearcherFingerprint/";
	public static final String DEFAULT_NAME = "wifi.dev";
	public static final String NEWLINE = "\r\n";
	private String currentFile;

	private int counter = 1;

	public static LogWriterSensors instance() {
		return sInstance == null ? sInstance = new LogWriterSensors() : sInstance;
	}

	public static void reset() {
		sInstance = null;
	}

	private File logFile;
	private BufferedWriter fStream;
	private boolean isOk = true;

	public LogWriterSensors() {
		
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
		String str = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NEWLINE + "<DeviceLog>" + NEWLINE;
		write(str);
	}


	public boolean isOk() {
		return isOk;
	}

	public String generateTag(Integer type, Object[] data) {
		String tag = "";
		String val = "";
		switch(type){
		case Sensor.TYPE_ACCELEROMETER:
			tag = "accel";
			val = String.format("<%s x='%.3f' y='%.3f' z='%.3f' time='%s' />%n", tag, data[1], data[2], data[3], data[0]);
			break;
		case Sensor.TYPE_GRAVITY:
			tag = "gravity";
			val = String.format("<%s x='%.3f' y='%.3f' z='%.3f' time='%s' />%n", tag, data[1], data[2], data[3], data[0]);
			break;
		case Sensor.TYPE_GYROSCOPE:
			tag = "gyro";
			val = String.format("<%s x='%.3f' y='%.3f' z='%.3f' time='%s' />%n", tag, data[1], data[2], data[3], data[0]);
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			tag = "magnet";
			val = String.format("<%s x='%.3f' y='%.3f' z='%.3f' heading='%.3f' time='%s' />%n", tag, data[1], data[2], data[3], data[4], data[0]);
			break;        
		case Sensor.TYPE_ORIENTATION:
			tag = "attitude";
			val = String.format("<%s roll='%.3f' pitch='%.3f' yaw='%.3f' x='%.3f' y='%.3f' z='%.3f' w='%.3f' time='%s' />%n", tag, data[4], data[5], data[6], data[1], data[2], data[3], data[7], data[0]);
			break;        
		}    
		return val.replace(",", ".");
	}

	private void write( String str) {
		if(fStream != null) {
			try {
				fStream.write(str);
				fStream.flush();
			} catch (Exception e) {
				e.printStackTrace();
				isOk = false;
			}
		}
	}


	public synchronized void write(HashMap<Integer, ArrayList<Object[]>> data) {
		StringBuilder buffer = new StringBuilder(); 

		for(Integer type: data.keySet()){
			switch(type){
			case Sensor.TYPE_ACCELEROMETER:
				buffer.append("<accelValue count='" +  counter + "'>" + NEWLINE);
				break;
			case Sensor.TYPE_GRAVITY:
				buffer.append("<gravityValue count='" +  counter + "'>" + NEWLINE);
				break;
			case Sensor.TYPE_GYROSCOPE:
				buffer.append("<gyroValue count='" +  counter + "'>"  + NEWLINE);
				break;          
			case Sensor.TYPE_MAGNETIC_FIELD:
				buffer.append("<magnetValue count='" +  counter + "'>" + NEWLINE);
				break;          
			case Sensor.TYPE_ORIENTATION:
				buffer.append("<attitudeValue count='" +  counter + "'>" + NEWLINE);
				break;          
			}
		
			ArrayList<Object[]> sensorList = data.get(type);
			for(Object[] sensorInfo: sensorList){
				buffer.append(generateTag(type, sensorInfo));
			}
			
			switch(type){
			case Sensor.TYPE_ACCELEROMETER:
				buffer.append("</accelValue>" + NEWLINE);
				break;
			case Sensor.TYPE_GRAVITY:
				buffer.append("</gravityValue>" + NEWLINE);
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				buffer.append("</magnetValue>" + NEWLINE);
				break;          
			case Sensor.TYPE_GYROSCOPE:
				buffer.append("</gyroValue>" + NEWLINE);
				break;          
			case Sensor.TYPE_ORIENTATION:
				buffer.append("</attitudeValue>" + NEWLINE);
				break;
			}            
		}
		counter++;
		write(buffer.toString());
	}

	public void endLog() {
		String str = "</DeviceLog>";
		write(str);
		fStream = null;
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
		return builder.toString();
	}
}
