package com.lighthousesignal.fingerprint2.utilities;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class UiFactories {
	
	/**
	 * Standard Confirm Dialog
	 * 
	 * @param title
	 * @param message
	 * @param onClickListener
	 * @return
	 */
	public static AlertDialog standardConfirmDialog(Context context, String title, String message,
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
	public static AlertDialog standardAlertDialog(Context context, String title, String message,
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

	/**
	 * get string from file
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
