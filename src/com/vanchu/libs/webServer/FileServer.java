package com.vanchu.libs.webServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileServer extends NanoHTTPD {
	private String ROOT = "/";
	private Pattern PATTERN_EXTENSION = null;
		
    public FileServer(int port) {
		super(port);
		this.PATTERN_EXTENSION = Pattern.compile("\\.[a-zA-Z0-9]+$");
	}
    
    public FileServer(String hostname, int port){
    	super(hostname, port);
    	this.PATTERN_EXTENSION = Pattern.compile("\\.[a-zA-Z0-9]+$");
    }
    
    public void setRoot(String root){
    	this.ROOT = root;
    }

	@Override
    public Response serve(String uri, Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files) {
		String mime = this._analyzeMIME(uri);

		File file = new File(this.ROOT + uri);
		if(!file.exists() || !file.isFile() || !file.canRead())
			return new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, mime, "");
		
		NanoHTTPD.Response response = null;
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, mime, in);
		} catch (FileNotFoundException e) {
			response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, mime, "");
		}
		
		return response;
    }
	
	private String _analyzeMIME(String uri){
	    Matcher matcher = this.PATTERN_EXTENSION.matcher(uri);
	    String extension = matcher.find() ? matcher.group().toLowerCase() : ".dat";
	    if(extension.equals(".html"))
	    	return "text/html";
	    else if(extension.equals(".png"))
	    	return "image/png";
	    else if(extension.equals(".jpg") || extension.equals(".jpeg"))
	    	return "image/jpeg";
	    else if(extension.equals(".js"))
	    	return "application/js";
	    else if(extension.equals(".css"))
	    	return "text/css";
    	return "application/octet-stream";
	}
}
