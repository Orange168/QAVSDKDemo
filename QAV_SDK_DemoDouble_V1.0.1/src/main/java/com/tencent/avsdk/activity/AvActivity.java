package com.tencent.avsdk.activity;

import imsdk.ac;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.Toast;

import com.tencent.av.sdk.AVAudioCtrl;
import com.tencent.av.sdk.AVConstants;
import com.tencent.av.utils.PhoneStatusTools;
import com.tencent.avsdk.MyCheckable;
import com.tencent.avsdk.QavsdkApplication;
import com.tencent.avsdk.R;
import com.tencent.avsdk.Util;
import com.tencent.avsdk.control.QavsdkControl;

public class AvActivity extends Activity implements OnClickListener {
	private static final String TAG = "AvActivity";
	private static final int DIALOG_INIT = 0;
	private static final int DIALOG_AT_ON_CAMERA = DIALOG_INIT + 1;
	private static final int DIALOG_ON_CAMERA_FAILED = DIALOG_AT_ON_CAMERA + 1;
	private static final int DIALOG_AT_OFF_CAMERA = DIALOG_ON_CAMERA_FAILED + 1;
	private static final int DIALOG_OFF_CAMERA_FAILED = DIALOG_AT_OFF_CAMERA + 1;
	private static final int DIALOG_AT_SWITCH_FRONT_CAMERA = DIALOG_OFF_CAMERA_FAILED + 1;
	private static final int DIALOG_SWITCH_FRONT_CAMERA_FAILED = DIALOG_AT_SWITCH_FRONT_CAMERA + 1;
	private static final int DIALOG_AT_SWITCH_BACK_CAMERA = DIALOG_SWITCH_FRONT_CAMERA_FAILED + 1;
	private static final int DIALOG_SWITCH_BACK_CAMERA_FAILED = DIALOG_AT_SWITCH_BACK_CAMERA + 1;
	private boolean mIsPaused = false;
	private int mOnOffCameraErrorCode = AVConstants.AV_ERROR_OK;
	private int mSwitchCameraErrorCode = AVConstants.AV_ERROR_OK;
	private ProgressDialog mDialogInit = null;
	private ProgressDialog mDialogAtOnCamera = null;
	private ProgressDialog mDialogAtOffCamera = null;
	private ProgressDialog mDialogAtSwitchFrontCamera = null;
	private ProgressDialog mDialogAtSwitchBackCamera = null;
	private QavsdkControl mQavsdkControl;
	private String mRecvIdentifier = "";
	private String mSelfIdentifier = "";
	private OrientationEventListener mOrientationEventListener = null;
	private Context ctx = null;
	private boolean peerCameraOpend = false;
	private boolean peerMicOpend = false;
	private boolean surfaceCreated = false;

	private MyCheckable mMuteCheckable = new MyCheckable(true) {
		@Override
		protected void onCheckedChanged(boolean checked) {
			Button button = (Button) findViewById(R.id.qav_bottombar_mute);
			AVAudioCtrl avAudioCtrl = mQavsdkControl.getAVContext().getAudioCtrl();

			if (checked) {
				button.setSelected(false);
				button.setText(R.string.gaudio_close_mic_acc_txt);
				avAudioCtrl.enableMic(true);
			} else {
				button.setSelected(true);
				button.setText(R.string.gaudio_open_mic_acc_txt);
				avAudioCtrl.enableMic(false);
			}
		}
	};

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.e(TAG, "WL_DEBUG ANR AvActivity onReceive action = " + action + " In");			
			Log.e(TAG, "WL_DEBUG onReceive action = " + action);
			if (TextUtils.isEmpty(action))
				return;
			
