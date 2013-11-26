package com.lighthousesignal.lsslib.floorselector;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.lighthousesignal.lsslib.Floor;
import com.lighthousesignal.lsslib.R;
import com.lighthousesignal.lsslib.ScanService;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

@SuppressLint("ValidFragment")
public class FloorSelectorFragment extends DialogFragment {

	public FragmentActivity parent;
	private ScanService mService;

	public static FloorSelectorFragment newInstance(ArrayList<Floor> floors,
			FragmentActivity parent) {

		FloorSelectorFragment f = new FloorSelectorFragment();
		Bundle args = new Bundle();

		ArrayList<String> names = new ArrayList<String>();
		ArrayList<Bitmap> maps = new ArrayList<Bitmap>();
		ArrayList<String> paths = new ArrayList<String>();
		ArrayList<Integer> ids = new ArrayList<Integer>();

		for (Floor floor : floors) {
			names.add(floor.getName());
			maps.add(floor.getMapImage());
			paths.add(floor.getImagePath());
			ids.add(floor.getId());
		}

		args.putParcelableArrayList("maps", maps);
		args.putStringArrayList("names", names);
		args.putStringArrayList("paths", paths);
		args.putIntegerArrayList("ids", ids);

		f.setArguments(args);

		f.parent = parent;

		return f;
	}

	public FloorSelectorFragment() {
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.floor_fragment_layout, container);
		getDialog().setTitle("Swipe to pick a floor");

		ArrayList<String> names = getArguments().getStringArrayList("names");
		ArrayList<Bitmap> maps = getArguments().getParcelableArrayList("maps");
		ArrayList<String> paths = getArguments().getStringArrayList("paths");
		ArrayList<Integer> ids = getArguments().getIntegerArrayList("ids");
		List<Fragment> fragments = new Vector<Fragment>();
		for (int i = 0; i < names.size(); ++i) {
			fragments.add(SelectableFloorFragment.newInstance(maps.get(i),
					names.get(i), paths.get(i), ids.get(i)));
		}

		// ImagePagerAdapter mPagerAdapter = new
		// ImagePagerAdapter(getChildFragmentManager(), fragments);

		final ViewPager pager = (ViewPager) view.findViewById(R.id.view_pager);
		// pager.setAdapter(mPagerAdapter);

		Button confirmButton = (Button) view
				.findViewById(R.id.confirm_floor_button);
		confirmButton.setText("Ok");
		confirmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// -System.out.println(pager.getCurrentItem());
				// TODO
				// mService.selected_floor =
				// mService.getFloors().valueAt(pager.getCurrentItem()).getId();
				FloorSelectorFragment.this.dismiss();
			}
		});
		Button cancelButton = (Button) view.findViewById(R.id.cancel_button);
		cancelButton.setText("Cancel");
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO
				// mService.selected_floor =
				// mService.getFloors().valueAt(0).getId();
				FloorSelectorFragment.this.dismiss();
			}
		});

		return view;
	}
}
