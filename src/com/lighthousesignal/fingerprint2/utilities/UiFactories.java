package com.lighthousesignal.fingerprint2.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lighthousesignal.fingerprint2.R;
import com.lighthousesignal.fingerprint2.activities.MainActivity;
import com.lighthousesignal.fingerprint2.activities.MapViewActivity;
import com.lighthousesignal.fingerprint2.logs.ErrorLog;
import com.lighthousesignal.fingerprint2.logs.LogWriter;
import com.lighthousesignal.fingerprint2.logs.LogWriterSensors;

public class UiFactories {

	/**
	 * Standard Confirm Dialog
	 * 
	 * @param title
	 * @param message
	 * @param onClickListener
	 * @return
	 */
	public static AlertDialog standardConfirmDialog(Context context,
			String title, String message,
			DialogInterface.OnClickListener onClickListenerPositive,
			DialogInterface.OnClickListener onClickListenerNegative,
			boolean cancelable) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
		alertDialog.setTitle(title);
		alertDialog.setPositiveButton("YES", onClickListenerPositive);
		alertDialog.setNegativeButton("NO", onClickListenerNegative);
		alertDialog.setCancelable(cancelable);
		alertDialog.setMessage(message);
		alertDialog.show();

		return alertDialog.create();
	}

	/**
	 * Standard Alert Message Dialog
	 * 
	 * @param title
	 * @param message
	 * @param onClickListener
	 * @return
	 */
	public static AlertDialog standardAlertDialog(Context context,
			String title, String message,
			DialogInterface.OnClickListener onClickListener) {
		try {
			AlertDialog alertDialog = new AlertDialog.Builder(context).create();
			alertDialog.setTitle(title);
			alertDialog.setButton("OK", onClickListener);
			alertDialog.setMessage(message);

			alertDialog.show();

			return alertDialog;
		} catch (Exception e) {
			// sometime it loses current window
			return null;
		}
	}

	public static AlertDialog segmentNameDailog(String title,
			final Context context, final String existingFilename,
			final MapViewActivity activity) {
		final AlertDialog.Builder alert = new AlertDialog.Builder(context);

		final LinearLayout view = new LinearLayout(context);
		final TextView datestamp = new TextView(context);
		final EditText segnum = new EditText(context);
		segnum.setInputType(InputType.TYPE_CLASS_NUMBER);
		final Spinner segmode = new Spinner(context);
		final EditText nameinput = new EditText(context);

		List<String> items = new ArrayList<String>();
		items.add("orig");
		items.add("mod");
		items.add("missing");
		items.add("new");
		items.add("a");
		items.add("b");
		items.add("c");
		items.add("d");
		items.add("e");
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_spinner_item, items);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		segmode.setAdapter(dataAdapter);
		segnum.setHint("#");
		nameinput.setHint("Name");
		TextView splitter = new TextView(context);
		splitter.setText("-");

		datestamp.setText(LogWriter.generateFilename() + "-");
		segmode.setSelection(0);
		if (activity.getResources().getConfiguration().orientation == 1) {
			LinearLayout horizontal = new LinearLayout(context);
			LinearLayout vertical = new LinearLayout(context);
			horizontal.setOrientation(LinearLayout.HORIZONTAL);
			vertical.setOrientation(LinearLayout.VERTICAL);

			horizontal.addView(datestamp);
			horizontal.addView(segnum);
			horizontal.addView(segmode);
			horizontal.addView(splitter);

			vertical.addView(horizontal);
			vertical.addView(nameinput);
			view.addView(vertical);

		} else {
			view.addView(datestamp);
			view.addView(segnum);
			view.addView(segmode);
			view.addView(splitter);
			view.addView(nameinput);
		}

		alert.setView(view);
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				try {
					String value = "";
					String num = segnum.getText().toString().trim();
					String mode = segmode.getSelectedItem().toString().trim();
					String name = nameinput.getText().toString().trim()
							.replaceAll("[^0-9a-zA-Z]", "_");

					if (name == "")
						name = "noname";
					if (mode == "orig")
						mode = "";
					if (num.isEmpty()) {
						standardAlertDialog(
								context,
								"Warning",
								"Please provide a segment number and save again!",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										dialog.cancel();
										Toast.makeText(
												activity.getApplicationContext(),
												"Data not saved!",
												Toast.LENGTH_SHORT).show();
									}
								});
					} else {
						value = datestamp.getText().toString().trim() + num
								+ mode + "-" + name;

						LogWriter.instance().saveLog(value + ".log");
						LogWriterSensors.instance().saveLog(value + ".dev");
						dialog.dismiss();
						Toast.makeText(activity.getApplicationContext(),
								"Data saved!", Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					e.printStackTrace();
					ErrorLog.e(e);
					Log.e("Error", "Unknown error");
					standardAlertDialog(context,
							activity.getString(R.string.msg_error),
							activity.getString(R.string.msg_error), null);
				}
			}
		});
		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
						Toast.makeText(activity.getApplicationContext(),
								"Data not saved!", Toast.LENGTH_SHORT).show();
					}
				});
		return alert.create();
	}

	/**
	 * get string from file
	 * 
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public static String convertStreamToString(InputStream is) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line).append("\n");
		}
		return sb.toString();
	}
}
