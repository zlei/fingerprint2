package com.lighthousesignal.fingerprint2.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.lighthousesignal.fingerprint2.R;
import com.nostra13.universalimageloader.core.ImageLoader;

public class OptionsFragment extends Fragment implements OnClickListener {

	private Context mContext;
	protected static ImageLoader imageLoader;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_options, container, false);
		mContext = getActivity();
		imageLoader = ImageLoader.getInstance();
		Button btn_clear_memory_cache = (Button) v
				.findViewById(R.id.button_clear_memory_cache);
		btn_clear_memory_cache.setOnClickListener(this);
		Button btn_clear_sdcard_cache = (Button) v
				.findViewById(R.id.button_clear_sdcard_cache);
		btn_clear_sdcard_cache.setOnClickListener(this);
		return v;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch (id) {
		case R.id.button_clear_memory_cache:
			imageLoader.clearMemoryCache();
			break;
		case R.id.button_clear_sdcard_cache:
			imageLoader.clearDiscCache();
			break;
		}
		Toast.makeText(mContext, "Cache Cleared!", Toast.LENGTH_SHORT).show();
	}
}
