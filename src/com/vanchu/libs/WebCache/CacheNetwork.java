package com.vanchu.libs.WebCache;

import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.zip.GZIPInputStream;

public class CacheNetwork {
    private int _timeout = 60000;

    public void setup(int timeout){
        this._timeout = timeout;
    }

    public InputStream get(final String url){
        try {
            BasicHttpParams httpParams		= new BasicHttpParams();
            HttpConnectionParams.setSoTimeout(httpParams, this._timeout);
            DefaultHttpClient httpClient	= new DefaultHttpClient(httpParams);

            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("Accept-Encoding", "gzip");

            HttpResponse httpResponse	= httpClient.execute(httpGet);
            int statusCode	= httpResponse.getStatusLine().getStatusCode();
            if(statusCode != HttpURLConnection.HTTP_OK)
                return null;

            InputStream inputStream = httpResponse.getEntity().getContent();
            Header contentEncoding = httpResponse.getFirstHeader("Content-Encoding");
            if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip"))
                inputStream = new GZIPInputStream(inputStream);
            return inputStream;
        }
        catch (IOException e){
            Log.e("Hello", e.getMessage());
        }
        catch (Exception e){
            Log.e("Hello", e.getMessage());
        }
        return null;
    }
}