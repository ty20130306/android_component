package com.vanchu.test;

import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.eventCenter.Event;
import com.vanchu.libs.eventCenter.EventCenter;
import com.vanchu.libs.eventCenter.IListener;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

public class TestEventCenterActivity extends Activity {

	private static final String LOG_TAG	= TestEventCenterActivity.class.getSimpleName();
	
	private EventCenter _eventCenter	= null;	
	private IListener	_listener		= null;
	private IListener	_listener2		= null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_event_center);
		
		_eventCenter	= EventCenter.getInstance();
		_listener	= new IListener() {
			
			@Override
			public void handle(Event event) {
				SwitchLogger.d(LOG_TAG, "listener one receive event, type="+event.getType()
										+ ", msg is " + (String)(event.getData()));
			}
		};
		
		_listener2	= new IListener() {
			
			@Override
			public void handle(Event event) {
				SwitchLogger.d(LOG_TAG, "listener two receive event, type="+event.getType()
										+ ", msg is " + (String)(event.getData()));
			}
		};
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test_event_center, menu);
		return true;
	}

	public void addEventListener(View v) {
		
		_eventCenter.addEventListener(1, _listener);
		_eventCenter.addEventListener(2, _listener2);
	}
	
	public void removeEventListener(View v) {
		_eventCenter.removeEventListener(1, _listener);
		_eventCenter.removeEventListener(2, _listener2);
	}

	public void dispatchEvent(View v) {
		_eventCenter.dispatchEvent(new Event(1, "hello, i am event 1") );
		_eventCenter.dispatchEvent(new Event(2, "hello, i am event 2") );
	}
}
