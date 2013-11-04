package com.lighthouse.fingerprint2.networks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.http.client.methods.HttpGet;

import android.os.Handler;
import android.util.Log;

import com.lighthouse.fingerprint2.activities.BasicActivity;

public class NetworkManager {
	private static String mCookies;
	private static NetworkManager mInstance;

	public static NetworkManager getInstance() {
		return mInstance == null ? mInstance = new NetworkManager() : mInstance;
	}

	public static NetworkManager getNewThreadInstance() {
		return new NetworkManager();
	}// TODO

	Thread mNetworkThread = new Thread() {
		public void run() {
			while (true) {
				if (mTasks.size() == 0) {
					try {
						synchronized (this) {
							wait(200);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					final NetworkTask task = mTasks.poll();
					final NetworkResult result = processTask(task);
					mHandler.post(new Runnable() {
						public void run() {
							if (result.getException() == null
									&& result.getData() != null
									&& result.getData().length > 0)
								task.getListener().nTaskSucces(result);
							else
								task.getListener().nTaskErr(result);
						}
					});
				}
			}
		}
	};
	private Handler mHandler;

	private NetworkManager() {
		mHandler = new Handler();
		mNetworkThread.start();
	}

	Queue<NetworkTask> mTasks = new ConcurrentLinkedQueue<NetworkTask>();

	public void addTask(NetworkTask task) {
		mTasks.add(task);
	}

	private NetworkResult processTask(NetworkTask task) {
		NetworkResult result = new NetworkResult();
		result.setTask(task);
		HttpURLConnection c = null;
		InputStream is = null;
		OutputStream os = null;
		try {
			URL url = new URL(task.getUrl()
					+ (task.isGet() ? task.getParams() : ""));
			URLConnection conn = url.openConnection();
			if (!(conn instanceof HttpURLConnection))
				throw new IOException("Not an HTTP connection");
			c = (HttpURLConnection) conn;
			if (task.isGet()) {
				c.setRequestMethod(HttpGet.METHOD_NAME);
			} else {
				c.setDoInput(true);
				c.setDoOutput(true);
				c.setRequestMethod("POST");
				if (task.isPostUrlEncoded())
					c.setRequestProperty("Content-Type",
							"application/x-www-form-urlencoded;");
				else
					c.setRequestProperty("Content-Type",
							"multipart/form-data; boundary="
									+ NetworkTask.BOUNDARY);
			}
			c.connect();
			if (!task.isGet()) {
				os = c.getOutputStream();
				os.write(task.getParams().getBytes());
				if (!task.isPostUrlEncoded())
					os.write(("\r\n--" + task.BOUNDARY + "--\r\n").getBytes());
			}
			final int rc = c.getResponseCode();
			result.setResponceCode(rc);
			if (rc == HttpURLConnection.HTTP_OK
					|| rc == HttpURLConnection.HTTP_MOVED_PERM
					|| rc == HttpURLConnection.HTTP_INTERNAL_ERROR) {
				is = c.getInputStream();
				byte[] data;
				int len = (int) c.getContentLength();
				if (len > 0) {
					int actual = 0;
					int bytesread = 0;
					data = new byte[len];
					while ((bytesread != len) && (actual != -1)) {
						actual = is.read(data, bytesread, len - bytesread);
						bytesread += actual;
					}
				} else {

					data = new byte[NetworkTask.BUFFER_SIZE];
					int ch;
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					while ((ch = is.read(data)) != -1) {
						out.write(data, 0, ch);
					}
					data = out.toByteArray();
				}
				result.setData(data);
			}
		} catch (final Exception e) {
			try {
				final int rc = c.getResponseCode();
				result.setResponceCode(rc);
				is = c.getErrorStream();
				ByteArrayOutputStream fo = new ByteArrayOutputStream();
				int read;

				do {
					read = is.read();
					fo.write(read);
				} while (read != -1);

				result.setData(fo.toByteArray());

				Log.v(BasicActivity.LOG_TAG,
						"response " + result.getDataString());
			} catch (Exception e2) {
				if (e2 != null
						&& e2.getMessage().contains(
								"Received authentication challenge is null")) {
					result.setResponceCode(401);
				}

				else if (e2 != null
						&& e2.getMessage().contains("failed to connect")) {
					result.setResponceCode(1006);
				}

				result.setException(e2);
			}
			result.setException(e);
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {
				}
			if (c != null)
				c.disconnect();
		}
		return result;
	}

	private String getResponseCookies(HttpURLConnection c) throws IOException {
		String result = new String();
		int i = 0;
		String headerValue = c.getHeaderField(i);
		String headerKey = c.getHeaderFieldKey(i);
		while (headerValue != null) {
			Log.v("NET", "" + headerKey + " = " + headerValue);
			if (headerKey != null && headerKey.equalsIgnoreCase("set-cookie")) {
				int index = headerValue.indexOf(';');
				String header = null;
				if (index == -1)
					header = headerValue;
				else
					header = headerValue.substring(0, index);
				// if (result.length() > 1)
				// result += "; ";
				result = header;
			}
			i++;
			headerKey = c.getHeaderFieldKey(i);
			headerValue = c.getHeaderField(i);
		}
		return result;
	}
}
