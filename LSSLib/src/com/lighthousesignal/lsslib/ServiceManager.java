package com.lighthousesignal.lsslib;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class ServiceManager {
	
	protected ScanService mService;
	protected Intent serviceIntent;
	protected ScanListener mListener;
	
	protected Context mContext;
	
	public ServiceManager(Context context) {
	    mContext = context;
	    serviceIntent = new Intent(context, ScanService.class);
	}
	
	public void setListener(ScanListener listener){
		mListener = listener;
	}
	
	protected void startService(int mode) {
		if(!ScanService.isRunning()){
			mContext.startService(serviceIntent);
		}
		
		if(mService == null){
			bind();
		}else{
			mService.startScans();
		}
	}
	
	protected void stopService(){
		try{
			mContext.unbindService(connection);
			mContext.stopService(serviceIntent);
			mService = null;
		} catch(IllegalArgumentException e) {
			//Service already gone
		}
	}
	
	public void bind() {
		mContext.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
	}
	
	public void unBind(){
		try{
			mContext.unbindService(connection);
		}catch(IllegalArgumentException e){
			//Service already unbound
		}
	}
	
	public boolean serviceRunning(){
		return ScanService.isRunning();
	}
	
	protected ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			mService = ((ScanService.ServiceBinder) binder).getService();
			mService.setListener(mListener);
			mService.startScans();
		}
		public void onServiceDisconnected(ComponentName className) {
			mService = null;
		}
	};
}
