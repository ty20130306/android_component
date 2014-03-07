package com.vanchu.sample;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.vanchu.libs.common.container.ByteArray;
import com.vanchu.libs.common.ui.Tip;
import com.vanchu.libs.common.util.ActivityUtil;
import com.vanchu.libs.common.util.IdUtil;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.common.util.TimeUtil;
import com.vanchu.libs.talkClient.TalkClient;
import com.vanchu.test.R;
import com.vanchu.test.R.layout;
import com.vanchu.test.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TalkClientSampleActivity extends Activity {
	private static final String LOG_TAG		= TalkClientSampleActivity.class.getSimpleName();
	
	private static final int SEND_STATE_SUCC	= 0;
	private static final int SEND_STATE_FAIL	= 1;
	private static final int SEND_STATE_SENDING	= 2;

	private TalkClient	_client	= null;
	private String 		_uid	= null;
	
	private Timer		_msgTimer		= null;
	private TimerTask	_msgTimerTask	= null;
	
	
	private ListView _listView;
	private EditText _inputMsg;
	private Object	_msgItemListLock		= new Object();
	private List<MsgItem>	_msgItemList	= new LinkedList<MsgItem>();
	private BaseAdapter	_adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_talk_client_sample);
		
		_listView	= (ListView)findViewById(R.id.talk_client_content);
		_inputMsg	= (EditText)findViewById(R.id.talk_client_msg);
		_adapter	= new ContentListAdapter();
		_listView.setAdapter(_adapter);
	}

	private void initMsgTimer() {
		_msgTimer	= new Timer();
		_msgTimerTask	= new TimerTask() {
			
			@Override
			public void run() {
				long now	= System.currentTimeMillis();
				SwitchLogger.d(LOG_TAG, "check send msg time out, now="+(now/1000)+" seconds---------------" );
				synchronized (_msgItemListLock) {
					for(int i = 0; i < _msgItemList.size(); ++i) {
						MsgItem item	= _msgItemList.get(i);
						if(item.senderId == _uid && item.sendState == SEND_STATE_SENDING
							&& now - item.timestamp > 6000) 
						{
							item.sendState	= SEND_STATE_FAIL;
						}
					}
				}
				
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						_adapter.notifyDataSetChanged();
					}
				});
			}
		};
		
		_msgTimer.schedule(_msgTimerTask, 0, 6 * 1000);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		stop(null);
	}
	
	private void updateSendState(String msgId, int newSendState){
		synchronized (_msgItemListLock) {
			for(int i = 0; i < _msgItemList.size(); ++i) {
				MsgItem msgItem	= _msgItemList.get(i);
				if(msgItem.msgId.equals(msgId)){
					msgItem.sendState	= newSendState;
					break;
				}
			}
		}
		_adapter.notifyDataSetChanged();
	}
	
	private void initClient() {
		SwitchLogger.d(LOG_TAG, "initClient clicked");
		String host	= getInput(R.id.talk_client_host);
		String port	= getInput(R.id.talk_client_port);
		_uid		= getInput(R.id.talk_client_uid);
		SwitchLogger.d(LOG_TAG, "host="+host+",port="+port+",uid="+_uid);
		
		_client	= new TalkClient(getApplicationContext(), host, Integer.parseInt(port),_uid, _uid, new TalkClient.Callback() {
			
			@Override
			public void onTalkSucc(String msgId) {
				SwitchLogger.d(LOG_TAG, "msg sent succ, msgId=" + msgId);
				updateSendState(msgId, SEND_STATE_SUCC);
			}
			
			@Override
			public void onStopped() {
				SwitchLogger.d(LOG_TAG, "TalkClient.onStopped" );
				Tip.show(TalkClientSampleActivity.this, "连接失败，请重连");
			}
			
			@Override
			public void onRecvMsg(String msgId, byte msgType, String fromUid, String msg, long timestamp) {
				SwitchLogger.d(LOG_TAG, "TalkClient.onRecvMsg,fromUid="+fromUid
						+",msg="+msg+",timestamp="+timestamp+",msgType="+msgType);
						
				MsgItem msgItem	= new MsgItem(msgId, timestamp, fromUid, _uid, msg, SEND_STATE_SUCC);
				synchronized (_msgItemListLock) {
					_msgItemList.add(msgItem);
				}
				_adapter.notifyDataSetChanged();
				_listView.setSelection(_msgItemList.size() - 1);
			}
			
			@Override
			public void onLogon() {
				SwitchLogger.d(LOG_TAG, "talk client is logon");
				Tip.show(TalkClientSampleActivity.this, "连接成功");
			}
			
			@Override
			public void onError(int reason) {
				SwitchLogger.e(LOG_TAG, "TalkClient.onError, reason="+reason);
				switch (reason) {
				case TalkClient.ERR_CONNECT_FAIL:
					SwitchLogger.e(LOG_TAG, "TalkClient connect fail");
					break;
					
				case TalkClient.ERR_LOGIN_FAIL:
					SwitchLogger.e(LOG_TAG, "TalkClient login fail");
					break;
					
				default:
					SwitchLogger.e(LOG_TAG, "unknown error");
					break;
				}
			}
		});
	}
	
	public void checkConnect(View v) {
		if(_client.isLogon()) {
			SwitchLogger.d(LOG_TAG, "talk client is logon");
			Tip.show(this, "已经连接成功");
		} else {
			SwitchLogger.d(LOG_TAG, "talk client is not logon yet");
			Tip.show(this, "暂未连接成功");
		}
	}
	
	public void restart(View v) {
		SwitchLogger.d(LOG_TAG, "restart clicked");
		
		if(null == _client) {
			Tip.show(this, "未连接过，请先连接");
			return ;
		}
		
		_client.restart();
	}
	public void start(View v) {
		SwitchLogger.d(LOG_TAG, "start clicked");
		
		if(null != _client) {
			Tip.show(this, "已初始化连接，如果断开请重连");
			return ;
		}
		
		initClient();
		initMsgTimer();
		boolean succ = _client.start();
		if( ! succ) {
			Tip.show(this, "正在连接中。。。");
		}
	}
	
	public void stop(View v) {
		SwitchLogger.d(LOG_TAG, "stop clicked");
		
		if(null != _client) {
			_client.stop();
		}
		
		if(null != _msgTimer) {
			_msgTimer.cancel();
			_msgTimer	= null;
		}
		
		if(null != _msgTimerTask) {
			_msgTimerTask.cancel();
			_msgTimerTask	= null;
		}
		
	}
	
	public void send(View v) {
		SwitchLogger.d(LOG_TAG, "send clicked");
		try {
			if(null == _client) {
				String logMsg	= "未连接，请先连接";
				Tip.show(this, logMsg);
				SwitchLogger.e(LOG_TAG, "not connected, connect first");
				return ;
			}
			
			String msg	= getInput(R.id.talk_client_msg);
			if(msg.equals("")) {
				Tip.show(this, "请输入消息");
				return ;
			}
			sendMsg(getInput(R.id.talk_client_to_uid), msg);
			_inputMsg.setText("");
		} catch (Exception e) {
			SwitchLogger.e(e);
		}
	}
	
	private void sendMsg(String toUid, String msg) throws Exception {
		String msgId	= _client.talk(toUid, msg);
		SwitchLogger.d(LOG_TAG, _uid + " send msg to " + toUid + ", msgId="+msgId+",msg="+msg);
		MsgItem msgItem	 = new MsgItem(msgId, System.currentTimeMillis(), _uid, toUid, msg, SEND_STATE_SENDING);
		synchronized (_msgItemListLock) {
			_msgItemList.add(msgItem);
		}
		_adapter.notifyDataSetChanged();
		_listView.setSelection(_msgItemList.size() - 1);
	}
	
	private void resendMsg(String toUid, String msg, String msgId) throws Exception {
		_client.talk(toUid, msg, msgId);
		SwitchLogger.d(LOG_TAG, _uid + " resend msg to " + toUid + ", msgId="+msgId+",msg="+msg);
	}
	
	private String getInput(int id) {
		EditText et	= (EditText)findViewById(id);
		return et.getText().toString();
	}
	
	public class MsgItem {
		public String msgId;
		public long timestamp;
		public String senderId;
		public String recverId;
		public String msg;
		public int sendState;
		
		public MsgItem(String id, long timestamp, String senderId, String recverId, String msg, int sendState) {
			this.msgId	= id;
			this.timestamp	= timestamp;
			this.senderId	= senderId;
			this.recverId	= recverId;
			this.msg	= msg;
			this.sendState	= sendState;
		}
	}
	
	public class MsgItemView {
		public View		view;
		public TextView	senderId;
		public TextView	time;
		public TextView	sendState;
		public TextView	msg;
		public Button	resendBtn;
		
		public MsgItemView(Context context) {
			
			view	= LayoutInflater.from(context).inflate(R.layout.item_talk_client_list_view, null);
			senderId	= (TextView)view.findViewById(R.id.item_talk_client_sender_id);
			time		= (TextView)view.findViewById(R.id.item_talk_client_send_time);
			sendState	= (TextView)view.findViewById(R.id.item_talk_client_send_state);
			msg			= (TextView)view.findViewById(R.id.item_talk_client_msg);
			resendBtn	= (Button)view.findViewById(R.id.item_talk_client_resend);
		}
	}
	
	public class ContentListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			synchronized (_msgItemListLock) {
				return _msgItemList.size();
			}
		}

		@Override
		public Object getItem(int position) {
			synchronized (_msgItemListLock) {
				return _msgItemList.get(position);
			}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			MsgItemView itemView;
			
			if(null == convertView) {
				itemView	= new MsgItemView(TalkClientSampleActivity.this);
				convertView	= itemView.view;
				convertView.setTag(itemView);
			} else {
				itemView	= (MsgItemView)convertView.getTag();
			}
			
			final MsgItem msgItem	= (MsgItem)getItem(position);
			
			itemView.senderId.setText(msgItem.senderId);
			itemView.time.setText(TimeUtil.timestampToDateStr(msgItem.timestamp));
			if(msgItem.sendState == SEND_STATE_FAIL) {
				itemView.sendState.setText("发送失败");
				itemView.resendBtn.setVisibility(View.VISIBLE);
				itemView.resendBtn.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						try {
							if( ! _client.isLogon()) {
								Tip.show(TalkClientSampleActivity.this, "未连接，请先连接");
								return ;
							}
							resendMsg(msgItem.recverId, msgItem.msg, msgItem.msgId);
							msgItem.timestamp	= System.currentTimeMillis();
						} catch (Exception e) {
							SwitchLogger.e(e);
						}
					}
				});
			} else if(msgItem.sendState == SEND_STATE_SENDING) {
				itemView.sendState.setText("正在发送中。。。");
				itemView.resendBtn.setVisibility(View.GONE);
			} else {
				itemView.sendState.setText("");
				itemView.resendBtn.setVisibility(View.GONE);
			}
			
			itemView.msg.setText(msgItem.msg);
			
			return convertView;
		}
		
	}
	
}
