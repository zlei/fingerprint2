package com.lighthousesignal.fingerprint2.network;

import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;

public class NetworkTask {
  public static final int BUFFER_SIZE = 256;
  public static final String BOUNDARY = "AaB03xdf4FdcFuM7";
  public static String DEFAULT_BASE_URL;
  public static final boolean DEFAULT_POST_URL_ENCODE = false;

  private String mUrl;
  private boolean mIsGet;
  private boolean mIsPostUrlEncoded;
  private String mParams;
  private Hashtable<String, String> mSourceParams;
  private boolean mIsCompleted = false;
  private INetworkTaskStatusListener mListener;
  private Hashtable<String, Object> mTag;

  public NetworkTask(INetworkTaskStatusListener listener, String baseUrl, String appendUrl,
      boolean isGet, Hashtable<String, String> params, boolean isPostUrlencoded) {
    setListener(listener);
    mIsPostUrlEncoded = isPostUrlencoded;
    mUrl = baseUrl + appendUrl;
    mIsGet = isGet;
    mParams = prepareParams(isGet, params, mIsPostUrlEncoded);
  }

  public NetworkTask(INetworkTaskStatusListener listener, String baseUrl, String appendUrl,
      boolean isGet, Hashtable<String, String> params) {
    this(listener, baseUrl, appendUrl, isGet, params, DEFAULT_POST_URL_ENCODE);
  }

  public NetworkTask(INetworkTaskStatusListener listener, String appendUrl, boolean isGet,
      Hashtable<String, String> params, boolean isPostUrlencoded) {
    this(listener, DEFAULT_BASE_URL, appendUrl, isGet, params, isPostUrlencoded);
  }

  public NetworkTask(INetworkTaskStatusListener listener, String appendUrl, boolean isGet,
      Hashtable<String, String> params) {
    this(listener, DEFAULT_BASE_URL, appendUrl, isGet, params);
  }

  public String getUrl() {
    return mUrl;
  }

  public void addParam(String key, byte[] paramBytes) {

  }

  public Object getTag(String key) {
    return mTag == null ? null : mTag.get(key);
  }

  public void setTag(String key, Object tag) {
    if (mTag == null)
      mTag = new Hashtable<String, Object>();
    mTag.put(key, tag);
  }

  public boolean isGet() {
    return mIsGet;
  }

  public boolean isPostUrlEncoded() {
    return mIsPostUrlEncoded;
  }

  public String getParams() {
    return mParams;
  }

  public void setComplete() {
    mIsCompleted = true;
  }

  public boolean isCompleted() {
    return mIsCompleted;
  }

  private static String prepareParams(boolean isGet, Hashtable<String, String> params,
      boolean isPostUrlEncode) {
    if (params == null)
      return "";
    String result = "";
    if (isGet) {
      StringBuffer postBuffer = new StringBuffer(params.size() * 20);
      boolean isFirst = true;
      for (Enumeration<String> it = params.keys(); it.hasMoreElements();) {
        String key = it.nextElement();
        String value = URLEncoder.encode(params.get(key));
        postBuffer.append(isFirst ? '?' : '&').append(key).append('=').append(value);
        isFirst = false;
      }
      result = postBuffer.toString();
    } else if (isPostUrlEncode) {
      boolean isFirst = true;
      StringBuffer postBuffer = new StringBuffer(params.size() * 20);
      for (Enumeration<String> it = params.keys(); it.hasMoreElements();) {
        String key = it.nextElement();
        String value = URLEncoder.encode(params.get(key));
        if (!isFirst)
          postBuffer.append('&');
        postBuffer.append(key).append('=').append(value);
        isFirst = false;
      }
      result = postBuffer.toString();
    } else {
      StringBuffer postBuffer = new StringBuffer(params.size() * 40);
      for (Enumeration<String> it = params.keys(); it.hasMoreElements();) {
        String key = it.nextElement();
        String value = params.get(key);
        postBuffer.append("\r\n--" + BOUNDARY + "\r\n");
        postBuffer.append("Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n");
        postBuffer.append(value);
      }
      result = postBuffer.toString();
    }
    return result;
  }

  public void setListener(INetworkTaskStatusListener mListener) {
    this.mListener = mListener;
  }

  public INetworkTaskStatusListener getListener() {
    return mListener;
  }
}
