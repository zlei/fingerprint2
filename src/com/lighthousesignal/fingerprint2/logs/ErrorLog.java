/*
 * For the full copyright and license information, please view the LICENSE file that was distributed
 * with this source code. (c) 2011
 */
package com.lighthousesignal.fingerprint2.logs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.os.Build;

/**
 * Error Log
 * 
 */
public class ErrorLog {

	public static final boolean DISABLED = false;

	/**
	 * path
	 */
	public static final String APPEND_PATH = "/sdcard/Fingerprint2/";

	/**
	 * filename
	 */
	public static final String DEFAULT_NAME = "error.log";

	/**
	 * instance of object
	 */
	private static ErrorLog sInstance;

	/**
	 * Writes exception to a log file
	 * 
	 * @param e
	 */
	public static void e(Exception e) {
		if (DISABLED)
			return;

		if (sInstance == null) {
			sInstance = new ErrorLog();
		}
		/**
		 * Collect info about error log : date, version, stacktrace
		 */
		StringBuilder log = new StringBuilder();
		log.append("ERROR DATE: ")
		.append(sInstance.mFormatter.format(Calendar.getInstance().getTime())).append("\n").append(
				"Device Model: ").append(Build.MODEL).append("\n").append("API Version ").append(
						Build.VERSION.SDK).append("\n").append("CALL STACK: ").append(getStackTrace(e)).append(
								"\n\n");

		sInstance.write(log.toString());
	}

	/**
	 * Writes your message to a log file
	 * 
	 * @param msg
	 */
	public static void msg(String msg) {
		if (DISABLED)
			return;

		if (sInstance == null) {
			sInstance = new ErrorLog();
		}

		/**
		 * Collect info about error log : date, version, stacktrace
		 */
		StringBuilder log = new StringBuilder();
		log.append("ERROR DATE: ")
		.append(sInstance.mFormatter.format(Calendar.getInstance().getTime()))
		.append("\n")
		.append("Device Model: ")
		.append(Build.MODEL)
		.append("\n")
		.append(
				"API Version ").append(Build.VERSION.SDK).append("\n").append(
						"MSG: ").append(msg).append("\n\n");

		sInstance.write(log.toString());
	}

	/**
	 * Gets stack trace info into string
	 * 
	 * @param aThrowable
	 * @return
	 */
	public static String getStackTrace(Throwable aThrowable) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);
		return result.toString();
	}

	/**
	 * Current file path
	 */
	private String mCurrentFile;

	/**
	 * Default date formatter
	 */
	public SimpleDateFormat mFormatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aaa");

	/**
	 * output stream
	 */
	private BufferedWriter mStream;

	/**
	 * File to a log
	 */
	private File mLogFile;

	/**
	 * Singleton
	 * 
	 * Creates instance
	 */
	private ErrorLog() {
		/**
		 * creating directories
		 */
		new File(APPEND_PATH).mkdirs();
		mCurrentFile = APPEND_PATH + DEFAULT_NAME;
		mLogFile = new File(mCurrentFile);
		try {
			if (!mLogFile.exists())
				mLogFile.createNewFile();
			mStream = new BufferedWriter(new FileWriter(mLogFile, true));
		} catch (Exception e) {
			sInstance = null;
			e.printStackTrace();
		}
	}

	/**
	 * Write string to a log file
	 * 
	 * @param str
	 */
	private void write(String str) {
		try {
			mStream.write(str);
			mStream.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
