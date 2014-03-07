package com.vanchu.libs.socketClient;


import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.os.Handler;

import com.vanchu.libs.common.container.ByteArray;
import com.vanchu.libs.common.util.NetUtil;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.common.util.ThreadUtil;

public class SocketClient implements Runnable {
	private static final String LOG_TAG		= SocketClient.class.getSimpleName();
	
	public static final int ERR_CONNECT_FAIL		= 1;
	
	private static final int ON_CONNECTED		= 1;
	private static final int ON_STOPPED			= 2;
	private static final int ON_RECV			= 3;
	private static final int ON_ERROR			= 4;
	
	private static final int SOCKET_SEND_BUFFFER_SIZE	= 64 * 1024; // 64K
	private static final int SOCKET_RECV_BUFFER_SIZE	= 64 * 1024; // 64K
	
	private static final int TMP_READ_BUFFER_SIZE	= 4 * 1024; // 4K
	private static final int EACH_WRITE_MAX_SIZE	= 4 * 1024; // 4K
	
	private static final int RECONNECT_INTERVAL_INIT	= 3 * 1000; //milliseconds 
	
	private ByteBuffer _tmpReadBuffer	= ByteBuffer.allocate(TMP_READ_BUFFER_SIZE);
	
	private Thread			_thread		= null;
	private SocketChannel	_channel	= null;
	private Selector		_selector	= null;

	private SocketClientBuffer _clientRecvBuffer	= new SocketClientBuffer();
	private SocketClientBuffer _clientSendBuffer	= new SocketClientBuffer();
	
	private AtomicBoolean _stopped	= new AtomicBoolean(false); 
	
	private int _reconnectInterval		= RECONNECT_INTERVAL_INIT;
	
	private Object _selectionKeyLock	= new Object();
	
	private boolean _needRestart		= false;
	
	private boolean _debug				= false;
	
	private Context	_context;
	private String	_host;
	private int		_port;
	private Callback	_callback;
	
	private Handler		_handler	= new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case ON_CONNECTED:
				_callback.onConnected();
				break;
				
			case ON_STOPPED:
				if(_needRestart) {
					start();
				} else {
					_callback.onStopped();
				}
				
				break;
				
			case ON_RECV:
				_callback.onRecv();
				break;
				
			case ON_ERROR:
				_callback.onError(msg.arg1);
				break;
				