			if (action.equals(Util.ACTION_SURFACE_CREATED)) {
				locateCameraPreview();
				

				if (mQavsdkControl.isVideo()) {
					boolean isEnable = mQavsdkControl.getIsEnableCamera();
					mOnOffCameraErrorCode = mQavsdkControl.toggleEnableCamera();
					refreshCameraUI();
					if (mOnOffCameraErrorCode != AVConstants.AV_ERROR_OK) {
						showDialog(isEnable ? DIALOG_OFF_CAMERA_FAILED : DIALOG_ON_CAMERA_FAILED);
					}			
				}	
							
			} else if (action.equals(Util.ACTION_ENABLE_CAMERA_COMPLETE)) {
				refreshCameraUI();

				mOnOffCameraErrorCode = intent.getIntExtra(Util.EXTRA_AV_ERROR_RESULT, AVConstants.AV_ERROR_OK);
				boolean isEnable = intent.getBooleanExtra(Util.EXTRA_IS_ENABLE, false);
				if (mOnOffCameraErrorCode == AVConstants.AV_ERROR_OK) {
					if (!mIsPaused) {
						mQavsdkControl.setSelfId(mSelfIdentifier);
//						mQavsdkControl.setLocalHasVideo(peerCameraOpend, mRecvOpenid);					
						mQavsdkControl.setRemoteHasVideo(isEnable, mSelfIdentifier);
					}
				} else {
					showDialog(isEnable ? DIALOG_ON_CAMERA_FAILED : DIALOG_OFF_CAMERA_FAILED);
				}
			} else if (action.equals(Util.ACTION_SWITCH_CAMERA_COMPLETE)) {
				refreshCameraUI();

				mSwitchCameraErrorCode = intent.getIntExtra(Util.EXTRA_AV_ERROR_RESULT, AVConstants.AV_ERROR_OK);
				boolean isFront = intent.getBooleanExtra(Util.EXTRA_IS_FRONT, false);
				if (mSwitchCameraErrorCode != AVConstants.AV_ERROR_OK) {
					showDialog(isFront ? DIALOG_SWITCH_FRONT_CAMERA_FAILED : DIALOG_SWITCH_BACK_CAMERA_FAILED);
				}
			} else if (action.equals(Util.ACTION_OUTPUT_MODE_CHANGE)) {
				updateHandfreeButton();
			} else if (action.equals(Util.ACTION_PEER_LEAVE)) {
				Toast.makeText(context, R.string.peer_leave, Toast.LENGTH_LONG).show();
				finish();
			} else if (action.equals(Util.ACTION_PEER_CAMERA_OPEN)) {
				mQavsdkControl.setSelfId(mSelfIdentifier);							
				mQavsdkControl.setLocalHasVideo(true, mRecvIdentifier);				
				if (mQavsdkControl.isVideo()) {							
					if (!peerCameraOpend) {
						Toast.makeText(ctx, getResources().getString(R.string.notify_peer_camera_open), Toast.LENGTH_SHORT).show();		
					}
					peerCameraOpend = true;			
				}
			} else if (action.equals(Util.ACTION_PEER_CAMERA_CLOSE)) {						
				mQavsdkControl.setLocalHasVideo(false, mRecvIdentifier);				
				if (mQavsdkControl.isVideo()) {
					if (peerCameraOpend) {
						Toast.makeText(ctx, getResources().getString(R.string.notify_peer_camera_close), Toast.LENGTH_SHORT).show();		
					}
					peerCameraOpend = false;			
				}
			} else if (action.equals(Util.ACTION_PEER_MIC_OPEN)) {
				if (!peerMicOpend) {
					Toast.makeText(ctx, getResources().getString(R.string.notify_peer_mic_open), Toast.LENGTH_SHORT).show();		
				}
				peerMicOpend = true;
			} else if (action.equals(Util.ACTION_PEER_MIC_CLOSE)) {
				if (peerMicOpend) {
					Toast.makeText(ctx, getResources().getString(R.string.notify_peer_mic_close), Toast.LENGTH_SHORT).show();	
				}
				peerMicOpend = false;
			}
			Log.e(TAG, "WL_DEBUG ANR AvActivity onReceive action = " + action + " Out");					
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.e(TAG, "WL_DEBUG onCreate start");
		super.onCreate(savedInstanceState);
		ctx = this;
		setContentView(R.layout.av_activity);
		findViewById(R.id.qav_bottombar_handfree).setOnClickListener(this);
		findViewById(R.id.qav_bottombar_mute).setOnClickListener(this);
		View cameraButton = findViewById(R.id.qav_bottombar_camera);
		cameraButton.setOnClickListener(this);
		findViewById(R.id.qav_bottombar_hangup).setOnClickListener(this);
		findViewById(R.id.qav_bottombar_switchcamera).setOnClickListener(this);

