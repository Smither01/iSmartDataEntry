package com.guogu.ismartdataentry;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DataAdapter extends BaseAdapter{

	private Context context;
	private List<JSONObject>  list;
	private OnClickListener listener;
	public DataAdapter(Context context,List<JSONObject>  list,OnClickListener listener){
		this.context = context;
		this.list = list;
		this.listener = listener;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.brank_list_item, null);
			viewHolder.model_text = (TextView)convertView.findViewById(R.id.model_front_Name);
			viewHolder.buttonTest = (RelativeLayout)convertView.findViewById(R.id.testImage_parent);
			viewHolder.buttonADD = (TextView)convertView.findViewById(R.id.model_add);
			convertView.setTag(viewHolder);
		}else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		Log.v("LZP", "position:"+position);
		if (position == list.size()-1) {
			viewHolder.buttonADD.setVisibility(View.VISIBLE);
			viewHolder.buttonTest.setVisibility(View.GONE);
			viewHolder.model_text.setVisibility(View.GONE);
		}else {
			viewHolder.buttonADD.setVisibility(View.GONE);
			viewHolder.buttonTest.setVisibility(View.VISIBLE);
			viewHolder.model_text.setVisibility(View.VISIBLE);
		}	
		try {
			viewHolder.model_text.setText("第"+(position+1)+"个设备"+" MAC:"+list.get(position).getString("ad"));
			viewHolder.buttonTest.setTag(position);
			viewHolder.buttonTest.setOnClickListener(listener);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return convertView;
	}
	
	private static class ViewHolder {
		TextView model_text;
		RelativeLayout buttonTest;
		TextView buttonADD;
	}

}
