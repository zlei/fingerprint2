package com.lighthousesignal.lsslib;

public class NetworkResult {
	
	public int getResponseCode() {
		return mResponseCode;
	}
	public void setResponseCode(int responseCode) {
		this.mResponseCode = responseCode;
	}
	public int getResultType(){
		return mResultType;
	}
	public void setResultType(int resultType){
		this.mResultType = resultType;
	}
	public Exception getException() {
		return mException;
	}
	public void setException(Exception exception) {
		this.mException = exception;
	}
	public byte[] getData() {
		return mData;
	}
	public void setData(byte[] data) {
		this.mData = data;
	}
	public void setQueryId(int id){
		this.mQueryId = id;
	}
	public int getQueryId(){
		return mQueryId;
	}
	
	private int mResponseCode;
	private int mResultType;
	private Exception mException;
	private byte[] mData;
	private int mQueryId;
}