		// 注册广播
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Util.ACTION_SURFACE_CREATED);
		intentFilter.addAction(Util.ACTION_ENABLE_CAMERA_COMPLETE);
		intentFilter.addAction(Util.ACTION_SWITCH_CAMERA_COMPLETE);
		intentFilter.addAction(Util.ACTION_OUTPUT_MODE_CHANGE);
		intentFilter.addAction(Util.ACTION_PEER_LEAVE);
		intentFilter.addAction(Util.ACTION_PEER_CAMERA_OPEN);
		intentFilter.addAction(Util.ACTION_PEER_CAMERA_CLOSE);	
		intentFilter.addAction(Util.ACTION_PEER_MIC_OPEN);
		intentFilter.addAction(Util.ACTION_PEER_MIC_CLOSE);				
		registerReceiver(mBroadcastReceiver, intentFilter);

		showDialog(DIALOG_INIT);

		mQavsdkControl = ((QavsdkApplication) getApplication()).getQavsdkControl();
		mRecvIdentifier = getIntent().getExtras().getString(Util.EXTRA_IDENTIFIER);
		mSelfIdentifier = getIntent().getExtras().getString(Util.EXTRA_SELF_IDENTIFIER);
		if (mQavsdkControl.getAVContext() != null) {
			mQavsdkControl.onCreate(getApplication(), findViewById(android.R.id.content));
			setTitle(getString(R.string.room_id) + String.valueOf(mQavsdkControl.getRoomId()));
			cameraButton.setVisibility(mQavsdkControl.getIsVideo() ? View.VISIBLE : View.GONE);
			updateHandfreeButton();
		} else {
			finish();
		}
		
		registerOrientationListener();	
		Log.e(TAG, "=Test=WL_DEBUG AvActivity onCreate");
	}

	@Override
	public void onResume() {
		super.onResume();
		mIsPaused = false;
		mQavsdkControl.onResume();
		startOrientationListener();
//		mQavsdkControl.getAVContext().getAudioCtrl().startTRAEService();
		Log.e(TAG, "=Test=WL_DEBUG AvActivity onResume");	
	}

	@Override
	protected void onPause() {
		super.onPause();
		mIsPaused = true;
		mQavsdkControl.onPause();
        stopOrientationListener();	
//		mQavsdkControl.getAVContext().getAudioCtrl().stopTRAEService();       
		Log.e(TAG, "=Test=WL_DEBUG AvActivity onPause");        
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mQavsdkControl.onDestroy();
		// 注销广播
		unregisterReceiver(mBroadcastReceiver);
  		
		Log.e(TAG, "=Test=WL_DEBUG AvActivity onDestroy");
	}

	private void locateCameraPreview() {
		SurfaceView localVideo = (SurfaceView) findViewById(R.id.av_video_surfaceView);
		MarginLayoutParams params = (MarginLayoutParams) localVideo.getLayoutParams();
		params.leftMargin = -3000;
		localVideo.setLayoutParams(params);

		if (mDialogInit != null && mDialogInit.isShowing()) {
			mDialogInit.dismiss();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.qav_bottombar_handfree:
			mQavsdkControl.getAVContext().getAudioCtrl().setAudioOutputMode(mQavsdkControl.getHandfreeChecked() ? AVAudioCtrl.OUTPUT_MODE_SPEAKER : AVAudioCtrl.OUTPUT_MODE_HEADSET);
			break;
		case R.id.qav_bottombar_mute:
			mMuteCheckable.toggle();
			break;
		case R.id.qav_bottombar_camera:
			boolean isEnable = mQavsdkControl.getIsEnableCamera();
			mOnOffCameraErrorCode = mQavsdkControl.toggleEnableCamera();
			refreshCameraUI();
			if (mOnOffCameraErrorCode != AVConstants.AV_ERROR_OK) {
				showDialog(isEnable ? DIALOG_OFF_CAMERA_FAILED : DIALOG_ON_CAMERA_FAILED);
			}
			break;
		case R.id.qav_bottombar_hangup:
			finish();
			break;
		case R.id.qav_bottombar_switchcamera:
			boolean isFront = mQavsdkControl.getIsFrontCamera();
			mSwitchCameraErrorCode = mQavsdkControl.toggleSwitchCamera();
			refreshCameraUI();
			if (mSwitchCameraErrorCode != AVConstants.AV_ERROR_OK) {
				showDialog(isFront ? DIALOG_SWITCH_BACK_CAMERA_FAILED : DIALOG_SWITCH_FRONT_CAMERA_FAILED);
			}
			break;
		default:
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;

		switch (id) {
		case DIALOG_INIT:
			dialog = mDialogInit = Util.newProgressDialog(this, R.string.interface_initialization);
			break;
		case DIALOG_AT_ON_CAMERA:
			dialog = mDialogAtOnCamera = Util.newProgressDialog(this, R.string.at_on_camera);
			break;
		case DIALOG_ON_CAMERA_FAILED:
			dialog = Util.newErrorDialog(this, R.string.on_camera_failed);
			break;
		case DIALOG_AT_OFF_CAMERA:
			dialog = mDialogAtOffCamera = Util.newProgressDialog(this, R.string.at_off_camera);
			break;
		case DIALOG_OFF_CAMERA_FAILED:
			dialog = Util.newErrorDialog(this, R.string.off_camera_failed);
			break;
		case DIALOG_AT_SWITCH_FRONT_CAMERA:
			dialog = mDialogAtSwitchFrontCamera = Util.newProgressDialog(this, R.string.at_switch_front_camera);
			break;
		case DIALOG_SWITCH_FRONT_CAMERA_FAILED:
			dialog = Util.newErrorDialog(this, R.string.switch_front_camera_failed);
			break;
		case DIALOG_AT_SWITCH_BACK_CAMERA:
			dialog = mDialogAtSwitchBackCamera = Util.newProgressDialog(this, R.string.at_switch_back_camera);
			break;
		case DIALOG_SWITCH_BACK_CAMERA_FAILED:
			dialog = Util.newErrorDialog(this, R.string.switch_back_camera_failed);
			break;

		default:
			break;
		}
		return dialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DIALOG_ON_CAMERA_FAILED:
		case DIALOG_OFF_CAMERA_FAILED:
			((AlertDialog) dialog).setMessage(getString(R.string.error_code_prefix) + mOnOffCameraErrorCode);
			break;
		case DIALOG_SWITCH_FRONT_CAMERA_FAILED:
		case DIALOG_SWITCH_BACK_CAMERA_FAILED:
			((AlertDialog) dialog).setMessage(getString(R.string.error_code_prefix) + mSwitchCameraErrorCode);
			break;
		default:
			break;
		}
	}

	private void refreshCameraUI() {
		boolean isEnable = mQavsdkControl.getIsEnableCamera();
		boolean isFront = mQavsdkControl.getIsFrontCamera();
		boolean isInOnOffCamera = mQavsdkControl.getIsInOnOffCamera();
		boolean isInSwitchCamera = mQavsdkControl.getIsInSwitchCamera();
		Button buttonEnableCamera = (Button) findViewById(R.id.qav_bottombar_camera);
		Button buttonSwitchCamera = (Button) findViewById(R.id.qav_bottombar_switchcamera);

		if (isEnable) {
			buttonEnableCamera.setSelected(true);
			buttonEnableCamera.setText(R.string.audio_close_camera_acc_txt);
			buttonSwitchCamera.setVisibility(View.VISIBLE);
		} else {
			buttonEnableCamera.setSelected(false);
			buttonEnableCamera.setText(R.string.audio_open_camera_acc_txt);
			buttonSwitchCamera.setVisibility(View.GONE);
		}

		if (isFront) {
			buttonSwitchCamera.setText(R.string.gaudio_switch_camera_front_acc_txt);
		} else {
			buttonSwitchCamera.setText(R.string.gaudio_switch_camera_back_acc_txt);
		}

		if (isInOnOffCamera) {
			if (isEnable) {
				Util.switchWaitingDialog(this, mDialogAtOffCamera, DIALOG_AT_OFF_CAMERA, true);
				Util.switchWaitingDialog(this, mDialogAtOnCamera, DIALOG_AT_ON_CAMERA, false);
			} else {
				Util.switchWaitingDialog(this, mDialogAtOffCamera, DIALOG_AT_OFF_CAMERA, false);
				Util.switchWaitingDialog(this, mDialogAtOnCamera, DIALOG_AT_ON_CAMERA, true);
			}
		} else {
			Util.switchWaitingDialog(this, mDialogAtOffCamera, DIALOG_AT_OFF_CAMERA, false);
			Util.switchWaitingDialog(this, mDialogAtOnCamera, DIALOG_AT_ON_CAMERA, false);
		}

		if (isInSwitchCamera) {
			if (isFront) {
				Util.switchWaitingDialog(this, mDialogAtSwitchBackCamera, DIALOG_AT_SWITCH_BACK_CAMERA, true);
				Util.switchWaitingDialog(this, mDialogAtSwitchFrontCamera, DIALOG_AT_SWITCH_FRONT_CAMERA, false);
			} else {
				Util.switchWaitingDialog(this, mDialogAtSwitchBackCamera, DIALOG_AT_SWITCH_BACK_CAMERA, false);
				Util.switchWaitingDialog(this, mDialogAtSwitchFrontCamera, DIALOG_AT_SWITCH_FRONT_CAMERA, true);
			}
		} else {
			Util.switchWaitingDialog(this, mDialogAtSwitchBackCamera, DIALOG_AT_SWITCH_BACK_CAMERA, false);
			Util.switchWaitingDialog(this, mDialogAtSwitchFrontCamera, DIALOG_AT_SWITCH_FRONT_CAMERA, false);
		}
	}

	private void updateHandfreeButton() {
		Button button = (Button) findViewById(R.id.qav_bottombar_handfree);

		if (mQavsdkControl.getHandfreeChecked()) {
			button.setSelected(true);
			button.setText(R.string.audio_switch_to_speaker_mode_acc_txt);
		} else {
			button.setSelected(false);
			button.setText(R.string.audio_switch_to_headset_mode_acc_txt);
		}
	}
	
	class VideoOrientationEventListener extends OrientationEventListener {
		
		boolean mbIsTablet = false;	
	
		public VideoOrientationEventListener(Context context, int rate) {
			super(context, rate);
			mbIsTablet = PhoneStatusTools.isTablet(context);
		}

		int mLastOrientation = -25;
		@Override
		public void onOrientationChanged(int orientation) {
			if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
				if (mLastOrientation != orientation) {
					/*
					 * if (mControlUI != null) { mControlUI.setRotation(270); }
					 * if (mVideoLayerUI != null) {
					 * mVideoLayerUI.setRotation(270); }
					 */
				}
				mLastOrientation = orientation;
				return;
			}

			if (mLastOrientation < 0) {
				mLastOrientation = 0;
			}

			if (((orientation - mLastOrientation) < 20) 
					&& ((orientation - mLastOrientation) > -20)) {
				return;
			}
			
			if(mbIsTablet){
				orientation -= 90;
				if (orientation < 0) {
					orientation += 360;
				} 
			}
				
			mLastOrientation = orientation;
			
			  		
            if (orientation > 314 || orientation < 45) {
                if (mQavsdkControl != null) {               	
                	mQavsdkControl.setRotation(0);
 
                }
            } else if (orientation > 44 && orientation < 135) {
                if (mQavsdkControl != null) {              	
                	mQavsdkControl.setRotation(90);               	
                }
            } else if (orientation > 134 && orientation < 225) {
                if (mQavsdkControl != null) {             	
                	mQavsdkControl.setRotation(180);             	
                }
            } else {
                if (mQavsdkControl != null) {                	
                	mQavsdkControl.setRotation(270);              	
                }
            }
		}		
	}
	
	void registerOrientationListener() {
		if (mOrientationEventListener == null) {
			mOrientationEventListener = new VideoOrientationEventListener(super.getApplicationContext(), SensorManager.SENSOR_DELAY_UI);
		}
	}
	
	void startOrientationListener() {
		if (mOrientationEventListener != null) {
			mOrientationEventListener.enable();
		}
	}

	void stopOrientationListener() {
		if (mOrientationEventListener != null) {
			mOrientationEventListener.disable();
		}
	} 	
}