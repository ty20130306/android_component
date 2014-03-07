package com.vanchu.libs.addressBook;

import java.io.Serializable;

public class AddressBookData implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String iconURL 		= null;
	private String name 		= null;
	private String letter		= null;
	private String uid			= null;
	
	
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getLetter() {
		return letter;
	}
	public void setLetter(String letter) {
		this.letter = letter;
	}
	public String getName() {
		return name;
	}
	public String getIconURL() {
		return iconURL;
	}
	public void setIconURL(String iconURL) {
		this.iconURL = iconURL;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		
		if(o == null) {
			return false;
		}
		
		if(this.getClass() != o.getClass()){
			return false;
		}
		AddressBookData entity = (AddressBookData)o;
		if(null != uid && uid.equals(entity.getUid())){
			return true;
		}
		return false;
	}
	

}
