package com.lighthouse.fingerprint2.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.lighthouse.fingerprint2.R;

public class SplashActivity extends BasicActivity {

	private static int SPLASH_TIME_OUT = 2000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		// if firstrun, popup terms and conditions
		boolean firstrun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
				.getBoolean("firstrun", true);

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
									fadeSplash();
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

	// fade splash screen to main activity
	public void fadeSplash() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent iLogin = new Intent(SplashActivity.this,
						LoginActivity.class);
				Intent iMain = new Intent(SplashActivity.this,
						MainMenuActivity.class);
				if (hasToken()) {
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