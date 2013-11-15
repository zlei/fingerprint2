package com.lighthouse.fingerprint2.networks;

import java.util.Vector;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lighthouse.fingerprint2.R;

public class WifiSearcherAdapter extends BaseAdapter {
	private Vector<WifiScanResult> mData;
	private Context mCtx;
	private int mTryCount;

	public WifiSearcherAdapter(Context ctx) {
		mCtx = ctx;
	}

	public void setTryCount(int count) {
		mTryCount = count;
		notifyDataSetChanged();
	}

	public void incTryCount() {
		mTryCount++;
		notifyDataSetChanged();
	}

	public void setData(Vector<WifiScanResult> data) {
		mData = data;
		notifyDataSetInvalidated();
	}

	public int getCount() {
		return mData != null ? mData.size() * 7 : 0;
	}

	public int getApCount() {
		return mData != null ? mData.size() : 0;
	}

	public Object getItem(int arg0) {
		return mData.get(arg0);
	}

	public long getItemId(int arg0) {
		return arg0;
	}

	public boolean isEnabled(int position) {
		return false;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View result = convertView;
		if (result == null)
			result = LayoutInflater.from(mCtx).inflate(R.layout.search_cell,
					parent, false);
		TextView textView = (TextView) result
				.findViewById(R.id.tSearchCellText);
		String text = "";
		WifiScanResult scResult = mData.get(position / 7);
		switch (position % 7) {
		case 0:
			text = scResult.getBSSID();
			break;
		case 1:
			text = scResult.getSSID();
			break;
		case 2:
			text = String.format("mean RSSI: %2.2f", scResult.getMeanRSSI());
			break;
		case 3:
			text = String
					.format("median RSSI: %2.2f", scResult.getMedianRSSI());
			break;
		case 4:
			text = String.format("std. dev. of RSSI: %2.2f",
					scResult.getDeviationRSSI());
			break;
		case 5:
			text = String.format("average noise floor: %2.2f",
					scResult.getAverageNoise());
			break;
		case 6:
			text = String.format("yield: %1.2f", scResult.getYield(mTryCount));
			break;
		}
		textView.setText(text);
		if (position % 7 == 0)
			result.setBackgroundColor(0xff9999cc);
		else
			result.setBackgroundColor(Color.WHITE);
		return result;
	}

	public void reset() {
		mTryCount = 0;
		if (mData != null)
			mData.clear();
		notifyDataSetChanged();
	}
}