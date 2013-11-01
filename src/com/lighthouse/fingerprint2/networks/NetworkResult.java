package com.lighthouse.fingerprint2.networks;

public class NetworkResult {
	private int mResponceCode;
	private Exception mException;
	private NetworkTask mTask;
	private byte[] mData;
	public int getResponceCode() {
		return mResponceCode;
	}
	public void setResponceCode(int responceCode) {
		mResponceCode = responceCode;
	}
	public Exception getException() {
		return mException;
	}
	public void setException(Exception exception) {
		mException = exception;
	}
	public NetworkTask getTask() {
		return mTask;
	}
	
	public void setTask(NetworkTask task) {
		mTask = task;
	}
	
	public byte[] getData() {
		return mData;
	}
	
	public String getDataString() {
		return new String(mData);
	}
	
	public void setData(byte[] data) {
		mData = data;
	}

	public String toString() {
		return "RespCode=" + mResponceCode + "\nExc=" + mException + "\n" + " data:" + (mData == null? null : new String(mData));
	}
}
