package com.vanchu.libs.eventCenter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EventCenter {

	private static EventCenter _instance	= null;
	private Map<Integer, LinkedList<IListener>>	_typeListMap	= null;
	
	public static EventCenter getInstance() {
		if(null == _instance) {
			_instance	= new EventCenter();
			_instance.init();
		}
		
		return _instance;
	}
	
	private void init() {
		_typeListMap	= new HashMap<Integer,  LinkedList<IListener>>();
	}
	
	public synchronized void addEventListener(int type, IListener listener) {
		Integer typeObj	= new Integer(type);
		if( ! _typeListMap.containsKey(typeObj)) {
			LinkedList<IListener>	list	= new LinkedList<IListener>();
			_typeListMap.put(typeObj, list);
		}
		
		List<IListener> list	= _typeListMap.get(typeObj);
		list.add(listener);
	}
	
	public synchronized void removeEventListener(int type, IListener listener) {
		Integer typeObj	= new Integer(type);
		if( ! _typeListMap.containsKey(typeObj)) {
			return ;
		}
		
		List<IListener> list	= _typeListMap.get(typeObj);
		for(int i = 0; i < list.size(); ++i) {
			if(listener == list.get(i)) {
				list.remove(i);
				return ;
			}
		}
	}
	
	public synchronized void dispatchEvent(Event event) {
		int type		= event.getType();
		Integer typeObj	= new Integer(type);
		
		if( ! _typeListMap.containsKey(typeObj)) {
			return ;
		}
		
		List<IListener> list	= _typeListMap.get(typeObj);
		for(int i = 0; i < list.size(); ++i) {
			IListener listener	= list.get(i);
			listener.handle(event);
		}
	}
}
