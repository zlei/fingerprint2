package com.lighthouse.fingerprint2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.lighthouse.fingerprint2.R;

public class MainMenuActivity extends BasicActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);

		Button button_maplists = (Button) findViewById(R.id.button_maplists);
		button_maplists.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainMenuActivity.this, MapListActivity.class);
				startActivity(intent);
				// Perform action on click
			}
		});
		Button button_savelog = (Button) findViewById(R.id.button_reviewlog);
		button_savelog.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Perform action on click
			}
		});
		Button button_uploadlog = (Button) findViewById(R.id.button_uploadlog);
		button_uploadlog.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Perform action on click
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

}
