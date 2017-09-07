package com.example.kao.brontras;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Kao on 5/31/2017.
 */


    public class CustomAdapter_spinner_export extends BaseAdapter {
        Context context;
    ArrayList<HashMap<String, String>> Data;
        LayoutInflater inflter;

        public CustomAdapter_spinner_export(Context applicationContext,ArrayList<HashMap<String, String>> data) {
            this.context = applicationContext;

            this.Data = data;
            inflter = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return Data.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = inflter.inflate(R.layout.custom_spinner_items, null);

            TextView tvPoNo = (TextView) view.findViewById(R.id.tvPoNo);
            TextView tvDueDate = (TextView) view.findViewById(R.id.tvDueDate);
           // TextView tvCust = (TextView) view.findViewById(R.id.tvCust);
            TextView tvOrderQTY = (TextView) view.findViewById(R.id.tvOrderQTY);
            if ( i  == 0) {
                tvPoNo.setTextSize(15f);
                tvPoNo.setTextColor(Color.parseColor("#686868"));
                tvDueDate.setTextSize(15f);
                tvDueDate.setTextColor(Color.parseColor("#686868"));
                tvOrderQTY.setTextSize(15f);
                tvOrderQTY.setTextColor(Color.parseColor("#686868"));

            }else{
                tvPoNo.setTextSize(13f);
                tvPoNo.setTextColor(Color.parseColor("#afafaf"));
                tvDueDate.setTextSize(13f);
                tvDueDate.setTextColor(Color.parseColor("#afafaf"));
                tvOrderQTY.setTextSize(13f);
                tvOrderQTY.setTextColor(Color.parseColor("#afafaf"));

            }
            tvPoNo.setText(Data.get(i).get("ORDER_NO"));
            tvDueDate.setText(Data.get(i).get("ITEM_DATE"));
           // tvCust.setText(Data.get(i).get("CUST_NAME_TH"));
            tvOrderQTY.setText(Data.get(i).get("REMAIN_QTY"));
            return view;
        }
}
