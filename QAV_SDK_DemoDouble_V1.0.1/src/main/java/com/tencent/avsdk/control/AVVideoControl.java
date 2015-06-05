package com.tencent.avsdk.control;

import com.tencent.av.sdk.AVConstants;
import com.tencent.av.sdk.AVVideoCtrl;
import com.tencent.av.sdk.AVVideoCtrl.EnableCameraCompleteCallback;
import com.tencent.av.sdk.AVVideoCtrl.SwitchCameraCompleteCallback;
import com.tencent.avsdk.QavsdkApplication;
import com.tencent.avsdk.util.Util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AVVideoControl {
	private static final String TAG = "AVVideoControl";
	private Context mContext = null;
	private boolean mIsEnableCamera = false;
	private boolean mIsFrontCamera = true;
	private boolean mIsInOnOffCamera = false;
	private boolean mIsInSwitchCamera = false;
	private static final int CAMERA_NONE = 0;
	private static final int FRONT_CAMERA = CAMERA_NONE + 1;
	private static final int BACK_CAMERA = FRONT_CAMERA + 1;

	private EnableCameraCompleteCallback mEnableCameraCompleteCallback = new EnableCameraCompleteCallback() {
		protected void onComplete(boolean enable, int result) {
			super.onComplete(enable, result);
			Log.e(TAG,
					"WL_DEBUG mEnableCameraCompleteCallback.onComplete enable = "
							+ enable);
			Log.e(TAG,
					"WL_DEBUG mEnableCameraCompleteCallback.onComplete result = "
							+ result);
			mIsInOnOffCamera = false;

			if (result == AVConstants.AV_ERROR_OK) {
				mIsEnableCamera = enable;
			}

			mContext.sendBroadcast(new Intent(
					Util.ACTION_ENABLE_CAMERA_COMPLETE).putExtra(
					Util.EXTRA_AV_ERROR_RESULT, result).putExtra(
					Util.EXTRA_IS_ENABLE, enable));
		}
	};

	private SwitchCameraCompleteCallback mSwitchCameraCompleteCallback = new SwitchCameraCompleteCallback() {
		protected void onComplete(int cameraId, int result) {
			super.onComplete(cameraId, result);
			Log.e(TAG,
					"WL_DEBUG mSwitchCameraCompleteCallback.onComplete cameraId = "
							+ cameraId);
			Log.e(TAG,
					"WL_DEBUG mSwitchCameraCompleteCallback.onComplete result = "
							+ result);
			mIsInSwitchCamera = false;
			boolean isFront = cameraId == FRONT_CAMERA;

			if (result == AVConstants.AV_ERROR_OK) {
				mIsFrontCamera = isFront;
			}

			mContext.sendBroadcast(new Intent(
					Util.ACTION_SWITCH_CAMERA_COMPLETE).putExtra(
					Util.EXTRA_AV_ERROR_RESULT, result).putExtra(
					Util.EXTRA_IS_FRONT, isFront));
		}
	};

	public AVVideoControl(Context context) {
		mContext = context;
	}

	int enableCamera(boolean isEnable) {
		int result = AVConstants.AV_ERROR_OK;

		if (mIsEnableCamera != isEnable) {
			QavsdkControl qavsdk = ((QavsdkApplication) mContext)
					.getQavsdkControl();
			AVVideoCtrl avVideoCtrl = qavsdk.getAVContext().getVideoCtrl(
					AVConstants.AV_VIDEO_CHANNEL_MAIN);
			mIsInOnOffCamera = true;
			result = avVideoCtrl.enableCamera(isEnable,
					mEnableCameraCompleteCallback);
		}
		Log.e(TAG, "WL_DEBUG enableCamera isEnable = " + isEnable);
		Log.e(TAG, "WL_DEBUG enableCamera result = " + result);
		return result;
	}

	int switchCamera(boolean isFront) {
		int result = AVConstants.AV_ERROR_OK;

		if (mIsFrontCamera != isFront) {
			QavsdkControl qavsdk = ((QavsdkApplication) mContext)
					.getQavsdkControl();
			AVVideoCtrl avVideoCtrl = qavsdk.getAVContext().getVideoCtrl(
					AVConstants.AV_VIDEO_CHANNEL_MAIN);
			mIsInSwitchCamera = true;
			result = avVideoCtrl.switchCamera(isFront ? FRONT_CAMERA
					: BACK_CAMERA, mSwitchCameraCompleteCallback);
		}
		Log.e(TAG, "WL_DEBUG switchCamera isFront = " + isFront);
		Log.e(TAG, "WL_DEBUG switchCamera result = " + result);
		return result;
	}
	
	void setRotation(int rotation) {
		QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
		AVVideoCtrl avVideoCtrl = qavsdk.getAVContext().getVideoCtrl(AVConstants.AV_VIDEO_CHANNEL_MAIN);
		avVideoCtrl.setRotation(rotation);
		Log.e(TAG, "WL_DEBUG setRotation rotation = " + rotation);

	}

	int toggleEnableCamera() {
		return enableCamera(!mIsEnableCamera);
	}

	int toggleSwitchCamera() {
		return switchCamera(!mIsFrontCamera);
	}

	boolean getIsInOnOffCamera() {
		return mIsInOnOffCamera;
	}

	boolean getIsInSwitchCamera() {
		return mIsInSwitchCamera;
	}

	boolean getIsEnableCamera() {
		return mIsEnableCamera;
	}

	boolean getIsFrontCamera() {
		return mIsFrontCamera;
	}

	void initAVVideo() {
		mIsEnableCamera = false;
		mIsFrontCamera = true;
		mIsInOnOffCamera = false;
		mIsInSwitchCamera = false;
	}
}