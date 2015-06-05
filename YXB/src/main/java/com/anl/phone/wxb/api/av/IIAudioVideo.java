package com.anl.phone.wxb.api.av;

/**
 * Created by Edward.Lin on 2015/6/5 19:01
 * 音频视频的操作规范
 */
public interface IIAudioVideo {
    /**
     *第三方登陆验证
     * @param identification 身份ID
     * @param signature 验证的签名
     * @return 验证返回的信息
     */
    int login(String identification, String signature) ;




}


