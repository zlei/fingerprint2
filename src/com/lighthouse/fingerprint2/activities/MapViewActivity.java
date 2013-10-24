package com.lighthouse.fingerprint2.activities;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;
import android.os.Bundle;
import android.view.Menu;

import com.lighthouse.fingerprint2.R;

public class MapViewActivity extends BasicActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_view);
		ImageViewTouch imageView = (ImageViewTouch) findViewById(R.id.map_image);
		imageView.setDisplayType(DisplayType.FIT_IF_BIGGER);
		imageView.setImageResource(R.drawable.ic_launcher);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map_view, menu);
		return true;
	}

}
