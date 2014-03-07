package com.vanchu.libs.addressBook;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.webCache.WebCache;

import android.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

 public class AddressBookActivity extends Activity implements OnTouchListener{
	private final String TAG	=  AddressBookActivity.class.getSimpleName();
	
	private ListView 						_contentListView;
	private LinearLayout 					_mLinearLayout;
	private TextView 						_dialogText;
	
	private String[] 						_letters;//右边指示器文案
	private Context 						_context;
	
	private List<AddressBookData> 			_personData;
	private AddressBookItemView				_itemView;
	private AddressBookAdapter 				_adapter;
	
	private int 							_alphaTextColor = 0;
	private int 							_alphaLayoutColor = 0;
	
    private WindowManager mWindowManager;
    
    
    
    private HashMap<String,Integer> _alphaIndexMap = null;
    
    public void initResId(int itemLayoutId,int iconId,int nameId,int dialogTextId,
    		int categoryWordId,int defaultIconId,ListView listView,LinearLayout linearLayout) {
    	_itemView = new AddressBookItemView();
    	_itemView.setContentListView(listView);
    	_itemView.setIndicatorLayout(linearLayout);
    	
    	_itemView.setDialogTextId(dialogTextId);
    	_itemView.setItemLayoutId(itemLayoutId);
    	_itemView.setNameId(nameId);
    	_itemView.setIconId(iconId);
    	_itemView.setCategoryWordId(categoryWordId);
    	_itemView.setDefaultIconId(defaultIconId);
    	
    	this._contentListView 	= _itemView.getContentListView();
		this._mLinearLayout		= _itemView.getIndicatorLayout();
    	
    	
    }
    
    

	public void initData(String[] letters ,List<AddressBookData> personData , Context context , WebCache webCache) {
		this._letters			= letters;
		this._context			= context;
		this._personData 		= personData;
		
		_alphaIndexMap = new HashMap<String, Integer>();
		_mLinearLayout.setBackgroundResource(android.R.color.transparent);
		_mLinearLayout.setOnTouchListener(this);
		
		addDialogText();
		
		addAlphaText();
		
		structureAlphaIndex();
		
		beforeRefreshAdapter(_personData , _itemView.getContentListView(),true);
		AddressBookActivity book = this;
		_adapter = new AddressBookAdapter(_personData, _itemView, _context,_alphaIndexMap,webCache,book);
		_contentListView.setAdapter(_adapter);
		_contentListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position < _personData.size()) {
					itemLongClick(position);
				}
				return true;
			}
			
		});
		_contentListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position < _personData.size()) {
					itemClick(position);
				}
			}
			
		});
	}
	
	public void itemLongClick(int position){};
	public void itemClick(int position){};
	/**
	 * 刷新adapter之前调用，可复写本函数添加footview
	 * @param data
	 * @param listView
	 * @param isSetAdapter -是否是setAdapter之前调用
	 */
	public void beforeRefreshAdapter(List<AddressBookData> data , ListView listView ,boolean  isSetAdapter){};
	
	public void handleNewView(View view , int position) {
		Log.d(TAG, "addressBookActivity setPrivateResId");
	}
	
	private void structureAlphaIndex() {
		String temp = "-1";
		for (int i = 0; i < _personData.size(); i++) {
			String alpha = _personData.get(i).getLetter();
				if(!alpha.equals(temp)){	
					_alphaIndexMap.put(alpha, i);
					temp = alpha;
				}
		}
	}
	
	public void addAlphaText(){
		for (int i = 0; i < _letters.length; i++) {	
			TextView textView = new TextView(_context);
			textView.setText(_letters[i]);
			if (_alphaTextColor != 0) {
				textView.setTextColor(_alphaTextColor);
			}
			textView.setTextSize(15);
			textView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 0, 1.0f));
			textView.setBackgroundResource(android.R.color.transparent);
			textView.setPadding(4, 0, 2, 0);
			if (!((Activity)_context).isFinishing()) {
				_mLinearLayout.addView(textView);
			}
		}
	}
	
	public void addDialogText(){
		mWindowManager = (WindowManager)_context.getSystemService(Context.WINDOW_SERVICE);
		LayoutInflater inflate = (LayoutInflater)_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		_dialogText = (TextView) inflate.inflate(_itemView.getDialogTextId(), null);
        _dialogText.setVisibility(View.INVISIBLE);
		new Handler().post(new Runnable() {
            public void run() {
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
                if (!((Activity)_context).isFinishing()) {
                	mWindowManager.addView(_dialogText, lp);
				}
            }});
	}
	
	public void removeWindow() {
		if (null != mWindowManager && null != _dialogText) {
			mWindowManager.removeView(_dialogText);
		}
    }

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int height = v.getHeight();
		final int alphaHeight = height / _letters.length;
		final int fingerY = (int) event.getY();
		int selectIndex = fingerY / alphaHeight;
		if (selectIndex < 0 || selectIndex > _letters.length - 1) {
			_mLinearLayout.setBackgroundResource(android.R.color.transparent);
			_dialogText.setVisibility(View.INVISIBLE);
			return true;
		}
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			String letter = _letters[selectIndex];
			findLocation(letter);
			break;
		case MotionEvent.ACTION_MOVE:
			letter = _letters[selectIndex];
			findLocation(letter);
			break;
		case MotionEvent.ACTION_UP:
			if (_alphaLayoutColor != 0) {
				_mLinearLayout.setBackgroundColor(_context.getResources().getColor(R.color.transparent));
			}else{
				_mLinearLayout.setBackgroundResource(android.R.color.transparent);
			}
			_dialogText.setVisibility(View.INVISIBLE);
			break;
		default:
			break;
		}
		return true;
	}
	
	private void findLocation(String letter){
		_dialogText.setVisibility(View.VISIBLE);
		_dialogText.setText(letter);
		if (_alphaLayoutColor != 0) {
			_mLinearLayout.setBackgroundColor(_alphaLayoutColor);
		}else{
			_mLinearLayout.setBackgroundResource(android.R.color.darker_gray);
		}
		if(_alphaIndexMap.containsKey(letter)){			
			_dialogText.setVisibility(View.VISIBLE);
			_dialogText.setText(letter);
			int position = _alphaIndexMap.get(letter);
			_contentListView.setSelection(position);
		}
	}
	
	protected void setColor(int indicatorColor,int alphaLayoutColor) {
		_alphaTextColor = indicatorColor;
		_alphaLayoutColor = alphaLayoutColor;
	}
	
	
	public void deletePerson(int position){
		_personData.remove(position);
		  Set<Map.Entry<String, Integer>> set = _alphaIndexMap.entrySet();
	        for (Iterator<Map.Entry<String, Integer>> it = set.iterator(); it.hasNext();) {
	            Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) it.next();
	            if (entry.getValue() > position) {
	            	_alphaIndexMap.put(entry.getKey(), entry.getValue()-1);
				}
	        }
	        beforeRefreshAdapter(_personData,_contentListView,false);
	        _adapter.notifyDataSetChanged(); 
	}
	
	
	
	public void updateData(List<AddressBookData> personData){
		beforeRefreshAdapter(_personData,_contentListView,false);
		_alphaIndexMap.clear();
		structureAlphaIndex();
		_adapter.notifyDataSetChanged();
		SwitchLogger.d(TAG, "updateData:refresh listview");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		removeWindow();
	}

}
