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
import android.text.InputType;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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
	private Button btn_multiple_submit;
	private CheckBox chk_multiple_selection;
	private Boolean isMultiple = false;
	private ArrayAdapter<String> mAdapter;

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
		btn_multiple_submit = (Button) v
				.findViewById(R.id.button_multiple_submit);
		chk_multiple_selection = (CheckBox) v
				.findViewById(R.id.checkBox_multiple_selection);

		initSpn();
		setFilelist();
		updateLogFilterSpn();
		updateSortOrderSpn();
		updateMultipleSelection();
		return v;
	}

	/**
	 * set file list in list view
	 * 
	 * @return
	 */
	private boolean setFilelist() {
		updateLogfileList();
		updateLogFilter(0);
		updateSortOrder(0);
		if (!isMultiple) {
			listView_filelist
					.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							String filename = listView_filelist
									.getItemAtPosition(position).toString();
							setReviewFileOptions(filename);
							/*
							 * try { UiFactories.standardAlertDialog(mContext,
							 * filename, getLogText(filename), null); } catch
							 * (Exception e) { // TODO Auto-generated catch
							 * block e.printStackTrace(); } getLogMap();
							 */// TODO Auto-generated method stub
						}
					});
		} else {
			listView_filelist.setOnItemClickListener(null);
			btn_multiple_submit.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					SparseBooleanArray checked = listView_filelist
							.getCheckedItemPositions();
					final ArrayList<String> selectedItems = new ArrayList<String>();
					for (int i = 0; i < checked.size(); i++) {
						// Item position in adapter
						int position = checked.keyAt(i);
						if (checked.valueAt(i))
							selectedItems.add(listView_filelist
									.getItemAtPosition(position).toString());
					}
					setMultipleSelectionOptions(selectedItems);
				}
			});
		}
		return true;
	}

	/**
	 * set review file options
	 * 
	 * @param logfile
	 * @return
	 */
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
		arrayAdapter.add(mContext.getString(R.string.rename_file));
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
							showLogInMap(filename);
							break;
						// send to server
						case 2:
							mSFileList.add(filename);
							updateLogFilter(spn_log_filter
									.getSelectedItemPosition());
							Toast.makeText(mContext,
									"Log file sent to server!",
									Toast.LENGTH_SHORT).show();
							break;
						// send by email
						case 3:
							mEFileList.add(filename);
							updateLogFilter(spn_log_filter
									.getSelectedItemPosition());
							Toast.makeText(mContext, "Log file sent by email!",
									Toast.LENGTH_SHORT).show();
							break;
						// rename selected file
						case 4:
							renameSelectedFile(filename);
							break;
						// delete selected file
						case 5:
							deleteSelectedFile(filename);
							break;
						}
					}
				});
		builderSingle.show();
		return true;

	}

	/**
	 * set multiple review file options
	 * 
	 * @param logfile
	 * @return
	 */
	private boolean setMultipleSelectionOptions(ArrayList<String> logfiles) {
		final ArrayList<String> filename = logfiles;
		AlertDialog.Builder builderSingle = new AlertDialog.Builder(mContext);
		builderSingle.setIcon(R.drawable.ic_launcher);
		builderSingle.setTitle("Select One: ");
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				mContext, android.R.layout.select_dialog_item);
		arrayAdapter.add(mContext.getString(R.string.send_to_server));
		arrayAdapter.add(mContext.getString(R.string.send_email));
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
						switch (which) {
						// send to server
						case 0:
							mSFileList.addAll(filename);
							updateLogFilter(spn_log_filter
									.getSelectedItemPosition());
							Toast.makeText(mContext,
									"Log file sent to server!",
									Toast.LENGTH_SHORT).show();
							break;
						// send by email
						case 1:
							mEFileList.addAll(filename);
							updateLogFilter(spn_log_filter
									.getSelectedItemPosition());
							Toast.makeText(mContext, "Log file sent by email!",
									Toast.LENGTH_SHORT).show();
							break;
						}
					}
				});
		builderSingle.show();
		return true;
	}

	/**
	 * get log content from log file
	 * 
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	private String getLogText(String filename) throws Exception {
		filename = LogWriter.APPEND_PATH + filename;
		File fl = new File(filename);
		FileInputStream fin = new FileInputStream(fl);
		String ret = UiFactories.convertStreamToString(fin);
		// Make sure you close all streams.
		fin.close();
		return ret;
	}

	/**
	 * TODO show data on log, need to figure out a way to identify specific map
	 * 
	 * @return
	 */
	private boolean getLogMap() {
		return false;
	}

	/**
	 * read file names from directory
	 * 
	 * @return
	 */
	private boolean updateLogfileList() {
		String filePath = LogWriter.APPEND_PATH;
		File file = new File(filePath);
		File[] files = file.listFiles();
		mFileList = new ArrayList<String>();
		for (File mCurrentFile : files) {
			mFileList.add(mCurrentFile.getName());
		}
		return true;
	}

	/**
	 * log filter spinner
	 * 
	 * @return
	 */
	private boolean updateLogFilterSpn() {
		// set listener on log filter
		spn_log_filter
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {
						updateLogFilter(pos);
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub

					}
				});
		return true;
	}

	private boolean updateLogFilter(int pos) {
		updateLogfileList();
		switch (pos) {
		// all files
		case 0:
			break;
		// not sent to server files
		case 1:
			mFileList.removeAll(mSFileList);
			break;
		// not sent by email files
		case 2:
			mFileList.removeAll(mEFileList);
			break;
		}
		updateSortOrder(spn_sort_by_order.getSelectedItemPosition());
		return true;
	}

	/**
	 * sort by order
	 * 
	 * @return
	 */
	private boolean updateSortOrderSpn() {
		// set listener on sort by order
		spn_sort_by_order
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {
						updateSortOrder(pos);
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub

					}
				});
		return true;
	}

	private boolean updateSortOrder(int pos) {
		String sortOrder = spn_sort_by_order.getItemAtPosition(pos).toString();
		if (sortOrder.equals("Ascending")) {
			Collections.sort(mFileList);
		} else if (sortOrder.equals("Descending")) {
			Collections.sort(mFileList, Collections.reverseOrder());
		}
		if (isMultiple) {
			mAdapter = new ArrayAdapter<String>(mContext,
					R.drawable.custom_multiple_selection,
					mFileList);
			listView_filelist.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		} else {
			mAdapter = new ArrayAdapter<String>(mContext,
					android.R.layout.simple_list_item_1, mFileList);

		}

		listView_filelist.setAdapter(mAdapter);
		return true;
	}

	private boolean updateMultipleSelection() {
		chk_multiple_selection
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							isMultiple = true;
							btn_multiple_submit.setVisibility(View.VISIBLE);
						} else {
							isMultiple = false;
							btn_multiple_submit.setVisibility(View.GONE);
						}
						// updateSortOrder(spn_sort_by_order
						// .getSelectedItemPosition());
						setFilelist();
					}
				});
		return true;
	}

	/**
	 * initiate spinners
	 * 
	 * @return
	 */
	private boolean initSpn() {
		sortOrderList.add("Ascending");
		sortOrderList.add("Descending");
		sortTypeList.add("Name");
		logFilterList.add("All files");
		logFilterList.add("Not sent to server");
		logFilterList.add("Not sent by email");
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

		return true;
	}

	/**
	 * show log in map
	 * 
	 * @param logname
	 * @return
	 */
	private boolean showLogInMap(String logname) {

		String[] mapinfo = logname.split("-");
		String building = mapinfo[1];
		String floor = mapinfo[2];

		return true;

	}

	/**
	 * rename selected log file
	 * 
	 * @param logname
	 * @return
	 */
	private boolean renameSelectedFile(String logname) {
		final String oldFilename = LogWriter.APPEND_PATH + logname;
		final EditText editText = new EditText(mContext);
		editText.setInputType(InputType.TYPE_CLASS_TEXT);
		editText.setText(logname);
		new AlertDialog.Builder(mContext)
				.setTitle("Rename Log File")
				.setView(editText)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String newFilename = LogWriter.APPEND_PATH
								+ editText.getText().toString();
						File oldName = new File(oldFilename);
						File newName = new File(newFilename);
						oldName.renameTo(newName);
						updateLogFilter(spn_log_filter
								.getSelectedItemPosition());
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
							}
						}).show();
		return true;
	}

	/**
	 * delete selected log file
	 * 
	 * @param logname
	 * @return
	 */
	private boolean deleteSelectedFile(String logname) {
		final String filename = LogWriter.APPEND_PATH + logname;

		new AlertDialog.Builder(mContext)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Delete Log File")
				.setMessage("Are you sure you want to delete this log file?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								File file = new File(filename);
								file.delete();
								updateLogFilter(spn_log_filter
										.getSelectedItemPosition());
							}
						}).setNegativeButton("No", null).show();
		return true;
	}

	/**
	 * get file list
	 * 
	 * @return
	 */
	public ArrayList<String> getLogfileList() {
		return mFileList;
	}

}
