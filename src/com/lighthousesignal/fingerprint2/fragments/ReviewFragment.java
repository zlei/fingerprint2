package com.lighthousesignal.fingerprint2.fragments;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.lighthousesignal.fingerprint2.R;
import com.lighthousesignal.fingerprint2.logs.LogWriter;
import com.lighthousesignal.fingerprint2.utilities.UiFactories;

public class ReviewFragment extends Fragment {

	private Context mContext;
	private ArrayList<String> mFileList;
	private ArrayList<String> mSFileList;
	private ArrayList<String> mEFileList;
	private ArrayList<String> sortOrderList;
	private ArrayList<String> sortTypeList;
	private ArrayList<String> logFilterList;
	private ListView listView_filelist;
	private Spinner spn_sort_by_type;
	private Spinner spn_sort_by_order;
	private Spinner spn_log_filter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContext = getActivity();
		sortOrderList = new ArrayList<String>();
		sortTypeList = new ArrayList<String>();
		mSFileList = new ArrayList<String>();
		mEFileList = new ArrayList<String>();
		logFilterList = new ArrayList<String>();
		View v = inflater.inflate(R.layout.fragment_review, container, false);
		listView_filelist = (ListView) v.findViewById(R.id.listView_filelist);
		spn_sort_by_type = (Spinner) v.findViewById(R.id.spinner_sort_type);
		spn_sort_by_order = (Spinner) v.findViewById(R.id.spinner_sort_order);
		spn_log_filter = (Spinner) v.findViewById(R.id.spinner_log_filter);
		sortOrderList.add("Ascending");
		sortOrderList.add("Descending");
		sortTypeList.add("Name");
		logFilterList.add("All files");
		logFilterList.add("Not sent to server");
		logFilterList.add("Not sent by email");
		setFilelist();
		listView_filelist
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						String filename = listView_filelist.getItemAtPosition(
								position).toString();
						setReviewFileOptions(filename);
						/*
						 * try { UiFactories.standardAlertDialog(mContext,
						 * filename, getLogText(filename), null); } catch
						 * (Exception e) { // TODO Auto-generated catch block
						 * e.printStackTrace(); } getLogMap();
						 */// TODO Auto-generated method stub
					}
				});

		return v;
	}

	private boolean setFilelist() {
		String filePath = LogWriter.APPEND_PATH;
		File file = new File(filePath);
		File[] files = file.listFiles();
		mFileList = new ArrayList<String>();
		for (File mCurrentFile : files) {
			mFileList.add(mCurrentFile.getName());
		}

		// filelist to be shown
		final ArrayList<String> showFileList = new ArrayList<String>();
		showFileList.addAll(mFileList);
		// adapter for sort by type
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(mContext,
				android.R.layout.simple_spinner_item, sortTypeList);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spn_sort_by_type.setAdapter(dataAdapter);

		// adapter for sort by order
		dataAdapter = new ArrayAdapter<String>(mContext,
				android.R.layout.simple_spinner_item, sortOrderList);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spn_sort_by_order.setAdapter(dataAdapter);

		// adapter for log sent status
		dataAdapter = new ArrayAdapter<String>(mContext,
				android.R.layout.simple_spinner_item, logFilterList);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spn_log_filter.setAdapter(dataAdapter);

		// set listener on log filter
		spn_log_filter
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {
						switch (pos) {
						// all files
						case 0:
							break;
						// not sent to server files
						case 1:
							showFileList.removeAll(mSFileList);
							break;
						// not sent by email files
						case 2:
							showFileList.removeAll(mEFileList);
							break;
						}
						ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(
								mContext,
								android.R.layout.simple_expandable_list_item_1,
								showFileList);
						listView_filelist.setAdapter(mAdapter);
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub

					}
				});
		// set listener on sort by order
		spn_sort_by_order
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {
						String sortOrder = spn_sort_by_order.getItemAtPosition(
								pos).toString();
						if (sortOrder.equals("Ascending")) {
							Collections.sort(showFileList);
						} else if (sortOrder.equals("Descending")) {
							Collections.sort(showFileList,
									Collections.reverseOrder());
						}
						ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(
								mContext,
								android.R.layout.simple_expandable_list_item_1,
								showFileList);
						listView_filelist.setAdapter(mAdapter);
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub

					}
				});

		return true;
	}

	private boolean setReviewFileOptions(String logfile) {
		final String filename = logfile;
		AlertDialog.Builder builderSingle = new AlertDialog.Builder(mContext);
		builderSingle.setIcon(R.drawable.ic_launcher);
		builderSingle.setTitle("Select One: ");
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				mContext, android.R.layout.select_dialog_item);
		arrayAdapter.add(mContext.getString(R.string.review_on_text));
		arrayAdapter.add(mContext.getString(R.string.review_on_map));
		arrayAdapter.add(mContext.getString(R.string.send_to_server));
		arrayAdapter.add(mContext.getString(R.string.send_email));
		arrayAdapter.add(mContext.getString(R.string.delete_file));
		builderSingle.setNegativeButton("cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		builderSingle.setAdapter(arrayAdapter,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String strName = arrayAdapter.getItem(which);
						switch (which) {
						// review in text
						case 0:
							try {
								UiFactories.standardAlertDialog(mContext,
										filename, getLogText(filename), null);
							} catch (Exception e) {
								e.printStackTrace();
							}
							break;
						// review on map
						case 1:
							break;
						// send to server
						case 2:
							mSFileList.add(filename);
							Toast.makeText(mContext,
									"Log file sent to server!",
									Toast.LENGTH_SHORT).show();
							break;
						// send by email
						case 3:
							mEFileList.add(filename);
							Toast.makeText(mContext, "Log file sent by email!",
									Toast.LENGTH_SHORT).show();
							break;
						// delete selected file
						case 4:
							break;
						}
					}
				});
		builderSingle.show();
		return true;

	}

	private String getLogText(String filename) throws Exception {
		filename = LogWriter.APPEND_PATH + filename;
		File fl = new File(filename);
		FileInputStream fin = new FileInputStream(fl);
		String ret = UiFactories.convertStreamToString(fin);
		// Make sure you close all streams.
		fin.close();
		return ret;
	}

	private boolean getLogMap() {
		return false;
	}

	public ArrayList<String> getLogfileList() {
		return mFileList;
	}

}
