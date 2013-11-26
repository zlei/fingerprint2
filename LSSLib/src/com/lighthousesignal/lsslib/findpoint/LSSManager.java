package com.lighthousesignal.lsslib.findpoint;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;

import com.lighthousesignal.lsslib.ApiUtil;

import com.lighthousesignal.lsslib.Fence;
import com.lighthousesignal.lsslib.Floor;
import com.lighthousesignal.lsslib.NetworkListener;
import com.lighthousesignal.lsslib.NetworkResult;
import com.lighthousesignal.lsslib.NetworkTask;
import com.lighthousesignal.lsslib.Offer;
import com.lighthousesignal.lsslib.ServiceManager;
import com.lighthousesignal.lsslib.floorselector.FloorSelectorFragment;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.location.Location;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.SparseArray;

/**
 * LSSManager is used to setup and manage a LSSService and coordinate LSSListeners.
 */
public class LSSManager extends ServiceManager implements NetworkListener {
	
	protected SharedPreferences mPreferences;
	protected LSSService mService;
	protected Intent serviceIntent;
	protected LSSListener mListener;
	protected ApiUtil mApiUtil;
	
	protected Context mContext;
	
	public LSSManager(Context context){
		super(context);
	    mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	    NetworkTask.mPreferences = mPreferences;
	    
	    mContext = context;
	    serviceIntent = new Intent(context, LSSService.class);
	    mApiUtil = new V1Util();
	    mPreferences.edit().remove("login_token").commit();
	}
	
	/**
	 * Set the string to use as the token.
	 * This replaces the login/logout functionality.
	 * This token will be included in network Api tasks to authenticate with the server.
	 * @param token The login token.
	 */
	public void setLoginToken(String token){
		mPreferences.edit().putString("login_token", token).commit();
	}
	
	/**
	 * Set the url of the server.
	 * Format: http://99.99.99.99
	 * 
	 * @param url The url
	 */
	public void setServerURL(String url){
		mPreferences.edit().putString("hostname", url).commit();
	}

	/**
	 * Set the string to use in the customer_id field of Api tasks.
	 * @param customer_id
	 */
	public void setCustomerId(String customer_id){
		mPreferences.edit().putString("customer_id", customer_id).commit();
	}
	
	/**
	 * Set the string to use in the developer_id field of Api tasks.
	 * @param developer_id
	 */
	public void setDeveloperId(String developer_id){
		mPreferences.edit().putString("developer_id", developer_id).commit();
	}
	
	/**
	 * @deprecated Use {@link #setLoginToken(String)}
	 * 
	 * Attempts to login to the server and store a session token.
	 * @param username The username to login with.
	 * @param password The password to login with.
	 * @param hostname The hostname of the server to login to.
	 */
	public void login(String username, String password, String hostname){
		
		mPreferences.edit().putString("hostname", hostname).commit();
		
		Hashtable<String, String> credentials = new Hashtable<String,String>(2);
		credentials.put("login", username);
		credentials.put("password", password);
		NetworkTask loginTask = mApiUtil.loginTask(this, credentials);
		
		//Not multithreaded, must finish before other net tasks anyway
		loginTask.execute();
	}
	
	/**
	 * @deprecated See {@link #setLoginToken(String)}
	 * Attempts to logout of the server.
	 * Deletes session token.
	 */
	public void logout(){		
		Hashtable<String, String> tokenHash = new Hashtable<String,String>(1);
        tokenHash.put("token",  mPreferences.getString("login_token", ""));
		NetworkTask logoutTask = mApiUtil.logoutTask(this, tokenHash);
		logoutTask.execute();
		mPreferences.edit().remove("login_token").commit();
	}
	
	/**
	 * @deprecated See {@link #setLoginToken(String)}
	 * @return True if the service has a valid session token, false otherwise.
	 */
	public boolean loggedIn(){
		String token = mPreferences.getString("login_token", "notoken");
		if(token.equals("") || token.equals("notoken")) return false;
		return true;
	}
	
	/**
	 * Tell the LSSService which LSSListener to post updates to.
	 * @param listener The LSSListener to receive updates.
	 */
	public void setListener(LSSListener listener){
		mListener = listener;
	}
	
	/**
	 * Starts the LSSService with the specified options.
	 * 
	 * @param mode
	 * @param scanDuration
	 * @param scanPeriod
	 */
	public void requestLocationUpdates(int mode, int scanDuration, int scanPeriod){
		
		mPreferences.edit().putInt("scan_mode", mode).commit();
		mPreferences.edit().putInt("scan_duration", scanDuration).commit();
		mPreferences.edit().putInt("scan_period", scanPeriod).commit(); 

		startService(mode);
		
	}
	
