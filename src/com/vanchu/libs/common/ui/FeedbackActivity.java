package com.vanchu.libs.common.ui;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.vanchu.libs.common.util.ActivityUtil;
import com.vanchu.libs.common.util.NetUtil;
import com.vanchu.libs.common.util.SwitchLogger;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;


abstract public class FeedbackActivity extends Activity implements OnClickListener {
	
	private static final String LOG_TAG		= FeedbackActivity.class.getSimpleName();
	
	private static final int DEFAULT_MAX_MSG_LEN		= 200;
	private static final int DEFAULT_MAX_CONTACT_LEN	= 50;
	
	private static final int RESPONSE_SUCC		= 0;
	
	private static final int SUBMIT_SUCC		= 0; 
	private static final int SUBMIT_FAIL		= 1; 
	
	private int	_maxMsgLen		= DEFAULT_MAX_MSG_LEN;
	private int	_maxContactLen	= DEFAULT_MAX_CONTACT_LEN;
	
	private ImageButton _cancelImageBtn		= null;
	private ImageButton _submitImageBtn		= null;
	
	private Button		_cancelBtn		= null;
	private Button		_submitBtn		= null;
	
	private boolean		_isCancelImageBtn	= true;
	private boolean		_isSubmitImageBtn	= true;
	
	private EditText	_contactText	= null;
	private EditText	_msgText		= null;
	
	private String	_contactKey	= "contact";
	private String	_contact	= "";
	private String	_msgKey		= "msg";
	private String	_msg		= "";
	
	private int _layoutResId;
	private int _contactTextResId;
	private int _msgTextResId;
	private int _cancelBtnResId;
	private int _submitBtnResId;
	
	private String	_submitUrl;
	private Map<String, String>	_submitUrlParams;
	
