package com.lighthouse.fingerprint2.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.lighthouse.fingerprint2.R;

public class SplashActivity extends BasicActivity {

	private static int SPLASH_TIME_OUT = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		boolean firstrun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
				.getBoolean("firstrun", true);

		// if firstrun, popup terms and conditions
		if (!firstrun) {
			new AlertDialog.Builder(this)
					.setIcon(R.drawable.ic_launcher)
					.setTitle(R.string.terms)
					.setMessage(R.string.app_name)
					.setPositiveButton("Agree",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Toast.makeText(getApplicationContext(),
											"Welcome to Fingerprint2!",
											Toast.LENGTH_SHORT).show();
									firstLogin();
								}
							})
					.setNegativeButton("Decline",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Toast.makeText(getApplicationContext(),
											"Exiting...", Toast.LENGTH_SHORT)
											.show();
									exitSplash();
								}
							}).show();

			getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
					.putBoolean("firstrun", false).commit();
		} else
			fadeSplash();

	}

	public void firstLogin() {
		Button button_login_facebook = (Button) findViewById(R.id.login_facebook);
		button_login_facebook.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(SplashActivity.this,
						LoginActivity.class);
				startActivity(intent);
			}
		});

		Button button_login_fingerprint2 = (Button) findViewById(R.id.login_fingerprint2);
		button_login_fingerprint2
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Intent intent = new Intent(SplashActivity.this,
								LoginActivity.class);
						startActivity(intent);
						// Perform action on click
					}
				});

	}

	// fade splash screen to main activity
	public void fadeSplash() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent iLogin = new Intent(SplashActivity.this,
						LoginActivity.class);
				Intent iMain = new Intent(SplashActivity.this,
						MainMenuActivity.class);
				if (!hasToken()) {
					startActivityForResult(iLogin, INTENT_LOGIN_CODE);
				} else {
					startActivity(iMain);
				}
				finish();
			}
		}, SPLASH_TIME_OUT);
	}

	// Declined terms and conditions, exit app
	public void exitSplash() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				finish();
			}
		}, SPLASH_TIME_OUT);
	}
}