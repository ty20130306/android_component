package com.vanchu.libs.smile;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class SmileEditText extends EditText {

	private SmileParser parser = null;

	public SmileEditText(Context context) {
		super(context);
	}

	public SmileEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SmileEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void bindSmileParser(SmileParser smileParser) {
		parser = smileParser;
	}

	public void addSmile(String smile) {
		String msg = getText() + "";
		int i = getSelectionStart();
		String msg_head = msg.substring(0, i);
		String msg_tail = msg.substring(i, msg.length());
		String msg_new = msg_head + smile + msg_tail;
		setText(parser.translate(msg_new));
		setSelection(i + smile.length());
	}

}
