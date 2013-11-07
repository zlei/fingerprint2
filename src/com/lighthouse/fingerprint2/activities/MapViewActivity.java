package com.lighthouse.fingerprint2.activities;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.lighthouse.fingerprint2.R;
import com.lighthouse.fingerprint2.activities.MapListActivity.MapData;
import com.lighthouse.fingerprint2.networks.INetworkTaskStatusListener;
import com.lighthouse.fingerprint2.networks.NetworkManager;
import com.lighthouse.fingerprint2.networks.NetworkResult;
import com.lighthouse.fingerprint2.networks.NetworkTask;
import com.lighthouse.fingerprint2.utilities.DataPersistence;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class MapViewActivity extends BasicActivity implements
		INetworkTaskStatusListener {
	private static final int TAG_GET_POINTS = 2;
	protected static ImageLoader imageLoader;
	DisplayImageOptions options;
	ImageView imageView;
	String imageUrl;
	protected MapData mData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_view);
		setImageLoaderOption();

		// mBundle = getIntent().getExtras();
		// mMapData = (MapData) mBundle.get("data");
		imageUrl = getImgUrl();
		downloadMap();
		
		// setMapImage();
	}

	public void downloadMap() {
		// use ImageViewTouch lib to deal with image zooming and panning
		imageLoader = ImageLoader.getInstance();
		ImageViewTouch imageView = (ImageViewTouch) findViewById(R.id.map_image);
		// ImageView imageView = (ImageView) findViewById(R.id.map_image);
		imageView.setDisplayType(DisplayType.FIT_IF_BIGGER);
		imageLoader.displayImage(imageUrl, imageView, options,
				new SimpleImageLoadingListener() {
					@Override
					public void onLoadingStarted(String imageUri, View view) {
					}

					@Override
					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {
						String message = null;
						switch (failReason.getType()) { // fail type
						case IO_ERROR:
							message = "Input/Output error";
							break;
						case DECODING_ERROR:
							message = "Image can't be decoded";
							break;
						case NETWORK_DENIED:
							message = "Downloads are denied";
							break;
						case OUT_OF_MEMORY:
							message = "Out Of Memory error";
							break;
						case UNKNOWN:
							message = "Unknown error";
							break;
						}
						Toast.makeText(MapViewActivity.this, message,
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
					}
				});

	}

	/**
	 * Loads map
	 */
	public void loadMap() {
		/**
		 * Loading points
		 */
		Hashtable<String, String> hash = new Hashtable<String, String>(3);
		hash.put("imageId", Integer.valueOf(mData.imageId).toString());
		hash.put("token", getToken());
		DataPersistence d = new DataPersistence(this);
		NetworkTask task = new NetworkTask(this, d.getServerName(),
				"/logs/pars/getpoint", false, hash, true);
		task.setTag(TAG_KEY, new Integer(TAG_GET_POINTS));
		NetworkManager.getInstance().addTask(task);
	}

	/**
	 * network task error
	 */
	public void nTaskErr(NetworkResult result) {
		// initTitleProgressBar(false);

		if (result.getResponceCode() == 401) {
			standardAlertDialog(getString(R.string.msg_error),
					getString(R.string.msg_alert_1), null);
		} else {
			standardAlertDialog(getString(R.string.msg_error),
					getString(R.string.msg_alert), null);
		}
	}

	/**
	 * network task success
	 */
	public void nTaskSucces(NetworkResult result) {
		try {
			XmlPullParser parser = XmlPullParserFactory.newInstance()
					.newPullParser();
			parser.setInput(new ByteArrayInputStream(result.getData()), "UTF-8");
			switch (((Integer) result.getTask().getTag(TAG_KEY)).intValue()) {
			// case TAG_LOG_SUBMIT:
			// break;
			case TAG_GET_POINTS:
				parser.nextTag();
				ArrayList<HashMap<String, String>> points = new ArrayList<HashMap<String, String>>();
				if (XmlPullParser.START_TAG == parser.getEventType()) {
					if (parser.getName().equalsIgnoreCase("images")) {
						while (parser.next() != XmlPullParser.END_DOCUMENT)
							if (parser.getEventType() == XmlPullParser.START_TAG
									&& parser.getName().equalsIgnoreCase("img")) {
								HashMap<String, String> data = new HashMap<String, String>();
								data.put("point_id", parser.getAttributeValue(
										null, "scan_point_id"));
								data.put("point_name", parser
										.getAttributeValue(null,
												"scan_point_name"));
								data.put("point_x", parser.getAttributeValue(
										null, "point_x"));
								data.put("point_y", parser.getAttributeValue(
										null, "point_y"));
								points.add(data);
							}
					}
				}

				// mMapView.setPoints(points);

				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setImageLoaderOption() {
		options = new DisplayImageOptions.Builder()
				// .showImageForEmptyUri(R.drawable.ic_empty)
				// .showImageOnFail(R.drawable.ic_error)
				.showImageForEmptyUri(R.drawable.ic_launcher)
				.showImageOnFail(R.drawable.ic_launcher)
				.resetViewBeforeLoading(true)
				.cacheInMemory(true)
				// cache to memory
				.cacheOnDisc(true)
				// cache to SD card
				.imageScaleType(ImageScaleType.EXACTLY)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.displayer(new FadeInBitmapDisplayer(300)).build();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map_view, menu);
		return true;
	}

}
