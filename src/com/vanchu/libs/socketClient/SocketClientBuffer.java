package com.vanchu.libs.socketClient;

import java.nio.ByteBuffer;

import com.vanchu.libs.common.container.ByteArray;
import com.vanchu.libs.common.util.SwitchLogger;

public class SocketClientBuffer {

	private static final String LOG_TAG		= SocketClientBuffer.class.getSimpleName();
	
	private static final int BUFFER_MAX_LEN		= 256 * 1024; // 256K 
	
	private boolean _debug		= false;
	
	private ByteBuffer	_buffer;
	private int			_start;
	private int			_end;
	
	public SocketClientBuffer() {
		_buffer		= ByteBuffer.allocate(BUFFER_MAX_LEN);
		_start		= 0;
		_end		= 0;
	}
	
	public synchronized void setDebug(boolean value) {
		_debug	= value;
	}
	
	public synchronized void clear() {
		_start	= 0;
		_end	= 0;
	}
	
	public synchronized boolean write(ByteBuffer value) {
		byte[]	bytes	= new byte[value.limit()];
		value.get(bytes);
		
		ByteArray byteArr	= new ByteArray(bytes.length);
		byteArr.write(bytes);
		
		return write(byteArr);
	}
	
	public synchronized boolean write(ByteArray value) {
		if(value.length() > BUFFER_MAX_LEN - length()){
			SwitchLogger.e(LOG_TAG, "no available space for value, value.length()="+value.length()
									+",BUFFER_MAX_LEN="+BUFFER_MAX_LEN+",data in buffer len="+length());
			
			return false;
		}
		
		if(_debug) {
			SwitchLogger.d(LOG_TAG, "_start="+_start+",_end="+_end+",value.length()="+value.length()
								+",BUFFER_MAX_LEN="+BUFFER_MAX_LEN+",data in buffer len="+length());
		}
		
		if(BUFFER_MAX_LEN - _end >= value.length()) {
			_buffer.position(_end);
			_buffer.put(value.array());
			_end	= (_end + value.length()) % BUFFER_MAX_LEN;
		} else {
			_buffer.position(_end);
			int firstPartLen	= BUFFER_MAX_LEN - _end;
			_buffer.put(value.array(), 0, firstPartLen);
			
			_buffer.position(0);
			int secondPartLen	= value.length() - firstPartLen;
			
			
			_buffer.put(value.array(), firstPartLen, secondPartLen);
			
			_end	= secondPartLen;
			
			if(_debug) {
				SwitchLogger.d(LOG_TAG, "BUFFER_MAX_LEN="+BUFFER_MAX_LEN
						+",value.length()="+value.length()+",firstPartLen="+firstPartLen
						+",secondPartLen="+secondPartLen);
			}
		}
		
		if(_debug) {
			SwitchLogger.d(LOG_TAG, "_start:"+_start+",_end="+_end+",length()="+length());
		}
		
		return true;
	}
	
	public synchronized ByteArray read(int len) {
		ByteArray data	= new ByteArray();
		if(length() <= 0) {
			return data;
		}
		
		len	= len < length() ? len : length();
		
		if(_start < _end) {
			data.write(_buffer.array(), _start, len);
			_start	= _start + len;
		} else {
			int firstPartLen	= BUFFER_MAX_LEN - _start;
			if(len <= firstPartLen) {
				data.write(_buffer.array(), _start, len);
				_start	= (_start + len) % BUFFER_MAX_LEN;
			} else {
				data.write(_buffer.array(), _start, firstPartLen);
				data.write(_buffer.array(), 0, len - firstPartLen);
				_start	= len - firstPartLen;
			}
		}
		
		return data;
	}
	
	public synchronized ByteArray peek(int len) {
		ByteArray data	= new ByteArray();
		if(length() <= 0) {
			return data;
		}
		
		len	= len < length() ? len : length();
		
		if(_start < _end) {
			data.write(_buffer.array(), _start, len);
		} else {
			int firstPartLen	= BUFFER_MAX_LEN - _start;
			if(len <= firstPartLen) {
				data.write(_buffer.array(), _start, len);
			} else {
				data.write(_buffer.array(), _start, firstPartLen);
				data.write(_buffer.array(), 0, len - firstPartLen);
			}
		}
		
		return data;
	}
	
	public synchronized ByteArray peek() {
		return peek(length());
	}
	
	public synchronized int length() {
		if(_end >= _start) {
			return _end - _start;
		} else {
			return _end + BUFFER_MAX_LEN - _start;
		}
	}
}
