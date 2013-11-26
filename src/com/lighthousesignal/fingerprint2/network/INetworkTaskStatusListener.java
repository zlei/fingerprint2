package com.lighthousesignal.fingerprint2.network;

public interface INetworkTaskStatusListener {
	void nTaskSucces(NetworkResult result);
	void nTaskErr(NetworkResult result);
}
