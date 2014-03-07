package com.vanchu.libs.addressBook;

import java.util.HashMap;
import java.util.List;

import com.vanchu.libs.common.task.CachedImageLoader;
import com.vanchu.libs.common.task.CachedImageLoader.Callback;
import com.vanchu.libs.webCache.WebCache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AddressBookAdapter extends BaseAdapter{
	
	
	private List<AddressBookData> 		_personData;
	private AddressBookItemView				_itemView;
	private Context					_context;
	private WebCache 				_webCache;
	private HashMap<String,Integer> _mAlphaIndexMap;
	private CachedImageLoader		_imageLoader;
	private AddressBookActivity 	_book;
	
	public AddressBookAdapter(List<AddressBookData> personInfoEntities , AddressBookItemView	personItemEntity , Context context,
			HashMap<String,Integer> mAlphaIndexMap , WebCache webCache , AddressBookActivity book) {
		this._personData = personInfoEntities;
		this._itemView   = personItemEntity;
		this._context			 = context;
		this._mAlphaIndexMap	 = mAlphaIndexMap;
		this._webCache			 = webCache;
		this._book				 = book;
		_imageLoader	 = new CachedImageLoader();
	}
	

	@Override
	public int getCount() {
		return _personData.size();
	}

	@Override
	public Object getItem(int position) {
		return _personData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = null;
		if (null == convertView) {
			view = LayoutInflater.from(_context).inflate(_itemView.getItemLayoutId(), null);
			convertView = view;
			convertView.setTag(view);
		}else{
			view = (View)convertView.getTag();
		}
		ImageView icon = (ImageView)view.findViewById(_itemView.getIconId());
		TextView  name = (TextView)view.findViewById(_itemView.getNameId());
		TextView  alphaText = (TextView)view.findViewById(_itemView.getCategoryWordId());
		
		_book.handleNewView(view,position);
		
		String loginName = _personData.get(position).getName();
		if (loginName.length() > 10) {
			loginName = loginName.substring(0, 10);
		}
		name.setText(loginName);
		if (_mAlphaIndexMap.get(_personData.get(position).getLetter()) == position) {
			alphaText.setVisibility(View.VISIBLE);
			alphaText.setText(_personData.get(position).getLetter());
		}else{
			alphaText.setVisibility(View.GONE);
		}
		showImage(icon, _personData.get(position).getIconURL());
		return convertView;
	}
	
	
	private void showImage(final ImageView imageView, String url) {
		Bitmap bitmap = BitmapFactory.decodeResource(_context.getResources(), _itemView.getDefaultIconId());
		imageView.setImageBitmap(bitmap);
		_imageLoader.loadImage(url, _webCache, new Callback() {
			
			@Override
			public void onSucc(String url, Bitmap bitmap) {
				imageView.setImageBitmap(bitmap);
			}
			
			@Override
			public void onProgress(String url, int progress) {
				
			}
			
			@Override
			public void onFail(String url) {
				
			}
		});
	}
}
