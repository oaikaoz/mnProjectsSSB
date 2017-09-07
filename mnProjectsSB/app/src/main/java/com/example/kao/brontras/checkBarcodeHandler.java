package com.example.kao.brontras;

import android.os.AsyncTask;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class checkBarcodeHandler extends AsyncTask<String, Void, String> {
    //การทำงานส่วนนี้เป็นการทำงาน Check ช้อมูลส่วนส่วน ทั้ง Barcode เเละ JOB_NO
    OkHttpClient client = new OkHttpClient();
    String Barcode, nameSer, userSer, passSer, dbName;

    public checkBarcodeHandler(String Barcode, String nameSer, String userSer, String passSer, String dbName) {

        this.Barcode = Barcode;
        this.nameSer = nameSer;
        this.userSer = userSer;
        this.passSer = passSer;
        this.dbName = dbName;
    }

    @Override
    protected String doInBackground(String... params) {
        //Post Server  okHTTP android
        RequestBody formBody = new FormEncodingBuilder()
                .add("barcode", Barcode)
                .add("job_no",Barcode)
                .add("nameSer", nameSer)
                .add("userSer", userSer)
                .add("passSer", passSer)
                .add("dbName", dbName)
                .build();
        Request request = new Request.Builder()
                .url(params[0])
                .post(formBody)
                .build();
        try {
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response.toString());
            return response.body().string();
        } catch (Exception e) {
        }
        return null;
    }
}