package com.example.kao.brontras;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;


public class CustomAdapter_listdataBarcodeExport extends BaseAdapter {
    Context mContext;
    ArrayList<HashMap<String, String>> Data;

    public CustomAdapter_listdataBarcodeExport(Context context,ArrayList<HashMap<String, String>> data) {
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
            view = mInflater.inflate(R.layout.list_row_databarcode, parent, false);

        TextView lotsize = (TextView) view.findViewById(R.id.lotsize);
        TextView remark = (TextView) view.findViewById(R.id.Remark);
        TextView amount = (TextView) view.findViewById(R.id.Amount);
        TextView jobnos = (TextView)view.findViewById(R.id.jobno);
        jobnos.setText("Job No. :");
        lotsize.setText(Data.get(position).get("lotsize"));
        amount.setText(Data.get(position).get("amount"));
        remark.setText(Data.get(position).get("remark"));
        return view;
    }
}