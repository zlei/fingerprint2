package com.lighthouse.fingerprint2.networks;

public interface INetworkTaskStatusListener {
	void nTaskSucces(NetworkResult result);

	void nTaskErr(NetworkResult result);
}