			default:
				break;
			}
		}
	};
	
	public SocketClient(Context context, String host, int port, Callback callback) {
		_context	= context;
		_host	= host;
		_port	= port;
		_callback	= callback;
	}
	
	public void setDebug(boolean value) {
		_debug	= value;
	}
	
	@Override
	public void run() {
		SwitchLogger.d(LOG_TAG, "event loop begin--------------------------" );
		
		while ( ! _stopped.get()) {
			SwitchLogger.d(LOG_TAG, "thread while loop");
			try {
				connect();
			} catch (Exception e) {
				SwitchLogger.e(e);
				_handler.obtainMessage(ON_ERROR, ERR_CONNECT_FAIL, 0).sendToTarget();
				_stopped.set(true);
			}
			
			try {
				while( ! _stopped.get() && _channel.isOpen()) {
					int eventNum	= _selector.select();
					if(_debug) {
						SwitchLogger.d(LOG_TAG, "channel while loop, event num="+eventNum);
					}
					if(eventNum > 0) {
						processEvent();
					}
				}
			} catch (Exception e) {
				SwitchLogger.e(e);
			}
			
			if( ! _stopped.get()) {
				SwitchLogger.d(LOG_TAG, "sleep " + _reconnectInterval + " milliseconds for reconnect");
				ThreadUtil.sleep(_reconnectInterval); // 重连间隔
				_reconnectInterval	*= 2;
				if(_reconnectInterval < 0) {
					_reconnectInterval	= RECONNECT_INTERVAL_INIT;
				}
			}
		}
		
		cleanUp();
		
		_handler.sendEmptyMessage(ON_STOPPED);
		SwitchLogger.d(LOG_TAG, "event loop end----------------------------" );
	}
	
	private synchronized void cleanUp() {
		try {
			_clientRecvBuffer.clear();
			_clientSendBuffer.clear();
			
			if(null != _channel) {
				_channel.close();
				_channel	= null;
			}

			if(null != _selector) {
				_selector.close();
				_selector	= null;
			}
			
			_thread	= null;
		} catch (Exception e) {
			SwitchLogger.e(e);
		}
	}
	
	private void processEvent() throws Exception {
		Set<SelectionKey> keySet	= _selector.selectedKeys();
		Iterator<SelectionKey> iter	= keySet.iterator();
		while(iter.hasNext()) {
			SelectionKey key = iter.next();

			if (key.isConnectable()){
				processConnect(key);
			}
			
			if (key.isReadable()){
				processRead(key);
			}
			
			/**
			 * if channel.read return -1 means the server close the connection
			 * client will close the channel too, the the key becomes invalid, 
			 * so must check first or the key.isWritable will throw CancelledKeyException 
			 */
			if (key.isValid() && key.isWritable()){
				processWrite(key);
			}
			
			iter.remove();
		}
	}

	private void processConnect(SelectionKey key) throws Exception {
		SocketChannel ch = (SocketChannel) key.channel();
		if (ch.isConnectionPending() && ch.finishConnect()) {
			SwitchLogger.d(LOG_TAG, "select, connected to " + _host + ":" + _port + " succ");

			key.interestOps(key.interestOps() ^ SelectionKey.OP_CONNECT);
			key.interestOps(key.interestOps() | SelectionKey.OP_READ);
			_handler.sendEmptyMessage(ON_CONNECTED);
		} else {
			SwitchLogger.e(LOG_TAG, "fail to finish connect" );
		}
	}
	
	private void processRead(SelectionKey key) throws Exception {
		ReadableByteChannel ch = (ReadableByteChannel)key.channel();
		int byteRead 		= 0;
		int byteReadTotal	= 0;
		while (_tmpReadBuffer.hasRemaining() && (byteRead = ch.read(_tmpReadBuffer)) > 0) {
			byteReadTotal	+= byteRead;
			if(_debug) {
				SwitchLogger.d(LOG_TAG, "read loop, byteRead=" + byteRead + ", byteReadTotal=" + byteReadTotal
									+ ", _readBuffer.remaining=" + _tmpReadBuffer.remaining() 
									+ ", _readBuffer.capacity=" + _tmpReadBuffer.capacity());
			}
		}

		/**
		 * 这里必须用_tmpReadBuffer.position() > 0来判断是否有数据可返回上一层，
		 * 不能用byteReadTotal来做判断，因为如果写到_clientRecvBuffer发生异常，
		 * 直接跳到外边的catch异常代码块，_tmpReadBuffer.compact()将不会执行，
		 * 这样将导致_tmpReadBuffer.hasRemaining()永远为0，即使重连上去也读不进数据了
		 * 这样将导致read事件不断触发又不能处理，导致死循环
		 */
		if (_tmpReadBuffer.position() > 0) {
			_tmpReadBuffer.flip();
			_clientRecvBuffer.write(_tmpReadBuffer);
			_handler.sendEmptyMessage(ON_RECV);
			_tmpReadBuffer.compact();
		}
		
		if(-1 == byteRead) {
			SwitchLogger.d(LOG_TAG, "ch.read return -1, server peer closed, close channel");
			ch.close();
		}
		if(_debug) {
			SwitchLogger.d(LOG_TAG, "byteRead=" + byteRead + ", byteReadTotal=" + byteReadTotal
				+ ", _readBuffer.remaining=" + _tmpReadBuffer.remaining() 
				+ ", _readBuffer.capacity=" + _tmpReadBuffer.capacity());
		}
	}
	
	private void processWrite(SelectionKey key) throws Exception {
		WritableByteChannel ch = (WritableByteChannel)key.channel();
		while(_clientSendBuffer.length() > 0) {
			ByteArray pendingData	= _clientSendBuffer.peek(EACH_WRITE_MAX_SIZE);
			ByteBuffer writeBuffer	= ByteBuffer.wrap(pendingData.array());

			int byteWritten			= 0;
			int byteWrittenTotal	= 0;
			while (writeBuffer.hasRemaining() && (byteWritten = ch.write(writeBuffer)) > 0){
				byteWrittenTotal += byteWritten;

				if(_debug) {
					SwitchLogger.d(LOG_TAG, "byteWritten=" + byteWritten + ", byteWrittenTotal=" + byteWrittenTotal
						+ ", writeBuffer.remaining=" + writeBuffer.remaining() 
						+ ", writeBuffer.capacity=" + writeBuffer.capacity());
				}
			}
			if(_debug) {
				SwitchLogger.d(LOG_TAG, "write loop, _clientSendBuffer.length()="+_clientSendBuffer.length()
					+", writeBuffer.remaining()="+writeBuffer.remaining());
			}
			_clientSendBuffer.read(byteWrittenTotal);
			if(writeBuffer.hasRemaining()) {
				// can not write any more this time
				_clientSendBuffer.read(byteWrittenTotal);
				break;
			}
		}
		
		if (_clientSendBuffer.length() <= 0) {
			if(_debug) {
				SwitchLogger.d(LOG_TAG, "data in _clientSendBuffer is all written succ, delete SelectionKey.OP_WRITE");
			}
			synchronized (_selectionKeyLock) {
				// send函数也有操作key的interestOps，所以增加按_selectionKeyLock同步的机制
				key.interestOps(key.interestOps() ^ SelectionKey.OP_WRITE);
			}
		}
	}
	
	private void init() {
		_stopped.set(false);
		_reconnectInterval	= RECONNECT_INTERVAL_INIT;
		_needRestart	= false;
	}
	
	public synchronized boolean restart() {
		if(_needRestart) {
			SwitchLogger.e(LOG_TAG, "socket client is waiting to restart now");
			return false;
		}
		
		if(null == _thread) {
			SwitchLogger.d(LOG_TAG, "socket client not started yet, start it now");
			start();
		} else {
			_needRestart	= true;
			stop();
		}
		return true;
	}
	
	public synchronized boolean start() {
		if(null != _thread) {
			SwitchLogger.e(LOG_TAG, "socket client thread is running, no need to start");
			
			return false;
		}
		
		init();
		try {
			_thread	= new Thread(this);
			_thread.start();
		} catch (Exception e) {
			SwitchLogger.e(e);
			
			if(null != _thread) {
				_thread.interrupt();
				_thread	= null;
			}
			SwitchLogger.e(LOG_TAG, "socket client start fail");
			
			return false;
		}
		
		return true;
	}
	
	private void connect() throws Exception {
		SwitchLogger.d(LOG_TAG, "begin to connect to " + _host + ":" + _port);
		
		_selector	= Selector.open();
		_channel	= SocketChannel.open();
		configureChannel();

		InetSocketAddress isa	= new InetSocketAddress(_host, _port);
		boolean connectSucc	= _channel.connect(isa);
		if(connectSucc) {
			SwitchLogger.d(LOG_TAG, "direct, connected to " + _host + ":" + _port + " succ, register OP_READ");
			_handler.sendEmptyMessage(ON_CONNECTED);
			_channel.register(_selector, SelectionKey.OP_READ);
		} else {
			_channel.register(_selector, SelectionKey.OP_CONNECT);
			SwitchLogger.d(LOG_TAG, "fail to connect to " + _host + ":" + _port + ", register OP_CONNECT");
		}
	}
	
	private void configureChannel() throws Exception {
		_channel.configureBlocking(false);
		_channel.socket().setSendBufferSize(SOCKET_SEND_BUFFFER_SIZE);
		_channel.socket().setReceiveBufferSize(SOCKET_RECV_BUFFER_SIZE);
		_channel.socket().setKeepAlive(true);
		_channel.socket().setReuseAddress(true);
		_channel.socket().setSoLinger(false, 0);
		_channel.socket().setSoTimeout(0);
		_channel.socket().setTcpNoDelay(true);
	}
	
	public synchronized boolean isConnected() {
		if(null != _thread && null != _channel 
			&& _channel.isConnected() 
			&& NetUtil.isConnected(_context)) 
		{
			return true;
		} else {
			return false;
		}
	}
	
	public synchronized void stop() {
		SwitchLogger.d(LOG_TAG, "SocketClient.stop" );
		
		_stopped.set(true);
		
		if(null != _thread) {
			_thread.interrupt();
		}
		
		if(null != _selector) {
			_selector.wakeup();
		}
	}
	
	public synchronized boolean send(ByteArray data) {
		if( ! isConnected()) {
			SwitchLogger.e(LOG_TAG, "send fail, socket client not connected yet");
			return false;
		}
		
		boolean succ = _clientSendBuffer.write(data);
		if(! succ) {
			SwitchLogger.e(LOG_TAG, "send fail, no enough write buffer to contain the data");
			return false;
		}
		
		synchronized (_selectionKeyLock) {
			// processWrite函数也有操作key的interestOps，所以增加按_selectionKeyLock同步的机制
			SelectionKey key = _channel.keyFor(_selector);
			key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
			_selector.wakeup();
		}
		
		return true;
	}
	
	public SocketClientBuffer getRecvBuffer() {
		return _clientRecvBuffer;
	}
	
	public SocketClientBuffer getSendBuffer() {
		return _clientSendBuffer;
	}
	
	public interface Callback {
		public void onConnected();
		public void onRecv();
		public void onError(int reason);
		public void onStopped();
	}
}
