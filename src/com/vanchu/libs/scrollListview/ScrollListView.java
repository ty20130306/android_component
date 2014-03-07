package com.vanchu.libs.scrollListview;

//import com.vanchu.libs.common.util.SwitchLogger;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;

public class ScrollListView extends ListView implements OnScrollListener {

	private int lastItem;
	private int firstItem;
	private int count;
	private ScrollListViewAdd footView;
	private ScrollListViewAdd headView;
	private BaseAdapter adapter;
	private ScrollListViewListener listener;
	private OnScrollListener onScrollListener;
	private boolean hasHead;
	private boolean hasFoot;
	private boolean isHeadLoading;
	private boolean isFootLoading;
	private boolean isHeadAfter;
	private boolean isFootAfter;
	private int addViewNum = 0;
	private int minItem = 1;
	private int visibleItemCount;
	private int totalItemCount;

	private boolean touchControl = false;

	public ScrollListView(Context context) {
		super(context);
	}

	public ScrollListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ScrollListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void init(BaseAdapter adapter, ScrollListViewAdd headView,
			ScrollListViewAdd footView, ScrollListViewListener listener,
			int minItem) {
		this.minItem = 1;
		this.adapter = adapter;
		this.headView = headView;
		this.footView = footView;
		this.listener = listener;
		hasHead = (headView != null);
		hasFoot = (footView != null);
		isHeadLoading = false;
		isFootLoading = false;
		if (hasHead) {
			addHeaderView(headView);
			headView.afterLoading();
		}
		if (hasFoot) {
			addFooterView(footView);
			footView.afterLoading();
		}
		if (adapter == null) {
			setVisibility(View.GONE);
		} else {
			count = adapter.getCount();
			setVisibility(View.VISIBLE);
			setAdapter(adapter);
		}
		setOnScrollListener(this);
	}

	public void setScrollListener(OnScrollListener onScrollListener) {
		this.onScrollListener = onScrollListener;
	}

	public void refresh(int addSize) {
		// SwitchLogger.e("MyLog", "refresh()");
		if (adapter == null) {
			setVisibility(View.GONE);
		} else {
			setVisibility(View.VISIBLE);
		}
		if (hasHead) {
			headView.afterLoading();
			isHeadAfter = true;
		}
		if (hasFoot) {
			footView.afterLoading();
			isFootAfter = true;
		}
		adapter.notifyDataSetChanged();
		isHeadLoading = false;
		isFootLoading = false;
		count = adapter.getCount();
		check(count, addSize);
	}

	public void refresh() {
		refresh(-1);
	}

	public void simpleRefresh() {
		adapter.notifyDataSetChanged();
	}

