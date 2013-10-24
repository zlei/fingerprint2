package com.lighthouse.fingerprint2.activities;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.lighthouse.fingerprint2.R;

public class MapListActivity extends BasicActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_list);
		final ListView menulist = (ListView) findViewById(R.id.listView_menu);
		String[] values = new String[] { "Massechusets", "New York", "Vermont",
				"New Hampshire", "California" };
		final ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < values.length; ++i) {
			list.add(values[i]);
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.list_text, list);
		menulist.setAdapter(adapter);

		menulist.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				Intent intent = new Intent(MapListActivity.this,
						MapViewActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.maplist, menu);
		return true;
	}

}
