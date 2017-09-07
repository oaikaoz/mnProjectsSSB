package com.example.kao.brontras;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;

import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.edwardvanraak.materialbarcodescanner.MaterialBarcodeScanner;
import com.edwardvanraak.materialbarcodescanner.MaterialBarcodeScannerBuilder;
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ExportBarcodeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Barcode barcodeResult;
    public static final String BARCODE_KEY = "BARCODE";
    ListView listBarcode, listlotsize;
    TextView Scanner;
    ArrayList<HashMap<String, String>> alData,alDataEx,alDataPoNo; //ข้อมูลในList Barcode  //aldataEx คือ Data ที่เก็บข้อมูลจ่ายออก เเล้วนำไป Insert !!
    HashMap<String, String> hmData,hmDataEx;
    SharedPreferences data;
    SharedPreferences.Editor editor;
    Button btSave;
    MediaPlayer sound, miss;
    Spinner spFLG;
    ArrayList<String> List_flgID, List_flgName;
    String Barcodes, MSID, WHID;
    EditText etDocname,edTagBarcode;
    RelativeLayout rtlNoData,rtlNoDatas;
    NavigationView navigationView;
    String IP;
    ProgressDialog dialog;
    CustomAdapter_listlotsize adapter;
    String msids,flgids;
    CustomAdapter_listdataBarcodeExport adapterBarcode;
    ImageView imScan,imScanText;
    Boolean chkspFLG=false;
    int positionFLG=0 ;
    int positionItemListView = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_barcode);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); //Hide keybord onCreate

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("จ่ายออก");
        //setSubtitle("รับเข้าสินค้า");
        toolbar.setTitleTextColor(Color.WHITE);
        //toolbar.setSubtitleTextColor(Color.BLACK);
        toolbar.setBackgroundColor(Color.parseColor("#212121"));
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        alData =        new ArrayList<HashMap<String, String>>();
        alDataEx =      new ArrayList<HashMap<String,String >>();
        alDataPoNo =      new ArrayList<HashMap<String,String >>();
        data = getSharedPreferences("data", Context.MODE_PRIVATE);
        editor = data.edit();
        IP =data.getString("IP","");
        Barcodes = "";

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Scanner =     (TextView) findViewById(R.id.scanBarcode); //ปุ่ม Scanner barcode
        etDocname =     (EditText) findViewById(R.id.Docname);
        btSave =      (Button) findViewById(R.id.btSave);
        spFLG =       (Spinner) findViewById(R.id.spFlg);
        listBarcode = (ListView) findViewById(R.id.listlotsize);  //ListView barcode
        listlotsize = (ListView) findViewById(R.id.listexport);
        edTagBarcode =  (EditText) findViewById(R.id.tagBarcode);
        rtlNoData = (RelativeLayout) findViewById(R.id.rtlNoData);
        rtlNoDatas = (RelativeLayout)findViewById(R.id.rtlNoDatas);
        imScan = (ImageView)findViewById(R.id.imScan);
        imScanText = (ImageView)findViewById(R.id.imTextScan);

        List_flgID =         new ArrayList<String>(); //spiner ID
        List_flgName =       new ArrayList<String>();// spinername

        sound = MediaPlayer.create(ExportBarcodeActivity.this, R.raw.pass);
        miss = MediaPlayer.create(ExportBarcodeActivity.this, R.raw.miss);

        adapter = new CustomAdapter_listlotsize(getApplicationContext(),alDataEx); //set listview แบบ Custom
        adapterBarcode = new CustomAdapter_listdataBarcodeExport(getApplicationContext(),alData); //set listview แบบ Custom
        listlotsize.setAdapter(adapter);
        listBarcode.setAdapter(adapterBarcode);

        dialog = new ProgressDialog(ExportBarcodeActivity.this); // this = YourActivity
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("กำลังโหลด กรุณารอสักครู่.....");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();


        Show_flg showflg = new Show_flg();
        showflg.execute("http://" + IP + "/Barcode/show_flg_export.php");

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.bgColor));
        }
        setMenuList();

        Scanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alData.clear();
                startScan();
                adapter.notifyDataSetChanged();

            }
        });
        imScanText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                Barcodes=edTagBarcode.getText().toString();
                alData.clear();
                showLotsize showWH = new showLotsize();
                showWH.execute("http://" + IP + "/Barcode/show_export.php");
                adapter = new CustomAdapter_listlotsize(getApplicationContext(),alDataEx); //set listview แบบ Custom
                listlotsize.setAdapter(adapter);

            }
        });

        imScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanText();
            }
        });

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alData.size() == 0) {
                    Snackbar.make(v, "กรุณาสแกนบาร์โค้ด", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).setAction("สแกนบาร์โค้ด", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startScan();

                        }
                    }).show();
                } else if (alDataEx.size() == 0) {
                    Snackbar.make(v, "ยังไม่ได้เพิ่มข้อมูลLotsize", Snackbar.LENGTH_LONG)
                            .show();
                } else {
                    dialog.show();
                    insertBarcode insert = new insertBarcode();
                    insert.execute("http://" + IP + "/Barcode/insert_export_lotsize.php");
                }
            }
        });

        listBarcode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
                Object selectedItem = spFLG.getSelectedItem();
                String FLGNAME = selectedItem.toString();
                int position = List_flgName.indexOf(FLGNAME);
                String FLG = List_flgID.get(position);
                flgids = FLG;
                dialog.show();
                positionItemListView = arg2;
                showPoNo showspflg = new showPoNo();
                showspflg.execute("http://" + IP + "/Barcode/show_spflg_export.php");

            }
        });
    }

    public void EvenListviewItem(){

        final Dialog dialog = new Dialog(ExportBarcodeActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_export_sp); // lotsize
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));  // ตี่งค่า ให้พื้น หลัง โปร่ง เเสง
        dialog.setCancelable(true);


        Button btCancel = (Button) dialog.findViewById(R.id.btCancel);
        btCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext()
                        , "Close dialog", Toast.LENGTH_SHORT);
                dialog.cancel();
            }
        });
        positionFLG =0;
        final EditText lotsize = (EditText) dialog.findViewById(R.id.lotsize);
        lotsize.setEnabled(false);
        lotsize.setText(alData.get(positionItemListView).get("lotsize"));
        final EditText amount = (EditText) dialog.findViewById(R.id.amount);
        amount.setText(alData.get(positionItemListView).get("amount"));
        final Button btAdd = (Button) dialog.findViewById(R.id.btAdd);
        final Spinner spPoNo = (Spinner) dialog.findViewById(R.id.spFlg);
        final LinearLayout llnspinner = (LinearLayout)dialog.findViewById(R.id.llnspinner);


        if(chkspFLG==false){ //  false ==
            llnspinner.setVisibility(View.GONE);
        }else {
            llnspinner.setVisibility(View.VISIBLE);
            CustomAdapter_spinner_export customAdapter = new CustomAdapter_spinner_export(getApplicationContext(), alDataPoNo);
            spPoNo.setAdapter(customAdapter);
        }
        spPoNo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                positionFLG=arg2; // Var Position Of Spinner
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(positionFLG==0 && chkspFLG == true){
                    Toast.makeText(getApplicationContext(), "กรุณาเลือก Po No.", Toast.LENGTH_SHORT).show();
                }else {
                double bfamount = Double.parseDouble(alData.get(positionItemListView).get("amount"));  //จำนวนที่เหลือในlotsize
                double atamount = Double.parseDouble(amount.getText().toString());//จำนวนที่นำออก เปลี่ยน ชื่อตัวแปร ด้วย  แปลงเป็น Double
                if (atamount < 0) {
                    Toast.makeText(getApplicationContext(), "จำนวนน้อยกว่า 0 กรุณากรอกใหม่", Toast.LENGTH_SHORT).show();
                } else if (atamount > bfamount) {
                    Toast.makeText(getApplicationContext(), "จำนวนในคลัง ไม่พอกรุณากรอกใหม่", Toast.LENGTH_SHORT).show();
                } else {
                    alData.get(positionItemListView).put("amount", bfamount - atamount + "");

                    hmDataEx = new HashMap<String, String>();
                    hmDataEx.put("lotsize", alData.get(positionItemListView).get("lotsize"));
                    hmDataEx.put("amount", atamount + "");
                    hmDataEx.put("msid", alData.get(positionItemListView).get("msid"));
                    if( chkspFLG == true ) {
                        hmDataEx.put("ORDER_NO", alDataPoNo.get(positionFLG).get("ORDER_NO"));
                        hmDataEx.put("ITEM_DATE", alDataPoNo.get(positionFLG).get("ITEM_DATE"));
                        hmDataEx.put("CUST_NAME_TH", alDataPoNo.get(positionFLG).get("CUST_NAME_TH"));
                        hmDataEx.put("REMAIN_QTY", alDataPoNo.get(positionFLG).get("REMAIN_QTY"));
                    }
                    alDataEx.add(0, hmDataEx);
                    if((bfamount - atamount)==0){
                        //  rtlNoDatas.setVisibility(View.VISIBLE);
                        //  listlotsize.setVisibility(View.GONE);
                        rtlNoData.setVisibility(View.VISIBLE);
                        listBarcode.setVisibility(View.GONE);
                    }
                    rtlNoDatas.setVisibility(View.GONE);
                    listlotsize.setVisibility(View.VISIBLE);
                    adapterBarcode = new CustomAdapter_listdataBarcodeExport(getApplicationContext(), alData); //set listview แบบ Custom
                    listBarcode.setAdapter(adapterBarcode);
                    adapter.notifyDataSetChanged();
                    dialog.cancel();
                    }
                }
            }
        });
        dialog.show();

    }  //  การทำงานหลังจาก กด ListView ที่เอามาไ้ตรงนี้เพราะ การทำงาน ระหว่าง รอ การ Respone มันทำงานไม่ตรงตาม Procress

    private void startScan() {
        final MaterialBarcodeScanner materialBarcodeScanner = new MaterialBarcodeScannerBuilder()
                .withActivity(ExportBarcodeActivity.this)
                .withEnableAutoFocus(true)
                .withBleepEnabled(false) // ปิดเสียง หลังสแกนBarcode
                .withBackfacingCamera()
                .withCenterTracker()
                .withText("กำลังสแกน......")
                .withResultListener(new MaterialBarcodeScanner.OnResultListener() {
                    @Override
                    public void onResult(Barcode barcode) {
                        barcodeResult = barcode;
                        Barcodes = barcode.rawValue + "";
                        dialog.show();
                        showLotsize showWH = new showLotsize();
                        showWH.execute("http://" + IP + "/Barcode/show_export.php");
                    }
                })
                .build();
        materialBarcodeScanner.startScan();
    }

    private void startScanText() {
        final MaterialBarcodeScanner materialBarcodeScanner = new MaterialBarcodeScannerBuilder()
                .withActivity(ExportBarcodeActivity.this)
                .withEnableAutoFocus(true)
                .withBleepEnabled(false) // ปิดเสียง หลังสแกนBarcode
                .withBackfacingCamera()
                .withCenterTracker()
                .withText("กำลังสแกน......")
                .withResultListener(new MaterialBarcodeScanner.OnResultListener() {
                    @Override
                    public void onResult(Barcode barcode) {
                        etDocname.setText(barcode.rawValue);
                    }
                })
                .build();
        materialBarcodeScanner.startScan();
    }

    public class Show_flg extends AsyncTask<String, Void, String> {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
        @Override
        protected String doInBackground(String... params) {

            Request.Builder builder = new Request.Builder();
            builder.url(params[0]);
            RequestBody formBody = new FormBody.Builder()
                    .add("barcode","Barcodes")
                    .build();
            Request request = builder.url(params[0])
                    .post(formBody).build();

            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if(result!=null) {
                String jsonData = result;
                JSONObject Jobject = null;
                String PartNo = "";
                try {
                    Jobject = new JSONObject(jsonData);
                    JSONArray data = Jobject.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject message = data.getJSONObject(i);
                        List_flgID.add(message.getString("STOCKFLG_ID") + "");
                        List_flgName.add(message.getString("STOCKFLG_REMARK2") + "");
                        //ArrayAdapter<String>myAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item);
                    }

                    ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(ExportBarcodeActivity.this, android.R.layout.simple_spinner_item, List_flgName);
                    myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spFLG.setAdapter(myAdapter);


                } catch (JSONException e) {

                    e.printStackTrace();
                }
            }else{
                showTimeoutDialogshowfig("");
            }

        }
    }

    public class showLotsize extends AsyncTask<String, Void, String> {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
        @Override
        protected String doInBackground(String... params) {

            Request.Builder builder = new Request.Builder();
            builder.url(params[0]);
            RequestBody formBody = new FormBody.Builder()
                    .add("barcode",Barcodes)
                    .build();
            Request request = builder.url(params[0])
                    .post(formBody).build();

            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if(result!=null) {
                String jsonData = result;
                JSONObject Jobject = null;
                try {
                    Jobject = new JSONObject(jsonData);
                    JSONArray data = Jobject.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject message = data.getJSONObject(i);
                        //////tttt
                        if(Double.parseDouble(message.getString("QTY"))!=0) {
                            //////ttt
                            hmData = new HashMap<String, String>();
                            hmData.put("lotsize", message.getString("LOTSIZE"));
                            hmData.put("amount", message.getString("QTY"));
                            hmData.put("remark", message.getString("JOB_NO"));
                            hmData.put("msid", message.getString("MSID"));
                            hmData.put("TagNo", Barcodes);
                            alData.add(0, hmData);
                            WHID = message.getString("WHID") + "";
                            msids = message.getString("MSID") + "";
                        }

                    }
                    if (alData.size()==0) {
                        miss.start();
                        Snackbar.make(findViewById(android.R.id.content), "ไม่มีข้อมูล กรุณาสแกนบาร์โค้ด", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).setAction("สแกนบาร์โค้ด", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startScan();
                            }
                        }).show();
                        rtlNoData.setVisibility(View.VISIBLE);
                        listBarcode.setVisibility(View.GONE);

                    } else {
                        sound.start();
                        edTagBarcode.setText(Barcodes);
                        final CustomAdapter_listdataBarcodeExport adapterBarcode = new CustomAdapter_listdataBarcodeExport(getApplicationContext(),alData); //set listview แบบ Custom
                        listBarcode.setAdapter(adapterBarcode);
                        rtlNoData.setVisibility(View.GONE);
                        listBarcode.setVisibility(View.VISIBLE);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                showTimeoutDialog();
            }

        }
    }

    public class showPoNo extends AsyncTask<String, Void, String> {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
        @Override
        protected String doInBackground(String... params) {

            Request.Builder builder = new Request.Builder();
            builder.url(params[0]);
            RequestBody formBody = new FormBody.Builder()
                    .add("FLGID",flgids)
                    .add("MS_ID",msids)
                    .build();
            Request request = builder.url(params[0])
                    .post(formBody).build();

            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            chkspFLG=false;
            dialog.dismiss();
            if(result!=null) {
                chkspFLG=true;
                alDataPoNo.clear();
                String jsonData = result;
                JSONObject Jobject = null;
                try {
                    Jobject = new JSONObject(jsonData);
                    JSONArray data = Jobject.getJSONArray("data");
                    JSONObject message = data.getJSONObject(0);

                    for (int i = 0; i < data.length(); i++) {
                        JSONObject messages = data.getJSONObject(i);

                        if(i==0){
                            hmData = new HashMap<String, String>();
                            hmData.put("ORDER_NO", "Po No.");
                            hmData.put("ITEM_DATE", "Due.Date");
                            hmData.put("CUST_NAME_TH", "");
                            hmData.put("REMAIN_QTY", "Order QTY");
                            alDataPoNo.add(0, hmData);
                        }
                        hmData = new HashMap<String, String>();
                        hmData.put("ORDER_NO", messages.getString("ORDER_NO"));
                        hmData.put("ITEM_DATE", messages.getString("ITEM_DATE"));
                        hmData.put("CUST_NAME_TH", messages.getString("CUST_NAME_TH"));
                        hmData.put("REMAIN_QTY", messages.getString("REMAIN_QTY"));
                        alDataPoNo.add(hmData);

                    }

                } catch (JSONException e) {
                    // Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();
                    chkspFLG=false;
                    e.printStackTrace();
                }
            }else{
                showTimeoutDialog();
            }
            EvenListviewItem();

        }
    }

    public Boolean checkVar(){  //เช็ตตัว Tag Barcode ไม่ให้ ขึ้นอันซ้ำ

        Boolean CHECK=false;
        for(int position =0; position< alData.size();position++) {
            if(alData.get(position).get("TagNo").equals(Barcodes)){
                CHECK = true;
            }
        }
        return  CHECK;
    }

    public String toJSON() {
        String jsons = "";
        try {
            ArrayList<JSONObject> MyArrJson = new ArrayList<JSONObject>();
            JSONObject object;
            String docname = etDocname.getText().toString();
            String presonalID = data.getString("PERSONAL_ID", null);


            Object selectedItem = spFLG.getSelectedItem();
            String FLGNAME = selectedItem.toString();
            int position = List_flgName.indexOf(FLGNAME);
            String FLG = List_flgID.get(position);

            for (int i = 0; i < alDataEx.size(); i++) {
                object = new JSONObject();
                object.put("DocNo", docname);
                object.put("FLG", FLG);
                object.put("WHID", WHID);
                object.put("lotsize", alDataEx.get(i).get("lotsize"));
                object.put("amount", alDataEx.get(i).get("amount"));
                object.put("barcode",Barcodes);
                object.put("presonalID",presonalID);
                object.put("MSID", alDataEx.get(i).get("msid"));
                if(alDataEx.get(i).get("ORDER_NO")!=null) {
                    object.put("PONO", alDataEx.get(i).get("ORDER_NO"));
                    object.put("DueDate", alDataEx.get(i).get("ITEM_DATE"));
                    object.put("Cust", alDataEx.get(i).get("CUST_NAME_TH"));

                }

                MyArrJson.add(object);
            }
            JSONArray json = new JSONArray(MyArrJson);

            jsons = json.toString();
            // tvJSON.setText(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsons;
    }

    public class insertBarcode extends AsyncTask<String, Void, String> {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10,TimeUnit.SECONDS)
                .build();

        String json = toJSON();
        @Override
        protected String doInBackground(String... params) {

            Request.Builder builder = new Request.Builder();
            builder.url(params[0]);
            RequestBody formBody = new FormBody.Builder()
                    .add("JSON",json)
                    .build();
            Request request = builder .url(params[0])
                    .post(formBody).build();

            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if(result!=null||result.equals("")){
                Toast.makeText(getApplication(), "บันทึกเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show();
                alData.clear();
                alDataEx.clear();
                etDocname.setText("");
                adapter = new CustomAdapter_listlotsize(getApplicationContext(),alDataEx); //set listview แบบ Custom
                adapterBarcode = new CustomAdapter_listdataBarcodeExport(getApplicationContext(),alData); //set listview แบบ Custom
                listlotsize.setAdapter(adapter);
                listBarcode.setAdapter(adapterBarcode);
                rtlNoData.setVisibility(View.VISIBLE);
                rtlNoDatas.setVisibility(View.VISIBLE);
                listBarcode.setVisibility(View.GONE);
                listlotsize.setVisibility(View.GONE);


            }else {
                showTimeoutDialog();
            }

        }
    }

    public void showTimeoutDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                ExportBarcodeActivity.this);
        builder.setTitle("แจ้งเตือน");
        builder.setMessage("การเชื่อมต่อมีปัญหากรุณาตรวจสอบIntenet");
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {
                    }
                });
        builder.show();
    }

    public void setMenuList(){
        String menuList = data.getString("MenuList", null);
        String[] MenuList = menuList.split(",");

        Menu menu  = navigationView.getMenu();
        for(int menuIndex = 0; menuIndex < menu.size(); menuIndex++){
            MenuItem menuItem = menu.getItem(menuIndex);
            menuItem.setVisible(false);
            if(Arrays.asList(MenuList).contains(Integer.toString(menuIndex+1))){
                menuItem.setVisible(true);
            }

        }
    }

    public String showTimeoutDialogshowfig(final String check) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                ExportBarcodeActivity.this);
        builder.setTitle("แจ้งเตือน");
        builder.setMessage("การเชื่อมต่อมีปัญหากรุณาตรวจสอบ Intenet ");
        builder.setPositiveButton("ลองอีกครั้ง",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogs,
                                        int which) {
                        dialog.show();
                        Show_flg showflg = new Show_flg();
                        showflg.execute("http://" + IP + "/Barcode/show_flg_export.php");
                    }
                });
        builder.setNegativeButton("ออกจากโปรแกรม",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        finish();
                    }
                });

        builder.show();
        return null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(BARCODE_KEY, barcodeResult);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { // ขออนุญาติกาใช้งาน กล้อง android ver 6.0 +
        if (requestCode != MaterialBarcodeScanner.RC_HANDLE_CAMERA_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScan();
            return;
        }
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error")
                .setMessage("ไม่สามารถเชื่อมต่อกล้องได้.....")
                .setPositiveButton(android.R.string.ok, listener)
                .show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            editor.clear();
            editor.commit();
            Intent intent = new Intent(ExportBarcodeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(ExportBarcodeActivity.this, settingServerActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_import) {
            Intent intent = new Intent(ExportBarcodeActivity.this, ImportBarcodeActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_check){
            Intent intent = new Intent(ExportBarcodeActivity.this, showCheckBarcodeActivity.class);
            startActivity(intent);
            finish();
        }else  if (id == R.id.nav_checkStore) {
            Intent intent = new Intent(ExportBarcodeActivity.this, CheckBarcodeActivity.class);
            startActivity(intent);
            finish();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean
    onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bar_export, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.action_save){
            if (alData.size() == 0) {
                Snackbar.make(findViewById(android.R.id.content), "กรุณาสแกนบาร์โค้ด", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).setAction("สแกนบาร์โค้ด", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startScan();

                    }
                }).show();
            } else if (alDataEx.size() == 0) {
                Snackbar.make(findViewById(android.R.id.content), "ยังไม่ได้เพิ่มข้อมูลLotsize", Snackbar.LENGTH_LONG)
                        .show();
            } else {
                dialog.show();
                insertBarcode insert = new insertBarcode();
                insert.execute("http://" + IP + "/Barcode/insert_export_lotsize.php");

               // Toast.makeText(getApplicationContext(),""+toJSON(),Toast.LENGTH_LONG).show();

            }
        }else
        if(id== R.id.action_scan){
            alData.clear();
            startScan();
            adapter = new CustomAdapter_listlotsize(getApplicationContext(),alDataEx); //set listview แบบ Custom
            listlotsize.setAdapter(adapter);
        }
        return super.onOptionsItemSelected(item);
    }
}
