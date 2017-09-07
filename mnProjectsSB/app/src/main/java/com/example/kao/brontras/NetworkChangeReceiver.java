package com.example.kao.brontras;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import static android.R.attr.type;

public class NetworkChangeReceiver extends BroadcastReceiver {  // check status internet
    @Override
    public void onReceive(final Context context, final Intent intent) { // ตรวจสอบ การเชื่อมต่อ เครือข่าย ตลอดที่ เปิด Application

        if (intent != null) {
            ConnectivityManager connMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMan.getActiveNetworkInfo();
            String str = "";
            if (networkInfo != null && networkInfo.isConnected()) {
                str = "เชื่อมต่อInternetแล้ว";
            } else {
                str = "ไม่ได้เชื่อมต่อInternet";
            }
            Toast.makeText(context, "" + str, Toast.LENGTH_LONG).show();
        }
    }
}