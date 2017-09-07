package com.example.kao.brontras;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;


public class CustomAdapter_show_check_barcode extends BaseAdapter {
    Context mContext;
    ArrayList<HashMap<String, String>> Data;

    public CustomAdapter_show_check_barcode(Context context,ArrayList<HashMap<String, String>> data) {
        this.mContext = context;
        this.Data = data;

    }

    public int getCount() {
        return Data.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater mInflater =
                (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (view == null)
            view = mInflater.inflate(R.layout.list_show_check_listbarcode, parent, false);
        TextView TagNos = (TextView) view.findViewById(R.id.TagNo);
        TextView MSids = (TextView)view.findViewById(R.id.MSid);
        TextView QTYs =(TextView)view.findViewById(R.id.Qty);
        TextView Serials = (TextView)view.findViewById(R.id.Serial);
        TagNos.setText(Data.get(position).get("TagNo"));
        MSids.setText(Data.get(position).get("MSid"));
        QTYs.setText(Data.get(position).get("Amount"));
        Serials.setText(Data.get(position).get("LotNo"));


        return view;
    }
}