package com.vanchu.libs.common.container;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.vanchu.libs.common.util.SwitchLogger;

import android.content.Context;

public class SolidQueue <T>{
	private static final String LOG_TAG				= SolidQueue.class.getSimpleName();
	private static Map<String, Object>	_lockMap	= new HashMap<String, Object>();
	
	private static final String QUEUE_FILE_DIR	= "solid_queue";
	
	public static final int		UNLIMITED_SIZE	= -1;
	
	private Context	_context;
	private String	_name;
	private int		_maxSize;
	
	private String	_path;
	private LinkedList<T>	_linkedList;
	private Object	_lock;
	
	private SolidQueueCallback<T>	_callback;
	
	public SolidQueue(Context context, String name, int maxSize, SolidQueueCallback<T> callback) {
		_context	= context;
		_name		= name;
		_maxSize	= maxSize;
		_callback	= callback;
				
		_path		= _context.getDir(QUEUE_FILE_DIR, Context.MODE_PRIVATE) + "/" + _name;
		
		createLock();
		createList();
	}

	private void createLock() {
		if( ! _lockMap.containsKey(_name)){
			_lockMap.put(_name, new Object());
		}
		
		_lock	= _lockMap.get(_name);
	}

	@SuppressWarnings("unchecked")
	private void createList() {	
		synchronized (_lock) {
			File queueFile	= new File(_path);
			if( ! queueFile.exists()) {
				_linkedList		= new LinkedList<T>();
				SwitchLogger.d(LOG_TAG, "queue file not exist, file=" + _path + ", create an empty queue");
				return;
			}
			
			try{
				FileInputStream		fis	= new FileInputStream(_path);
				ObjectInputStream	ois	= new ObjectInputStream(fis);
				_linkedList		= (LinkedList<T>)(ois.readObject());
				ois.close();
			} catch(Exception e) {
				SwitchLogger.e(e);
				_linkedList		= new LinkedList<T>();
			}
		}
	}
	
	/**
	 * 入队
	 * @param element 
	 */
	public void enqueue(T element) {
		synchronized (_lock) {
			_linkedList.addFirst(element);
			if(null != _callback) {
				_callback.onAdd(element);
			}
			while(_maxSize != UNLIMITED_SIZE && _linkedList.size() > _maxSize) {
				T removedElement = _linkedList.removeLast();
				if(-1 == _linkedList.lastIndexOf(removedElement)){
					// no same element in queue any more
					if(null != _callback) {
						_callback.onRemove(removedElement);
					}
				}
			}
			solidify();
		}
	}
	
	/**
	 * 出队
	 * @return 如果队列不为空，返回出队的元素，否则返回null
	 */
	public T dequeue() {
		synchronized (_lock) {
			try {
				T removedElement	= _linkedList.removeLast();
				if(-1 == _linkedList.lastIndexOf(removedElement)){
					// no same element in queue any more
					if(null != _callback) {
						_callback.onRemove(removedElement);
					}
				}
				solidify();
				
				return removedElement;
			} catch(NoSuchElementException e) {
				SwitchLogger.e(e);
				return null;
			}
		}
	}
	
	/**
	 * 固化队列
	 */
	public void solidify() {
		synchronized (_lock) {
			try {
				FileOutputStream fos	= new FileOutputStream(_path);
				ObjectOutputStream oos	= new ObjectOutputStream(fos);
				while(_maxSize != UNLIMITED_SIZE && _linkedList.size() > _maxSize) {
					T element	= _linkedList.removeLast();
					if(-1 == _linkedList.lastIndexOf(element)){
						// no same element in queue any more
						if(null != _callback) {
							_callback.onRemove(element);
						}
					}
				}
				oos.writeObject(_linkedList);
				oos.close();
			} catch(Exception e) {
				SwitchLogger.e(e);
			}
		}
	}
	
	public void clear() {
		synchronized (_lock) {
			int oldMaxSize	= _maxSize;
			setMaxSize(0);
			solidify();
			setMaxSize(oldMaxSize);
		}
	}
	
	public boolean destroy() {
		synchronized (_lock) {
			try {
				clear();
				
				File queueFile	= new File(_path);
				if(queueFile.exists()) {
					boolean succ	= queueFile.delete();
					if(succ) {
						SwitchLogger.d(LOG_TAG, "queue file delete succ, path=" + _path);
					} else {
						SwitchLogger.e(LOG_TAG, "queue file delete fail, path=" + _path);
					}
					
					return succ;
				} else {
					SwitchLogger.d(LOG_TAG, "queue file not exist, delete fail, path=" + _path);
					return false;
				}

			} catch(Exception e) {
				SwitchLogger.e(e);
				return false;
			}
		}
	}
	
	public void addAll(List<T> elementList, boolean smallIndexFirst) {
		synchronized (_lock) {
			if(smallIndexFirst) {
				for(int i = 0; i < elementList.size(); ++i) {
					T element	= elementList.get(i);
					_linkedList.addFirst(element);
					if(null != _callback) {
						_callback.onAdd(element);
					}
				}
			} else {
				for(int i = elementList.size() - 1; i >= 0; --i) {
					T element	= elementList.get(i);
					_linkedList.addFirst(element);
					if(null != _callback) {
						_callback.onAdd(element);
					}
				}
			}
			
			solidify();
		}
	}
	
	public void setMaxSize(int maxSize) {
		synchronized (_lock) {
			_maxSize	= maxSize;
		}
	}
	
	public int size() {
		synchronized (_lock) {
			return _linkedList.size();
		}
	}
	
	public LinkedList<T> getQueue() {
		synchronized (_lock) {
			return _linkedList;
		}
	}
	
	public LinkedList<T> getReverseQueue() {
		synchronized (_lock) {
			LinkedList<T> reverseQueue	= new LinkedList<T>();
			for(int i = 0; i < _linkedList.size(); ++i) {
				reverseQueue.addFirst(_linkedList.get(i));
			}
			return reverseQueue;
		}
	}
	
	public static interface SolidQueueCallback<E> {
		
		public void onAdd(E element);
		public void onRemove(E element);
	}
}
