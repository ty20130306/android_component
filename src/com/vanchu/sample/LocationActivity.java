package com.vanchu.sample;


import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.baidu.location.BDLocation;
import com.vanchu.libs.common.util.LocationUtil;
import com.vanchu.libs.common.util.LocationUtil.CallBack;
import com.vanchu.test.R;

public class LocationActivity extends Activity {
	private final static String TAG = LocationActivity.class.getSimpleName();
	
	private String key = "697f50541f8d4779124896681cb6584d";

	TextView text = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);
		text = (TextView)findViewById(R.id.textView2);
		Button btn = (Button)findViewById(R.id.StartBtn);
		
	    
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
		       LocationUtil location = new LocationUtil(key, LocationActivity.this);
		       location.getLocation(new CallBack() {
				
				@Override
				public void onSuccess(BDLocation location) {
					if (null != location.getCity()) {
						text.setText(location.getCity());
					}
				}
				
				@Override
				public void onFail() {
					
				}
			});
			}
		});
	}
	
	


}
