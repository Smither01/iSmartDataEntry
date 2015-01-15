package com.guogu.ismartdataentry;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener,OnItemClickListener{

	private ListView listView;
	private DataAdapter adapter;
	private List<JSONObject> listJson;
	private JSONObject objAdd;
	private boolean isAddPackage = false;
	private Button text;
	private String pkgID;
	private String pkgPwd;
	private ProgressDialog progressDialog;
	private Button back;
	
	private final static int PACKAGE_REQUEST = 0xFFFF;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listJson = new ArrayList<JSONObject>();
		objAdd = new JSONObject();
		try {
			objAdd.put("org", "ismart");
			objAdd.put("tp", "EXAMPLE");
			objAdd.put("ad", "FF-FF-FF-FF-FF-FF-FF-FF");
			objAdd.put("ver", "1");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		listJson.add(objAdd);
		listView = (ListView)findViewById(R.id.listView);
		adapter = new DataAdapter(this, listJson, this);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		text = (Button)findViewById(R.id.editBtn);
		text.setText("添加套餐");
		text.setOnClickListener(this);
		back = (Button)findViewById(R.id.backBtn);
		back.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.testImage_parent:
			int location  = Integer.valueOf(v.getTag().toString());
			listJson.remove(location);
			adapter.notifyDataSetChanged();
			break;
		case R.id.editBtn:
			if (!isAddPackage) {
				Intent intent = new Intent(this, MipcaActivityCapture.class);
				startActivityForResult(intent, PACKAGE_REQUEST);
			}
			else {
				//调用远程  提交
				if (listJson.size() < 2) {
					Toast.makeText(this, "请添加设备", Toast.LENGTH_SHORT).show();
					return;
				}else {
					progressDialog = ProgressDialog.show(this, "正在发送数据","请稍候", true, false);
					sendData();
				}
			}
			break;
		case R.id.backBtn:
			isAddPackage = false;
			text.setText("添加套餐");
			listJson.clear();
			listJson.add(objAdd);
			adapter.notifyDataSetChanged();
			break;
		default:
			break;
		}
	}
	
	
	private void sendData(){
		JSONArray arr = new JSONArray();
		for (int i = 0; i < (listJson.size()-1); i++) {
			arr.put(listJson.get(i));
		}
		Log.v("LZP", arr.toString());
		String code = "";
		try {
			code =enCrypt(pkgPwd);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Toast.makeText(MainActivity.this, "MD5加密失败,请重试!", Toast.LENGTH_LONG).show();
			return;
		} 
		RemoteUserService.sendData(pkgID, code, arr, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int statusCode, String content) {
				// TODO Auto-generated method stub
				if (progressDialog.isShowing()) {
					progressDialog.dismiss();	
				}
				if (statusCode == 200) {
					try {
						JSONObject json = new JSONObject(content);
						if (json.getBoolean("result")) {
							Toast.makeText(MainActivity.this, "提交成功!", Toast.LENGTH_LONG).show();
							isAddPackage = false;
							text.setText("添加套餐");
							listJson.clear();
							listJson.add(objAdd);
							adapter.notifyDataSetChanged();
						}
						else {
							Toast.makeText(MainActivity.this, json.getString("value"), Toast.LENGTH_LONG).show();
						}
				} catch (Exception e) {
					// TODO: handle exception
					Toast.makeText(getApplicationContext(), "数据解析异常", Toast.LENGTH_LONG).show();
				}
				}
			}
			
			@Override
			public void onFailure(Throwable error, String content) {
				// TODO Auto-generated method stub
				if (progressDialog.isShowing()) {
					progressDialog.dismiss();	
				}
				Toast.makeText(getApplicationContext(), "请检查网络是否联通.", Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) {
			return;
		}
		if (requestCode == PACKAGE_REQUEST) {
			//该处更新套餐信息
			try {
				text.setText("提交");
				isAddPackage = true;
				JSONObject obj;		
				obj = new JSONObject(data.getExtras().get("result").toString());
				pkgID = obj.getString("pkgid");
				pkgPwd = obj.getString("pwd");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				isAddPackage = false;
				text.setText("添加套餐");
				e.printStackTrace();
				Log.v("LZP", e.toString());
				Toast.makeText(this, "请扫描正确的二维码!", Toast.LENGTH_SHORT).show();
				return;
			}
		}else {
			
			try {
				JSONObject obj;
				obj = new JSONObject(data.getExtras().get("result").toString());
				String address = obj.getString("ad");
				Log.v("LZP", address.toString());
				for (int i = 0; i < listJson.size(); i++) {
					String addressTemp = listJson.get(i).getString("ad");
					if (address.equals(addressTemp)) {
						Toast.makeText(this, "该设备已添加过,位于第"+(i+1)+"位置.", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				if (listJson.size() == (requestCode+1)) {									
						Log.v("LZP","Add obj:"+obj.toString());
						listJson.remove(listJson.size()-1);
						listJson.add(obj);
						listJson.add(objAdd);
				}else {
					Log.v("LZP", "Set listJson Position:"+requestCode);
					listJson.set(requestCode, obj);
				}			
				adapter.notifyDataSetChanged();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.v("LZP", e.toString());
				Toast.makeText(this, "请扫描正确的二维码!", Toast.LENGTH_SHORT).show();
				return;
			}
			
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, MipcaActivityCapture.class);
		startActivityForResult(intent, arg2);
	}
	
	public static String enCrypt(String input) throws Exception{
		MessageDigest mess;
		String output = "";
		mess = MessageDigest.getInstance("MD5");
		mess.update(input.getBytes()); 
		output = bytetoString(mess.digest());
		return output;
	}
	
	public static String bytetoString(byte[] digest) {

		int i = 0;
		StringBuffer buf = new StringBuffer("");
		for (int offset = 0; offset < digest.length; offset++) {
			i = digest[offset];
			if (i < 0)
				i += 256;
			if (i < 16)
				buf.append("0");
			buf.append(Integer.toHexString(i));
		}

		return buf.toString();

	}
}
