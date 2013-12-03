package com.lighthousesignal.fingerprint2.activities;

import java.util.List;
import java.util.Vector;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.lighthousesignal.fingerprint2.R;
import com.lighthousesignal.fingerprint2.fragments.MapListFragment;
import com.lighthousesignal.fingerprint2.fragments.OptionsFragment;
import com.lighthousesignal.fingerprint2.fragments.ReviewFragment;
import com.viewpagerindicator.TabPageIndicator;

public class MainActivity extends SherlockFragmentActivity {
	private PageAdapter mPageAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(false);
		setupFragments();
	}

	private void setupFragments() {
		List<Fragment> fragments = new Vector<Fragment>();
		fragments.add(Fragment.instantiate(this,
				MapListFragment.class.getName()));
		fragments
				.add(Fragment.instantiate(this, ReviewFragment.class.getName()));
		fragments.add(Fragment.instantiate(this,
				OptionsFragment.class.getName()));
		mPageAdapter = new PageAdapter(getSupportFragmentManager(), fragments);

		ViewPager pager = (ViewPager) super.findViewById(R.id.viewpager);
		pager.setAdapter(mPageAdapter);

		TabPageIndicator tabIndicator = (TabPageIndicator) findViewById(R.id.tabs);
		tabIndicator.setViewPager(pager);
	}

	private class PageAdapter extends FragmentPagerAdapter {

		private List<Fragment> mFragments;

		public PageAdapter(FragmentManager fm, List<Fragment> fragments) {
			super(fm);
			mFragments = fragments;
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return "Maps";
			case 1:
				return "Review";
			case 2:
				return "Options";
			default:
				return "Title";
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.action_settings:
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
			break;
		}

		return true;
	}
}
