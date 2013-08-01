package com.vanchu.libs.common.util;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

public class NetUtil {
	private static final String LOG_TAG = NetUtil.class.getSimpleName();
	
	private static final int HTTP_CONNECTION_TIMEOUT	= 10000; // millisecond
	private static final int HTTP_SO_TIMEOUT			= 10000; // millisecond
	
	private static final int HTTP_RETRY_MAX				= 3;
	
	public static boolean isConnected(Context context){
		ConnectivityManager connMgr	= (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(null != connMgr){
			NetworkInfo networkInfo	= connMgr.getActiveNetworkInfo();
			return (null != networkInfo && networkInfo.isConnected());
		}
		
		return false;
	}
	
	private static DefaultHttpClient createHttpClient(){
		BasicHttpParams httpParams		= new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, HTTP_CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, HTTP_SO_TIMEOUT);
		DefaultHttpClient httpClient	= new DefaultHttpClient(httpParams);
		
		return httpClient;
	}
	
	private static int getRetryMax(int retry){
		if(retry <= 0){
			return 0;
		} else if(retry >= HTTP_RETRY_MAX){
			return HTTP_RETRY_MAX;
		} else {
			return retry;
		}
	}
	
	public static String httpPostRequest(String url, Map<String, String> params, int retry){
		int retryCnt	= 0;
		int retryMax	= getRetryMax(retry);
		String response	= null;
		
		List<BasicNameValuePair> listParams	= new ArrayList<BasicNameValuePair>();
		if(params == null){
			listParams	= null;
		} else {
			Iterator<Entry<String, String>> iterator	= params.entrySet().iterator();
			while(iterator.hasNext()){
				Entry<String, String> entry	= iterator.next();
				listParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
		}
		
		do{
			SwitchLogger.d(LOG_TAG, "retry cnt = " + retryCnt);
			try {
				DefaultHttpClient httpClient	= createHttpClient();
				HttpPost httpPost	= new HttpPost(url);
				httpPost.addHeader("Accept-Encoding", "gzip");
				if(listParams != null){
					httpPost.setEntity(new UrlEncodedFormEntity(listParams, HTTP.UTF_8));
					SwitchLogger.d(LOG_TAG, "post " + url + "?" + StringUtil.inputStreamToString(httpPost.getEntity().getContent()));
				} else {
					SwitchLogger.d(LOG_TAG, "post " + url);
				}
				
				HttpResponse httpResponse	= httpClient.execute(httpPost);
				int statusCode	= httpResponse.getStatusLine().getStatusCode();
				if(statusCode == HttpURLConnection.HTTP_OK){
					InputStream inputStream = httpResponse.getEntity().getContent();
					Header contentEncoding = httpResponse.getFirstHeader("Content-Encoding");
					if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
						SwitchLogger.d(LOG_TAG, "contentEncoding==gzip");
						inputStream = new GZIPInputStream(inputStream);
					}
					response = StringUtil.inputStreamToString(inputStream);
					inputStream.close();
					break;
				} else {
					++retryCnt;
				}
			} catch(Exception e){
				SwitchLogger.e(e);
				++retryCnt;
			}
			
			SwitchLogger.d(LOG_TAG, "httpPostRequest retry, retryCnt=" + retryCnt + ", retryMax=" + retryMax);

		} while(retryCnt <= retryMax);
		
		return response;
	}
	
	public static String httpGetRequest(String url, Map<String, String> params, int retry){
		int retryCnt	= 0;
		int retryMax	= getRetryMax(retry);
		String response	= null;
		
		do {
			StringBuilder urlBuilder	= new StringBuilder();
			urlBuilder.append(url);
			try {
				if(null != params){
					urlBuilder.append("?");
					Iterator<Entry<String, String>> iterator	= params.entrySet().iterator();
					while(iterator.hasNext()){
						Entry<String, String> entry	= iterator.next();
						urlBuilder.append(URLEncoder.encode(entry.getKey(), HTTP.UTF_8))
									.append("=")
									.append(URLEncoder.encode(entry.getValue(), HTTP.UTF_8));
						
						if(iterator.hasNext()){
							urlBuilder.append("&");
						}
					}
				}
			
				DefaultHttpClient httpClient	= createHttpClient();
				HttpGet httpGet	= new HttpGet(urlBuilder.toString());
				httpGet.addHeader("Accept-Encoding", "gzip");
				
				SwitchLogger.d(LOG_TAG, "get " + urlBuilder.toString());
				
				HttpResponse httpResponse	= httpClient.execute(httpGet);
				
				int statusCode	= httpResponse.getStatusLine().getStatusCode();
				if(statusCode == HttpURLConnection.HTTP_OK){
					InputStream inputStream = httpResponse.getEntity().getContent();
					Header contentEncoding = httpResponse.getFirstHeader("Content-Encoding");
					if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
						SwitchLogger.d(LOG_TAG, "contentEncoding==gzip");
						inputStream = new GZIPInputStream(inputStream);
					}
					response = StringUtil.inputStreamToString(inputStream);
					inputStream.close();
					break;
				} else {
					++retryCnt;
				}
			} catch(Exception e){
				SwitchLogger.e(e);
				++retryCnt;
			}
			SwitchLogger.d(LOG_TAG, "httpGetRequest retry, retryCnt=" + retryCnt + ", retryMax=" + retryMax);
		} while(retryCnt <= retryMax);
		
		return response;
	}
	
	public static void openUrl(Context context, String url) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addCategory(Intent.CATEGORY_BROWSABLE);
		intent.setData(Uri.parse(url));
		context.startActivity(intent);
	}
}
