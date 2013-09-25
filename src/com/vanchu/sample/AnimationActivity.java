package com.vanchu.sample;

import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.test.R;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

public class AnimationActivity extends Activity {

	private static final String LOG_TAG	= AnimationActivity.class.getSimpleName();
	
	private ImageView	_pic;
	private ImageView	_pic2;
	private Button		_topBtn;
	private Button		_bottomBtn;
	private Animation	_rotateAnimation;
	private AnimationDrawable	_frameAnimation;
	
	private boolean	_playing	= false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_animation);
		
		_pic	= (ImageView)findViewById(R.id.pic);
		_pic2	= (ImageView)findViewById(R.id.pic_2);
		_pic2.setBackgroundResource(R.drawable.music_playing);
		_frameAnimation	= (AnimationDrawable)_pic2.getBackground();
		
		_topBtn	= (Button)findViewById(R.id.top_btn);
		_topBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showBottomBtn(v);
			}
		});
		_topBtn.setVisibility(View.VISIBLE);
		
		_bottomBtn	= (Button)findViewById(R.id.bottom_btn);
		_bottomBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showTopBtn(v);
			}
		});
	}

	public void showBottomBtn(View v) {
		_topBtn.setVisibility(View.GONE);
		_bottomBtn.setVisibility(View.VISIBLE);
	}

	public void showTopBtn(View v) {
		_topBtn.setVisibility(View.VISIBLE);
		_bottomBtn.setVisibility(View.GONE);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.animation, menu);
		return true;
	}
	
	public void startRotate(View v) {
		_rotateAnimation	= AnimationUtils.loadAnimation(this, R.anim.rotate);
		_rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				SwitchLogger.d(LOG_TAG, "onAnimationStart");
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				SwitchLogger.d(LOG_TAG, "onAnimationRepeat");
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				SwitchLogger.d(LOG_TAG, "onAnimationEnd");
			}
		});
		_pic.startAnimation(_rotateAnimation);
	}

	public void stopRotate(View v) {
		_pic.clearAnimation();
	}
	
	public void startFrame(View v) {
		SwitchLogger.d(LOG_TAG, "startFrame");
		_frameAnimation.start();
	}

	public void stopFrame(View v) {
		SwitchLogger.d(LOG_TAG, "stopFrame");
		_frameAnimation.stop();
	}
	
	public void btnMoveDown(View v) {
		SwitchLogger.d(LOG_TAG, "btnMoveDown, playing="+_playing);
		
		if(_playing){
			SwitchLogger.d(LOG_TAG, "btnMoveDown is playing, need not to move");
			return;
		}
		
		Animation animation	= AnimationUtils.loadAnimation(this, R.anim.translate_down);
		animation.setFillAfter(true);
		_topBtn.startAnimation(animation);
		animation.setAnimationListener(new Animation.AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				_playing	= true;
				SwitchLogger.d(LOG_TAG, "btnMoveDown playing set to true" );
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				SwitchLogger.d(LOG_TAG, "btnMoveDown end" );
				_topBtn.setVisibility(View.GONE);
				_bottomBtn.setVisibility(View.VISIBLE);
				_playing	= false;
			}
		});
	}
	
	public void btnMoveUp(View v) {
		SwitchLogger.d(LOG_TAG, "btnMoveUp, playing="+_playing);
		
		if(_playing){
			SwitchLogger.d(LOG_TAG, "btnMoveUp is playing, need not to move");
			return;
		}
		
		Animation animation	= AnimationUtils.loadAnimation(this, R.anim.translate_up);
		animation.setFillAfter(true);
		_bottomBtn.startAnimation(animation);
		animation.setAnimationListener(new Animation.AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				_playing	= true;
				SwitchLogger.d(LOG_TAG, "btnMoveUp playing set to true" );
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				SwitchLogger.d(LOG_TAG, "btnMoveUp end" );
				_playing	= false;
				_topBtn.setVisibility(View.VISIBLE);
				_bottomBtn.setVisibility(View.GONE);
			}
		});
	}
}
