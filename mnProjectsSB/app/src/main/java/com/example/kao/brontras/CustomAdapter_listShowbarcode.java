package com.example.kao.brontras;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;


public class CustomAdapter_listShowbarcode extends BaseAdapter {
    Context mContext;
    ArrayList<String> lotsizes,amounts,jobno,updateBy,updateDate;

    public CustomAdapter_listShowbarcode(Context context, ArrayList<String> lotsize ,ArrayList<String> amount ,ArrayList<String> jobno,ArrayList<String> updateBy,ArrayList<String> updateDate) {
        this.mContext = context;
        this.lotsizes = lotsize;
        this.amounts = amount;
        this.jobno=jobno;
        this.updateBy=updateBy;
        this.updateDate=updateDate;

    }

    public int getCount() {
        return lotsizes.size();
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
            view = mInflater.inflate(R.layout.list_row_showcheckbarcode, parent, false);

        TextView lotsize = (TextView) view.findViewById(R.id.lotsize);
        TextView JobNos = (TextView) view.findViewById(R.id.JobNo);
        TextView amount = (TextView) view.findViewById(R.id.Amount);
        TextView jobnos = (TextView)view.findViewById(R.id.JobNo);
        TextView updateBys = (TextView)view.findViewById(R.id.UpdateBy);
        TextView updateDates = (TextView)view.findViewById(R.id.UpdateDate);
        jobnos.setText(jobno.get(position));
        lotsize.setText(lotsizes.get(position)+"");
        JobNos.setText(jobno.get(position)+"");
        amount.setText(amounts.get(position)+"");
        updateBys.setText(updateBy.get(position)+"");
        updateDates.setText(updateDate.get(position)+"");


        return view;
    }
}