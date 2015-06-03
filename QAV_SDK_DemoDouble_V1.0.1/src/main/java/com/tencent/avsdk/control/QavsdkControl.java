package com.tencent.avsdk.control;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.tencent.av.sdk.AVContext;
import com.tencent.av.sdk.AVRoom;
import com.tencent.avsdk.R;

public class QavsdkControl {
	private static final String TAG = "QavsdkControl";
	private AVContextControl mAVContextControl = null;
	private AVInvitationControl mAVInvitationControl = null;
	private AVRoomControl mAVRoomControl = null;
	private AVUIControl mAVUIControl = null;
	private AVVideoControl mAVVideoControl = null;
	private AVAudioControl mAVAudioControl = null;
	private boolean isVideo = false;
	public QavsdkControl(Context context) {
		try {
			System.loadLibrary("xplatform");
			System.loadLibrary("qav_graphics");
			System.loadLibrary("qavsdk");	
		} catch (UnsatisfiedLinkError e) {
			// TODO: handle exception
		}

		mAVContextControl = new AVContextControl(context);
		mAVInvitationControl = new AVInvitationControl(context);
		mAVRoomControl = new AVRoomControl(context);
		mAVVideoControl = new AVVideoControl(context);
		mAVAudioControl = new AVAudioControl(context);
		Log.e(TAG, "WL_DEBUG QavsdkControl");
	}

	/**
	 * 启动SDK系统
	 * 
	 * @param identifier
	 *            用户身份的唯一标识
	 * @param usersig
	 *            用户身份的校验信息
	 */
	public int startContext(String identifier, String usersig) {
		return mAVContextControl.startContext(identifier, usersig);
	}

	/**
	 * 关闭SDK系统
	 */
	public void closeContext() {
		mAVContextControl.closeContext();
	}

	public boolean hasAVContext() {
		return mAVContextControl.hasAVContext();
	}

	public String getSelfIdentifier() {
		return mAVContextControl.getSelfIdentifier();
	}

	public String getPeerIdentifier() {
		return mAVContextControl.getPeerIdentifier();
	}

	void setPeerIdentifier(String peerIdentifier) {
		mAVContextControl.setPeerIdentifier(peerIdentifier);
	}

	/**
	 * 创建房间
	 * 
	 * @param isVideo
	 *            是否有视频
	 */
	void createRoom(boolean isVideo) {
		this.isVideo = isVideo;
		mAVRoomControl.createRoom(isVideo);
	}

	/** 关闭房间 */
	public int closeRoom() {
		return mAVRoomControl.closeRoom();
	}

	public void joinRoom(long roomId, String peerIdentifier, boolean isVideo) {
		this.isVideo = isVideo;		
		mAVRoomControl.joinRoom(roomId, peerIdentifier, isVideo);
	}

	AVRoom getRoom() {
		AVContext avContext = getAVContext();

		return avContext != null ? avContext.getRoom() : null;
	}

	public long getRoomId() {
		return mAVRoomControl.getRoomId();
	}

	void setRoomId(long roomId) {
		mAVRoomControl.setRoomId(roomId);
	}

	public boolean getIsInStartContext() {
		return mAVContextControl.getIsInStartContext();
	}

	public boolean getIsInCloseContext() {
		return mAVContextControl.getIsInCloseContext();
	}

	public boolean getIsInCreateRoom() {
		return mAVRoomControl.getIsInCreateRoom();
	}

	public boolean getIsInCloseRoom() {
		return mAVRoomControl.getIsInCloseRoom();
	}

	public boolean getIsInJoinRoom() {
		return mAVRoomControl.getIsInJoinRoom();
	}

	public boolean getIsVideo() {
		return mAVRoomControl.getIsVideo();
	}
	public void setRemoteHasVideo(String identifier, int videoSrcType, boolean isRemoteHasVideo) {
		mAVUIControl.setRemoteHasVideo(identifier, videoSrcType, isRemoteHasVideo, false, false);
	}

	public AVContext getAVContext() {
		return mAVContextControl.getAVContext();
	}

	public void onCreate(Context context, View contentView) {
		mAVUIControl = new AVUIControl(context, contentView.findViewById(R.id.av_video_layer_ui));
		mAVVideoControl.initAVVideo();
		mAVAudioControl.initAVAudio();
//		mAVEndpointControl.initMembersUI((MultiVideoMembersControlUI) contentView.findViewById(R.id.qav_gaudio_gridlayout));
	}

	public void onResume() {
		mAVContextControl.getAVContext().onResume();
		mAVUIControl.onResume();
	}

	public void onPause() {
		mAVContextControl.getAVContext().onPause();
		mAVUIControl.onPause();
	}

	public void onDestroy() {
		if (mAVUIControl != null) {
			mAVUIControl.onDestroy();		
		}
	}


	public void setLocalHasVideo(boolean isLocalHasVideo, String identifier) {
		mAVUIControl.setLocalHasVideo(isLocalHasVideo, false, identifier);
	}
	public void setRemoteHasVideo(boolean isRemoteHasVideo, String identifier) {
		mAVUIControl.setSmallVideoViewLayout(isRemoteHasVideo, identifier);
	}
	public void setSelfId(String key) {
		mAVUIControl.setSelfId(key);
	}	

	public int toggleEnableCamera() {
		return mAVVideoControl.toggleEnableCamera();
	}

	public int toggleSwitchCamera() {
		return mAVVideoControl.toggleSwitchCamera();
	}

	public boolean getIsInOnOffCamera() {
		return mAVVideoControl.getIsInOnOffCamera();
	}

	public boolean getIsInSwitchCamera() {
		return mAVVideoControl.getIsInSwitchCamera();
	}

	public boolean getIsEnableCamera() {
		return mAVVideoControl.getIsEnableCamera();
	}

	public boolean getIsFrontCamera() {
		return mAVVideoControl.getIsFrontCamera();
	}

	void initAVInvitation() {
		mAVInvitationControl.initAVInvitation();
	}

	public void invite(String peerIdentifier, boolean isVideo) {
		setPeerIdentifier(peerIdentifier);
		createRoom(isVideo);
	}

	void inviteIntenal() {
		mAVInvitationControl.invite();
	}

	public void accept() {
		mAVInvitationControl.accept();
	}

	public void refuse() {
		mAVInvitationControl.refuse();
	}

	public boolean getIsInInvite() {
		return mAVInvitationControl.getIsInInvite();
	}

	public boolean getIsInAccept() {
		return mAVInvitationControl.getIsInAccept();
	}

	public boolean getIsInRefuse() {
		return mAVInvitationControl.getIsInRefuse();
	}

	void uninitAVInvitation() {
		mAVInvitationControl.uninitAVInvitation();
	}

	public boolean getHandfreeChecked() {
		return mAVAudioControl.getHandfreeChecked();
	}
	
	
	public AVVideoControl getAVVideoControl() {
		return mAVVideoControl;
	}
	public void setRotation(int rotation) {
		mAVUIControl.setRotation(rotation);
	}
	
	public boolean isVideo() {
		return isVideo;
	}
}