package com.lighthousesignal.fingerprint2.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.lighthousesignal.fingerprint2.R;

public class SplashActivity extends BasicActivity implements OnClickListener {
	private static int SPLASH_TIME_OUT = 0;

	/**
	 * To let user choose login method: Facebook or Lighthouse
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		boolean firstrun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
				.getBoolean("firstrun", true);

		// if firstrun, popup terms and conditions
		if (firstrun) {
			new AlertDialog.Builder(this)
					.setIcon(R.drawable.ic_launcher)
					.setTitle(R.string.terms)
					.setMessage(R.string.app_name)
					.setPositiveButton("Agree",
							new DialogInterface.OnClickListener() {
								@Override
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
								@Override
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

	/**
	 * To decide first login or not
	 */
	public void firstLogin() {
		Button button_login_facebook = (Button) findViewById(R.id.login_facebook);
		Button button_login_fingerprint2 = (Button) findViewById(R.id.login_fingerprint2);
		button_login_facebook.setOnClickListener(this);
		button_login_fingerprint2.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch (id) {
		case R.id.login_facebook:
			Intent i_loginFB = new Intent(SplashActivity.this,
					LoginActivity.class);
			startActivity(i_loginFB);
			finish();

			break;
		case R.id.login_fingerprint2:
			Intent i_loginFP = new Intent(SplashActivity.this,
					LoginActivity.class);
			startActivity(i_loginFP);
			finish();

			break;
		}

	}

	/**
	 * fade splash screen to main activity
	 */
	public void fadeSplash() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent iLogin = new Intent(SplashActivity.this,
						LoginActivity.class);
				Intent iMain = new Intent(SplashActivity.this,
						MainActivity.class);
				if (!hasToken()) {
					startActivityForResult(iLogin, INTENT_LOGIN_CODE);
				} else {
					startActivity(iMain);
				}
				finish();
			}
		}, SPLASH_TIME_OUT);
	}

	/**
	 * Declined terms and conditions, exit app
	 */
	public void exitSplash() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				finish();
			}
		}, SPLASH_TIME_OUT);
	}

}
