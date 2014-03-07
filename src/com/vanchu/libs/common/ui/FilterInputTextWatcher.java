package com.vanchu.libs.common.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class FilterInputTextWatcher implements TextWatcher{
	private Callback		_callback	= null;
	
	private CharSequence	_temp;
	private EditText		_editText;
	private String 			_filterStr;
	
	
	public FilterInputTextWatcher(EditText editText, String filterStr, Callback callback) {
		super();
		_editText	= editText;
		_filterStr	= filterStr;
		_callback	= callback;
	}
	
	@Override
	public void afterTextChanged(Editable s) {
		int i = 0;
		for(; i < _temp.length(); i++) {
			if(_filterStr.contains(String.valueOf(_temp.charAt(i)))) {
				break;
			}
		}
		
		if(i < _temp.length()) {
			String filteredStr = String.valueOf(_temp.charAt(i));
			s.delete(i, _temp.length());
			_editText.setText(s);
			_editText.setSelection(_editText.getText().toString().length());
			if(null != _callback) {
				_callback.onFiltered(filteredStr);
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
	}
	
	public interface Callback {
		public void onFiltered(String filteredStr);
	}
}