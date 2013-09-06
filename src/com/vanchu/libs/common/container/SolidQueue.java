package com.vanchu.libs.common.container;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

import com.vanchu.libs.common.util.SwitchLogger;

import android.content.Context;

public class SolidQueue <T>{
	private static final String LOG_TAG				= SolidQueue.class.getSimpleName();
	private static Map<String, Object>	_lockMap	= new HashMap<String, Object>();
	
	private static final String QUEUE_FILE_DIR	= "solid_queue";
	
	public static final int		UNLIMITED_SIZE	= 0;
	
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
				
		_path		= _context.getDir(QUEUE_FILE_DIR, Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE) 
							+ "/" + _name;
		
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
			_callback.onAdd(element);
			while(_maxSize != UNLIMITED_SIZE && _linkedList.size() > _maxSize) {
				T removedElement = _linkedList.removeLast();
				_callback.onRemove(removedElement);
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
				T element	= _linkedList.removeLast();
				_callback.onRemove(element);
				solidify();
				
				return element;
			} catch(NoSuchElementException e) {
				SwitchLogger.e(e);
				return null;
			}
		}
	}
	
	/**
	 * 固化队列
	 */
	private void solidify() {
		synchronized (_lock) {
			try {
				FileOutputStream fos	= new FileOutputStream(_path);
				ObjectOutputStream oos	= new ObjectOutputStream(fos);
				while(_maxSize != UNLIMITED_SIZE && _linkedList.size() > _maxSize) {
					T element	= _linkedList.removeLast();
					_callback.onRemove(element);
				}
				oos.writeObject(_linkedList);
				oos.close();
			} catch(Exception e) {
				SwitchLogger.e(e);
			}
		}
	}
	
	public LinkedList<T> getQueue() {
		return _linkedList;
	}
	
	public static interface SolidQueueCallback<E> {
		
		public void onAdd(E element);
		public void onRemove(E element);
	}
}
