package com.tencent.avsdk.activity;

import java.util.ArrayList;

import com.tencent.av.sdk.AVConstants;
import com.tencent.avsdk.util.LogcatHelper;
import com.tencent.avsdk.QavsdkApplication;
import com.tencent.avsdk.R;
import com.tencent.avsdk.util.Util;
import com.tencent.avsdk.control.QavsdkControl;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class StartContextActivity extends ListActivity {
	private static final String TAG = "StartContextActivity";
	private static final int DIALOG_LOGIN = 0;
	private static final int DIALOG_LOGOUT = DIALOG_LOGIN + 1;
	private static final int DIALOG_LOGIN_ERROR = DIALOG_LOGOUT + 1;
	private static final int REQUEST_CODE_CREATE_ACTIVITY = 0;
	private static final int REQUEST_CODE_ADD = REQUEST_CODE_CREATE_ACTIVITY + 1;
	private int mPosition;
	private int mLoginErrorCode = AVConstants.AV_ERROR_OK;
	private ArrayAdapter<String> mAdapter = null;
	private ProgressDialog mDialogLogin = null;
	private ProgressDialog mDialogLogout = null;
	private QavsdkControl mQavsdkControl;
	private ArrayList<String> mArrayList = new ArrayList<String>();
	private Context ctx = null;

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.e(TAG, "WL_DEBUG onReceive action = " + action);
			Log.e(TAG, "WL_DEBUG ANR StartContextActivity onReceive action = " + action + " In");				
			if (action.equals(Util.ACTION_START_CONTEXT_COMPLETE)) {			
				mLoginErrorCode = intent.getIntExtra(
						Util.EXTRA_AV_ERROR_RESULT, AVConstants.AV_ERROR_OK);

				if (mLoginErrorCode == AVConstants.AV_ERROR_OK) {
					refreshWaitingDialog();
					startActivityForResult(
							new Intent(StartContextActivity.this,
									CreateRoomActivity.class).putExtra(
									Util.EXTRA_QQ_LIST_INDEX, mPosition),
							REQUEST_CODE_CREATE_ACTIVITY);
				} else {
					showDialog(DIALOG_LOGIN_ERROR);
				}
			} else if (action.equals(Util.ACTION_CLOSE_CONTEXT_COMPLETE)) {
				refreshWaitingDialog();
			}
			Log.e(TAG, "WL_DEBUG ANR StartContextActivity onReceive action = " + action + " Out");					
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ctx = this;
		LogcatHelper.getInstance(this).start();
		setTitle(R.string.login);
		mArrayList.clear();
		mArrayList.addAll(Util.getQQList(this));
		mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mArrayList);
		setListAdapter(mAdapter);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Util.ACTION_START_CONTEXT_COMPLETE);
		intentFilter.addAction(Util.ACTION_CLOSE_CONTEXT_COMPLETE);	
		registerReceiver(mBroadcastReceiver, intentFilter);
		mQavsdkControl = ((QavsdkApplication) getApplication()).getQavsdkControl();
		Log.e(TAG, "=Test=WL_DEBUG StartContextActivity onCreate");
	}

	@Override
	protected void onResume() {
		super.onResume();

		refreshWaitingDialog();
		Log.e(TAG, "=Test=WL_DEBUG StartContextActivity onResume");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.e(TAG, "=Test=WL_DEBUG StartContextActivity onPause");
	}	

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mBroadcastReceiver);
		Log.e(TAG, "=Test=WL_DEBUG StartContextActivity onDestroy");
		LogcatHelper.getInstance(this).stop();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (!mQavsdkControl.hasAVContext()) {
			mLoginErrorCode = mQavsdkControl.startContext(
					Util.getIdentifierList(this).get(position),
					Util.getUserSigList(this).get(position));

			if (mLoginErrorCode != AVConstants.AV_ERROR_OK) {
				showDialog(DIALOG_LOGIN_ERROR);
			}

			mPosition = position;
			refreshWaitingDialog();
		}
		Log.e(TAG, "WL_DEBUG onListItemClick");
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;

		switch (id) {
		case DIALOG_LOGIN:
			dialog = mDialogLogin = Util.newProgressDialog(this,
                R.string.at_login);
			break;

		case DIALOG_LOGOUT:
			dialog = mDialogLogout = Util.newProgressDialog(this,
					R.string.at_logout);
			break;

		case DIALOG_LOGIN_ERROR:
			dialog = Util.newErrorDialog(this, R.string.login_failed);
			break;

		default:
			break;
		}
		return dialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DIALOG_LOGIN_ERROR:
			((AlertDialog) dialog)
					.setMessage(getString(R.string.error_code_prefix)
							+ mLoginErrorCode);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e(TAG, "WL_DEBUG onActivityResult requestCode = " + requestCode);
		Log.e(TAG, "WL_DEBUG onActivityResult resultCode = " + resultCode);
		switch (requestCode) {
		case REQUEST_CODE_CREATE_ACTIVITY:
			mQavsdkControl.closeContext();
			refreshWaitingDialog();
			break;

		case REQUEST_CODE_ADD:
			switch (resultCode) {
			case RESULT_OK:
				mArrayList.clear();
				mArrayList.addAll(Util.getQQList(this));
				mAdapter.notifyDataSetChanged();
				break;
			}
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;

		switch (item.getItemId()) {
		case R.id.action_add:
			startActivityForResult(new Intent(this, AddNewQQActivity.class),
					REQUEST_CODE_ADD);
			break;
		default:
			break;
		}

		return result;
	}

	private void refreshWaitingDialog() {
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {

				Util.switchWaitingDialog(ctx, mDialogLogin, DIALOG_LOGIN,
                        mQavsdkControl.getIsInStartContext());
				Util.switchWaitingDialog(ctx, mDialogLogout, DIALOG_LOGOUT,
						mQavsdkControl.getIsInCloseContext());		
			}
		});

	}
}