	private void check(int count, int addSize) {
		if (addSize == -1) {
			if (count == 0) {// count没有数据
				hideFoot();
			} else if (count % minItem == 0) {// count一打的倍数
				showFoot();
			} else {// count不是一打的倍数
				hideFoot();
			}
		} else {
			if (addSize < minItem) {
				hideFoot();
			} else {
				showFoot();
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		this.visibleItemCount = visibleItemCount;
		this.totalItemCount = totalItemCount;
		this.totalItemCount = totalItemCount;
		if (onScrollListener != null) {
			onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount,
					totalItemCount);
		}
		if (touchControl) {
			return;
		}
		// SwitchLogger.e("MyLog", "onScroll");
		if (isHeadAfter || isFootAfter) {
			return;
		}
		firstItem = firstVisibleItem;
		lastItem = firstVisibleItem + visibleItemCount - addViewNum;
		if (hasHead && firstItem == 0 && !isHeadLoading) {
			headView.beforeLoading();
		} else if (hasFoot && lastItem == count && !isFootLoading) {
			footView.beforeLoading();
		}
	}

	private void changeListener(int visibleItemCount, int totalItemCount) {
		// SwitchLogger.e("MyLog", "changeListener : visibleItemCount = "
		// + visibleItemCount + " , totalItemCount = " + totalItemCount);
		if (visibleItemCount == totalItemCount) {
			// SwitchLogger.e("MyLog", "不足一屏,交给touch控制");
			touchControl = true;
		} else {
			// SwitchLogger.e("MyLog", "超过一屏,交给scroll控制");
			touchControl = false;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (onScrollListener != null) {
			onScrollListener.onScrollStateChanged(view, scrollState);
		}
		if (touchControl) {
			return;
		}
		switch (scrollState) {
		case SCROLL_STATE_IDLE:
			// SwitchLogger.e("MyLog", "stateChanged = SCROLL_STATE_IDLE");
			break;
		case SCROLL_STATE_FLING:
			// SwitchLogger.e("MyLog", "stateChanged = SCROLL_STATE_FLING");
			break;
		case SCROLL_STATE_TOUCH_SCROLL:
			// SwitchLogger.e("MyLog",
			// "stateChanged = SCROLL_STATE_TOUCH_SCROLL");
			break;
		default:
			break;
		}
		if (isHeadAfter || isFootAfter) {
			isFootAfter = isHeadAfter = false;
			if (hasHead && headView != null) {
				headView.beforeLoading();
			}
			if (hasFoot && footView != null) {
				footView.beforeLoading();
			}
			return;
		}
		if (isFootLoading || isHeadLoading) {
			return;
		}
		if (hasHead && firstItem == 0 && scrollState == SCROLL_STATE_IDLE) {
			isHeadLoading = true;
			headView.onLoading();
			listener.onTopAction();
		}
		if (hasFoot && lastItem == count && scrollState == SCROLL_STATE_IDLE) {
			isFootLoading = true;
			footView.onLoading();
			listener.onBottomAction();
		}
	}

	@Override
	public void addHeaderView(View v) {
		super.addHeaderView(v,null,false);
		addViewNum++;
	}

	@Override
	public void addFooterView(View v) {
		super.addFooterView(v);
		addViewNum++;
	}

	@Override
	public boolean removeHeaderView(View v) {
		boolean isRemoved = super.removeHeaderView(v);
		if (isRemoved) {
			addViewNum--;
		}
		return isRemoved;
	}

	@Override
	public boolean removeFooterView(View v) {
		boolean isRemoved = super.removeFooterView(v);
		if (isRemoved) {
			addViewNum--;
		}
		return isRemoved;
	}

	private void hideFoot() {
		if (footView != null) {
			hasFoot = false;
			footView.noMoreData();
		}
	}

	private void showFoot() {
		if (footView != null && !hasFoot) {
			hasFoot = true;
		}
	}

	public interface ScrollListViewListener {

		public void onTopAction();

		public void onBottomAction();

	}

	// 实现该方法处理触摸事件
	private int downPositionY = 0;
	/** 0不动，1下拉，2上拉 */
	private int state = 0;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			changeListener(visibleItemCount, totalItemCount);
		}
		if (!touchControl || isHeadLoading || isFootLoading) {
			return super.onTouchEvent(event);
		}
		final int positionY = (int) event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			if (state == 0) {
			} else if (state == 1) {
				if (hasHead && !isHeadLoading) {
					isHeadLoading = true;
					headView.onLoading();
					listener.onTopAction();
				}
			} else if (state == 2) {
				if (hasFoot && !isFootLoading) {
					isFootLoading = true;
					footView.onLoading();
					listener.onBottomAction();
				}
			}
			state = 0;
			break;
		case MotionEvent.ACTION_DOWN:
			state = 0;
			downPositionY = positionY;
			break;
		case MotionEvent.ACTION_MOVE:
			if (state != 2 && positionY > downPositionY + 50) {
				state = 1;
				if (hasHead && !isHeadLoading) {
					// SwitchLogger.e("MyLog", "headView.beforeLoading()");
					headView.beforeLoading();
					if (footView != null) {
						// SwitchLogger.e("MyLog", "footView.afterLoading()");
						footView.afterLoading();
					}
				}
				// SwitchLogger.e("MyLog", "向下 state = " + state);
			} else if (state != 1 && positionY < downPositionY - 50) {
				state = 2;
				if (hasFoot && !isFootLoading) {
					footView.beforeLoading();
				}
				// SwitchLogger.e("MyLog", "向上 state = " + state);
			} else {
				// SwitchLogger.e("MyLog", "不动 state = " + state);
			}
			break;
		}
		return super.onTouchEvent(event);
	}
}
