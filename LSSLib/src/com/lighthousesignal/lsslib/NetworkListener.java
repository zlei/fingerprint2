package com.lighthousesignal.lsslib;

public interface NetworkListener {

    static int LOGIN = 1;
    static int LOGOUT = 2;
	static int FIND_POINT = 3;
	static int GET_IMAGE = 4;
	static int CORRECT_LOCATION = 5;
	static int SEND_FEEDBACK = 6;
	static int GET_BUILDINGS = 7;
	static int GET_FLOORS = 8;
	static int FIND_PATH = 9;
	static int NEAREST_NODE = 10;
    
	public void onTaskSuccess(NetworkResult result);
	public void onTaskError(NetworkResult result);
}
