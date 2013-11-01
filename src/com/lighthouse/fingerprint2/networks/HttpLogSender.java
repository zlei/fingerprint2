/*
 * For the full copyright and license information, please view the LICENSE file that was distributed
 * with this source code. (c) 2011
 */

package com.lighthouse.fingerprint2.networks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;

import com.lighthouse.fingerprint2.R;
import com.lighthouse.fingerprint2.activities.BasicActivity;
import com.lighthouse.fingerprint2.activities.MainMenuActivity;
import com.lighthouse.fingerprint2.logs.ErrorLog;
import com.lighthouse.fingerprint2.logs.LogWriter;

/**
 * HttpLogSender
 * 
 * @author Kuban Dzhakipov <kuban.dzhakipov@sibers.com>
 * @version SVN: $Id$
 */
public class HttpLogSender extends AsyncTask<Void, Integer, Long> {

	/**
	 * Send url
	 */
	protected String mUrl;

	/**
	 * List of logs
	 */
	protected ArrayList<String> mFiles;

	/**
	 * Exception
	 */
	protected Exception mException;

	/**
	 * Context
	 */
	protected BasicActivity mContext;

	protected String mToken;

	/**
	 * alert dialog with loading info
	 */
	protected AlertDialog mLoaderDialog;

	public HttpLogSender(BasicActivity context, String url,
			ArrayList<String> files) {
		mFiles = files;
		mUrl = url;
		mContext = context;
	}

	/**
	 * Shows progress bar
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		mLoaderDialog = new ProgressDialog(mContext);
		mLoaderDialog.setMessage(mContext
				.getString(R.string.msg_dialog_loading));
		mLoaderDialog.setCancelable(false);
		mLoaderDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				cancel(true);
			}
		});
		mLoaderDialog.show();
	}

	/**
	 * Sending logs to a server
	 */
	protected Long doInBackground(Void... params) {

		

		for (String file : mFiles) {
			try {
				// Create a new HttpClient and Post Header
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(mUrl);

				String filename = file.substring(file.lastIndexOf("/") + 1);

				filename = filename.equals(LogWriter.DEFAULT_NAME) ? LogWriter
						.generateFilename() : filename.replace(".log", "");

						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
								2);
						nameValuePairs.add(new BasicNameValuePair("logdata", LogWriter
								.readFile(file)));
						nameValuePairs
						.add(new BasicNameValuePair("scanname", filename));
						if(mToken!=null)
							nameValuePairs
							.add(new BasicNameValuePair("token",  mToken));				
						nameValuePairs.add(new BasicNameValuePair("device_log",
								LogWriter.readFile(file.replace(".log", ".dev"))));
						httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

						if (isCancelled())
							break;

						// Execute HTTP Post Request
						HttpResponse response = httpclient.execute(httppost);

						if (response.getStatusLine().getStatusCode() != 200) {
							System.out.println("not 200");
							throw new Exception("Http code: "
									+ response.getStatusLine().getStatusCode()
									+ "\nResponse: "
									+ readStream(response.getEntity().getContent()));
						}else{
							System.out.println("Marking sent" + filename);
							MainMenuActivity.setSentFlags(filename + ".log", 1, mContext);	//Mark file as sent
						}

						XmlPullParser parser = XmlPullParserFactory.newInstance()
								.newPullParser();

						parser.setInput(response.getEntity().getContent(), "UTF-8");

						while (parser.getEventType() == XmlPullParser.END_DOCUMENT) {
							if (parser.getEventType() == XmlPullParser.START_TAG
									&& parser.getName().equalsIgnoreCase("result")) {
								parser.next();
								if (!parser.getText().equals("true")) {
									throw new Exception("Http code: "
											+ response.getStatusLine().getStatusCode()
											+ "\nResponse: " + parser.getText());
								}
							}
							parser.nextTag();
						}
						
			} catch (Exception e) {
				mException = e;
				e.printStackTrace();
				ErrorLog.e(e);
			}
		}

		return null;
	}

	public HttpLogSender setToken(String token){
		mToken = token;
		return this;
	}		

	public static String readStream(InputStream is) {
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			ErrorLog.e(e);
		} finally {

		}
		return builder.toString();
	}

	/**
	 * Dismiss progress bar and show success message
	 */
	protected void onPostExecute(Long unused) {
		mLoaderDialog.dismiss();

		if (!isCancelled()) {
			if (mException == null){
//				for(String f : mFiles) {
//					//Find filename by splitting off directories
//					String[] tokens = f.split("/");
//					String fname = tokens[ tokens.length -1 ];
//					MainActivity.setSentFlags(fname, 1, mContext);	//Mark file as sent
//				}
				mContext.standardAlertDialog(
						mContext.getString(R.string.msg_alert),
						mContext.getString(R.string.msg_alert_1),
						null);
			}else
				mContext.standardAlertDialog(
						mContext.getString(R.string.msg_error),
						mContext.getString(R.string.msg_alert_2), null);
		}
	}

}
