package com.vanchu.test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.vanchu.libs.common.container.ByteArray;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.socketClient.SocketClientBuffer;
import com.vanchu.sample.SocketClientSampleActivity;
import com.vanchu.sample.TalkClientSampleActivity;
import com.vanchu.sample.VasClientSampleActivity;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class AnotherTestActivity extends Activity {
	
	private static final String LOG_TAG		= AnotherTestActivity.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_another_test);
	}
	
	public void testUCE(View v) {
		String a	= null;
		SwitchLogger.d(LOG_TAG, a.toLowerCase());
	}
	
	public void testTalkClient(View v) {
		gotoActivity(TalkClientSampleActivity.class);
	}
	
	public void testVasClient(View v) {
		gotoActivity(VasClientSampleActivity.class);
	}
	
	public void testSocketClient(View v) {
		gotoActivity(SocketClientSampleActivity.class);
	}
	
	public void testSocketClientBuffer(View view) {
		SocketClientBuffer scb	= new SocketClientBuffer();
		scb.setDebug(true);
		
		SwitchLogger.d(LOG_TAG, "scrb.length()="+scb.length());
		ByteArray ba	= new ByteArray(1);
		//ba.setDebug(true);
		ba.writeByte((byte)8);
		ba.writeByte((byte)4);
		ba.writeByte((byte)2);
		ba.writeByte((byte)1);
		scb.write(ba);
		
		SwitchLogger.d(LOG_TAG, "after write byte, scrb.length()="+scb.length());

		ByteArray out	= scb.peek();
		byte[]	data	= out.array();
		DataInputStream dis	= new DataInputStream(new ByteArrayInputStream(data));
		try {
			SwitchLogger.d(LOG_TAG, dis.readByte()+","+dis.readByte()+","+dis.readByte()+","+dis.readByte());
		} catch(IOException e) {
			SwitchLogger.e(e);
		}
		
		SwitchLogger.d(LOG_TAG, "after peek byte, scrb.length()="+scb.length());
		
		out	= scb.read(out.length());
		data	= out.array();
		dis	= new DataInputStream(new ByteArrayInputStream(data));
		try {
			SwitchLogger.d(LOG_TAG, dis.readByte()+","+dis.readByte()+","+dis.readByte()+","+dis.readByte());
		} catch(IOException e) {
			SwitchLogger.e(e);
		}
		
		SwitchLogger.d(LOG_TAG, "after read byte, scrb.length()="+scb.length());
		
		ba.writeInt(77777777);
		ba.writeShort((short)888);
//		ba.writeByte((byte)3);
//		ba.writeByte((byte)7);
//		ba.writeByte((byte)8);
//		ba.writeByte((byte)1);
		
		scb.write(ba);
		SwitchLogger.d(LOG_TAG, "after write int and byte, scrb.length()="+scb.length());
		
		out	= scb.peek(4);
		data	= out.array();
		dis	= new DataInputStream(new ByteArrayInputStream(data));
		try {
			SwitchLogger.d(LOG_TAG, dis.readByte()+","+dis.readByte()+","+dis.readByte()+","+dis.readByte());
					//+","+dis.readInt()+","+dis.readShort());
					//+","+dis.readByte()+","+dis.readByte()+","+dis.readByte()+","+dis.readByte());
		} catch(IOException e) {
			SwitchLogger.e(e);
		}
		
		out	= scb.peek();
		data	= out.array();
		dis	= new DataInputStream(new ByteArrayInputStream(data));
		try {
			SwitchLogger.d(LOG_TAG, dis.readByte()+","+dis.readByte()+","+dis.readByte()+","+dis.readByte()+","
					+dis.readInt()+","+dis.readShort());
					//+","+dis.readByte()+","+dis.readByte()+","+dis.readByte()+","+dis.readByte());
		} catch(IOException e) {
			SwitchLogger.e(e);
		}
		
		SwitchLogger.d(LOG_TAG, "after peek int and byte, scrb.length()="+scb.length());
		
		out	= scb.read(out.length());
		data	= out.array();
		dis	= new DataInputStream(new ByteArrayInputStream(data));
		try {
			SwitchLogger.d(LOG_TAG, dis.readByte()+","+dis.readByte()+","+dis.readByte()+","+dis.readByte()
					+","+dis.readInt()+","+dis.readShort());
					//+","+dis.readByte()+","+dis.readByte()+","+dis.readByte()+","+dis.readByte());
		} catch(IOException e) {
			SwitchLogger.e(e);
		}
		
//		out	= scb.read(6);
//		data	= out.array();
//		dis	= new DataInputStream(new ByteArrayInputStream(data));
//		try {
//			SwitchLogger.d(LOG_TAG, //dis.readByte()+","+dis.readByte()+","+dis.readByte()+","+dis.readByte()+","
//					dis.readInt()+","+dis.readShort());
//					//+","+dis.readByte()+","+dis.readByte()+","+dis.readByte()+","+dis.readByte());
//		} catch(IOException e) {
//			SwitchLogger.e(e);
//		}
		
		SwitchLogger.d(LOG_TAG, "after read int and byte, scrb.length()="+scb.length());
		
//		try {
//			String str	= "测试测试中文字符";
//			ba.writeShort((short)str.getBytes("UTF-8").length);
//			ba.write(str.getBytes("UTF-8"));
//			
//			String str2	= "test ByteArray";
//			ba.writeShort((short)str2.getBytes("UTF-8").length);
//			ba.write(str2.getBytes("UTF-8"));
//			
//			String str3	= "中文test ByteArray";
//			ba.writeShort((short)str3.getBytes("UTF-8").length);
//			ba.write(str3.getBytes("UTF-8"));
//		} catch(UnsupportedEncodingException e) {
//			SwitchLogger.e(e);
//		}
//		
//		scb.write(ba);
//		
//		SwitchLogger.d(LOG_TAG, "after write str, scrb.length()="+scb.length());
//		
//		out	= scb.peek();
//		data	= out.array();
//		dis	= new DataInputStream(new ByteArrayInputStream(data));
//		try {
//			SwitchLogger.d(LOG_TAG, dis.readByte()+","+dis.readByte()+","+dis.readByte()+","+dis.readByte()+","
//					+dis.readInt()+","+dis.readShort()+","
//					+dis.readByte()+","+dis.readByte()+","+dis.readByte()+","+dis.readByte());
//			for(int i = 0; i < 3; ++i) {
//				short len;
//				byte[] strByte;
//				String resultStr;
//				
//				len	= dis.readShort();
//				strByte	= new byte[len];
//				dis.read(strByte);
//				resultStr	= new String(strByte);
//				SwitchLogger.d(LOG_TAG, "resultStr "+i+",len:"+len+",value:"+resultStr);
//			}
//		} catch(IOException e) {
//			SwitchLogger.e(e);
//		}
//		
//		SwitchLogger.d(LOG_TAG, "after peek str, scrb.length()="+scb.length());
//		
//		out	= scb.read(out.length());
//		data	= out.array();
//		dis	= new DataInputStream(new ByteArrayInputStream(data));
//		try {
//			SwitchLogger.d(LOG_TAG, dis.readByte()+","+dis.readByte()+","+dis.readByte()+","+dis.readByte()+","
//					+dis.readInt()+","+dis.readShort()+","
//					+dis.readByte()+","+dis.readByte()+","+dis.readByte()+","+dis.readByte());
//			for(int i = 0; i < 3; ++i) {
//				short len;
//				byte[] strByte;
//				String resultStr;
//				
//				len	= dis.readShort();
//				strByte	= new byte[len];
//				dis.read(strByte);
//				resultStr	= new String(strByte);
//				SwitchLogger.d(LOG_TAG, "resultStr "+i+",len:"+len+",value:"+resultStr);
//			}
//		} catch(IOException e) {
//			SwitchLogger.e(e);
//		}
//		SwitchLogger.d(LOG_TAG, "after read str, scrb.length()="+scb.length());
//		
//		SwitchLogger.d(LOG_TAG, "ba2-----------------------------------------");
//		ByteArray ba2	= new ByteArray(1);
//		ba2.writeByte((byte)8);
//		ba2.writeByte((byte)4);
//		ba2.writeByte((byte)2);
//		ba2.writeByte((byte)1);
//		scb.write(ba2);
//		
//		SwitchLogger.d(LOG_TAG, "after write byte, scrb.length()="+scb.length());
//
//		out	= scb.peek();
//		data	= out.array();
//		dis	= new DataInputStream(new ByteArrayInputStream(data));
//		try {
//			SwitchLogger.d(LOG_TAG, dis.readByte()+","+dis.readByte()+","+dis.readByte()+","+dis.readByte());
//		} catch(IOException e) {
//			SwitchLogger.e(e);
//		}
//		
//		SwitchLogger.d(LOG_TAG, "after peek byte, scrb.length()="+scb.length());
//		
//		out	= scb.read(out.length());
//		data	= out.array();
//		dis	= new DataInputStream(new ByteArrayInputStream(data));
//		try {
//			SwitchLogger.d(LOG_TAG, dis.readByte()+","+dis.readByte()+","+dis.readByte()+","+dis.readByte());
//		} catch(IOException e) {
//			SwitchLogger.e(e);
//		}
//		SwitchLogger.d(LOG_TAG, "after read byte, scrb.length()="+scb.length());
	}
	
	public void testByteArray(View v) {
		ByteArray ba	= new ByteArray(1);
		ba.writeByte((byte)8);
		ba.writeByte((byte)4);
		ba.writeByte((byte)2);
		ba.writeByte((byte)1);
		
//		byte[]	data	= ba.array();
//		SwitchLogger.d(LOG_TAG, "data.length="+data.length);
//		for(int i = 0; i < data.length; ++i) {
//			SwitchLogger.d(LOG_TAG, "data["+i+"]="+data[i]);
//		}
		
		ba.writeInt(77777777);
		ba.writeShort((short)888);

		
		ba.writeByte((byte)3);
		ba.writeByte((byte)7);
		ba.writeByte((byte)8);
		ba.writeByte((byte)1);
		
		try {
			String str	= "测试测试中文字符";
			ba.writeShort((short)str.getBytes("UTF-8").length);
			ba.write(str.getBytes("UTF-8"));
			
			String str2	= "test ByteArray";
			ba.writeShort((short)str2.getBytes("UTF-8").length);
			ba.write(str2.getBytes("UTF-8"));
			
			String str3	= "中文test ByteArray";
			ba.writeShort((short)str3.getBytes("UTF-8").length);
			ba.write(str3.getBytes("UTF-8"));
		} catch(UnsupportedEncodingException e) {
			SwitchLogger.e(e);
		}
		
		byte[]	data	= ba.array();
		DataInputStream dis	= new DataInputStream(new ByteArrayInputStream(data));
		try {
			SwitchLogger.d(LOG_TAG, dis.readByte()+","+dis.readByte()+","+dis.readByte()+","+dis.readByte()+","
								+dis.readInt()+","+dis.readShort()+","
								+dis.readByte()+","+dis.readByte()+","+dis.readByte()+","+dis.readByte());
			
			for(int i = 0; i < 3; ++i) {
				short len;
				byte[] strByte;
				String resultStr;
				
				len	= dis.readShort();
				strByte	= new byte[len];
				dis.read(strByte);
				resultStr	= new String(strByte);
				SwitchLogger.d(LOG_TAG, "resultStr "+i+",len:"+len+",value:"+resultStr);
			}
		} catch(IOException e) {
			SwitchLogger.e(e);
		}
	}
	
	private void gotoActivity(Class<?> cls) {
		Intent intent = new Intent(this, cls);
		startActivity(intent);
	}

}
