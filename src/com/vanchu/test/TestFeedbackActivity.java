package com.vanchu.test;

import com.vanchu.libs.common.ui.FeedbackActivity;

public class TestFeedbackActivity extends FeedbackActivity {

	@Override
	protected void beforeSetContentView() {
		initResId(R.layout.activity_test_feedback, 
				R.id.feedback_txt_contact, 
				R.id.feedback_txt_msg, 
				R.id.feedback_btn_back, 
				R.id.feedback_btn_submit);
		
		initRequest("http://pesiwang.devel.rabbit.oa.com/test_feedback.php", null);
	}
}
