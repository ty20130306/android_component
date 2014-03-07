package com.vanchu.libs.common.container;

import java.nio.ByteBuffer;

import com.vanchu.libs.common.util.SwitchLogger;

public class ByteArray {

	private static final String LOG_TAG		= ByteArray.class.getSimpleName();
	
	private static final int CAPACITY_DEFAULT_INIT	= 1024;	// 1K
	private static final float CAPACITY_INCR_FACTOR	= 1.25f;
	
	private boolean _debug		= false;
	
	private ByteBuffer	_buffer;
	private int 		_capacity;
	private int			_dataLen;
	
	public ByteArray(int capacity) {
		_capacity	= capacity;
		_dataLen	= 0;
		_buffer	= ByteBuffer.allocate(capacity);
		
		if(_debug) {
			SwitchLogger.d(LOG_TAG, "ByteArray constructor, _capacity="+_capacity);
			showBufferParam();
		}
	}
	
	public ByteArray() {
		this(CAPACITY_DEFAULT_INIT);
	}
	
	public synchronized void setDebug(boolean value) {
		_debug	= value;
	}
	
	public synchronized void write(ByteArray value) {
		write(value.array(), 0, value.length());
	}
	
	public synchronized void write(ByteArray value, int valueOffset, int byteCount) {
		write(value.array(), valueOffset, byteCount);
	}
	
	public synchronized void write(byte[] value) {
		write(value, 0, value.length);
	}
	
	public synchronized void write(byte[] src, int offset, int length) {
		int byteNum	= length;
		if(spaceAvai() < byteNum) {
			reallocate(byteNum);
		}
		
		_buffer.put(src, offset, length);
		_dataLen	+= byteNum;
	}
	
	public synchronized void writeByte(byte value) {
		int byteNum	= 1;
		if(spaceAvai() < byteNum) {
			reallocate(byteNum);
		}
		
		_buffer.put(value);
		_dataLen += byteNum;
	}
	
	public synchronized void writeShort(short value) {
		int byteNum	= 2;
		if(spaceAvai() < byteNum) {
			reallocate(byteNum);
		}
		
		_buffer.putShort(value);
		_dataLen += byteNum;
	}
	
	public synchronized boolean writeShort(int index, short value) {
		if(index < 0 || index > _buffer.limit() - 2) {
			return false;
		}
		
		_buffer.putShort(index, value);
		return true;
	}
	
	public synchronized void writeInt(int value) {
		int byteNum	= 4;
		if(spaceAvai() < byteNum) {
			reallocate(byteNum);
		}
		
		_buffer.putInt(value);
		_dataLen += byteNum;
	}
	
	public synchronized boolean writeInt(int index, int value) {
		if(index < 0 || index > _buffer.limit() - 4) {
			return false;
		}
		
		_buffer.putInt(index, value);
		return true;
	}
	
	public synchronized byte[] array() {
		byte[] data	=  new byte[_dataLen];
		_buffer.position(0);
		_buffer.get(data);
		
		return data;
	}
	
	public synchronized int length() {
		return _dataLen;
	}

	private synchronized void showBufferParam(){
		SwitchLogger.d(LOG_TAG, "buffer param, _buffer.position="+_buffer.position()
				+",_buffer.capacity="+_buffer.capacity()+",_buffer.remaining="+_buffer.remaining());
	}
	
	private synchronized void reallocate(int need) {
		int newCapacity			= getNewCapacity(need);
		ByteBuffer tmpBuffer	= _buffer;
		
		if(_debug) {
			SwitchLogger.d(LOG_TAG, "reallocate, new capacity="+newCapacity+",_dataLen="+_dataLen
								+",old capacity="+tmpBuffer.capacity());
		}
		_buffer	= ByteBuffer.allocate(newCapacity);
		tmpBuffer.position(0);
		tmpBuffer.limit(_dataLen);
		_buffer.put(tmpBuffer);

		_capacity	= newCapacity;
	}
	
	private synchronized int spaceAvai() {
		return _capacity - _dataLen;
	}
	
	private synchronized int getNewCapacity(int need) {
		int newCapacity	= (int)(Math.ceil(_capacity * CAPACITY_INCR_FACTOR));
		while(newCapacity - _dataLen < need) {
			newCapacity	= (int)(Math.ceil(newCapacity * CAPACITY_INCR_FACTOR));
		}
		
		return newCapacity;
	}
}
