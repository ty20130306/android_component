package com.vanchu.libs.vasClient;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import android.content.Context;

import com.vanchu.libs.common.container.ByteArray;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.socketClient.SocketClient;
import com.vanchu.libs.socketClient.SocketClientBuffer;

public class VasClient {
	
	private static final String LOG_TAG		= VasClient.class.getSimpleName();
	
	public static final int ERR_CONNECT_FAIL		= 1;
	
	public static final byte HEAD		= (byte)0xaa;
	
	public static final int BYTE_NUM_HEAD	= 1;
	public static final int BYTE_NUM_LEN	= 2;
	public static final int BYTE_NUM_CMD	= 1;
	
	private Context	_context;
	private String	_host;
	private int		_port;
	private Callback	_callback;
	
	private SocketClient	_socketClient;
	
	public VasClient(Context context, String host, int port, Callback callback) {
		_context	= context;
		_host	= host;
		_port	= port;
		_callback	= callback;
		
		initSocketClient();
	}
	
	private int validateVasPacket() throws Exception {
		SocketClientBuffer recvBuffer	= _socketClient.getRecvBuffer();
		if(recvBuffer.length() < BYTE_NUM_HEAD + BYTE_NUM_LEN) {
			SwitchLogger.d(LOG_TAG, "socket client only recv " + recvBuffer.length() + " bytes, wait" );
			return -1;
		}
		
		ByteArray recvData	= recvBuffer.peek(BYTE_NUM_HEAD + BYTE_NUM_LEN);
		DataInputStream dis	= new DataInputStream(new ByteArrayInputStream(recvData.array()));
		byte head = dis.readByte();
		if(HEAD != head) {
			SwitchLogger.e(LOG_TAG, "head byte not correct, data corrupt, stop socket client");
			_socketClient.stop();
			dis.close();
			return -1;
		}
		
		short totalLen	= dis.readShort();
		if(totalLen < BYTE_NUM_HEAD + BYTE_NUM_LEN + BYTE_NUM_CMD) {
			SwitchLogger.e(LOG_TAG, "packet total len is wrong, totalLen=" + totalLen 
									+ ",data corrupt, stop socket client");
			
			_socketClient.stop();
			dis.close();
			return -1;
		}
		
		if(recvBuffer.length() < totalLen) {
			SwitchLogger.d(LOG_TAG, "only recv " + recvBuffer.length() + " bytes, need " + totalLen + ", wait" );
			dis.close();
			return -1;
		}
		dis.close();
		
		return totalLen;
	}
	
	private boolean handleRecv() throws Exception {
		int totalLen = validateVasPacket();
		if(totalLen < 0) {
			return false;
		}
		
		SwitchLogger.d(LOG_TAG, "vas packet is valid, begin to get the vas cmd and body" );
		SocketClientBuffer recvBuffer	= _socketClient.getRecvBuffer();
		ByteArray recvData	= recvBuffer.read(totalLen);
		DataInputStream dis	= new DataInputStream(new ByteArrayInputStream(recvData.array()));
		dis.readByte(); // head
		dis.readShort(); // total len
		byte cmd	= dis.readByte();
		
		int bodyLen	= totalLen - BYTE_NUM_HEAD - BYTE_NUM_LEN - BYTE_NUM_CMD;
		ByteArray recvVasPacket;
		if(bodyLen <= 0) {
			recvVasPacket	= new ByteArray(1);
		} else {
			byte[] body	= new byte[bodyLen];
			dis.read(body);
			
			recvVasPacket	= new ByteArray(BYTE_NUM_CMD + bodyLen);
			recvVasPacket.writeByte(cmd);
			recvVasPacket.write(body);
		}
		_callback.onRecv(recvVasPacket);
		dis.close();
		
		return true;
	}
	
	private void initSocketClient() {
		_socketClient	= new SocketClient(_context, _host, _port, new SocketClient.Callback() {
			
			@Override
			public void onRecv() {
				try {
					int i = 0;
					while(handleRecv()){
						i++;
					}
					SwitchLogger.d(LOG_TAG, "recv " + i + " msg in one packet");
				} catch (Exception e) {
					SwitchLogger.e(e);
				}
			}
			
			@Override
			public void onError(int reason) {
				SwitchLogger.e(LOG_TAG, "SocketClient.onError, reason="+reason);
				switch (reason) {
				case SocketClient.ERR_CONNECT_FAIL:
					_callback.onError(ERR_CONNECT_FAIL);
					break;
				default:
					SwitchLogger.e(LOG_TAG, "unknown error");
					break;
				}
			}
			
			@Override
			public void onStopped() {
				_callback.onStopped();
			}
			
			@Override
			public void onConnected() {
				_callback.onConnected();
			}
		});
	}
	
	public boolean restart() {
		return _socketClient.restart();
	}
	
	public boolean start() {
		return _socketClient.start();
	}
	
	public void stop() {
		_socketClient.stop();
	}
	
	public boolean isConnected() {
		return _socketClient.isConnected();
	}
	
	public boolean sendRaw(ByteArray data) {
		return _socketClient.send(data);
	}
	
	public boolean send(ByteArray data) {
		short packetLen	= (short)(BYTE_NUM_HEAD + BYTE_NUM_LEN + data.length());
		ByteArray vasPacket	= new ByteArray(packetLen);
		
		vasPacket.writeByte(HEAD);
		vasPacket.writeShort(packetLen);
		vasPacket.write(data);
		
		return _socketClient.send(vasPacket);
	}
	
	public interface Callback {
		public void onConnected();
		public void onRecv(ByteArray vasPacket);
		public void onError(int reason);
		public void onStopped();
	}
}
