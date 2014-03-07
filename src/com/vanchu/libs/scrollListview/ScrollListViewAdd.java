package com.vanchu.libs.scrollListview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class ScrollListViewAdd extends LinearLayout {

	private AddViewListener listener;

	public ScrollListViewAdd(Context context) {
		super(context);
	}

	public ScrollListViewAdd(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setListener(AddViewListener listener) {
		this.listener = listener;
	}

	public final void beforeLoading() {
		if (listener != null) {
			listener.beforeLoading(this);
		}
	}

	public final void onLoading() {
		if (listener != null) {
			listener.onLoading(this);
		}
	}

	public final void afterLoading() {
		if (listener != null) {
			listener.afterLoading(this);
		}
	}

	public final void noMoreData() {
		if (listener != null) {
			listener.noMoreData(this);
		}
	}

	public interface AddViewListener {

		public void beforeLoading(ScrollListViewAdd myAddView);

		public void onLoading(ScrollListViewAdd myAddView);

		public void afterLoading(ScrollListViewAdd myAddView);

		public void noMoreData(ScrollListViewAdd myAddView);
	}
}
