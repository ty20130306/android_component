package com.vanchu.sample;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.vanchu.libs.common.container.ByteArray;
import com.vanchu.libs.common.ui.Tip;
import com.vanchu.libs.common.util.IdUtil;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.socketClient.SocketClient;
import com.vanchu.libs.socketClient.SocketClientBuffer;
import com.vanchu.test.R;
import com.vanchu.test.R.layout;
import com.vanchu.test.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class SocketClientSampleActivity extends Activity {
	private static final String LOG_TAG		= SocketClientSampleActivity.class.getSimpleName();
	
	private static final byte HEAD		= (byte)0xaa;
	private static final byte TAIL		= (byte)0xff;
	
	private static final byte CMD_LOGIN		= 0x01;
	private static final byte CMD_TALK		= 0x02;
	private static final byte CMD_HEARTBEAT	= (byte)0xff;
	
	private static final byte CMD_RESP_LOGIN	= 0x11;
	private static final byte CMD_RESP_TALK		= 0x12;
	
	private static final int HEARTBEAT_PERIOD	= 5 * 1000; //milliseconds
	
	private SocketClient	_client	= null;
	private String 			_uid	= null;
	
	private Timer		_heartbeatTimer		= null;
	private TimerTask	_heartbeatTimerTask	= null;
	
	private Map<String, String> _msgMap	= new HashMap<String, String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_socket_client_sample);
		
		auto();
	}
	
	private void auto() {
		initClient(null);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stop(null);
	}
	
	private void consumeCorruptData() {
		SwitchLogger.d(LOG_TAG, "begin to consume corrupt data");
		
		SwitchLogger.d(LOG_TAG, "consume corrupt data end");
	}
	
	private void handleRecv() throws Exception {
		SocketClientBuffer recvBuffer	= _client.getRecvBuffer();
		if(recvBuffer.length() < 3) {
			SwitchLogger.d(LOG_TAG, "only recv " + recvBuffer.length() + " bytes, wait" );
			return ;
		}
		
		DataInputStream dis;
		
		ByteArray response	= recvBuffer.peek(3);
		dis	= new DataInputStream(new ByteArrayInputStream(response.array()));
		byte head = dis.readByte();
		if(HEAD != head) {
			SwitchLogger.e(LOG_TAG, "head byte not correct, data corrupt");
			consumeCorruptData();
			return ;
		}
		
		short totalLen	= dis.readShort();
		if(recvBuffer.length() < totalLen) {
			SwitchLogger.d(LOG_TAG, "only recv " + recvBuffer.length() + " bytes, need " + totalLen + ", wait" );
			return ;
		}
		dis.close();
		
		SwitchLogger.d(LOG_TAG, "whole cmd response recved, begin to parse the cmd response" );
		response	= recvBuffer.read((int)totalLen);
		dis	= new DataInputStream(new ByteArrayInputStream(response.array()));
		parseResponse(dis);
		dis.close();
	}
	
	private void parseLoginResponse (DataInputStream dis) throws Exception {
		byte result	= dis.readByte();
		SwitchLogger.d(LOG_TAG, "login cmd response, result = " + result);
	}
	
	private void parseTalkResponse (DataInputStream dis) throws Exception {
		byte msgIdLen	= dis.readByte();
		byte[] msgIdByte	= new byte[msgIdLen];
		dis.read(msgIdByte);
		String msgId	= new String(msgIdByte);
		
		SwitchLogger.d(LOG_TAG, "talk cmd response, msgIdLen="+msgIdLen+",msgId="+msgId);
	}
	
	private void initHeartbeat() {
		_heartbeatTimer	= new Timer();
		_heartbeatTimerTask	= new TimerTask() {
			
			@Override
			public void run() {
				//sendHeartbeat();
			}
		};
		
		_heartbeatTimer.schedule(_heartbeatTimerTask, 0, HEARTBEAT_PERIOD);
	}
	
	private void parseResponse(DataInputStream dis) throws Exception {
		dis.readByte(); // head
		dis.readShort(); // total len
		byte cmdResp	= dis.readByte();
		switch (cmdResp) {
		case CMD_RESP_LOGIN:
			parseLoginResponse(dis);
			initHeartbeat();
			break;
			
		case CMD_RESP_TALK:
			parseTalkResponse(dis);
			break;
		default:
			SwitchLogger.d(LOG_TAG, "unkonwn cmd resp " + cmdResp);
			break;
		}
	}
	
	public void initClient(View v) {
		SwitchLogger.d(LOG_TAG, "initClient clicked");
		String host	= getInput(R.id.socket_client_host);
		String port	= getInput(R.id.socket_client_port);
		_uid		= getInput(R.id.socket_client_uid);
		SwitchLogger.d(LOG_TAG, "host="+host+",port="+port+",uid="+_uid);
		
		_client	= new SocketClient(this, host, Integer.parseInt(port), new SocketClient.Callback() {
			
			@Override
			public void onRecv() {
				SwitchLogger.d(LOG_TAG, "SocketClient.onRecv");
				try {
					handleRecv();
				} catch (Exception e) {
					SwitchLogger.e(e);
				}
			}
			
			@Override
			public void onError(int reason) {
				SwitchLogger.e(LOG_TAG, "SocketClient.onError, reason="+reason);
				switch (reason) {
				case SocketClient.ERR_CONNECT_FAIL:
					SwitchLogger.e(LOG_TAG, "socket client connect fail");
					break;
				default:
					SwitchLogger.e(LOG_TAG, "unknown error");
					break;
				}
			}
			
			@Override
			public void onStopped() {
				SwitchLogger.d(LOG_TAG, "SocketClient.onStopped");
			}
			
			@Override
			public void onConnected() {
				SwitchLogger.d(LOG_TAG, "SocketClient.onConnected");
				login();
			}
		});
	}
	
	public void checkConnect(View v) {
		if(_client.isConnected()) {
			SwitchLogger.d(LOG_TAG, "socket client is connected");
		} else {
			SwitchLogger.d(LOG_TAG, "socket client is not connected yet");
		}
	}
	
	public void restart(View v) {
		SwitchLogger.d(LOG_TAG, "restart clicked");
		
		_client.restart();
	}
	
	public void start(View v) {
		SwitchLogger.d(LOG_TAG, "start clicked");
		
		_client.start();
	}
	
	public void stop(View v) {
		SwitchLogger.d(LOG_TAG, "stop clicked");
		
		if(null != _client) {
			_client.stop();
		}
		
		if(null != _heartbeatTimer) {
			_heartbeatTimer.cancel();
		}
		
		if(null != _heartbeatTimerTask) {
			_heartbeatTimerTask.cancel();
		}
	}
	
	private void login() {
		SwitchLogger.d(LOG_TAG, "server connected, begin to login");
		try {
			ByteArray data	= new ByteArray();
			data.writeByte(HEAD);//head
			data.writeShort((short)0); //total len, occupy first
			data.writeByte(CMD_LOGIN); // cmd
			
			data.writeByte((byte)_uid.getBytes("UTF-8").length); // uid length
			data.write(_uid.getBytes("UTF-8")); // uid
			
			data.writeByte(TAIL);//tail
			data.writeShort(1, (short)data.length());//total len
			
			SwitchLogger.d(LOG_TAG, "login cmd data prepared, begin to send");
			_client.send(data);
		} catch (Exception e) {
			SwitchLogger.e(e);
		}
	}
	
	public void send(View v) {
		SwitchLogger.d(LOG_TAG, "send clicked");
		try {
			if(null == _client) {
				String logMsg	= "not connected, connect first";
				Tip.show(this, logMsg);
				SwitchLogger.e(LOG_TAG, logMsg);
				return ;
			}
			
			String msg	= getInput(R.id.socket_client_msg);
			SwitchLogger.d(LOG_TAG, "input msg="+msg);
			sendMsg(msg, "bbb");
		} catch (Exception e) {
			SwitchLogger.e(e);
		}
	}
	
	private void sendMsg(String msg, String toUid) throws Exception {
		ByteArray data	= new ByteArray();
		data.writeByte(HEAD);//head
		data.writeShort((short)0); //total len, occupy first
		data.writeByte(CMD_TALK); // cmd
		
		data.writeByte((byte)toUid.getBytes("UTF-8").length); // uid length
		String msgId	= IdUtil.getUUID();
		data.writeByte((byte)msgId.getBytes("UTF-8").length); // msg id length
		data.writeShort((short)msg.getBytes("UTF-8").length ); // msg length
		
		long millis	= System.currentTimeMillis();
		data.writeInt((int)(millis/1000)); // time
		
		data.write(toUid.getBytes("UTF-8")); // to uid
		data.write(msgId.getBytes("UTF-8")); // msg id
		data.write(msg.getBytes("UTF-8")); // msg
		
		data.writeByte(TAIL);//tail
		
		data.writeShort(1, (short)data.length());
		
		SwitchLogger.d(LOG_TAG, _uid +" send to " + toUid + ", msgId="+msgId+",msg="+msg);
		_msgMap.put(msgId, msg);
		
		_client.send(data);
	}
	
	private String getInput(int id) {
		EditText et	= (EditText)findViewById(id);
		return et.getText().toString();
	}
	

	
}
