package com.lighthousesignal.fingerprint2.fragments;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import com.lighthousesignal.fingerprint2.network.HttpLogSender;
import com.lighthousesignal.fingerprint2.utilities.DataPersistence;
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
	private HashMap<String, List<String>> statusList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContext = getActivity();
		sortOrderList = new ArrayList<String>();
		sortTypeList = new ArrayList<String>();
		mSFileList = new ArrayList<String>();
		mEFileList = new ArrayList<String>();
		logFilterList = new ArrayList<String>();
		statusList = new HashMap<String, List<String>>();
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
		// load all files sent status into hash table
		getStatusFile();
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
		// get current spinner status
		updateLogFilter(spn_log_filter.getSelectedItemPosition());
		updateSortOrder(spn_sort_by_order.getSelectedItemPosition());
		if (!isMultiple) {
			listView_filelist
					.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							String filename = listView_filelist
									.getItemAtPosition(position).toString();
							// for now, just open log file, no dev file
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
		final String loadFilename = filename + ".log";
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
						// get the origin status, do not change the other
						// status(send to server and send by email)
						String origin;

						switch (which) {
						// review in text
						case 0:
							try {
								UiFactories.standardAlertDialog(mContext,
										loadFilename, getLogText(loadFilename),
										null);
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
							if (!mSFileList.contains(filename)) {
								mSFileList.add(filename);
								// do not change the status of email
								origin = statusList.get(filename).get(1);
								updateTable(filename, "Y", origin);
								sendToServer(filename);
								updateStatusFile();
								updateLogFilter(spn_log_filter
										.getSelectedItemPosition());
							}
							break;
						// send by email
						case 3:
							if (!mEFileList.contains(filename)) {
								mEFileList.add(filename);
								origin = statusList.get(filename).get(0);
								updateTable(filename, origin, "Y");
								sendByEmail();
								updateStatusFile();
								updateLogFilter(spn_log_filter
										.getSelectedItemPosition());
							}
							break;
						// rename selected file
						case 4:
							renameSelectedFile(loadFilename);
							break;
						// delete selected file
						case 5:
							deleteSelectedFile(loadFilename);
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
						// get the origin status, do not change the other
						// status(send to server and send by email)
						String origin;

						switch (which) {
						// send to server
						case 0:
							for (int i = 0; i < filename.size(); i++) {
								if (!mSFileList.contains(filename.get(i))) {
									mSFileList.add(filename.get(i));
									origin = statusList.get(filename.get(i))
											.get(1);
									updateTable(filename.get(i), "Y", origin);
									sendToServer(filename.get(i));
								}
							}
							updateStatusFile();
							updateLogFilter(spn_log_filter
									.getSelectedItemPosition());
							break;
						// send by email
						case 1:
							for (int i = 0; i < filename.size(); i++) {
								if (!mEFileList.contains(filename.get(i))) {
									origin = statusList.get(filename.get(i))
											.get(0);
									updateTable(filename.get(i), origin, "Y");
									mEFileList.add(filename.get(i));
								}
							}
							if (sendByEmail()) {
								updateStatusFile();
								updateLogFilter(spn_log_filter
										.getSelectedItemPosition());
							} else
								Toast.makeText(mContext, "Sent Failed!",
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
	 * read file names from directory, update sendToServer and sendByEmail lists
	 * 
	 * @return
	 */
	private boolean updateLogfileList() {
		String filePath = LogWriter.APPEND_PATH;
		String filename = "";
		File file = new File(filePath);
		File[] files = file.listFiles();
		mFileList = new ArrayList<String>();
		for (File mCurrentFile : files) {
			filename = mCurrentFile.getName();
			if (filename.contains(".log")) {
				String[] name = filename.split("\\.");
				String filekey = name[0];
				// do not include wifi.log and error.log
				if (!filekey.equals("error") && !filekey.equals("wifi"))
					// only input name of files
					mFileList.add(filekey);
				// deal with server list
				if (statusList.containsKey(filekey)) {
					if (statusList.get(filekey).get(0).equals("Y")
							&& !mSFileList.contains(filekey)) {
						mSFileList.add(filekey);
					}
					// deal with email list
					if (statusList.get(filekey).get(1).equals("Y")
							&& !mEFileList.contains(filekey)) {
						mEFileList.add(filekey);
					}
				}
				// for new logs, donot have data in the status file, update here
				else {
					updateTable(filekey, "N", "N");
				}
			}
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
					R.drawable.custom_multiple_selection, mFileList);
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
		sortOrderList.add("Descending");
		sortOrderList.add("Ascending");
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
	 * send selected file to server TODO save send logs status permanently
	 * 
	 * @return
	 */
	private boolean sendToServer(String filename) {
		String token = DataPersistence.getToken(mContext);
		// TODO get response from server before return true
		ArrayList<String> toSend = new ArrayList<String>();
		for (String file : mSFileList) {
			if (statusList.get(file).get(0).equals("N"))
				toSend.add(file);
		}
		new HttpLogSender(mContext, DataPersistence.getServerName(mContext)
				+ getString(R.string.submit_log_url), toSend).setToken(token)
				.execute();
		return true;
	}

	/**
	 * Email
	 **/
	private boolean sendByEmail() {
		// convert from paths to Android friendly
		// Parcelable Uri's
		ArrayList<Uri> uris = new ArrayList<Uri>();
		for (int i = 0; i < mEFileList.size(); i++) {
			/** wifi **/
			File fileIn = new File(LogWriter.APPEND_PATH + mEFileList.get(i)
					+ ".log");
			Uri u = Uri.fromFile(fileIn);
			uris.add(u);

			/** sensors **/
			File fileInSensors = new File(LogWriter.APPEND_PATH
					+ mEFileList.get(i) + ".dev");
			Uri uSens = Uri.fromFile(fileInSensors);
			uris.add(uSens);
		}

		/**
		 * Run sending email activity
		 */
		Intent emailIntent = new Intent(
				android.content.Intent.ACTION_SEND_MULTIPLE);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				"Wifi Searcher Scan Log");
		emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		startActivity(Intent.createChooser(emailIntent, "Send mail..."));

		return true;
	}

	/**
	 * rename selected log file
	 * 
	 * @param logname
	 * @return
	 */
	private boolean renameSelectedFile(String logname) {
		String[] filename = logname.split("\\.");
		logname = filename[0];
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
						File oldLogName = new File(oldFilename + ".log");
						File newLogName = new File(newFilename + ".log");
						File oldDevName = new File(oldFilename + ".dev");
						File newDevName = new File(newFilename + ".dev");
						oldLogName.renameTo(newLogName);
						oldDevName.renameTo(newDevName);
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
								String devFilename = filename.replace(".log",
										".dev");
								File devFile = new File(devFilename);
								file.delete();
								devFile.delete();
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

	/**
	 * get status file
	 */
	public boolean getStatusFile() {
		try {
			File f = new File(LogWriter.APPEND_PATH + "status");
			FileInputStream is = new FileInputStream(f);
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			String s = new String(buffer);
			/**
			 * deal with the logs, split strings to hashmap and arraylist
			 */
			s = s.replaceAll("[\\[\\{\\}\\s]", "");
			String[] pairs = s.split("\\],");
			pairs[pairs.length - 1] = pairs[pairs.length - 1].replaceAll("\\]",
					"");
			for (int i = 0; i < pairs.length; i++) {
				String[] key = pairs[i].split("=");
				String[] value = key[1].split(",");
				updateTable(key[0], value[0], value[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * write to json file for file status
	 * 
	 * @param params
	 * @param mJsonResponse
	 */
	public boolean updateStatusFile() {
		try {
			FileWriter file = new FileWriter(LogWriter.APPEND_PATH + "status");
			file.write(statusList.toString());
			file.flush();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * save file status to json
	 */
	public Boolean updateTable(String filename, String server, String email) {
		/**
		 * status format: sentToServer, sentByEmail
		 */
		ArrayList<String> status = new ArrayList<String>();
		status.add(server);
		status.add(email);
		statusList.put(filename, status);
		return true;
	}
}
