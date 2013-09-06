package com.vanchu.libs.WebCache;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by ray on 9/5/13.
 */
public class Storage {
    private class ItemMeta{
        public int time;
        public byte media; //0--internal, 1--external
        public String id;
    }

    private final static String DIR_NAME = "webcache";
    private final static String FILE_ENTRIES = "entries.idx";
    private Map<String, ItemMeta> _entries;
    private String _internal;
    private String _external;
    private int _capacity;

    public Storage(Context context, String type){
        //step 1. setup internal & external directory
        this._internal = context.getDir(DIR_NAME, Context.MODE_PRIVATE).getAbsolutePath();
        File dir = new File(this._internal, type);
        dir.mkdirs();
        this._internal += "/" + type;

        this._external = "";
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            dir = new File(Environment.getExternalStorageDirectory(), context.getPackageName() + "/" + DIR_NAME + "/" + type);
            dir.mkdirs();
            this._external = dir.getAbsolutePath();
        }

        //step 2. load items entries
        this._loadEntries();
    }

    public void setup(int capacity){
        this._capacity = capacity;
    }

    public File get(String url){
        synchronized (this){
            ItemMeta meta = this._entries.get(url);
            if(meta == null)
                return null;

            //step 1. checking external
            if(this._external.length() > 0){
                File file = new File(this._external + "/" + meta.id + ".dat");
                if (file.exists() && file.canRead())
                    return file;
            }

            //step 2. checking internal
            File file = new File(this._internal + "/" + meta.id + ".dat");
            if (file.exists() && file.canRead())
                return file;
            return null;
        }
    }

    public File set(String url, InputStream inputStream){
        ItemMeta meta = new ItemMeta();
        meta.id = _calcMd5(url);
        meta.time = (int)(System.currentTimeMillis() / 1000L);
        meta.media = 1;

        synchronized (this){
            //step 1. select location
            File file = null;
            if(this._external.length() > 0){
                file = new File(this._external + "/" + meta.id + ".dat");
                file.delete();
                try {
                    file.createNewFile();
                }
                catch (Exception e){
                    file = null;
                }
            }

            if(file == null){
                meta.media = 0;
                file = new File(this._internal + "/" + meta.id + ".dat");
                file.delete();
                try {
                    file.createNewFile();
                }
                catch (Exception e){
                    file = null;
                }
            }

            if(file == null)
                return null;

            //step 2. save file
            OutputStream outputStream = null;
            try{
                outputStream = new FileOutputStream(file);
                byte[] buffer	= new byte[512];
                int len = 0;
                while((len = inputStream.read(buffer)) != -1)
                    outputStream.write(buffer, 0, len);

                outputStream.flush();
                outputStream.close();
                inputStream.close();
            }
            catch (Exception e){
                if(outputStream != null){
                    try{
                        outputStream.close();
                    }
                    catch (Exception e1){
                    }
                }
                file.delete();
                return null;
            }

            //step 3. update entries
            this._entries.put(url, meta);
            if(this._entries.size() > (this._capacity * 2)){
                this._eliminateEntries();
                this._eliminateFiles();
            }
            this._saveEntries();

            return file;
        }
    }

    //------------------------private functions-------------------------//
    private void _loadEntries(){
        this._entries = new TreeMap<String, ItemMeta>();

        RandomAccessFile file = null;
        try{
            file = new RandomAccessFile(this._internal + "/" + FILE_ENTRIES, "r");
            int total = file.readInt();
            for(int i = 0; i < total; ++i){
                ItemMeta meta = new ItemMeta();
                meta.time = file.readInt();
                meta.media = file.readByte();

                byte[] id = new byte[32];
                file.read(id, 0, 32);
                meta.id = new String(id);

                int len = file.readInt();
                if(len <= 0)
                    throw new Exception("bad url length");
                byte[] bytes = new byte[len];
                file.read(bytes, 0, len);
                String url = new String(bytes);

                this._entries.put(url, meta);
                
                file.close();
            }
        }
        catch (FileNotFoundException e){
        }
        catch (IOException e){
            this._entries.clear();
        }
        catch (Exception e){
            this._entries.clear();
        }
        
        try{
            if(file != null) file.close();
        }
        catch (Exception e){
        }
    }

    private void _saveEntries(){
        if(this._entries.size() == 0)
            return;

        RandomAccessFile file = null;
        try{
            file = new RandomAccessFile(this._internal + "/" + FILE_ENTRIES, "rw");
            file.writeInt(this._entries.size());

            for(String url : this._entries.keySet()){
                ItemMeta meta = this._entries.get(url);
                file.writeInt(meta.time);
                file.writeByte(meta.media);
                file.write(meta.id.getBytes(), 0, 32);
                file.writeInt(url.length());
                file.write(url.getBytes());
            }
        }
        catch (Exception e){
            Log.e("ERROR", "cannot write file");
        }
        try {
            if(file != null) file.close();
        }
        catch (Exception e){
        }
    }

    private void _eliminateEntries(){
        ArrayList<Map.Entry<String, ItemMeta>> list = new ArrayList<Map.Entry<String, ItemMeta>>(this._entries.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, ItemMeta>>() {
            @Override
            public int compare(Map.Entry<String, ItemMeta> lhs, Map.Entry<String, ItemMeta> rhs) {
                return rhs.getValue().time - lhs.getValue().time;
            }
        });

        for(int idx = this._capacity; idx < list.size(); ++idx){
            String url = list.get(idx).getKey();
            this._entries.remove(url);
        }
    }

    private void _eliminateFiles(){
        Set<String> fileSet = new HashSet<String>();
        for(Map.Entry<String, ItemMeta> entry:this._entries.entrySet())
            fileSet.add(entry.getValue().id + ".dat");

        File dir = new File(this._internal);
        this._clearDirectory(dir, fileSet);
        dir = new File(this._external);
        this._clearDirectory(dir, fileSet);
    }

    private void _clearDirectory(File dir, Set<String> fileSet){
        if(dir.isDirectory()){
            for(String filename : dir.list()){
                if(filename.equals(FILE_ENTRIES))
                    continue;

                if(fileSet.contains(filename))
                    continue;

                File file = new File(dir.getAbsolutePath() + "/" + filename);
                if(file.isFile())
                    file.delete();
            }
        }
    }

    private static String _calcMd5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }
}
