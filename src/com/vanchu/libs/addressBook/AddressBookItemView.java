package com.vanchu.libs.addressBook;

import android.widget.LinearLayout;
import android.widget.ListView;


public class AddressBookItemView {
	/*item的layout资源Id*/
	private int itemLayoutId = -1;
	/*头像的IamgeView资源Id*/
	private int iconId = -1;
	/*name的textview资源Id*/
	private int nameId = -1;
	/*dialog显示text资源Id*/
	private int dialogTextId = -1;
	/*类别资源Id*/
	private int categoryWordId = -1;
	/*悄悄话按钮资源Id*/
	private int privateImgId = -1;
	/*默认头像资源Id*/
	private int defaultIconId = -1;
	
	public int getDefaultIconId() {
		return defaultIconId;
	}
	public void setDefaultIconId(int defaultIconId) {
		this.defaultIconId = defaultIconId;
	}
	private ListView contentListView = null;
	private LinearLayout indicatorLayout = null;
	public int getIconId() {
		return iconId;
	}
	public void setIconId(int iconId) {
		this.iconId = iconId;
	}
	public int getItemLayoutId() {
		return itemLayoutId;
	}
	public void setItemLayoutId(int itemLayoutId) {
		this.itemLayoutId = itemLayoutId;
	}
	public int getNameId() {
		return nameId;
	}
	public void setNameId(int nameId) {
		this.nameId = nameId;
	}
	public int getDialogTextId() {
		return dialogTextId;
	}
	public void setDialogTextId(int dialogTextId) {
		this.dialogTextId = dialogTextId;
	}
	public int getCategoryWordId() {
		return categoryWordId;
	}
	public void setCategoryWordId(int categoryWordId) {
		this.categoryWordId = categoryWordId;
	}
	public int getPrivateImgId() {
		return privateImgId;
	}
	public void setPrivateImgId(int privateImgId) {
		this.privateImgId = privateImgId;
	}
	public ListView getContentListView() {
		return contentListView;
	}
	public void setContentListView(ListView contentListView) {
		this.contentListView = contentListView;
	}
	public LinearLayout getIndicatorLayout() {
		return indicatorLayout;
	}
	public void setIndicatorLayout(LinearLayout indicatorLayout) {
		this.indicatorLayout = indicatorLayout;
	}
	
	
	
	
	
	

}
