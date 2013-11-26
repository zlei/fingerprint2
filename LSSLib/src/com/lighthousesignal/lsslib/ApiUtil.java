package com.lighthousesignal.lsslib;

import java.util.Hashtable;
import java.util.Vector;

import com.lighthousesignal.lsslib.pathfinding.MapNode;
import com.lighthousesignal.lsslib.pathfinding.Path;

import android.content.Context;


/*
 *	This interface is used to allow the library to support multiple server apis.
 */
public interface ApiUtil {
	
	/**
	 * A network task to login to the server.
	 * 
	 * @param listener The NetworkListener to receive the loginToken or error
	 * @param credentials A hashtable containing login, password strings
	 */
	public NetworkTask loginTask(NetworkListener listener, Hashtable<String,String> credentials);
	
	/**
	 * A network task to logout of the server.
	 * 
	 * @param listener The NetworkListener to receive confirmation of logout
	 * @param credentials A hashtable containing login, logout, and token strings
	 */
	public NetworkTask logoutTask(NetworkListener listener, Hashtable<String,String> credentials);
	
	public NetworkTask findPointTask(NetworkListener listener, String token, Vector<WifiScanResult> res, int count, int time, int floorId, Context context);
	
	public void processFindPointResponse(com.lighthousesignal.lsslib.findpoint.LSSService service, byte[] response, int queryId);
	
	public NetworkTask sendFeedbackTask(NetworkListener listener, Hashtable<String,String> feedback );
	
	public NetworkTask downloadMapTask(NetworkListener listener, String token, String path, int floorId);
	
	public String makePost(Hashtable<String, String> params, boolean isGet, boolean isPostUrlEncoded);	

	public NetworkTask findPathTask(NetworkListener listener, int start_node_id, int end_node_id);

	public Path processFindPathResponse(byte[] data);

	public NetworkTask findNearestNodeTask(NetworkListener listener, int floorId, double pointX, double pointY, int queryId);

	public MapNode processFindNearestNodeResponse(byte[] data);
}
