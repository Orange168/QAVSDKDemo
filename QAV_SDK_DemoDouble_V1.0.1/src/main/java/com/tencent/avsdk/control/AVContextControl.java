package com.tencent.avsdk.control;

import com.tencent.TIMCallBack;
import com.tencent.TIMManager;
import com.tencent.TIMUser;
import com.tencent.av.sdk.AVConstants;
import com.tencent.av.sdk.AVContext;
import com.tencent.av.sdk.AVRoom;
import com.tencent.avsdk.QavsdkApplication;
import com.tencent.avsdk.util.Util;
import com.tencent.openqq.IMSdkInt;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

class AVContextControl {
	private static final String TAG = "AvContextControl";
	private static final String APP_ID_TEXT = "1400000353";
	private static final String UID_TYPE = "361";
	private boolean mIsInStartContext = false;
	private boolean mIsInCloseContext = false;
	private Context mContext;
	private AVContext mAVContext = null;
	private String mSelfIdentifier = "";
	private String mPeerIdentifier = "";
	private AVContext.Config mConfig = null;
	/**
	 * 启动SDK系统的回调函数
	 */
	private AVContext.StartContextCompleteCallback mStartContextCompleteCallback = new AVContext.StartContextCompleteCallback() {
		public void OnComplete(int result) {
			mIsInStartContext = false;
			if (result == AVConstants.AV_ERROR_OK) {
				QavsdkControl qavsdkControl = ((QavsdkApplication) mContext).getQavsdkControl();
				qavsdkControl.initAVInvitation();			
			}
			
			Log.d(TAG,
					"WL_DEBUG mStartContextCompleteCallback.OnComplete result = "
							+ result);
			mContext.sendBroadcast(new Intent(
					Util.ACTION_START_CONTEXT_COMPLETE).putExtra(
					Util.EXTRA_AV_ERROR_RESULT, result));
			
			if (result != AVConstants.AV_ERROR_OK) {
				mAVContext = null;
				Log.d(TAG, "WL_DEBUG mStartContextCompleteCallback mAVContext is null");
			}
		}
	};

	/**
	 * 关闭SDK系统的回调函数
	 */
	private AVContext.CloseContextCompleteCallback mCloseContextCompleteCallback = new AVContext.CloseContextCompleteCallback() {
		public void OnComplete() {
			mIsInCloseContext = false;

			logout();
		}
	};

	AVContextControl(Context context) {
		mContext = context;
	}
	
	/**
	 * 启动SDK系统
	 * 
	 * @param identifier
	 *            用户身份的唯一标识
	 * @param usersig
	 *            用户身份的校验信息
	 */


	int startContext(String identifier, String usersig) {
		int result = AVConstants.AV_ERROR_OK;

		if (!hasAVContext()) {			
			Log.d(TAG, "WL_DEBUG startContext identifier = " + identifier);
			Log.d(TAG, "WL_DEBUG startContext usersig = " + usersig);
			mConfig = new AVContext.Config(UID_TYPE,
					APP_ID_TEXT, identifier, usersig, APP_ID_TEXT, "");
			login();
		}
		
		return result;
	}

	/**
	 * 关闭SDK系统
	 */
	void closeContext() {
		if (hasAVContext()) {
			Log.d(TAG, "WL_DEBUG closeContext");
			QavsdkControl qavsdkControl = ((QavsdkApplication) mContext)
				.getQavsdkControl();
			qavsdkControl.uninitAVInvitation();
			mAVContext.closeContext(mCloseContextCompleteCallback);
			
			mIsInCloseContext = true;
		}
	}
	
	boolean getIsInStartContext() {
		return mIsInStartContext;
	}

	boolean getIsInCloseContext() {
		return mIsInCloseContext;
	}
	
	boolean hasAVContext() {
		return mAVContext != null;
	}
	
	AVContext getAVContext() {
		return mAVContext;
	}
	
	String getSelfIdentifier() {
		return mSelfIdentifier;
	}
	
