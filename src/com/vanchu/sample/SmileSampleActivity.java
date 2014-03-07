package com.vanchu.sample;

import com.vanchu.libs.smile.SmileEditText;
import com.vanchu.libs.smile.SmileParser;
import com.vanchu.libs.smile.SmileTextView;
import com.vanchu.test.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class SmileSampleActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_smile);

		final SmileParser smileParser = new SmileParser(this,
				R.xml.smile_config, "drawable", "com.vanchu.test");

		final SmileTextView textView = (SmileTextView) findViewById(R.id.textView2);
		textView.bindSmileParser(smileParser);

		final SmileEditText editText = (SmileEditText) findViewById(R.id.editText1);
		editText.bindSmileParser(smileParser);

		ImageButton smile = (ImageButton) findViewById(R.id.smile);
		ImageButton input = (ImageButton) findViewById(R.id.input);

		input.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 新建html标签内容
				String htmlString = "<font color=\"#ff0000\">" + "@Alex"
						+ "</font>";
				// 插入并显示html标签内容
				textView.appendHtmlString(htmlString);
				// 新建含表情标签的文字
				String smileString = editText.getText() + "\n";
				// 插入并显示含表情标签的文字
				textView.appendSmileString(smileString);
				editText.setText("");
			}
		});
		smile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				editText.addSmile(smileParser.getKey(2));
				// 或者 editText.addSmile("[可爱]");
			}
		});
	}

}
