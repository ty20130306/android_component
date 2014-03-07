package com.vanchu.libs.smile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;

public class SmileParser {

	private Context context;
	private Pattern pattern;
	private List<SmileItem> list;

	public SmileParser(Context context, int configXmlSourse, String sourseFold,
			String packName) {
		list = new ArrayList<SmileItem>();
		XmlResourceParser xrp = null;
		xrp = context.getResources().getXml(configXmlSourse);
		try {
			list = readPackageXML(context, xrp, sourseFold, packName);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.context = context;
		this.pattern = buildPattern(list);
	}

	private ArrayList<SmileItem> readPackageXML(Context context,
			XmlPullParser xrp, String sourseFold, String packName)
			throws XmlPullParserException, IOException {
		ArrayList<SmileItem> list = new ArrayList<SmileItem>();
		while (xrp.getEventType() != XmlPullParser.END_DOCUMENT) {// 只要不是文档结束事件
			switch (xrp.getEventType()) {
			case XmlPullParser.START_DOCUMENT:
				break;
			case XmlPullParser.START_TAG:
				String xrpname = xrp.getName();
				if ("item".equals(xrpname)) {
					String key = xrp.getAttributeValue(0);
					String picName = xrp.getAttributeValue(1);
					int sourseId = context.getResources().getIdentifier(
							picName, sourseFold, packName);
					if (sourseId == 0) {
						throw new IllegalStateException(
								"Can't find the resourse named \""
										+ picName
										+ "\",make sure you have set the right fold and package");
					}
					list.add(new SmileItem(key, sourseId));
				}
				break;
			case XmlPullParser.END_TAG:
				break;
			}
			xrp.next();
		}
		return list;
	}

	// build pattern
	private Pattern buildPattern(List<SmileItem> list) {
		StringBuilder patternString = new StringBuilder(list.size() * 3);
		patternString.append('(');
		for (SmileItem item : list) {
			patternString.append(Pattern.quote(item.getFaceName()));
			patternString.append('|');
		}
		patternString.replace(patternString.length() - 1,
				patternString.length(), ")");
		return Pattern.compile(patternString.toString());
	}

	// get key
	public String getKey(int position) {
		if (position >= 0 && position < list.size()) {
			return list.get(position).getFaceName();
		} else {
			return "";
		}
	}

	// translate
	public CharSequence translate(CharSequence text) {
		SpannableStringBuilder builder = new SpannableStringBuilder(text);
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			int resId = list.get(0).getSourseId();
			for (SmileItem item : list) {
				if (matcher.group().equals(item.getFaceName())) {
					resId = item.getSourseId();
					break;
				}
			}
			builder.setSpan(new ImageSpan(context, resId), matcher.start(),
					matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return builder;
	}

	public List<SmileItem> getList() {
		return list;
	}

}
