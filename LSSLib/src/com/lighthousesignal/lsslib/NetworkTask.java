package com.lighthousesignal.lsslib;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.client.methods.HttpGet;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class NetworkTask extends AsyncTask<Void, Void, NetworkResult>{

	private static final int BUFFER_SIZE = 256;
	public static final String BOUNDARY = "AaB03xdf4FdcFuM7";
	private NetworkListener mListener;
	private String mUrl;
	private int mType;
	private boolean isGet, isPostUrlEncoded;
	private String mParams;
	private int mQueryId;
	
	public static SharedPreferences mPreferences;
		
	/**
	 * Creates a network task, which can be executed as an independent thread.
	 * Calls the listeners onTaskSuccess or onTaskError methods, with the result of the network operation.
	 * 
	 * @param listener The NetworkListener to receive the results of the task.
	 * @param type i.e. NetworkListener.LOGIN
	 * @param path "/logs/pars/findpoint"
	 * @param isGet Is this a get or a post?
	 * @param isPostUrlEncoded Is this post url encoded?
	 * @param send The string to send.
	 * @param queryId Used to identify this task when results are returned.
	 */
	public NetworkTask(NetworkListener listener, int type, String path, boolean isGet, boolean isPostUrlEncoded, String send, int queryId){
		
		String host = mPreferences.getString("hostname", "");
		mListener = listener;
		mType = type;
		mUrl = host + path;
		this.isGet = isGet;
		this.mQueryId = queryId;
		this.isPostUrlEncoded = isPostUrlEncoded;
		mParams = send;
	}
	
	@Override
	protected void onPreExecute() {
		
	}
	
	@Override
	protected NetworkResult doInBackground(Void... arg0) {
		Log.d("LSS NetworkTask", "Started network task url " + mUrl + mParams);
		NetworkResult result = new NetworkResult();
		result.setQueryId(mQueryId);
		result.setResultType(mType);
		HttpURLConnection c = null;
		InputStream is = null;
		OutputStream os = null;
		Log.d("LSS NetworkTask", "before try block");
		try{
			URL url = new URL(mUrl + (isGet ? mParams : ""));
						
			URLConnection conn = url.openConnection();
			if( !(conn instanceof HttpURLConnection))throw new IOException("Not an HTTP connection");
			c = (HttpURLConnection)conn;
			if(isGet){
				c.setRequestMethod(HttpGet.METHOD_NAME);
			} else { //endget
				c.setDoInput(true);
				c.setDoOutput(true);
				c.setRequestMethod("POST");
				if (isPostUrlEncoded){
					c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;");
				} else {
					c.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
				}
			}//endpost
			c.connect();
			if (!isGet) {
				os = c.getOutputStream();
				os.write(mParams.getBytes());
				if(!isPostUrlEncoded)os.write(("\r\n--" + BOUNDARY + "--\r\n").getBytes());
			}
			
			final int rc = c.getResponseCode();
			result.setResponseCode(rc);
			if (rc == HttpURLConnection.HTTP_OK || rc == HttpURLConnection.HTTP_MOVED_PERM) {
				is = c.getInputStream();
				byte[] data;
				int len = (int) c.getContentLength();
				if (len > 0) {
					int actual = 0;
					int bytesread = 0;
					data = new byte[len];
					while ((bytesread != len) && (actual != -1)){
						actual = is.read(data, bytesread, len-bytesread);
						bytesread += actual;
					}
				} else {
					data = new byte[BUFFER_SIZE];
					int ch;
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					while ((ch = is.read(data)) != -1) {
						out.write(data, 0, ch);
					}
					data = out.toByteArray();
				}
				result.setData(data);
			}
		}catch(final Exception e){
			try{
				final int rc = c.getResponseCode();
				result.setResponseCode(rc);
			}catch(Exception e2){
				
			}
			result.setException(e);
		} finally {
			if(is != null){
				try{
					is.close();
				}catch(IOException e){}
			}
			if(os != null){
				try{
					os.close();
				}catch(IOException e){}
			}
			if(c != null)c.disconnect();
		}
		Log.d("LSS NetworkTask", "Returning result");
		return result;
	}
	
	@Override
	protected void onPostExecute(NetworkResult result){
		//TODO Other results can mean success?
		if(result.getResponseCode() == 200){
			mListener.onTaskSuccess(result);
		}else{
			mListener.onTaskError(result);
		}
	}
}