	String getPeerIdentifier() {
		return mPeerIdentifier;
	}

	void setPeerIdentifier(String peerIdentifier) {
		mPeerIdentifier = peerIdentifier;
	}
	
	private void login()
	{
		//login
//		TIMManager.getInstance().setEnv(1);

		//请确保TIMManager.getInstance().init()一定执行在主线程
		TIMManager.getInstance().init(mContext);	
		
		//for qq 
		//IMSdkInt.get().setSdkType("openqq");
		
		
//		IMAuthListener authlistener = new IMAuthListener() {
//			@Override
//			public void onError(IMError code, String desc){
//				Log.e(TAG, "init failed.");
//				onLogin(false, 0);
//				
//			}
//
//			@Override
//			public void onSucc() {
//				Log.i(TAG, "init successfully. tiny id = " + IMSdkInt.get().getTinyId());
//				onLogin(true, IMSdkInt.get().getTinyId());					
//			}
//		};
		
		TIMUser userId = new TIMUser();
//		userId.setUidType(mConfig.uidType);
//		userId.setUserAppId(Integer.parseInt(mConfig.openAppId));
//		userId.setUserId(mConfig.openId);
		userId.setAccountType(UID_TYPE);   //以前的uidtype
		userId.setAppIdAt3rd(mConfig.app_id_at3rd);     //以前的openAppId
		userId.setIdentifier(mConfig.identifier);     //以前的OpenId	

//		TIMManager.getInstance().login(Integer.parseInt(mConfig.sdkAppId), mConfig.sdkAppIdToken, 
//				userId, mConfig.accessToken, IMStatus.ONLINE, authlistener);		
	    TIMManager.getInstance().login(
	    		Integer.parseInt(mConfig.sdkAppId),
	            userId,
	            mConfig.user_sig,           //以前的accessToken
	            new TIMCallBack() {//回调接口，以前的listener

	                @Override
	                public void onSuccess() {
	    				Log.i(TAG, "init successfully. tiny id = " + IMSdkInt.get().getTinyId());
	    				onLogin(true, IMSdkInt.get().getTinyId());	
	                }

	                @Override
	                public void onError(int code, String desc) {
	    				Log.e(TAG, "init failed.");
	    				onLogin(false, 0);
	                }
	            });
	}
	
	private void onLogin(boolean result, long tinyId)
	{
		if(result)
		{
			mAVContext = AVContext.createContext(mConfig);
			Log.d(TAG, "WL_DEBUG startContext mAVContext is null? " + (mAVContext == null));
			mSelfIdentifier = mConfig.identifier;

			int ret = mAVContext.startContext(mContext, AVRoom.AV_ROOM_PAIR, mStartContextCompleteCallback);

			mIsInStartContext = true;
		}
		else
		{
			mStartContextCompleteCallback.OnComplete(AVConstants.AV_ERROR_INITSDKFAIL);
		}
	}
	
	private void logout()
	{
//		IMAuthListener authlistener = new IMAuthListener() {
//			@Override
//			public void onError(IMError code, String desc){
//				Log.e(TAG, "uninit failed.");
//				onLogout(false);				
//			}
//
//			@Override
//			public void onSucc() {
//				Log.i(TAG, "uninit successfully.");
		
//				onLogout(true);				
//			}
//		};
//		IMSdkInt.get().logout(authlistener);
		
		TIMManager.getInstance().logout();	
		onLogout(true);
	}
	;
	private void onLogout(boolean result)
	{
		Log.d(TAG, "WL_DEBUG mCloseContextCompleteCallback.OnComplete");
		mAVContext.onDestroy();
		mAVContext = null;
		Log.d(TAG, "WL_DEBUG mCloseContextCompleteCallback mAVContext is null");
		mIsInCloseContext = false;
		mContext.sendBroadcast(new Intent(Util.ACTION_CLOSE_CONTEXT_COMPLETE));
	}
}