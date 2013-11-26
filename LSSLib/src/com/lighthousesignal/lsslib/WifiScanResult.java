package com.lighthousesignal.lsslib;


import java.util.Collections;
import java.util.Vector;

import android.os.Parcel;
import android.os.Parcelable;

/*
 * A result from a wifi scan.
 * Represents a wifi network.
 */
public class WifiScanResult implements Parcelable{
	
	private String mBssid;
	private String mSsid;
	private String mFrequency;

	private int mCount;
	private double mRssiSum;
	private double mRssiSum2;
	private double mNoiseSum;

	private Vector<Double> mRssi;
	
	/**
	 * Used to reconstruct the WifiScanResult from a parcel.
	 * @param in The parcel to unpack.
	 */
	public WifiScanResult(Parcel in){
		readFromParcle(in);
	}

	/**
	 * Pack the WifiScanResult into a parcel.
	 */
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mBssid);
		dest.writeString(mSsid);
		dest.writeString(mFrequency);
		dest.writeInt(mCount);
		dest.writeDouble(mNoiseSum);
	}
	
	/**
	 * Unpack the WifiScanResult from a parcel.
	 * Used by the parcel constructor.
	 * @param in The parcel holding the WifiScanResult.
	 */
	private void readFromParcle(Parcel in){
		mBssid = in.readString();
		mSsid = in.readString();
		mFrequency = in.readString();
		mCount = in.readInt();
		mNoiseSum = in.readDouble();
	}
	
	/**
	 * Used to facilitate the packing and unpacking of the parcel.
	 */
	@SuppressWarnings("rawtypes")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public WifiScanResult createFromParcel(Parcel in) {
			return new WifiScanResult(in);
        }
 
        public WifiScanResult[] newArray(int size) {
        	return new WifiScanResult[size];
        }
	};
	
	/**
	 * Unused
	 */
	public int describeContents(){
		return 0;
	}
	
	/**
	 * Create a new WifiScanResult.
	 * Used by LSSLocationService after a scan completes.
	 * 
	 * @param bssid	The mac address of the wifi network
	 * @param ssid The name of the wifi network
	 * @param frequency The frequency of the wifi network
	 */
	public WifiScanResult(String bssid, String ssid, String frequency) {
		mBssid = bssid;
		mSsid = ssid;
		mFrequency = frequency;
		mRssi = new Vector<Double>(10);
	}

	/**
	 * Add a new data point to the wifi network
	 * 
	 * @param rssi Received Signal Strength Indication (strength of the wifi network)
	 * @param noise The noise of the wifi network
	 */
	public void add(double rssi, double noise) {
		mRssiSum += rssi;
		mRssiSum2 += rssi*rssi;
		mNoiseSum += noise;
		mCount++;
		mRssi.addElement(Double.valueOf(rssi));
	}

	/**
	 * @return The median signal strength of the wifi network.
	 */
	public double getMedianRSSI() {
		if (mRssi.size() == 0)
			return 0;
		Collections.sort(mRssi);
		if (mRssi.size() % 2 != 0) {
			return mRssi.get(mRssi.size() / 2).doubleValue();
		} else { 
			return (mRssi.get(mRssi.size() / 2).doubleValue()  + mRssi.get(mRssi.size() / 2 - 1).doubleValue()) / 2;
		}
		 
	}

	/**
	 * @return The deviation in signal strength of the wifi netowrk.
	 */
	public double getDeviationRSSI() {
		if(mCount == 0)
			return 0;
		double av = mRssiSum;
		av /= mCount;
		return Math.sqrt(Math.pow(av, 2) -2.0 * av *  av + mRssiSum2 / mCount); //TODO right?//sqrt(pow(av,2)-2.0*av*av+RSSI2sum/count);
	}

	/**
	 * @return The mean signal strength of the wifi network.
	 */
	public double getMeanRSSI(){
		if(mCount == 0) 
			return 0;
		return mRssiSum / mCount;
	}

	/**
	 * @return The average noise of the wifi network.
	 */
	public double getAverageNoise() {
		if(mCount == 0)
			return 0;
		return mNoiseSum /mCount;
	}

	/**
	 * @return The frequency of the wifi network.
	 */
	public String getFrequency() {
		return mFrequency;
	}

	/**
	 * @return The mac address of the wifi network.
	 */
	public String getBSSID() {
		return mBssid;
	}
	
	/**
	 * @return The name of the wifi network.
	 */
	public String getSSID() {
		return mSsid;
	}

	/**
	 * @param tryCount The number of attempted scans
	 * @return The percentage of scans that were completed.
	 */
	public float getYield(int tryCount) {
		float yield = (float)mCount / tryCount;
		return yield <= 1? yield : 1;
	}

	/**
	 * @return The information about the wifi network represented as a string.
	 */
	public String toString() {
		return "" + mBssid + ";" + mSsid + ";" + mRssi + ";" + mNoiseSum;
	}

}
