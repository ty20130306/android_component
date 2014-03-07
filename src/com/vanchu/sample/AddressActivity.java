package com.vanchu.sample;

import java.util.ArrayList;
import java.util.List;

import com.vanchu.libs.addressBook.AddressBookActivity;
import com.vanchu.libs.addressBook.AddressBookData;
import com.vanchu.libs.addressBook.AddressBookItemView;
import com.vanchu.libs.webCache.WebCache;
import com.vanchu.test.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class AddressActivity extends AddressBookActivity{
	
	private static String[] LETTERS = new String[]{"#","A","B","C","D","E","F","G","H","I","J","K","L",
    	"M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
	
	public static final String TYPE_USER_HEAD_IMG = "type_cache_user_head_img";
	
	
	 public static final String[] sCheeseStrings = {"13456654",
         "Abbaye de Belloc", "Abbaye du Mont des Cats", "Abertam", "Abondance", "Ackawi",
         "Acorn", "Adelost",  "Avaxtskyr", "Baby Swiss",
         "Babybel", "Baguette Laonnaise", "Bakers", "Baladi", "Balaton", "Bandal", "Banon",
         "Barry's Bay Cheddar", "Basing", 
         "Buxton Blue", "Cabecou", "Caboc", "Crowdie", "Crowley",
         "Cuajada", "Curd", "Cure Nantais", "Curworthy", "Cwmtawe Pecorino",
         "Cypress Grove Chevre", "Danablu (Danish Blue)", "Danbo", "Danish Fontina",
         "Daralagjazsky", "Dauphin", "Delice des Fiouves", "Denhany Dorset Drum", "Derby",
         "Dessertnyj Belyj", "Emlett", "Emmental", "Epoisses de Bourgogne", "Esbareich",
         "Esrom", "Etorki", "Evansdale Farmhouse Brie", "Evora De L'Alentejo", "Exmoor Blue",
         "Explorateur", "Feta", "Fromage a Raclette", "Fromage Corse",
         "Fromage de Montagne de Savoie", "Fromage Frais", "Fruit Cream Cheese",
         "Frying Cheese", "Fynbo",
	 };

	 private View 		_footView;
	 private TextView 	_footText;
	 List<AddressBookData> 	personDataList;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_address);
		ListView contentListView = (ListView)findViewById(R.id.lv_datalist);
		LinearLayout indicatorLayout = (LinearLayout)findViewById(R.id.ll_indicator);
		initResId(R.layout.address_item, R.id.item_image, R.id.tv_data, R.layout.address_position, 
				R.id.tv_alphabar, R.drawable.default_potrait, contentListView, indicatorLayout);
		personDataList =  new ArrayList<AddressBookData>();;
		AddressBookData personData = null;
		for (int i = 0; i < sCheeseStrings.length; i++) {
			personData = new AddressBookData();
			personData.setName(sCheeseStrings[i]);
			personData.setIconURL("http://test.gmq.apps.vanchu.cn//attachments//144//265//201312231252b7b75e9e999802890509.jpg");
			if (i == 0) {
				personData.setLetter("#");
			}else{
				personData.setLetter("A");
			}
			if (i > 8 && i<20) {
				personData.setLetter("B");
			}else if(i > 19 && i < 30){
				personData.setLetter("C");
			}else if(i > 29 && i < 39){
				personData.setLetter("D");
			}else if(i > 38 && i < 48){
				personData.setLetter("E");
			}else if(i > 47){
				personData.setLetter("F");
			}
			personDataList.add(personData);
		}
		
		WebCache.Settings setting = new WebCache.Settings();
		setting.capacity = 200;
		setting.timeout  = 10*1000;
		WebCache webCache = WebCache.getInstance(this, TYPE_USER_HEAD_IMG);
		webCache.setup(setting);
		
		initData(LETTERS, personDataList, this ,webCache);
	}
	@Override
	public void beforeRefreshAdapter(List<AddressBookData> data, ListView listView, boolean isSetAdapter) {
		Log.d("tag", "addressactivity beforeRefreshAdapter , size:"+data.size());
		super.beforeRefreshAdapter(data, listView , isSetAdapter);
		if (null == _footView && null == _footText) {
			_footView = getLayoutInflater().inflate(R.layout.address_foot, null);
			
			_footText = (TextView)_footView.findViewById(R.id.foot_text);
		}
		String txt = null;
		if (data.size() == 0) {
			txt = "您还没有闺蜜哦~";
		}else{
			txt = "您有"+data.size()+"位闺蜜";
		}
		_footText.setText(txt);
		if (isSetAdapter) {
			listView.addFooterView(_footView);
		}
	}
	@Override
	public void itemClick(int position) {
		super.itemClick(position);
		Log.d("tag", "itemClick");
		personDataList.remove(position);
		updateData(personDataList);
	}
	
	@Override
	public void itemLongClick(final int position) {
		super.itemLongClick(position);
		Log.d("tag", "itemLongClick");
		AlertDialog.Builder builder = new AlertDialog.Builder(AddressActivity.this);
		builder.setMessage("是否删除闺蜜？");
		builder.setPositiveButton("确定", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				deletePerson(position);
			}
		});
		builder.setNegativeButton("取消", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	@Override
	public void handleNewView(View view,int position) {
		// TODO Auto-generated method stub
		super.handleNewView(view,position);
		ImageView image = (ImageView)view.findViewById(R.id.item_private);
		image.setBackgroundResource(R.drawable.private_conversation);
		image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			}
		});
	}
		
	
}
