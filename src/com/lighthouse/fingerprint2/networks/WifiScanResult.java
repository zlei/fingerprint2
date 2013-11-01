package com.lighthouse.fingerprint2.networks;

import java.util.Collections;
import java.util.Vector;

import org.xml.sax.Parser;

import android.os.Parcel;
import android.os.Parcelable;

public class WifiScanResult implements Parcelable{
	private String mBssid;
	private String mSsid;
	private String mFrequency;

	private int mCount;
	private double mRssiSum;
	private double mRssiSum2;
	private double mNoiseSum;

	private Vector<Double> mRssi;
	

	public static class Creator implements Parcelable.Creator<WifiScanResult> {
		public WifiScanResult createFromParcel(Parcel source) {
			WifiScanResult result = new WifiScanResult(source.readString(),source.readString(),source.readString());
			int count = source.readInt();
			double noise = source.readDouble();
			double rssi[] = new double[count];
			source.readDoubleArray(rssi);
			for(int i = 0; i < rssi.length; i++) {
				result.add(rssi[i], 0);
			}
			result.mNoiseSum = noise;
			return result;
		}
		public WifiScanResult[] newArray(int size) {
			return new WifiScanResult[size];
		}
	}
	public static final Creator CREATOR = new Creator();
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mBssid);
		dest.writeString(mSsid);
		dest.writeString(mFrequency);
		dest.writeInt(mCount);
		dest.writeDouble(mNoiseSum);
		double rssi[] = new double[mRssi.size()];
		for(int i = 0; i < mRssi.size(); i++) {
			rssi[i] = mRssi.get(i);
		}
		dest.writeDoubleArray(rssi);
	}
	public int describeContents() {
		return 0;
	}


	public WifiScanResult(String bssid, String ssid, String frequency) {
		mBssid = bssid;
		mSsid = ssid;
		mFrequency = frequency;
		mRssi = new Vector<Double>(10);
	}

	public void add(double rssi, double noise) {
		mRssiSum += rssi;
		mRssiSum2 += rssi*rssi;
		mNoiseSum += noise;
		mCount++;
		mRssi.addElement(new Double(rssi));
	}

	public synchronized double getMedianRSSI() {
		if (mRssi.size() == 0)
			return 0;
		Vector<Double> mRssi2 = (Vector<Double>) mRssi.clone(); 
		Collections.sort(mRssi2);
		return mRssi2.get(mRssi2.size() / 2).doubleValue();
	}

	public double getDeviationRSSI() {
		if(mCount == 0)
			return 0;
		double av = mRssiSum;
		av /= mCount;
		av *= av;
		return Math.sqrt( -av + mRssiSum2 / mCount); //TODO right?//sqrt(pow(av,2)-2.0*av*av+RSSI2sum/count);
	}

	public double getMeanRSSI(){
		if(mCount == 0) 
			return 0;
		return mRssiSum / mCount;
	}

	public double getAverageNoise() {
		if(mCount == 0)
			return 0;
		return mNoiseSum /mCount;
	}

	public String getFrequency() {
		return mFrequency;
	}

	public String getBSSID() {
		return mBssid;
	}
	public String getSSID() {
		return mSsid.replaceAll("[^0-9a-zA-Z]", "_");
	}

	public float getYield(int tryCount) {
		return (float)mCount / tryCount;
	}

	public String toString() {
		return new StringBuilder().append(mBssid).append(";").append(getSSID().replace(";", "")).append(";").append(mRssi).append(";").append(mNoiseSum).toString();
	}

}
