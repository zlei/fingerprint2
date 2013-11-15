package com.lighthouse.fingerprint2.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Radio {
	private final static List<Integer> channelsFrequency = new ArrayList<Integer>(
			Arrays.asList(2412, 2417, 2422, 2427, 2432, 2437, 2442, 2447, 2452,
					2457, 2462, 2467, 2472, 2484));

	public static Integer getFrequencyFromChannel(int channel) {
		return channelsFrequency.get(channel - 1);
	}

	public static int getChannelFromFrequency(int frequency) {
		return channelsFrequency.indexOf(Integer.valueOf(frequency)) + 1;
	}
}