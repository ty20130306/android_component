package com.vanchu.libs.platform;

import com.tencent.tauth.Constants;

import android.os.Bundle;

public class TencentSendStoryParam {

	/**
	 * 必传, 分享的标题
	 */
	private String 	_title;
	
	/**
	 * 必传, 分享的图片URL
	 */
	private String 	_imageUrl;
	
	/**
	 * 可选, 用户分享时的评论内容，可由用户输入
	 */
	private String 	_comment;

	/**
	 * 可选, 分享的故事摘要
	 */
	private String 	_summary;
	
	/**
	 * 可选, 由开发者自定义该参数内容，用于判断好友来源。
	 * 应用分享成功后，被邀请方通过邀请链接进入应用时会携带该参数并透传给应用
	 */
	private String 	_source;
	
	/**
	 * 可选, 分享feeds中显示的操作区文字，参数值可为： '进入应用', '领取奖励', '获取能量', '帮助TA'
	 */
	private String 	_act;
	
	/**
	 * 可选, 分享内容中携带的视频链接
	 */
	private String 	_playUrl;
	
	public TencentSendStoryParam (String title, String imageUrl) {
		_title		= title;
		_imageUrl	= imageUrl;
	}
	
	public void setComment(String comment) {
		_comment	= comment;
	}
	
	public void setSummary(String summary) {
		_summary	= summary;
	}
	
	public void setSource(String source) {
		_source		= source;
	}
	
	public void setAct(String act) {
		_act	= act;
	}
	
	public void setPlayUrl(String playUrl) {
		_playUrl	= playUrl;
	}
	
	public Bundle getParamBundle() {
		Bundle bundle	= new Bundle();
		bundle.putString(Constants.PARAM_TITLE, _title);
		bundle.putString(Constants.PARAM_IMAGE, _imageUrl);
		
		if(null != _comment) {
			bundle.putString(Constants.PARAM_COMMENT, _comment);
		}
		
		if(null != _summary) {
			bundle.putString(Constants.PARAM_SUMMARY, _summary);
		}
		
		if(null != _playUrl) {
			bundle.putString(Constants.PARAM_PLAY_URL, _playUrl);
		}
		
		if(null != _act) {
			bundle.putString(Constants.PARAM_ACT, _act);
		}
		
		if(null != _source) {
			bundle.putString(Constants.PARAM_SOURCE, _source);
		}
		
		return bundle;
	}
} 
