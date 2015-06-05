package com.tencent.avsdk.control;

import com.tencent.av.sdk.AVConstants;
import com.tencent.av.sdk.AVContext;
import com.tencent.av.sdk.AVRoom;
import com.tencent.av.sdk.AVRoomPair;
import com.tencent.avsdk.QavsdkApplication;
import com.tencent.avsdk.util.Util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

class AVRoomControl {
	private static final String TAG = "AVRoomControl";
	private boolean mIsInCreateRoom = false;
	private boolean mIsInCloseRoom = false;
	private boolean mIsInJoinRoom = false;
	private boolean mIsVideo = false;
	private long mRoomId = 0;
	private Context mContext = null;

	private AVRoomPair.Delegate mRoomDelegate = new AVRoomPair.Delegate() {
		// 创建房间成功回调
		protected void OnRoomCreateComplete(int result) {
			Log.e(TAG, "WL_DEBUG mRoomDelegate.OnRoomCreateComplete result = "
					+ result);
			mIsInCreateRoom = false;
			QavsdkControl qavsdkControl = ((QavsdkApplication) mContext)
					.getQavsdkControl();
			AVRoomPair roomPair = (AVRoomPair) qavsdkControl.getRoom();
			if (roomPair != null && result == AVConstants.AV_ERROR_OK) {
				mRoomId = roomPair.getRoomId();
				qavsdkControl.inviteIntenal();
				Log.d(TAG, "OnRoomCreateComplete. roomId = " + mRoomId);
			} else {
				mRoomId = 0;
				Log.e(TAG, "OnRoomCreateComplete. mRoomPair == null");
			}
			mContext.sendBroadcast(new Intent(Util.ACTION_ROOM_CREATE_COMPLETE)
					.putExtra(Util.EXTRA_ROOM_ID, mRoomId).putExtra(
							Util.EXTRA_AV_ERROR_RESULT, result));
		}

		// 加入房间成功回调
		protected void OnRoomJoinComplete(int result) {
			mIsInJoinRoom = false;
			Log.d(TAG, "OnRoomJoinComplete. result = " + result);
			mContext.sendBroadcast(new Intent(Util.ACTION_ROOM_JOIN_COMPLETE)
					.putExtra(Util.EXTRA_AV_ERROR_RESULT, result));
		}

		// 离开房间成功回调
		protected void OnRoomLeaveComplete(int result) {
			Log.d(TAG, "OnRoomLeaveComplete. result = " + result);
		}

		protected void OnRoomPeerEnter() {
			Log.d(TAG, "OnRoomPeerEnter.");
			//nothing to do
		}

		protected void OnRoomPeerLeave() {
			Log.d(TAG, "OnRoomPeerLeave.");
			mContext.sendBroadcast(new Intent(Util.ACTION_PEER_LEAVE));
		}
		
		protected void OnCameraStart() 
		{
			Log.d(TAG, "OnCameraStart"  );	
			mContext.sendBroadcast(new Intent(Util.ACTION_PEER_CAMERA_OPEN));			
		}
		protected void OnCameraClose() 
		{
			Log.d(TAG, "OnCameraClose"  );	
			mContext.sendBroadcast(new Intent(Util.ACTION_PEER_CAMERA_CLOSE));	
		}		
		protected void OnMicStart()
		{
			mContext.sendBroadcast(new Intent(Util.ACTION_PEER_MIC_OPEN));				
			Log.d(TAG, "OnMicStart"  );	
		}
		protected void OnMicClose()
		{
			mContext.sendBroadcast(new Intent(Util.ACTION_PEER_MIC_CLOSE));				
			Log.d(TAG, "OnMicClose"  );	
//			mContext.sendBroadcast(new Intent(Util.ACTION_PEER_LEAVE));
		}		
	
	};

