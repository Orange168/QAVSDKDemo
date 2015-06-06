package com.anl.phone.wxb.api.av;

/**
 * Created by Edward.Lin on 2015/6/5 19:01
 * 音频视频的操作规范
 */
public interface IIAudioVideo {
    /**
     *第三方登陆验证
     * @param args 登陆所需信息
     * @return 验证返回的信息
     */
    int login(String[] args) ;

    /**
     * 退出操作
     * @return 返回的信息
     */
    int logout() ;

    /**
     * 音频邀请
     * @param args 音频邀请所需要的信息 args[0] 为被邀请人的ID
     */
    void audioInvite(String[] args) ;

    /**
     * 视频邀请
     *  @param args 音频邀请所需要的信息 args[0] 为被邀请人的ID
     */
    void videoInvite(String[] args) ;

    /**
     * 免提控制
     * @param enable  true 打开免提 false 关闭免提
     */
    void handsFreeEnable(boolean enable) ;

    /**
     * 麦克风控制
     * @param enable  true 打开麦克风 false 关闭麦克风
     */
    void micEnable(boolean enable) ;

    /**
     * 摄像头控制
     * @param enable  true 打开摄像头 false 关闭摄像头
     */
    void cameraEnable(boolean enable) ;

    /**
     * 前后摄像头切换
     */
    void switchCamera() ;

    /**
     * 挂断电话
     */
    void handup() ;

}


