package com.lighthousesignal.lsslib.floorselector;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ImagePagerAdapter extends FragmentPagerAdapter {

	private List<Fragment> fragments;
	
	public ImagePagerAdapter(FragmentManager fm, List<Fragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}

	@Override
	public Fragment getItem(int arg0) {
		return fragments.get(arg0);
	}

	@Override
	public int getCount() {
		return fragments.size();
	}
	
	@Override
	public CharSequence getPageTitle (int position) {
		return ((SelectableFloorFragment)fragments.get(position)).getName();
	}
}
