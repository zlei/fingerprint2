package com.lighthouse.fingerprint2.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import com.lighthouse.fingerprint2.networks.NetworkResult;

public class Utils {

	public static final void log(String str) {
		Log.v("TAG", str);
	}

	public static void logError(String message) {
		if (message != null)
			Log.e("TAG", message);
	} 

	public static void errorFeedBack(Context ctx, String message) {
		Log.e("TAG", message);
		new AlertDialog.Builder(ctx).setMessage(message).setPositiveButton("Ok", null).create().show();
	}

	public static void networkErrorFeedBack(Context ctx, NetworkResult result) {
		Utils.errorFeedBack(ctx, "Network error! \n" + ((result.getException() != null)? result.getException().getLocalizedMessage() : "Http response code " + result.getResponceCode()));
	}
}
