package com.vanchu.libs.talkClient;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;

import com.vanchu.libs.common.container.ByteArray;
import com.vanchu.libs.common.util.IdUtil;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.common.util.UnsignedUtil;
import com.vanchu.libs.vasClient.VasClient;

public class TalkClient {

	private static final String LOG_TAG		= TalkClient.class.getSimpleName();
	
	public static final int ERR_UNKNOWN				= -1;
	public static final int ERR_CONNECT_FAIL		= 1;
	public static final int ERR_LOGIN_FAIL			= 2;
	
	public static final byte MSG_TYPE_AUTO			= 0x40;
	public static final byte MSG_TYPE_COMMON		= 0x00;
	
	private static final byte CMD_LOGIN		= 0x01;
	private static final byte CMD_TALK		= 0x02;
	private static final byte CMD_HEARTBEAT	= (byte)0xff;
	
	private static final byte CMD_RESP_LOGIN	= 0x11;
	private static final byte CMD_RESP_TALK		= 0x12;
	
	private static final int HEARTBEAT_PERIOD		= 30 * 1000; //milliseconds
	private static final int FIRST_HEARTBEAT_PERIOD	= 5 * 1000; //milliseconds
	
	private boolean	_isLogon		= false;
	
	private Timer		_heartbeatTimer		= null;
	private TimerTask	_heartbeatTimerTask	= null;
	
	private Context	_context;
	private String	_host;
	private int		_port;
	private String	_uid;
	private String	_auth;
	
	private Callback	_callback;
	
	private VasClient	_vasClient;
	
	public TalkClient(Context context, String host, int port, String uid, String auth, Callback callback) {
		_context	= context;
		_host	= host;
		_port	= port;
		_uid	= uid;
		_auth	= auth;
		_callback	= callback;
		
		initVasClient();
	}	
	private void prepare() {
		_isLogon	= false;
		startHeartbeat();
	}
	
	private void startHeartbeat() {
		cancelHeartbeat();
		
		_heartbeatTimer	= new Timer();
		_heartbeatTimerTask	= new TimerTask() {
			
			@Override
			public void run() {
				SwitchLogger.d(LOG_TAG, "send heart beat---------------" );
				if( ! _vasClient.isConnected()) {
					SwitchLogger.d(LOG_TAG, "send heart beat fail, vas client not connected" );
					return ;
				}
				ByteArray heartbeatCmd	= new ByteArray(1);
				heartbeatCmd.writeByte(CMD_HEARTBEAT);
				_vasClient.send(heartbeatCmd);
			}
		};
		
		_heartbeatTimer.schedule(_heartbeatTimerTask, FIRST_HEARTBEAT_PERIOD, HEARTBEAT_PERIOD);
	}
	
	public boolean start(){
		prepare();
		return _vasClient.start();
	}
	
	public boolean restart() {
		prepare();
		return _vasClient.restart();
	}
	
	public boolean isLogon() {
		return (_vasClient.isConnected() && _isLogon);
	}
	
	private void cancelHeartbeat() {
		if(null != _heartbeatTimer) {
			_heartbeatTimer.cancel();
			_heartbeatTimer	= null;
		}
		
		if(null != _heartbeatTimerTask) {
			_heartbeatTimerTask.cancel();
			_heartbeatTimerTask	= null;
		}
	}
	
	private void cleanUp() {
		_isLogon	= false;
		cancelHeartbeat();
	}
	
	public void stop() {
		cleanUp();
		_vasClient.stop();
	}
	
	public String getUid() {
		return _uid;
	}
	
	public String talk(String toUid, String msg) {
		String msgId	= IdUtil.getUUID();
		return talk(toUid, msg, msgId);
	}
	
	public String talk(String toUid, String msg, String msgId) {
		try {
			ByteArray talkCmd	= new ByteArray();
			talkCmd.writeByte(CMD_TALK); // cmd
			
			talkCmd.writeByte((byte)toUid.getBytes("UTF-8").length); // uid length
			talkCmd.writeByte((byte)msgId.getBytes("UTF-8").length); // msg id length
			talkCmd.writeShort((short)msg.getBytes("UTF-8").length ); // msg length
			talkCmd.writeByte(MSG_TYPE_COMMON);
			
			long millis	= System.currentTimeMillis();
			talkCmd.writeInt((int)(millis/1000)); // timestamp
			
			talkCmd.write(toUid.getBytes("UTF-8")); // to uid
			talkCmd.write(msgId.getBytes("UTF-8")); // msg id
			talkCmd.write(msg.getBytes("UTF-8")); // msg
			
			SwitchLogger.d(LOG_TAG, _uid +" send msg to " + toUid + ", msgId="+msgId+",msg="+msg);
			
			_vasClient.send(talkCmd);
			
			return msgId;
		}catch (Exception e) {
			SwitchLogger.e(e);
			_callback.onError(ERR_UNKNOWN);
			stop();
			
			return null;
		}
	}
	
