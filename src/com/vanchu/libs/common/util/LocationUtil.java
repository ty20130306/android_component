package com.vanchu.libs.common.util;

import android.content.Context;
import android.util.Log;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class LocationUtil {
	private final static String TAG = LocationUtil.class.getSimpleName();
	
	private LocationClient 		mLocationClient = null;
	private BDLocationListener 	myListener = new MyLocationListener();
	
	private Context 	_context;
	private CallBack 	_callBack;
	
	public LocationUtil(String key , Context context) {
		this._context = context;
		mLocationClient = new LocationClient(_context);    
		mLocationClient.setAK(key);
	    mLocationClient.registerLocationListener( myListener );    
	}
	
	
	public interface CallBack{
		public void onSuccess(BDLocation location);
		public void onFail();
	}
	
	public void  getLocation(CallBack callBack){
		this._callBack = callBack;
		
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setPriority(LocationClientOption.NetWorkFirst); 
		option.setServiceName("com.baidu.location.service_v2.9");
		option.setAddrType("all");//返回的定位结果包含地址
		option.setCoorType("bd09ll");//返回的定位结果是百度经纬�?默认值gcj02
		option.setScanSpan(5000);//设置发起定位请求的间隔时间为5000ms
		option.setPriority(LocationClientOption.NetWorkFirst);
		option.disableCache(true);//禁止启用缓存定位
		option.setPoiNumber(10);
		mLocationClient.setLocOption(option);
		
		mLocationClient.start();
		
	}
	
	
	public class MyLocationListener implements BDLocationListener {
	    @Override
	   public void onReceiveLocation(BDLocation location) {
	    	Log.d(TAG, "location:"+location);
	      if (location == null)
	          return ;
	      StringBuffer sb = new StringBuffer(256);
	      sb.append("time : ");
	      sb.append(location.getTime());
	      sb.append("\nerror code : ");
	      sb.append(location.getLocType());
	      sb.append("\nlatitude : ");
	      sb.append(location.getLatitude());
	      sb.append("\nlontitude : ");
	      sb.append(location.getLongitude());
	      sb.append("\nradius : ");
	      sb.append(location.getRadius());
	      if (location.getLocType() == BDLocation.TypeGpsLocation){
	           sb.append("\nspeed : ");
	           sb.append(location.getSpeed());
	           sb.append("\nsatellite : ");
	           sb.append(location.getSatelliteNumber());
	           } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){	
	           sb.append("\naddr : ");
	           sb.append(location.getAddrStr());
	           mLocationClient.stop();
	        } 
	      _callBack.onSuccess(location);
	      
	      Log.d(TAG, sb.toString());
	    }
	public void onReceivePoi(BDLocation poiLocation) {
	         if (poiLocation == null){
	                return ;
	          }
	         StringBuffer sb = new StringBuffer(256);
	          sb.append("Poi time : ");
	          sb.append(poiLocation.getTime());
	          sb.append("\nerror code : ");
	          sb.append(poiLocation.getLocType());
	          sb.append("\nlatitude : ");
	          sb.append(poiLocation.getLatitude());
	          sb.append("\nlontitude : ");
	          sb.append(poiLocation.getLongitude());
	          sb.append("\nradius : ");
	          sb.append(poiLocation.getRadius());
	          if (poiLocation.getLocType() == BDLocation.TypeNetWorkLocation){
	              sb.append("\naddr : ");
	              sb.append(poiLocation.getAddrStr());
	         } 
	          if(poiLocation.hasPoi()){
	               sb.append("\nPoi:");
	               sb.append(poiLocation.getPoi());
	         }else{             
	               sb.append("noPoi information");
	          }
	         Log.d(TAG, sb.toString());
	        }
	}

}