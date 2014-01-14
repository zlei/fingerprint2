package com.lighthousesignal.fingerprint2.activities;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.lighthousesignal.fingerprint2.R;
import com.lighthousesignal.fingerprint2.network.INetworkTaskStatusListener;
import com.lighthousesignal.fingerprint2.network.NetworkManager;
import com.lighthousesignal.fingerprint2.network.NetworkResult;
import com.lighthousesignal.fingerprint2.network.NetworkTask;
import com.lighthousesignal.fingerprint2.utilities.DataPersistence;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends BasicActivity implements
		INetworkTaskStatusListener {

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	public static final int NETWORK_LOGIN = 1;

	// Values for Username and password at the time of the login attempt.
	private String mUsername;
	private String mPassword;
	private String mCustomerID;
	private String mDeveloperID;
	private String mServer;
	// UI references.
	private EditText mUsernameView;
	private EditText mPasswordView;
	private EditText mCustomerIDView;
	private EditText mDeveloperIDView;
	private EditText mServerView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		NetworkTask.DEFAULT_BASE_URL = DataPersistence.getServerName(this);

		// Set up the login form.
		// mUsername = getIntent().getStringExtra(EXTRA_Username);
		mUsernameView = (EditText) findViewById(R.id.username);
		mUsernameView.setText(mUsername);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mCustomerIDView = (EditText) findViewById(R.id.customer_id);
		mCustomerIDView.setText(mCustomerID);

		mDeveloperIDView = (EditText) findViewById(R.id.developer_id);
		mDeveloperIDView.setText(mDeveloperID);

		mServerView = (EditText) findViewById(R.id.server_name);
		mServerView.setText(mServer);

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						// Intent intent = new Intent(LoginActivity.this,
						// MainMenuActivity.class);
						// startActivity(intent);
						attemptLogin();
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid Username, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */

	public void attemptLogin() {

		// Reset errors.
		mUsernameView.setError(null);
		mPasswordView.setError(null);
		mCustomerIDView.setError(null);
		mDeveloperIDView.setError(null);
		mServerView.setError(null);

		// Store values at the time of the login attempt.
		// mUsername = mUsernameView.getText().toString();
		// mPassword = mPasswordView.getText().toString();
		// mCustomerID = mCustomerIDView.getText().toString();
		// mDeveloperID = mDeveloperIDView.getText().toString();
		// mServer = mServerView.getText().toString();
		mUsername = "test";
		mPassword = "test3030";
		mCustomerID = "aa";
		mDeveloperID = "aa";
		mServer = getString(R.string.default_url);

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid username.
		if (TextUtils.isEmpty(mUsername)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		}
		// Check for a valid customer id.
		if (TextUtils.isEmpty(mCustomerID)) {
			mCustomerIDView.setError(getString(R.string.error_field_required));
			focusView = mCustomerIDView;
			cancel = true;
		}
		// Check for a valid developer id.
		if (TextUtils.isEmpty(mDeveloperID)) {
			mDeveloperIDView.setError(getString(R.string.error_field_required));
			focusView = mDeveloperIDView;
			cancel = true;
		}
		// Check for a valid Server address.
		if (TextUtils.isEmpty(mServer)) {
			mServerView.setError(getString(R.string.error_field_required));
			focusView = mServerView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {

			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			if (isOnline()) {
				Hashtable<String, String> hash = new Hashtable<String, String>(
						3);
				hash.put("login", mUsername);
				hash.put("password", mPassword);
				NetworkTask task = new NetworkTask(this, mServer,
						"/user/index/login", false, hash, true);
				task.setTag(TAG_KEY, Integer.valueOf(NETWORK_LOGIN));
				NetworkManager.getInstance().addTask(task);
			} else {
				// need internet connection
				standardAlertDialog(getString(R.string.msg_alert_connection),
						getString(R.string.msg_alert_connection), null);
			}
			try {
				// Simulate network access.
				Thread.sleep(0);
			} catch (InterruptedException e) {
				return;
			}

		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */

	@Override
	public void nTaskErr(NetworkResult result) {

		if (result.getResponceCode() == 401)
			standardAlertDialog(getString(R.string.msg_alert_connection),
					getString(R.string.msg_error_network_401), null);
		else {
			standardAlertDialog(getString(R.string.msg_alert_connection),
					getString(R.string.msg_error_network_unknown), null);
			Log.e(LOG_TAG, "error", result.getException());
		}
	}

	@Override
	public void nTaskSucces(NetworkResult result) {
		showProgress(true);
		switch ((Integer) (result.getTask().getTag(TAG_KEY))) {
		case NETWORK_LOGIN:
			try {
				BufferedReader buff = new BufferedReader(new InputStreamReader(
						new ByteArrayInputStream(result.getData())));
				StringBuffer strBuff = new StringBuffer();

				String s;

				while ((s = buff.readLine()) != null) {
					strBuff.append(s);
				}

				String loginToken = strBuff.toString();
				saveToken(loginToken, mUsername, mPassword);
				Editor editor = getLocalPreferences().edit();
				editor.putString(PREF_CUSTOMER_ID, mCustomerID);
				editor.putString(PREF_DEVELOPER_ID, mDeveloperID);
				editor.commit();
				DataPersistence.setToken(this, loginToken);
				DataPersistence.setServerName(this, mServer);
				Intent intent = new Intent(LoginActivity.this,
						MainActivity.class);
				// Intent intent = new Intent(LoginActivity.this,
				// MapViewActivity.class);
				startActivity(intent);

				finish();
			} catch (Exception e) {
				standardAlertDialog(getString(R.string.msg_alert_connection),
						getString(R.string.msg_error_network_unknown), null);

			}
			break;
		}
	}
}
