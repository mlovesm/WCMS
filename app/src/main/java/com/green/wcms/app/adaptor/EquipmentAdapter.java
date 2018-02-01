package com.green.wcms.app.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.green.wcms.app.R;

import java.util.ArrayList;
import java.util.HashMap;


public class EquipmentAdapter extends BaseAdapter{

	private LayoutInflater inflater;
	private ArrayList<HashMap<String,Object>> boardList;
	private ViewHolder viewHolder;
	private Context con;


	public EquipmentAdapter(Context con , ArrayList<HashMap<String,Object>> array){
		inflater = LayoutInflater.from(con);
		boardList = array;
		this.con = con;
	}

	@Override
	public int getCount() {
		return boardList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(final int position, final View convertview, ViewGroup parent) {

		View v = convertview;

		if(v == null){
			viewHolder = new ViewHolder();

			v = inflater.inflate(R.layout.equipment_list_item, parent,false);
			viewHolder.board_data1 = (TextView)v.findViewById(R.id.textView1);
			viewHolder.board_data2 = (TextView)v.findViewById(R.id.textView2);
			viewHolder.board_data3 = (TextView)v.findViewById(R.id.textView3);
			viewHolder.board_data4 = (TextView)v.findViewById(R.id.textView4);

			v.setTag(viewHolder);

		}else {
			viewHolder = (ViewHolder)v.getTag();
		}
		viewHolder.board_data1.setText(boardList.get(position).get("data1").toString());
		viewHolder.board_data2.setText(boardList.get(position).get("data2").toString());
		viewHolder.board_data3.setText(boardList.get(position).get("data3").toString());
		viewHolder.board_data4.setText(boardList.get(position).get("data4").toString());

		return v;
	}

	
	public void setArrayList(ArrayList<HashMap<String,Object>> arrays){
		this.boardList = arrays;
	}
	
	public ArrayList<HashMap<String,Object>> getArrayList(){
		return boardList;
	}
	
	
	/*
	 * ViewHolder
	 */
	class ViewHolder{
		TextView board_data1;
		TextView board_data2;
		TextView board_data3;
		TextView board_data4;
	}
	

}







