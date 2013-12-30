package com.lighthousesignal.fingerprint2.fragments;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lighthousesignal.fingerprint2.R;
import com.lighthousesignal.fingerprint2.activities.MapViewActivity;
import com.lighthousesignal.fingerprint2.network.INetworkTaskStatusListener;
import com.lighthousesignal.fingerprint2.network.NetworkManager;
import com.lighthousesignal.fingerprint2.network.NetworkResult;
import com.lighthousesignal.fingerprint2.network.NetworkTask;
import com.lighthousesignal.fingerprint2.utilities.AppLocationManager;
import com.lighthousesignal.fingerprint2.utilities.DataPersistence;
import com.lighthousesignal.fingerprint2.utilities.UiFactories;

public class MapListFragment extends Fragment implements
		OnItemSelectedListener, INetworkTaskStatusListener {

	private Spinner spnState, spnBuilding, spnFloor;
	private Button button_select_map;
	private TextView textBuilding, textFloor;
	private ArrayList mData;
	boolean firstrun;
	private SharedPreferences mPrefs;
	private Context mContext;

	public static final String TAG_KEY = "TAG_KEY";
	public static final String LOG_TAG = "FINGERPRINT2";
	public static final String PREF_IMG_URL = "";
	public static final String PREF_LOGIN_TOKEN = "login_token";
	public static final String PREF_LOGIN_USERNAME = "login_username";
	public static final String PREF_LOGIN_PASS = "login_password";
	public static final String PREF_CUSTOMER_ID = "customer_id";
	public static final String PREF_DEVELOPER_ID = "developer_id";
	public static final String PREF_BUILDING_ID = "";

	/**
	 * Current bundle
	 */
	protected Bundle mBundle;

	public enum params {
		BUILDING_ID, MAP_NAME
	}

	private Vector<Integer> mIds = new Vector<Integer>();
	public static final int GET_BUILDINGS = 1;
	public static final int GET_FLOORS = 2;

	private String[] state_list;
	private List<String> building_list;
	private List<String> floor_list;
	private HashMap<String, String> building_table;
	private HashMap<String, String> floor_table;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_map_list, container, false);

		spnBuilding = (Spinner) v.findViewById(R.id.spinner_building);
		textBuilding = (TextView) v.findViewById(R.id.select_building);
		spnFloor = (Spinner) v.findViewById(R.id.spinner_floor);
		textFloor = (TextView) v.findViewById(R.id.select_floor);
		firstrun = true;
		// addItemsOnSpnState(v);
		button_select_map = (Button) v.findViewById(R.id.button_select_ok);
		button_select_map.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, MapViewActivity.class);
				final String map_info = getBuildingInfo();
				intent.putExtra("MAP_INFO", map_info);
				// change to startactivityforresult later
				startActivity(intent);
			}
		});

		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		System.out.println("MapList Activity Attatched!");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mContext = getActivity();
		System.out.println("MapList Activity created!");
		System.out.println("Context null? : " + (mContext == null));
		mPrefs = mContext.getSharedPreferences(
				mContext.getString(R.string.shared_preferences_name),
				Context.MODE_PRIVATE);
		addItemsOnSpnState(getView());
	}

	/**
	 * deal with spinners
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long arg3) {
		int id = parent.getId();

		if (id == R.id.spinner_state) {
			spnBuilding.setVisibility(View.INVISIBLE);
			textBuilding.setVisibility(View.INVISIBLE);
			spnFloor.setVisibility(View.INVISIBLE);
			textFloor.setVisibility(View.INVISIBLE);
			if (!firstrun) {
				button_select_map.setEnabled(false);
				loadBuildings(position);
			}
			firstrun = false;
		} else if (id == R.id.spinner_building) {
			String building_id = building_table.get(Integer.toString(position));
			DataPersistence.setBuildingID(mContext, building_id);
			loadFloors(DataPersistence.getBuildingID(mContext));
		} else if (id == R.id.spinner_floor) {
			// String floor_id = floor_table.get(Integer.toString(position));
			// saveFloorID(floor_id);
			button_select_map.setEnabled(true);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		return;
	}

	/**
	 * state spinner
	 * 
	 * @param v
	 */
	public void addItemsOnSpnState(View v) {
		spnState = (Spinner) v.findViewById(R.id.spinner_state);

		state_list = getLocationManager().getStatesList();

		// Get State Database and Add to List

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(mContext,
				android.R.layout.simple_spinner_item, state_list);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnState.setAdapter(dataAdapter);
		spnState.setOnItemSelectedListener(this);

		long position = spnState.getSelectedItemId();
		Log.v(LOG_TAG, Long.toString(position));
		int which = safeLongToInt(position);
		which *= 3;
		/**
		 * update active state
		 */
		getLocationManager().writeActiveOption(which);
	}

	/**
	 * building spinner
	 */
	public void addItemOnSpnBuilding() {
		spnBuilding.setVisibility(View.VISIBLE);
		textBuilding.setVisibility(View.VISIBLE);

		// Get Building Database and Add to List
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(mContext,
				android.R.layout.simple_spinner_item, building_list);
		// ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
		// android.R.layout.simple_spinner_item, mData);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnBuilding.setAdapter(dataAdapter);
		spnBuilding.setOnItemSelectedListener(this);
	}

	/**
	 * floor spinner
	 */
	public void addItemOnSpnFloor() {
		spnFloor.setVisibility(View.VISIBLE);
		textFloor.setVisibility(View.VISIBLE);
		// Get Floor Database and Add to List
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(mContext,
				android.R.layout.simple_spinner_item, floor_list);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnFloor.setAdapter(dataAdapter);
		spnFloor.setOnItemSelectedListener(this);
	}

	/**
	 * load building map
	 * 
	 * @param position
	 */
	void loadBuildings(int position) {
		String token = DataPersistence.getToken(mContext);
		String server = DataPersistence.getServerName(mContext);

		// to find it is gps or changed
		building_list = new ArrayList<String>();
		building_table = new HashMap<String, String>();
		position *= 3;
		getLocationManager().writeActiveOption(position);

		Hashtable<String, String> hash = new Hashtable<String, String>(3);
		hash.put("lat",
				String.format("%.6f", getLocationManager().getLatitude()));
		hash.put("lng",
				String.format("%.6f", getLocationManager().getLongtitude()));
		Log.v(LOG_TAG,
				"lat "
						+ String.format("%.6f", getLocationManager()
								.getLatitude()));
		Log.v(LOG_TAG,
				"lng "
						+ String.format("%.6f", getLocationManager()
								.getLongtitude()));
		hash.put("token", token);
		NetworkTask task = new NetworkTask(this, server,
				"/logs/pars/getbuildings/", false, hash, true);
		task.setTag(TAG_KEY, new Integer(GET_BUILDINGS));
		NetworkManager.getInstance().addTask(task);

	}

	/**
	 * load floors
	 * 
	 * @param buildingId
	 */
	void loadFloors(String buildingId) {

		String token = DataPersistence.getToken(mContext);
		String server = DataPersistence.getServerName(mContext);

		mData = new ArrayList<MapData>();
		floor_list = new ArrayList<String>();
		floor_table = new HashMap<String, String>();
		Hashtable<String, String> hash = new Hashtable<String, String>(3);
		hash.put("buildingId", buildingId);
		hash.put("token", token);
		DataPersistence d = new DataPersistence(mContext);
		NetworkTask task = new NetworkTask(this, server,
				"/logs/pars/getimage/", false, hash, true);
		task.setTag(TAG_KEY, new Integer(GET_FLOORS));
		NetworkManager.getInstance().addTask(task);
	}

	/**
	 * if download success
	 */
	@Override
	public void nTaskSucces(NetworkResult result) {
		int counter = 0;
		try {
			XmlPullParser parser = XmlPullParserFactory.newInstance()
					.newPullParser();
			Log.v(LOG_TAG, new String(result.getData()).toString());
			Log.v(LOG_TAG, "url " + result.getTask().getUrl());
			parser.setInput(new ByteArrayInputStream(result.getData()), "UTF-8");
			switch ((Integer) (result.getTask().getTag(TAG_KEY))) {
			case GET_BUILDINGS:
				parser.nextTag();
				if (XmlPullParser.START_TAG == parser.getEventType())
					if (parser.getName().equalsIgnoreCase("buildings"))
						while (parser.next() != XmlPullParser.END_DOCUMENT)
							if (parser.getEventType() == XmlPullParser.START_TAG
									&& parser.getName().equalsIgnoreCase(
											"build")) {
								Log.v(LOG_TAG,
										"build "
												+ parser.getAttributeValue(
														null, "name")
												+ " ; attribute "
												+ parser.getAttributeValue(
														null, "building_id"));
								BuildingData buildingData = new BuildingData();
								buildingData.name = parser.getAttributeValue(
										null, "name");

								buildingData.building_id = Integer
										.parseInt(parser.getAttributeValue(
												null, "building_id"));
								building_list.add(buildingData.name);
								building_table
										.put(Integer.toString(counter),
												Integer.toString(buildingData.building_id));
								counter++;
								// mData.add(buildingData);
							}
				if (building_list.isEmpty() || building_list == null)
					Toast.makeText(mContext,
							"Sorry, we do not have map in this state yet!",
							Toast.LENGTH_SHORT).show();
				else
					addItemOnSpnBuilding();
				break;
			case GET_FLOORS:
				parser.nextTag();
				if (XmlPullParser.START_TAG == parser.getEventType())
					if (parser.getName().equalsIgnoreCase("images")) {
						MapData mapData = null;
						while (parser.next() != XmlPullParser.END_DOCUMENT) {
							if (parser.getEventType() == XmlPullParser.START_TAG
									&& parser.getName()
											.equalsIgnoreCase("data")) {
								mapData = new MapData();
								mData.add(mapData);
							} else if (parser.getEventType() == XmlPullParser.START_TAG
									&& parser.getName().equalsIgnoreCase("img")) {
								mIds.add(Integer.parseInt(parser
										.getAttributeValue(null, "floor_id")));
								mapData.name = parser.getAttributeValue(null,
										"name");
								mapData.floorId = Integer.parseInt(parser
										.getAttributeValue(null, "floor_id"));
								mapData.imageId = Integer.parseInt(parser
										.getAttributeValue(null, "image_id"));
								mapData.width = Integer.parseInt(parser
										.getAttributeValue(null, "width"));
								mapData.height = Integer.parseInt(parser
										.getAttributeValue(null, "height"));
								mapData.img = parser.getAttributeValue(null,
										"img");
								// floor_list.add(Integer
								// .toString(mapData.floorId));

								// need to be more accurate, for now it is from
								// floor 1 to floor n
								floor_list.add("Floor_"
										+ Integer.toString(counter + 1));

								floor_table.put(Integer.toString(counter),
										Integer.toString(mapData.floorId));
								counter++;
								DataPersistence
										.setImgUrl(mContext, mapData.img);
							}
							/*
							 * else if (parser.getEventType() ==
							 * XmlPullParser.START_TAG &&
							 * parser.getName().equalsIgnoreCase( "scale")) {
							 * int x = Integer.parseInt(parser
							 * .getAttributeValue(null, "x")); int y =
							 * Integer.parseInt(parser .getAttributeValue(null,
							 * "y")); parser.next(); int scale =
							 * Integer.parseInt(parser.getText());
							 * mapData.addZoom(scale, x, y); }
							 */
						}
						addItemOnSpnFloor();
					}
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Collections.sort(mData);
		// mAdapter.setData(mData);
	}

	/**
	 * if download error
	 */
	@Override
	public void nTaskErr(NetworkResult result) {
		Exception e = result.getException();

		if (result.getResponceCode() == 401
				|| (e != null && e.getMessage().contains(
						"Received authentication challenge is null"))) {
			UiFactories.standardAlertDialog(mContext,
					getString(R.string.msg_error_network_401),
					getString(R.string.msg_alert_connection), null);
		} else {
			UiFactories.standardAlertDialog(mContext,
					getString(R.string.msg_error_network_unknown),
					getString(R.string.msg_alert_connection), null);
			Log.e(LOG_TAG, "error", result.getException());
		}
	}

	/**
	 * Map data
	 */
	public class MapData implements Serializable, Comparable {

		private static final long serialVersionUID = -5948875275621573697L;

		public class ZoomInfo implements Serializable {
			private static final long serialVersionUID = 2849412401959020422L;

			public int x, y;

			private void writeObject(java.io.ObjectOutputStream out)
					throws IOException {
				out.writeInt(x);
				out.writeInt(y);
			}

			private void readObject(java.io.ObjectInputStream in)
					throws IOException, ClassNotFoundException {
				x = in.readInt();
				y = in.readInt();
			}
		}

		public HashMap<Integer, ZoomInfo> zoom = new HashMap<Integer, ZoomInfo>();

		public int imageId, floorId, width, height;

		public String img, name;

		public void addZoom(int scaly, int x, int y) {
			ZoomInfo info = new ZoomInfo();
			info.x = x;
			info.y = y;
			zoom.put(scaly, info);
		}

		@Override
		public String toString() {
			return name;
		}

		private void writeObject(java.io.ObjectOutputStream out)
				throws IOException {
			out.writeUTF(img);
			out.writeUTF(name);
			out.writeInt(imageId);
			out.writeInt(floorId);
			out.writeInt(width);
			out.writeInt(height);
			out.writeObject(zoom);
		}

		@SuppressWarnings("unchecked")
		private void readObject(java.io.ObjectInputStream in)
				throws IOException, ClassNotFoundException {
			img = in.readUTF();
			name = in.readUTF();
			imageId = in.readInt();
			floorId = in.readInt();
			width = in.readInt();
			height = in.readInt();
			zoom = (HashMap<Integer, ZoomInfo>) in.readObject();
		}

		@Override
		public int compareTo(Object another) {
			return this.name.toLowerCase().compareTo(
					((MapData) another).name.toLowerCase());
		}
	}

	/**
	 * building data
	 * 
	 */
	private class BuildingData implements Comparable<BuildingData> {

		public String name;
		public int building_id;

		@Override
		public int compareTo(BuildingData another) {
			return this.name.toLowerCase()
					.compareTo(another.name.toLowerCase());
		}

		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * get all building info to pass to mapview activity, need to add more
	 * information
	 * 
	 * @return
	 */
	private String getBuildingInfo() {
		String buildingInfo = "";
		String building = spnBuilding.getSelectedItem().toString();
		String floor = spnFloor.getSelectedItem().toString();
		buildingInfo = building + "-" + floor;
		return buildingInfo;
	}

	private AppLocationManager getLocationManager() {
		System.out.println("Context null glm? " + (mContext == null));
		// return new AppLocationManager(mContext,
		// mPrefs);//AppLocationManager.getInstance(mContext);
		return AppLocationManager.getInstance(mContext);
	}

	/**
	 * long to int cast
	 * 
	 * @param l
	 * @return
	 */
	public static int safeLongToInt(long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(l
					+ " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}

}
