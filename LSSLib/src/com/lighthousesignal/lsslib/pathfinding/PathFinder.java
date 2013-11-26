package com.lighthousesignal.lsslib.pathfinding;

import android.util.Log;

import com.lighthousesignal.lsslib.NetworkResult;
import com.lighthousesignal.lsslib.NetworkListener;
import com.lighthousesignal.lsslib.findpoint.V1Util;

public class PathFinder implements NetworkListener {

	private static final int START_NODE = 0;
	private static final int END_NODE = 1;

	private PathListener listener;
	private MapNode startNode;
	private MapNode endNode;
	private V1Util util;

	public PathFinder(PathListener listener) {
		this.listener = listener;
		util = new V1Util();
	}

	public void findPath(double startX, double startY, double endX, double endY) {
		Log.d("LSS FindPath", "finding path...");
		findNearestNode(startX, startY, START_NODE);
		findNearestNode(endX, endY, END_NODE);
	}

	private void findPath(int startNodeId, int endNodeId) {
		util.findPathTask(this, startNodeId, endNodeId).execute();
	}

	private void findNearestNode(double x, double y, int type) {
		int floorId = 0;//TODO LSSService.currentFloorId;
		util.findNearestNodeTask(this, floorId, x, y, type).execute();
	}

	public void onTaskSuccess(NetworkResult result) {
		switch(result.getResultType()){
			case FIND_PATH:
				Log.d("LSS PathFinder", "FIND_PATH Network Success");
				Path path = util.processFindPathResponse(result.getData());
				Log.d("LSS PathFinder", "Path of length " + path.getLength());
				listener.onPathFound(path);
				break;
			case NEAREST_NODE:
				Log.d("LSS PathFinder", "NEAREST_NODE Network Success");
				if(result.getQueryId() == START_NODE) {
					startNode = util.processFindNearestNodeResponse(result.getData());
					Log.d("LSS PathFinder", "Got start node, id " + startNode.id);
				} else if(result.getQueryId() == END_NODE) {
					endNode = util.processFindNearestNodeResponse(result.getData());
					Log.d("LSS PathFinder", "Got end node, id " + endNode.id);
				}

				if(startNode != null && endNode != null) {
					findPath(startNode.id, endNode.id);
					startNode = null;
					endNode = null;
				}
				break;
		}
	}

	public void onTaskError(NetworkResult result) {
		switch(result.getResultType()){
			case FIND_PATH:
				Log.d("LSS PathFinder", "FIND_PATH Network Error");
				break;
			case NEAREST_NODE:
				Log.d("LSS PathFinder", "NEAREST_NODE Network Error");
				break;
		}
	}

}
