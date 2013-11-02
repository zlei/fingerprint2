package com.lighthouse.fingerprint2.activities;

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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.lighthouse.fingerprint2.R;
import com.lighthouse.fingerprint2.networks.INetworkTaskStatusListener;
import com.lighthouse.fingerprint2.networks.NetworkManager;
import com.lighthouse.fingerprint2.networks.NetworkResult;
import com.lighthouse.fingerprint2.networks.NetworkTask;
import com.lighthouse.fingerprint2.utilities.DataPersistence;

public class MapListActivity extends BasicActivity implements
		OnItemSelectedListener, INetworkTaskStatusListener {

	private Spinner spnState, spnBuilding, spnFloor;
	private Button button_select_map;
	private TextView textBuilding, textFloor;
	private ArrayList mData;

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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_list);
		// mBundle = getIntent().getExtras();
		addItemsOnSpnState();
		button_select_map = (Button) findViewById(R.id.button_select_ok);
		button_select_map.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MapListActivity.this,
						MapViewActivity.class);
				// change to startactivityforresult later
				startActivity(intent);
			}
		});
	}

	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long arg3) {
		int id = parent.getId();
		switch (id) {
		case R.id.spinner_state:
			button_select_map.setEnabled(false);
			loadBuildings(position);
			break;
		case R.id.spinner_building:
			String building_id = building_table.get(Integer.toString(position));
			saveBuildingID(building_id);
			loadFloors(getBuildingID());
			break;
		case R.id.spinner_floor:
			String floor_id = floor_table.get(Integer.toString(position));
			saveFloorID(floor_id);
			button_select_map.setEnabled(true);
			break;
		}
	}

	public void onNothingSelected(AdapterView<?> arg0) {
		return;
	}

	public void addItemsOnSpnState() {
		spnState = (Spinner) findViewById(R.id.spinner_state);

		state_list = getLocationManager().getStatesList();

		// Get State Database and Add to List

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, state_list);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnState.setAdapter(dataAdapter);
		spnState.setOnItemSelectedListener(this);

		long position = spnState.getSelectedItemId();
		Log.v(BasicActivity.LOG_TAG, Long.toString(position));
		int which = safeLongToInt(position);
		which *= 3;
		/**
		 * update active state
		 */
		getLocationManager().writeActiveOption(which);
	}

	public void addItemOnSpnBuilding() {
		spnBuilding = (Spinner) findViewById(R.id.spinner_building);
		textBuilding = (TextView) findViewById(R.id.select_building);
		spnBuilding.setVisibility(View.VISIBLE);
		textBuilding.setVisibility(View.VISIBLE);

		// Get Building Database and Add to List
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, building_list);
		// ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
		// android.R.layout.simple_spinner_item, mData);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnBuilding.setAdapter(dataAdapter);
		spnBuilding.setOnItemSelectedListener(this);
	}

	public void addItemOnSpnFloor() {
		spnFloor = (Spinner) findViewById(R.id.spinner_floor);
		textFloor = (TextView) findViewById(R.id.select_floor);
		spnFloor.setVisibility(View.VISIBLE);
		textFloor.setVisibility(View.VISIBLE);
		// Get Floor Database and Add to List
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, floor_list);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnFloor.setAdapter(dataAdapter);
		spnFloor.setOnItemSelectedListener(this);
	}

	void loadBuildings(int position) {
		// to find it is gps or changed
		building_list = new ArrayList<String>();
		building_table = new HashMap<String, String>();
		if (position != 0) {
			position *= 3;
			getLocationManager().writeActiveOption(position);
		}

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
		hash.put("token", getToken());
		DataPersistence d = new DataPersistence(this);
		NetworkTask task = new NetworkTask(this, d.getServerName(),
				"/logs/pars/getbuildings/", false, hash, true);
		task.setTag(TAG_KEY, new Integer(GET_BUILDINGS));
		NetworkManager.getInstance().addTask(task);

	}

	void loadFloors(String buildingId) {
		mData = new ArrayList<MapData>();
		floor_list = new ArrayList<String>();
		floor_table = new HashMap<String, String>();
		Hashtable<String, String> hash = new Hashtable<String, String>(3);
		hash.put("buildingId", buildingId);
		hash.put("token", getToken());
		DataPersistence d = new DataPersistence(this);
		NetworkTask task = new NetworkTask(this, d.getServerName(),
				"/logs/pars/getimage/", false, hash, true);
		task.setTag(TAG_KEY, new Integer(GET_FLOORS));
		NetworkManager.getInstance().addTask(task);
	}

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
								Log.v(BasicActivity.LOG_TAG,
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
								addItemOnSpnBuilding();
							}
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
								floor_list.add(Integer
										.toString(mapData.floorId));
								floor_table.put(Integer.toString(counter),
										Integer.toString(mapData.floorId));
								counter++;
							} else if (parser.getEventType() == XmlPullParser.START_TAG
									&& parser.getName().equalsIgnoreCase(
											"scale")) {
								int x = Integer.parseInt(parser
										.getAttributeValue(null, "x"));
								int y = Integer.parseInt(parser
										.getAttributeValue(null, "y"));
								parser.next();
								int scale = Integer.parseInt(parser.getText());
								mapData.addZoom(scale, x, y);
							}
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

	@Override
	public void nTaskErr(NetworkResult result) {
		Exception e = result.getException();

		if (result.getResponceCode() == 401
				|| (e != null && e.getMessage().contains(
						"Received authentication challenge is null"))) {
			standardAlertDialog(getString(R.string.msg_alert_1),
					getString(R.string.msg_alert_connection), null);
		} else {
			standardAlertDialog(getString(R.string.msg_alert_1),
					getString(R.string.msg_alert_1), null);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.maplist, menu);
		return true;
	}

}
