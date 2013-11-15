package com.lighthouse.fingerprint2.activities;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.lighthouse.fingerprint2.R;
import com.lighthouse.fingerprint2.logs.ErrorLog;
import com.lighthouse.fingerprint2.logs.LogWriter;
import com.lighthouse.fingerprint2.logs.LogWriterSensors;
import com.lighthouse.fingerprint2.networks.HttpLogSender;
import com.lighthouse.fingerprint2.utilities.DataPersistence;

public class MainMenuActivity extends BasicActivity {

	public static final String FLOOR_ID = "FLOOR_ID";

	public static final String FLOOR_NAME = "FLOOR_NAME";

	public static final String IMAGE_PATH = "IMAGE_PATH";

	/**
	 * Dialog ids
	 */

	private static final int DIALOG_ID_REVIEW = 2;

	private static final int DIALOG_ID_STATES = 1;

	private static final int DIALOG_ID_COUNTRIES = 3;

	protected String[] files;

	public static final int logout_menu = Menu.FIRST + 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);

		Button button_maplists = (Button) findViewById(R.id.button_maplists);
		button_maplists.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainMenuActivity.this,
						MapListActivity.class);
				startActivity(intent);
				// Perform action on click
			}
		});

		Button button_buildings = (Button) findViewById(R.id.button_buildings);
		button_buildings.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainMenuActivity.this,
						MapViewActivity.class);
				startActivity(intent);
				// Perform action on click
			}
		});

		Button button_savelog = (Button) findViewById(R.id.button_reviewlog);
		button_savelog.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Perform action on click

				getDialogReviewLogs().show();
			}
		});
		Button button_uploadlog = (Button) findViewById(R.id.button_uploadlog);
		button_uploadlog.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Perform action on click
			}
		});

	}

	/**
	 * Create dialog list of logs
	 * 
	 * @return
	 */
	public AlertDialog getDialogReviewLogs() {
		/**
		 * List of logs
		 */
		File folder = new File(LogWriter.APPEND_PATH);
		final String[] files = folder.list(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				if (filename.contains(".log")
						&& !filename.equals(LogWriter.DEFAULT_NAME)
						&& !filename.equals(LogWriterSensors.DEFAULT_NAME)
						&& !filename.equals(ErrorLog.DEFAULT_NAME))
					return true;
				else
					return false;
			}
		});

		Arrays.sort(files);
		ArrayUtils.reverse(files);

		String[] files_with_status = new String[files.length];
		String[] sent_mode = { "", "(s) ", "(e) ", "(s+e) " };
		for (int i = 0; i < files.length; ++i) {
			// 0 -- not sent
			// 1 -- server
			// 2 -- email
			files_with_status[i] = sent_mode[getSentFlags(files[i], this)]
					+ files[i];
		}

		if (files != null && files.length > 0) {

			final boolean[] selected = new boolean[files.length];

			final AlertDialog ald = new AlertDialog.Builder(
					MainMenuActivity.this)
					.setMultiChoiceItems(files_with_status, selected,
							new DialogInterface.OnMultiChoiceClickListener() {
								public void onClick(DialogInterface dialog,
										int which, boolean isChecked) {
									selected[which] = isChecked;
								}
							})
					/*
					 * .setOnCancelListener(new OnCancelListener() {
					 * 
					 * @Override public void onCancel(DialogInterface dialog) {
					 * // removeDialog(DIALOG_ID_REVIEW); } })
					 *//**
					 * Delete log
					 */
					.setNegativeButton("Delete",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {

									// Show delete confirm
									standardConfirmDialog(
											"Delete Logs",
											"Are you sure you want to delete selected logs?",
											new OnClickListener() {
												// Confrim Delete
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {

													int deleteCount = 0;
													boolean flagSelected = false;
													for (int i = 0; i < selected.length; i++) {
														if (selected[i]) {
															flagSelected = true;
															LogWriter
																	.delete(files[i]);
															LogWriter
																	.delete(files[i]
																			.replace(
																					".log",
																					".dev"));
															deleteCount++;
														}
													}

													reviewLogsCheckItems(flagSelected);

													removeDialog(DIALOG_ID_REVIEW);

													Toast.makeText(
															getApplicationContext(),
															deleteCount
																	+ " logs deleted.",
															Toast.LENGTH_SHORT)
															.show();
												}
											}, new OnClickListener() {
												// Cancel Delete
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													// Do nothing
													dialog.dismiss();
													Toast.makeText(
															getApplicationContext(),
															"Delete cancelled.",
															Toast.LENGTH_SHORT)
															.show();
												}
											}, false);
								}
							})
					/**
					 * Send to server functional
					 **/
					.setNeutralButton("Send to Server",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									if (isOnline()) {
										ArrayList<String> filesList = new ArrayList<String>();

										for (int i = 0; i < selected.length; i++)
											if (selected[i]) {

												filesList
														.add(LogWriter.APPEND_PATH
																+ files[i]);
												// Move to httplogsender
												// setSentFlags(files[i], 1,
												// MainActivity.this); //Mark
												// file as sent
											}

										if (reviewLogsCheckItems(filesList
												.size() > 0 ? true : false)) {
											DataPersistence d = new DataPersistence(
													getApplicationContext());
											new HttpLogSender(
													MainMenuActivity.this,
													d.getServerName()
															+ getString(R.string.submit_log_url),
													filesList).setToken(
													getToken()).execute();
										}

										// removeDialog(DIALOG_ID_REVIEW);
									} else {
										standardAlertDialog(
												getString(R.string.msg_alert),
												getString(R.string.msg_alert_1),
												null);
									}
								}
							})
					/**
					 * Email
					 **/
					.setPositiveButton("eMail",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									boolean flagSelected = false;
									// convert from paths to Android friendly
									// Parcelable Uri's
									ArrayList<Uri> uris = new ArrayList<Uri>();
									for (int i = 0; i < selected.length; i++)
										if (selected[i]) {
											flagSelected = true;
											/** wifi **/
											File fileIn = new File(
													LogWriter.APPEND_PATH
															+ files[i]);
											Uri u = Uri.fromFile(fileIn);
											uris.add(u);

											/** sensors **/
											File fileInSensors = new File(
													LogWriter.APPEND_PATH
															+ files[i].replace(
																	".log",
																	".dev"));
											Uri uSens = Uri
													.fromFile(fileInSensors);
											uris.add(uSens);

											setSentFlags(files[i], 2,
													MainMenuActivity.this); // Mark
																			// file
																			// as
																			// emailed
										}

									if (reviewLogsCheckItems(flagSelected)) {
										/**
										 * Run sending email activity
										 */
										Intent emailIntent = new Intent(
												android.content.Intent.ACTION_SEND_MULTIPLE);
										emailIntent.setType("plain/text");
										emailIntent
												.putExtra(
														android.content.Intent.EXTRA_SUBJECT,
														"Wifi Searcher Scan Log");
										emailIntent
												.putParcelableArrayListExtra(
														Intent.EXTRA_STREAM,
														uris);
										startActivity(Intent.createChooser(
												emailIntent, "Send mail..."));
									}

									// removeDialog(DIALOG_ID_REVIEW);
								}
							}).create();

			ald.getListView().setOnItemLongClickListener(
					new OnItemLongClickListener() {

						@Override
						public boolean onItemLongClick(AdapterView<?> parent,
								View view, final int position, long id) {

							AlertDialog segmentNameAlert = segmentNameDailog(
									"Rename Segment", ald.getContext(),
									files[position], null, view, files,
									position);
							segmentNameAlert.setCanceledOnTouchOutside(false);
							segmentNameAlert.show();
							return false;
						}
					});
			return ald;
		} else {
			return standardAlertDialog(getString(R.string.msg_alert),
					getString(R.string.msg_alert), new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							removeDialog(DIALOG_ID_REVIEW);
						}
					});
		}
	}

	/**
	 * Create dialog for states list
	 * 
	 * @return
	 */
	public AlertDialog getDialogStates() {
		return new AlertDialog.Builder(MainMenuActivity.this).setItems(
				getLocationManager().getStatesList(),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						which *= 3;

						/**
						 * update active state
						 */
						getLocationManager().writeActiveOption(which);

						/**
						 * Update list of states with selected item
						 */
						removeDialog(DIALOG_ID_STATES);
					}
				}).create();
	}

	/*
	 * Review Logs. Validating Checking Items
	 */
	private boolean reviewLogsCheckItems(boolean flagSelected) {
		if (!flagSelected)
			standardAlertDialog(getString(R.string.msg_alert),
					getString(R.string.msg_alert), null);

		return flagSelected;
	}

	public static void setSentFlags(String filename, int mode, Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				"REVIEW_SENT_PREFS", MODE_PRIVATE);
		// See if a flag is already there
		int flag = prefs.getInt(filename, 0);
		if (flag == 1 && mode == 2)
			mode = 3; // Was sent, now emailed as well
		if (flag == 2 && mode == 1)
			mode = 3; // Was emailed, now sent as well
		prefs.edit().putInt(filename, mode).commit();
	}

	public static int getSentFlags(String filename, Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				"REVIEW_SENT_PREFS", MODE_PRIVATE);
		return prefs.getInt(filename, 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, logout_menu, 0, "Logout");
		// getMenuInflater().inflate(R.menu.main_menu, menu);
		return result;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case logout_menu:
			Intent intent = new Intent(MainMenuActivity.this,
					LoginActivity.class);
			startActivity(intent);
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