	/**
	 * Stop the LSSService and no longer receive updates.
	 */
	public void removeLocationUpdates(){
		stopService();
	}
	
	/*
	 * Called from requestLocationUpdates()
	 */
	protected void startService(int mode) {
		LSSService.currentFloorId = 0;
		if(!LSSService.isRunning()){
			mContext.startService(serviceIntent);
		}
		
		if(mService == null){
			bind();
		}else{
			mService.startScans();
		}
	}
	
	/*
	 * Stops the LSSService
	 */
	protected void stopService(){
		try{
			mContext.unbindService(connection);
			mContext.stopService(serviceIntent);
			mService = null;
		} catch(IllegalArgumentException ex) {
			//Service already gone
		}
	}
	
	public void bind() {
		mContext.bindService(serviceIntent,  connection,  Context.BIND_AUTO_CREATE);
	}
	
	public void unBind(){
		try{
			mContext.unbindService(connection);
		}catch(IllegalArgumentException e){
			//Service already unbound
		}
	}
	
	/**
	 * @return True if the LSSService is running, false otherwise.
	 */
	public boolean serviceRunning(){
		return LSSService.isRunning();
	}
	
	/**
	 * @return The current {@link Floor}, if one exists, otherwise Null if there is no current floor.
	 */
	public Floor getCurrentFloor() {
		int id = LSSService.currentFloorId;
		if(id == -1) return null;
		return mService.getFloors().get(id);
	}
	
	
	/**
	 * @deprecated Use {@link #showFloorSelector}
	 * Produces a dialog to select from available floors.
	 * 
	 * @param context The context to show the dialog from.
	 * @return An alert dialog to select floors.
	 */
	public AlertDialog floorSelectDialog(Context context){
		
		if(!LSSService.isRunning()){
			return new AlertDialog.Builder(context)
			.setMessage("LSS Location Service is not running.")
			.setPositiveButton("Close", new DialogInterface.OnClickListener() { 
				public void onClick(DialogInterface dialog, int which) { 
					dialog.dismiss();
				}
			}).create();
		}else if(mService == null){
			return new AlertDialog.Builder(context)
			.setMessage("LSS Location Service is not bound.")
			.setPositiveButton("Close", new DialogInterface.OnClickListener() { 
				public void onClick(DialogInterface dialog, int which) { 
					dialog.dismiss();
				}
			}).create();
		}else if(mService.getFloors().size() == 0){
			return new AlertDialog.Builder(context)
			.setMessage("No floors loaded yet.")
			.setPositiveButton("Close", new DialogInterface.OnClickListener() { 
				public void onClick(DialogInterface dialog, int which) { 
					dialog.dismiss();
				}
			}).create();
		}else{
		
			final SparseArray<Floor> floors = mService.getFloors();
			String[] data = new String[floors.size()];
			for(int i = 0; i < floors.size(); i++)data[i] = floors.valueAt(i).getName();
			
			return new AlertDialog.Builder(context).setItems(data, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					mService.setFloor(floors.valueAt(which).getId());
				}
			}).setTitle("Select a floor:").create();
		}
	}
	
	public void showFloorSelector(FragmentActivity parent) {
		if(LSSService.isRunning()){
			if(getFloors().size() == 0) {
				new AlertDialog.Builder(parent)
				.setMessage("No floors available.")
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() { 
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create().show();
			}else{
				FragmentManager fm = parent.getSupportFragmentManager();
				FloorSelectorFragment floorSelectDialog = FloorSelectorFragment.newInstance(getFloors(), parent);
				floorSelectDialog.show(fm, "fragment_select_floor");
			}
		}
	}
	
	/**
	 * Tell the service to change to a different floor.<br>
	 * The specified floor should have been obtained from {@link #getFloors()}.
	 * @param floor The new floor object.
	 * @see {@link #floorSelectDialog(Context)} May be an easier solution.
	 */
	public void setFloor(Floor floor) {
		if(mService != null && LSSService.isRunning()){
			mService.setFloor(floor.getId());
		}
	}

	/**
	 * Get all the offers currently available to the user.
	 * 
	 * @return An ArrayList of type Offer, blank if no offers are available.
	 * @see {@link Offer}
	 */
	public ArrayList<Offer> getOffers(){
		SparseArray<Offer> offers = mService.getOffers();
		ArrayList<Offer> ret = new ArrayList<Offer>();
		
		for(int i = 0; i < offers.size(); i++){
			ret.add(offers.valueAt(i));
		}
		
		return ret;
	}
	
	/**
	 * Get all of the floors the LSSService is currently aware of.
	 * 
	 * @return An ArrayList of type Floor
	 * @see {@link Floor}
	 */
	public ArrayList<Floor> getFloors(){
		SparseArray<Floor> floors = mService.getFloors();
		ArrayList<Floor> ret = new ArrayList<Floor>();
		
		for(int i = 0; i < floors.size(); i++){
			ret.add(floors.valueAt(i));
		}
		return ret;
	}
	
	/**
	 * Get all of the fences the LSSService is currently aware of.
	 * 
	 * @return An ArrayList of type Fence
	 * @see {@link Fence}
	 */
	public ArrayList<Fence> getFences() {
		return mService.getFences();
	}
	
	/**
	 * Creates a dialog for sending feedback.
	 * 
	 * @param context The context to show the dialog from.
	 * @return An alert dialog for sending feedback.
	 */
	public AlertDialog sendFeedbackDialog(Context context){
		if(!LSSService.isRunning()){
			return new AlertDialog.Builder(context)
			.setMessage("LSS Location Service is not running.")
			.setPositiveButton("Close", new DialogInterface.OnClickListener() { 
				public void onClick(DialogInterface dialog, int which) { 
					dialog.dismiss();
				}
			}).create();
		}else if(mService == null){
			return new AlertDialog.Builder(context)
			.setMessage("LSS Location Service is not bound.")
			.setPositiveButton("Close", new DialogInterface.OnClickListener() { 
				public void onClick(DialogInterface dialog, int which) { 
					dialog.dismiss();
				}
			}).create();
		}else{
			String[] items = new String[] {"wrong floor", "lows", "moderate", "high"};
			return new AlertDialog.Builder(context).setItems(items, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Hashtable<String, String> hash = new Hashtable<String, String>(3);
					hash.put("vote", "" + which);
					hash.put("id", "" + mService.queryId);
					hash.put("token", mService.getLoginToken());
					
					NetworkTask task = mApiUtil.sendFeedbackTask(mService, hash);
					task.execute();
				}
			}).create();
		}
	}
	
	public boolean isUserOutdoors() {
		if(mService == null) return false;
		
		Location l = LSSService.getLastGpsLocation();
		
		if(l == null){
			//No location to check
			System.out.println("No location");
			return false;
		}
		
		System.out.println("Satellites: " + l.getExtras().getInt("satellites"));
		
		if(System.currentTimeMillis() - l.getTime() > 20000) {
			//Outdated information
			System.out.println("Outdated. " + ( System.currentTimeMillis() - l.getTime()));
			return false;
		}
		
		if(!l.getProvider().equalsIgnoreCase("gps")) {
			//Only gps is reliable
			System.out.println("Not gps.");
			return false; 
		}
		
		if(l.getAccuracy() > 12) {
			//Not good signal
			System.out.println("Bad signal.");
			return false;
		}
		
		if(l.getExtras() != null) {
			if(l.getExtras().getInt("satellites") < 8) {
				System.out.println("Not enougth satellites.");
				return false;
			}
		}
		
		//TODO check sensitivity 
		//return false;
		return true;
	}
	
	public void onTaskSuccess(NetworkResult result) {
		switch(result.getResultType()){
			case LOGIN:
				BufferedReader buff = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(result.getData())));
    	        StringBuffer strBuff = new StringBuffer();
    	        String s;
				try {
					while ((s = buff.readLine()) != null) {
	    	        	strBuff.append(s);
	    	        }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
  	    		mPreferences.edit().putString("login_token", strBuff.toString()).commit();
  	    		
  	    		mListener.onStatusChanged(LSSListener.MSG_LOGIN_SUCCESS);
  	    		break;
			case LOGOUT:
				mListener.onStatusChanged(LSSListener.MSG_LOGOUT_SUCCESS);
				//TODO do anything else?
				break;
		}
	}

	public void onTaskError(NetworkResult result) {
		switch(result.getResultType()){
			case LOGIN:
				mListener.onStatusChanged(LSSListener.MSG_LOGIN_FAIL);
				break;
			case LOGOUT:
				mListener.onStatusChanged(LSSListener.MSG_LOGOUT_FAIL);
				break;
		}
	}
	
	protected ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			mService = ((LSSService.ServiceBinder) binder).getService();
			mService.setListener(mListener);
			mService.startScans();
		}
		public void onServiceDisconnected(ComponentName className) {
			mService = null;
		}
	};
}
