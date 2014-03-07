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

import com.vanchu.libs.common.util.SwitchLogger;

import android.content.Context;

public class DeadList <T>{
	private static final String LOG_TAG				= DeadList.class.getSimpleName();
	private static Map<String, Object>	_lockMap	= new HashMap<String, Object>();
	
	private static final String LIST_FILE_DIR	= "solid_list";
	
	public static final int		UNLIMITED_SIZE	= -1;
	
	private Context	_context;
	private String	_name;
	private int		_maxSize;
	
	private String	_path;
	private LinkedList<T>	_linkedList;
	private Object	_lock;
	
	private DeadListCallback<T>	_callback;
	
	public DeadList(Context context, String name, int maxSize, DeadListCallback<T> callback) {
		_context	= context;
		_name		= name;
		_maxSize	= maxSize;
		_callback	= callback;
				
		_path		= _context.getDir(LIST_FILE_DIR, Context.MODE_PRIVATE) + "/" + _name;
		
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
			File listFile	= new File(_path);
			if( ! listFile.exists()) {
				_linkedList		= new LinkedList<T>();
				SwitchLogger.d(LOG_TAG, "list file not exist, file=" + _path + ", create an empty list");
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
	
	public boolean remove(int index) {
		synchronized (_lock) {
			if(index < 0 || index >= _linkedList.size()) {
				return false;
			}
			
			T element	= _linkedList.remove(index);
			if(-1 == _linkedList.lastIndexOf(element)){
				// no same element in list any more
				if(null != _callback) {
					_callback.onRemove(element);
				}
			}
			solidify();
			
			return true;
		}
	}
	
	public boolean remove(T element) {
		synchronized (_lock) {
			if(_linkedList.remove(element)) {
				if(-1 == _linkedList.lastIndexOf(element)){
					// no same element in list any more
					if(null != _callback) {
						_callback.onRemove(element);
					}
				}
				solidify();
				
				return true;
			}
			
			return false;
		}
	}
	
	public boolean addAll(List<T> elementList) {
		synchronized (_lock) {
			int i = 0;
			for( ; i < elementList.size(); ++i) {
				if(UNLIMITED_SIZE == _maxSize || _linkedList.size() < _maxSize) {
					T element	= elementList.get(i);
					_linkedList.addLast(element);
					if(null != _callback) {
						_callback.onAdd(element);
					}
				} else {
					break;
				}
			}
			solidify();
			
			if(i < elementList.size()) {
				return false;
			}
			
			return true;
		}
	}
	
	public boolean add(T element) {
		synchronized (_lock) {
			if(UNLIMITED_SIZE == _maxSize || _linkedList.size() < _maxSize) {
				_linkedList.addLast(element);
				if(null != _callback) {
					_callback.onAdd(element);
				}
				solidify();
				
				return true;
			}
			
			return false;
		}
	}
	
	/**
	 * 固化链表
	 */
	public void solidify() {
		synchronized (_lock) {
			try {
				FileOutputStream fos	= new FileOutputStream(_path);
				ObjectOutputStream oos	= new ObjectOutputStream(fos);
				while(_maxSize != UNLIMITED_SIZE && _linkedList.size() > _maxSize) {
					T element	= _linkedList.removeLast();
					if(-1 == _linkedList.lastIndexOf(element)){
						// no same element in list any more
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
	
	public boolean destroy(){
		synchronized (_lock) {
			try {
				clear();
				
				File listFile	= new File(_path);
				if(listFile.exists()) {
					boolean succ	= listFile.delete();
					if(succ) {
						SwitchLogger.d(LOG_TAG, "list file delete succ, path=" + _path);
					} else {
						SwitchLogger.e(LOG_TAG, "list file delete fail, path=" + _path);
					}
					
					return succ;
				} else {
					SwitchLogger.d(LOG_TAG, "list file not exist, delete fail, path=" + _path);
					return false;
				}

			} catch(Exception e) {
				SwitchLogger.e(e);
				return false;
			}
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
	
	public LinkedList<T> getList() {
		synchronized (_lock) {
			return _linkedList;
		}
	}
	
	public static interface DeadListCallback<E> {
		public void onAdd(E element);
		public void onRemove(E element);
	}
}

