package com.vanchu.libs.common.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class MaxInputTextWatcher implements TextWatcher{
	private Callback		_callback	= null;
	
	private CharSequence	_temp;
	private EditText		_editText;
	private int 			_maxLen;
	
	
	public MaxInputTextWatcher(EditText editText, int maxLen, Callback callback) {
		super();
		_editText	= editText;
		_maxLen		= maxLen;
		_callback	= callback;
	}
	
	@Override
	public void afterTextChanged(Editable s) {
		if(_temp.length() > _maxLen){
			s.delete(_maxLen, _temp.length());
			_editText.setText(s);
			_editText.setSelection(_editText.getText().toString().length()); 
			if(null != _callback) {
				_callback.onMaxInputReached(_maxLen);
			}
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
		// nothing to do
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		_temp	= s;
		if(null != _callback) {
			_callback.onTextChanged(String.valueOf(_temp), _maxLen);
		}
	}
	
	public interface Callback {
		public void onMaxInputReached(int maxLen);
		public void onTextChanged(String currentStr, int maxLen);
	}
}