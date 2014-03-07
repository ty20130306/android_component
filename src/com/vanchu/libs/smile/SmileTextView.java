package com.vanchu.libs.smile;

import com.vanchu.libs.common.util.SwitchLogger;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.QuoteSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class SmileTextView extends TextView {

	private SmileParser parser = null;

	public SmileTextView(Context context) {
		super(context);
		this.context = context;
	}

	public SmileTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public SmileTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}

	public void bindSmileParser(SmileParser smileParser) {
		parser = smileParser;
	}

	public void showSmile() {
		String msg = getText() + "";
		setText(parser.translate(msg));
	}

	public void appendHtmlString(String htmlString) {
		append(Html.fromHtml(htmlString));
	}

	public void appendSmileString(String smileString) {
		append(parser.translate(smileString));
	}

	// 点击事件截断

	/** color传-1表示默认颜色 */
	// public void appendClickHtmlText(String text, String data, int textColor,
	// int backColor, Callback callback) {
	// this.callback = callback;
	// String htmlString = "<a color= '#0a8cd2' href='" + data + "'>" + text
	// + "</a>";
	// // String htmlString = "<a color= '#0a8cd2' href='" + data
	// // + "'><FONT color = #FFFFFF style= 'BACKGROUND-COLOR:#0000FF'>"
	// // + text + "</FONT></a>";
	// Spannable textWithLinkText = (Spannable) Html.fromHtml(htmlString);
	// int end = textWithLinkText.length();
	// URLSpan[] urls = textWithLinkText.getSpans(0, end, URLSpan.class);
	//
	// // 使用textWithLinkText创建一个SpannableStringBuilder，通过clearSpans()方法清除原有的Span
	// SpannableStringBuilder style = new SpannableStringBuilder(
	// textWithLinkText);
	// style.clearSpans();
	//
	// // 重新设置textWithLinkText中的URLSpan
	// for (URLSpan url : urls) {
	// MyURLSpan myURLSpan = new MyURLSpan(url.getURL());
	// style.setSpan(myURLSpan, textWithLinkText.getSpanStart(url),
	// textWithLinkText.getSpanEnd(url),
	// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	// // 设置前景色
	// if (textColor != -1) {
	// style.setSpan(new ForegroundColorSpan(textColor),
	// textWithLinkText.getSpanStart(url),
	// textWithLinkText.getSpanEnd(url),
	// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	// }
	// // 设置背景色
	// if (backColor != -1) {
	// style.setSpan(new BackgroundColorSpan(backColor),
	// textWithLinkText.getSpanStart(url),
	// textWithLinkText.getSpanEnd(url),
	// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	// }
	// }
	//
	// // 将重新设置Span的文本设置为forwardToXiaoMiLogin的显示文字
	// setText(style);
	//
	// // 通过setMovementMethod设置LinkMovementMethod类型来使LinkText有效
	// setMovementMethod(LinkMovementMethod.getInstance());
	//
	// }
	//
	// private class MyURLSpan extends URLSpan {
	//
	// private String data;
	//
	// public MyURLSpan(String url) {
	// super(url);
	// this.data = url;
	// }
	//
	// @Override
	// public void onClick(View widget) {
	// // super.onClick(widget);
	// if (callback != null) {
	// callback.onClick(data);
	// }
	// }
	//
	// }

	// 粗略做的html点击以后会修整
	// 目前只支持起点在左上角，长度最多2行
	private int end = 0;
	private Callback callback;
	private Context context;
	private Paint paint;
	private Rect rect;
	private Rect rect2;
	private int textSize;
	private boolean isTouchDownInRect = false;

	public void setTextClickListener(int end, Callback callback) {
		this.end = end;
		this.callback = callback;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (end == 0) {
			return super.onTouchEvent(event);
		}
		if (paint == null) {
			String s = getText() + "";
			if (end > s.length()) {
				SwitchLogger.e("MyLog", "!!!!!!!!!!!!!!!!!!!!!!!!!!!\n"
						+ "end = " + end + " , getText() = [" + s + "]");
				end = s.length();
			}
			paint = getPaint();
			rect = new Rect();
			paint.getTextBounds(s, 0, end, rect);
			textSize = px2sp(context, paint.getTextSize());
			if (rect.width() > getWidth()) {
				rect2 = new Rect(rect.left, rect.top + rect.height(),
						rect.right - getWidth() - rect.left + 10, rect.bottom
								+ rect.height());
			}
		}
		float x = event.getX();
		float y = event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// SwitchLogger.e("MyLog", "点击坐标：" + (int) event.getX() + " , "
			// + (int) event.getY());
			if (isInRect(x, y, rect, textSize)
					|| isInRect(x, y, rect2, textSize)) {
				isTouchDownInRect = true;
				// Log.i("MyLog", "listener.ACTION_DOWN");
			} else {
				isTouchDownInRect = false;
			}
			break;
		case MotionEvent.ACTION_UP:
			// SwitchLogger.e("MyLog", "离开坐标：" + (int) event.getX() + " , "
			// + (int) event.getY());
			if (isTouchDownInRect
					&& (isInRect(x, y, rect, textSize) || isInRect(x, y, rect2,
							textSize))) {
				// Log.i("MyLog", "listener.ACTION_UP");
				if (callback != null) {
					callback.onClick("OK");
				}
			}
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		default:
			break;
		}
		if (isTouchDownInRect) {
			return true;
		}
		return super.onTouchEvent(event);
	}

	private boolean isInRect(float x, float y, Rect rect, int textSize) {
		int margin = sp2px(context, 10);
		if (rect == null) {
			return false;
		}
		Rect rect2 = new Rect(rect.left, rect.top + textSize, rect.right
				+ margin * 2, rect.bottom + textSize + margin * 2);
		// SwitchLogger.e("MyLog",
		// " textSize = " + textSize + " margin = " + margin + " width = "
		// + rect2.width() + " , height = " + rect2.height()
		// + "\nrect.left = " + rect2.left + " , rect.right = "
		// + rect2.right + " , rect.top = " + rect2.top
		// + " , rect.bottom = " + rect2.bottom);
		if (x > rect2.left && x < rect2.right && y > rect2.top
				&& y < rect2.bottom) {
			return true;
		} else {
			return false;
		}
	}

	public interface Callback {

		public void onClick(String text);

	}

	public static int px2sp(Context context, float pxValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}

	public static int sp2px(Context context, float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}
}