	/**
	 * 关闭房间的回调函数
	 */
	private AVContext.CloseRoomCompleteCallback mCloseRoomCompleteCallback = new AVContext.CloseRoomCompleteCallback() {
		protected void OnComplete() {
			Log.e(TAG, "WL_DEBUG mCloseRoomCompleteCallback.OnComplete");
			mIsInCloseRoom = false;
			mContext.sendBroadcast(new Intent(Util.ACTION_CLOSE_ROOM_COMPLETE));
		}
	};

	AVRoomControl(Context context) {
		mContext = context;
	}

	/**
	 * 创建房间
	 * 
	 * @param isVideo
	 *            是否有视频
	 */
	void createRoom(boolean isVideo) {	
		QavsdkControl qavsdkControl = ((QavsdkApplication) mContext).getQavsdkControl();
		if ((qavsdkControl != null) && (qavsdkControl.getAVContext() != null)) {				
			String peerIdentifier = qavsdkControl.getPeerIdentifier();
			Log.e(TAG, "WL_DEBUG createRoom peerIdentifier = " + peerIdentifier);
			Log.e(TAG, "WL_DEBUG createRoom isVideo = " + isVideo);
			AVRoom.Info roomInfo = new AVRoom.Info(AVRoom.AV_ROOM_PAIR, 0, 0,
					isVideo ? AVRoom.AV_MODE_VIDEO : AVRoom.AV_MODE_AUDIO,
							peerIdentifier, null, 0);
			// create room
			qavsdkControl.getAVContext().createRoom(mRoomDelegate, roomInfo);
			mIsInCreateRoom = true;
			mIsVideo = isVideo;		
		} else {
			Log.e(TAG, "WL_DEBUG createRoom qavsdkControl = " + (qavsdkControl==null));			
			mIsInCreateRoom = false;
			mIsVideo = false;				
		}
	}

	void joinRoom(long roomId, String peerIdentifier, boolean isVideo) {
		QavsdkControl qavsdkControl = ((QavsdkApplication) mContext).getQavsdkControl();
		if ((qavsdkControl != null) && (qavsdkControl.getAVContext() != null)) {
			qavsdkControl.setPeerIdentifier(peerIdentifier);
			Log.e(TAG, "WL_DEBUG joinRoom peerIdentifier = " + peerIdentifier);
			Log.e(TAG, "WL_DEBUG joinRoom roomId = " + roomId);
			AVRoom.Info roomInfo = new AVRoom.Info(AVRoom.AV_ROOM_PAIR, roomId, 0,
					isVideo ? AVRoom.AV_MODE_VIDEO : AVRoom.AV_MODE_AUDIO, peerIdentifier, null, 0);
			// create room
			qavsdkControl.getAVContext().joinRoom(mRoomDelegate, roomInfo);
			mIsInJoinRoom = true;
			mIsVideo = isVideo;	
		} else {
			Log.e(TAG, "WL_DEBUG joinRoom qavsdkControl = " + (qavsdkControl==null));				
			mIsInJoinRoom = false;
			mIsVideo = false;			
		}

	}

	/** 关闭房间 */
	int closeRoom() {
		Log.e(TAG, "WL_DEBUG closeRoom");
		QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
		if ((qavsdk != null) && (qavsdk.getAVContext() != null)) {
			AVContext avContext = qavsdk.getAVContext();
			int result = avContext.closeRoom(mCloseRoomCompleteCallback);
			mIsInCloseRoom = true;	
			return result;	
		} else {
			Log.e(TAG, "WL_DEBUG joinRoom qavsdkControl = " + (qavsdk==null));					
			mIsInCloseRoom = false;	
			return -1;		
		}

	}

	boolean getIsInCreateRoom() {
		return mIsInCreateRoom;
	}

	boolean getIsInCloseRoom() {
		return mIsInCloseRoom;
	}

	boolean getIsInJoinRoom() {
		return mIsInJoinRoom;
	}

	boolean getIsVideo() {
		return mIsVideo;
	}

	long getRoomId() {
		return mRoomId;
	}

	void setRoomId(long roomId) {
		mRoomId = roomId;
	}
}