	private void login() {
		try {
			ByteArray loginCmd	= new ByteArray();
			loginCmd.writeByte(CMD_LOGIN); // cmd
			loginCmd.writeByte((byte)_auth.getBytes("UTF-8").length); // uid length
			loginCmd.write(_auth.getBytes("UTF-8")); // uid
			_vasClient.send(loginCmd);
		} catch (Exception e) {
			SwitchLogger.e(e);
			_callback.onError(ERR_UNKNOWN);
			stop();
		}
	}
	
	private void handleLoginResp(DataInputStream dis) throws Exception {
		byte result	= dis.readByte();
		if(0 == result) {
			SwitchLogger.d(LOG_TAG, "login succ, result = " + result);
			_isLogon	= true;
			_callback.onLogon();
		} else {
			SwitchLogger.e(LOG_TAG, "login fail, result = " + result);
			_callback.onError(ERR_LOGIN_FAIL);
			stop();
		}
	}
	
	private void handleTalkResp(DataInputStream dis) throws Exception {
		byte msgIdLen		= dis.readByte();
		byte[] msgIdByte	= new byte[msgIdLen];
		dis.read(msgIdByte);
		String msgId		= new String(msgIdByte);
		
		SwitchLogger.d(LOG_TAG, "talk cmd response, msgIdLen="+msgIdLen+",msgId="+msgId);
		_callback.onTalkSucc(msgId);
	}
	
	private void handleRecvMsg(DataInputStream dis) throws Exception {
		byte fromUidLen	= dis.readByte();
		byte msgIdLen	= dis.readByte();
		short msgLen	= dis.readShort();
		byte msgType	= dis.readByte();
		long timestamp	= UnsignedUtil.toUnsignedInt(dis.readInt()) * 1000;
		
		byte[] fromUidByte	= new byte[fromUidLen];
		dis.read(fromUidByte);
		String fromUid	= new String(fromUidByte);
		
		byte[] msgIdByte	= new byte[msgIdLen];
		dis.read(msgIdByte);
		String msgId	= new String(msgIdByte);
		
		byte[] msgByte	= new byte[msgLen];
		dis.read(msgByte);
		String msg	= new String(msgByte);
		SwitchLogger.d(LOG_TAG, "recv msg, fromUid="+fromUid+",msgId="+msgId+",msg="+msg
				+",msgType="+msgType+",fromUidLen="+fromUidLen+",msgIdLen="+msgIdLen+",msgLen="+msgLen);
		
		_callback.onRecvMsg(msgId, msgType, fromUid, msg, timestamp);
		sendTalkResp(msgId);
	}
	
	private void sendTalkResp(String msgId) throws Exception {
		SwitchLogger.d(LOG_TAG, "send talk resp cmd back" );
		byte[] msgIdByte	= msgId.getBytes("UTF-8");
		ByteArray talkRespCmd	= new ByteArray(VasClient.BYTE_NUM_CMD + 1 + msgIdByte.length);
		talkRespCmd.writeByte(CMD_RESP_TALK);
		
		talkRespCmd.writeByte((byte)msgIdByte.length);
		talkRespCmd.write(msgIdByte);
		_vasClient.send(talkRespCmd);
	}
	
	private void handleRecvVasPacket(ByteArray recvVasPacket) throws Exception {
		DataInputStream dis	= new DataInputStream(new ByteArrayInputStream(recvVasPacket.array()));
		byte cmd	= dis.readByte();
		switch (cmd) {
		case CMD_RESP_LOGIN:
			handleLoginResp(dis);
			break;
			
		case CMD_TALK:
			handleRecvMsg(dis);
			break;
			
		case CMD_RESP_TALK:
			handleTalkResp(dis);
			break;
			
		default:
			break;
		}
		dis.close();
	}
	
	private void initVasClient() {
		_vasClient	= new VasClient(_context, _host, _port, new VasClient.Callback() {
			
			@Override
			public void onStopped() {
				cleanUp();
				_callback.onStopped();
			}
			
			@Override
			public void onRecv(ByteArray vasPacket) {
				SwitchLogger.d(LOG_TAG, "VasClient.onRecv");
				try {
					handleRecvVasPacket(vasPacket);
				} catch (Exception e) {
					SwitchLogger.e(e);
					_callback.onError(ERR_UNKNOWN);
					stop();
				}
			}
			
			@Override
			public void onError(int reason) {
				SwitchLogger.e(LOG_TAG, "VasClient.onError, reason="+reason);
				cleanUp();
				switch (reason) {
				case VasClient.ERR_CONNECT_FAIL:
					_callback.onError(ERR_CONNECT_FAIL);
					break;
				default:
					SwitchLogger.e(LOG_TAG, "unknown error");
					_callback.onError(ERR_UNKNOWN);
					break;
				}
			}
			
			@Override
			public void onConnected() {
				SwitchLogger.d(LOG_TAG, "VasClient is connected, begin to login");
				login();
			}
		});
	}
	
	public interface Callback {
		public void onLogon();
		public void onStopped();
		public void onError(int reason);
		public void onTalkSucc(String msgId);
		public void onRecvMsg(String msgId, byte msgType, String fromUid, String msg, long timestamp);
	}
}
