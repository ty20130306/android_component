package com.vanchu.test;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.webCache.WebCache;
import com.vanchu.test.R;
import com.vanchu.test.R.id;
import com.vanchu.test.R.layout;
import com.vanchu.test.R.menu;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class WebCacheActivity extends Activity {
    private ButtonTestTcpOnClickListener _btnTestTcpOnClickListener;
    private ButtonTestLruOnClickListener _btnTestLruOnClickListener;
    private ButtonTestSoundOnClickListener _btnTestSoundOnClickListener;
    private ButtonGetRootOnClickListener _btnGetRootOnClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_web_cache);

        this._btnTestTcpOnClickListener = new ButtonTestTcpOnClickListener();
        ((Button)this.findViewById(R.id.button_test_tcp)).setOnClickListener(this._btnTestTcpOnClickListener);
        
        this._btnTestLruOnClickListener = new ButtonTestLruOnClickListener();
        ((Button)this.findViewById(R.id.button_test_lru)).setOnClickListener(this._btnTestLruOnClickListener);
        
        this._btnTestSoundOnClickListener = new ButtonTestSoundOnClickListener();
        ((Button)this.findViewById(R.id.button_test_sound)).setOnClickListener(this._btnTestSoundOnClickListener);
        this._btnGetRootOnClickListener = new ButtonGetRootOnClickListener();
        ((Button)this.findViewById(R.id.button_get_dir)).setOnClickListener(this._btnGetRootOnClickListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public class ButtonGetRootOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            String path = "";

            if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
                path = (Environment.getExternalStorageDirectory().getPath() + "/" + getPackageName());
            }

            Toast.makeText(WebCacheActivity.this, path, Toast.LENGTH_SHORT).show();

        }
    }

    public class ButtonTestSoundOnClickListener implements View.OnClickListener {
        private Thread _thread = null;
        @Override
        public void onClick(View view) {
            if(this._thread != null){
                this._thread.interrupt();
                this._thread = null;
            }

            this._thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    final float frequency = 440;
                    float increment = (float)(2 * Math.PI) * frequency / 441000;
                    float angle = 0;
                    AndroidAudioDevice device = new AndroidAudioDevice();
                    float samples[] = new float[1024];

                    while(true){
                        for(int i = 0; i < samples.length; ++i){
                            samples[i] = (float)Math.sin(angle);
                            angle += increment;
                        }

                        device.writeSamples(samples);
                    }
                }
            });
            this._thread.start();
        }

        public class AndroidAudioDevice{
            AudioTrack track;
            short[] buffer = new short[1024];

            public AndroidAudioDevice( )
            {
                int minSize =AudioTrack.getMinBufferSize( 44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT );
                track = new AudioTrack( AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, minSize, AudioTrack.MODE_STREAM);
                track.play();
            }

            public void writeSamples(float[] samples)
            {
                if( buffer.length < samples.length )
                    buffer = new short[samples.length];
                for( int i = 0; i < samples.length; i++ )
                    buffer[i] = (short)(samples[i] * Short.MAX_VALUE);
                track.write( buffer, 0, samples.length );
            }
        }
    }

    public class ButtonTestLruOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
        	Log.e("", "ButtonTestLruOnClickListener");
        	
            WebCache cache = WebCache.getInstance(WebCacheActivity.this.getApplicationContext(), "test");

            WebCache.Settings settings = new WebCache.Settings();
            settings.capacity = 3;
            cache.setup(settings);

            WebCache.GetCallback listener = new WebCache.GetCallback() {
                @Override
                public void onDone(String url, File file, Object param) {
                    Log.d("onDone", "--------------url="+url+",file="+file.getAbsolutePath());
                }

                @Override
                public void onFail(String url, int reason, Object param) {
                    Log.d("onFail", "--------------url="+url+",reason="+reason);
                }
                
                @Override
                public void onProgress(String url, int progress, Object param) {
                	SwitchLogger.d("onProgress", "--------------progress="+progress);
                }
            };
            
            List<String> list	= new ArrayList<String>();
            list.add("http://f.hiphotos.baidu.com/album/w%3D2048/sign=5ea4793d0eb30f24359aeb03fcadd043/b151f8198618367aff99ad812f738bd4b31ce55e.jpg");
            list.add("http://b.hiphotos.baidu.com/album/w%3D2048/sign=ebd14f612f738bd4c421b53195b386d6/3c6d55fbb2fb43165c474ac621a4462309f7d335.jpg");
            
            
            Log.d("", "begin request ,list.size="+list.size());
            for(int i = 0; i < list.size(); ++i) {
            	Log.d("", "request " + list.get(i));
                cache.get(list.get(i), listener, i, false);
            }
        }
    }

    public class ButtonTestTcpOnClickListener implements View.OnClickListener {
        private Thread _theThread;

        public ButtonTestTcpOnClickListener(){
            this._theThread = null;
        }


        @Override
        public void onClick(View view) {
            if(this._theThread != null){
                this._theThread.interrupt();
                this._theThread = null;
            }

            this._theThread = new NetworkThread();
            this._theThread.start();
        }

        private class NetworkThread extends Thread{
            @Override
            public void run(){
                while(true){
                    try{

                        Socket socket = new Socket("192.168.1.102", 6969);
                        DataOutputStream outs = new DataOutputStream(socket.getOutputStream());
                        DataInputStream ins = new DataInputStream(socket.getInputStream());
                        outs.writeByte(1);
                        outs.writeShort(32);
                        outs.writeBytes("00000000000000000000000000000001");

                        int cmd = ins.readByte();
                        short length = ins.readShort();
                    }
                    catch (IOException e){
                        Log.d("NETWORK:", e.getMessage());
                    }
                }
            }
        }
    }
}