	private Handler		_handler	= new Handler(){

		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			case SUBMIT_SUCC:
				sumbitSucc();
				break;

			case SUBMIT_FAIL:
				submitFail();
				break;
			default:
				break;
			}
		}
	};
	
	private void submitFail() {
		afterSendRequest();
		onSubmitFail();
	}

	private void sumbitSucc() {
		afterSendRequest();
		onSubmitSucc();
		quit();
	}

	private void quit() {
		LoadingDialog.cancel();
		finish();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		beforeSetContentView();
		setContentView(_layoutResId);
		initView();
		
		afterSetContentView();
	}

	private void initView(){
		if(_isCancelImageBtn) {
			_cancelImageBtn	= (ImageButton)findViewById(_cancelBtnResId);
			_cancelImageBtn.setOnClickListener(this);
		} else {
			_cancelBtn		= (Button)findViewById(_cancelBtnResId);
			_cancelBtn.setOnClickListener(this);
		}
		
		if(_isSubmitImageBtn) {
			_submitImageBtn	= (ImageButton)findViewById(_submitBtnResId);
			_submitImageBtn.setOnClickListener(this);
		} else {
			_submitBtn		= (Button)findViewById(_submitBtnResId);
			_submitBtn.setOnClickListener(this);
		}
		
		_contactText	= (EditText)findViewById(_contactTextResId);
		_msgText		= (EditText)findViewById(_msgTextResId);
		
		_msgText.addTextChangedListener(new MaxInputTextWatcher(_msgText, _maxMsgLen, new MaxInputTextWatcher.Callback() {
			
			@Override
			public void onMaxInputReached(int maxLen) {
				Tip.show(FeedbackActivity.this, "你只能输入"+maxLen+"个字");
			}
			
			@Override
			public void onTextChanged(String currentStr, int maxLen) {
				SwitchLogger.d(LOG_TAG, "current str="+currentStr+",len="+currentStr.length()+",maxLen="+maxLen);
			}
		}));
		_contactText.addTextChangedListener(new MaxInputTextWatcher(_contactText, _maxContactLen, new MaxInputTextWatcher.Callback() {
			
			@Override
			public void onMaxInputReached(int maxLen) {
				Tip.show(FeedbackActivity.this, "你只能输入"+maxLen+"个字");
			}
			
			@Override
			public void onTextChanged(String currentStr, int maxLen) {
				SwitchLogger.d(LOG_TAG, "current str="+currentStr+",len="+currentStr.length()+",maxLen="+maxLen);
			}
		}));
	}
	
	@Override
	public void onClick(View v) {
		int id	= v.getId();
		if(id == _submitBtnResId){
			submit();
		} else if(id == _cancelBtnResId){
			cancel();
		}
	}
	
	private void submit() {
		_contact	= _contactText.getText().toString();
		_msg		= _msgText.getText().toString();
		
		if(_msg.length() <= 0){
			onMsgEmpty();
			return ;
		}
		
		beforeSendRequest();		
		new Thread(){
			public void run(){
				_submitUrlParams.put(_contactKey, _contact);
				_submitUrlParams.put(_msgKey, _msg);
				String response	= NetUtil.httpPostRequest(_submitUrl, _submitUrlParams, 1);
				if(null != response && onSubmitResponse(response)){
					_handler.sendEmptyMessage(SUBMIT_SUCC);
				} else {
					_handler.sendEmptyMessage(SUBMIT_FAIL);
				}
			}
		}.start();
	}

	private void cancel() {
		ActivityUtil.hideInputPanel(this);
		quit();
	}

	protected void initResId(int layoutResId, int contactTextResId, int msgTextResId, 
			int cancelBtnResId, int submitBtnResId) 
	{
		_layoutResId		= layoutResId;
		_contactTextResId	= contactTextResId;
		_msgTextResId		= msgTextResId;
		_cancelBtnResId		= cancelBtnResId;
		_submitBtnResId		= submitBtnResId;
	}

	protected void initRequest(String submitUrl, Map<String, String> submitUrlParams){
		_submitUrl			= submitUrl;
		_submitUrlParams	= submitUrlParams;
		if(_submitUrlParams == null){
			_submitUrlParams	= new HashMap<String, String>();
		}
	}

	protected void setMaxMsgLen(int len){
		_maxMsgLen	= len;
	}
	
	protected void setMaxContactLen(int len){
		_maxContactLen	= len;
	}
	
	protected void onMsgEmpty() {
		Tip.show(this, "请输入反馈内容！");
	}
	
	protected void beforeSendRequest() {
		LoadingDialog.create(this, "正在提交反馈");
	}

	protected void afterSendRequest() {
		LoadingDialog.cancel();
	}
	
	protected void onSubmitFail(){
		Tip.show(this, "反馈失败, 请稍后再试");
	}
	
	protected void onSubmitSucc() {
		ActivityUtil.hideInputPanel(this);
		Tip.show(this, "反馈成功, 我们会尽快处理您的反馈");
	}
	
	protected boolean onSubmitResponse(String response){
		try {
			JSONObject data	= new JSONObject(response);
			if(data.getInt("ret") == RESPONSE_SUCC){
				return true;
			} else {
				return false;
			}
		} catch (JSONException e){
			SwitchLogger.e(e);
			return false;
		}
	}
	
	protected void setIsCancleImageBtn(boolean isCancelImageBtn) {
		_isCancelImageBtn	= isCancelImageBtn;
	}
	
	protected void setIsSubmitImageBtn(boolean isSubmitImageBtn) {
		_isSubmitImageBtn	= isSubmitImageBtn;
	}
	
	protected void setContactKey(String contactKey) {
		_contactKey	= contactKey;
	}
	
	protected void setMsgKey(String msgKey) {
		_msgKey		= msgKey;
	}
	
	/**
	 * called after setContentView
	 * 
	 * can call setMaxMsgLen or setMaxContactLen here
	 */
	protected void afterSetContentView(){
		
	}
	
	/**
	 * called before setContentView
	 * 
	 * must call initResId and initRequest here
	 */
	abstract protected void beforeSetContentView();
}
