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
import android.media.Image;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;


import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImportBarcodeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Barcode barcodeResult;
    public static final String BARCODE_KEY = "BARCODE";
    ListView listBarcode;
    ArrayList<HashMap<String, String>> alData; //ข้อมูลในList Barcode
    HashMap<String, String> hmData;
    TextView tvJOBNo,tvPARTNo;
    EditText etDocname,edTagBarcode;
    SharedPreferences data;
    SharedPreferences.Editor editor;
    Button btAdd, btSave, btInformation;
    Spinner spWH,spFLG;
    String Barcodes, JobNo, MSID;
    ImageView imScan,imTextScan;
    ArrayList<String> List_flgID, List_flgName;
    MediaPlayer sound, miss;
    ArrayList<String> List_WHname, List_IDWH;//Arraylist WH_NAME ใช้ กับ spinner
    String IP;
    ProgressDialog dialog;
    RelativeLayout rtlNoData;
    NavigationView navigationView;
            //, floatingActionButton3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_barcode);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); //Hide keybord onCreate

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("นำเข้าสโตร์");
        //setSubtitle("รับเข้าสินค้า");
        toolbar.setTitleTextColor(Color.WHITE);
        //toolbar.setSubtitleTextColor(Color.BLACK);
        toolbar.setBackgroundColor(Color.parseColor("#212121"));
        setSupportActionBar(toolbar);


        btAdd =         (Button) findViewById(R.id.btAdd);
        btSave =        (Button) findViewById(R.id.btSave);
        btInformation = (Button) findViewById(R.id.btInformation);
        tvJOBNo =       (TextView) findViewById(R.id.JOB_NO);
        tvPARTNo =      (TextView) findViewById(R.id.PART_NO);
        edTagBarcode =  (EditText) findViewById(R.id.tagBarcode);
        etDocname =     (EditText) findViewById(R.id.Docname);
        spWH =          (Spinner) findViewById(R.id.spWH);
        spFLG =         (Spinner) findViewById(R.id.spFlg);
        listBarcode =   (ListView) findViewById(R.id.listlotsize);
        imScan =        (ImageView) findViewById(R.id.imScan);
        imTextScan = (ImageView)findViewById(R.id.imTextScan);
        rtlNoData = (RelativeLayout) findViewById(R.id.rtlNoData);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);


       // floatingActionButton3 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item3);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        List_flgID =         new ArrayList<String>(); //spiner ID
        List_flgName =       new ArrayList<String>();// spinername
        alData =        new ArrayList<HashMap<String, String>>();
        List_WHname =   new ArrayList<String>();
        List_IDWH =     new ArrayList<String>();
        sound = MediaPlayer.create(ImportBarcodeActivity.this, R.raw.pass);
        miss =  MediaPlayer.create(ImportBarcodeActivity.this, R.raw.miss);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.bgColor));
        }

        data = getSharedPreferences("data", Context.MODE_PRIVATE);
        editor = data.edit();
        IP = data.getString("IP", "");

        dialog = new ProgressDialog(ImportBarcodeActivity.this); // this = YourActivity
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("กำลังโหลด กรุณารอสักครู่.....");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        JobNo = "";
        Show_flg showflg = new Show_flg();
        showflg.execute("http://" + IP + "/Barcode/show_flg_import.php");

        setMenuList();

        final CustomAdapter_listdataBarcodeimport adapter = new CustomAdapter_listdataBarcodeimport(getApplicationContext(), alData); //set listview แบบ Custom
        listBarcode.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (JobNo.equals("")) {
                    Snackbar.make(findViewById(android.R.id.content), "กรุณาสแกนบาร์โค้ด", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).setAction("สแกนบาร์โค้ด", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startScan();
                        }
                    }).show();
                } else {
                    final Dialog dialog = new Dialog(ImportBarcodeActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.dialog_lotsize);//dialog/////////
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

                    final EditText lotsize = (EditText) dialog.findViewById(R.id.lotsize);
                    lotsize.setText("A55");
                    final EditText amount = (EditText) dialog.findViewById(R.id.amount);
                    final EditText remark = (EditText) dialog.findViewById(R.id.remark);
                    final Button btAdd = (Button) dialog.findViewById(R.id.btAdd);
                    final Button btAdds = (Button) dialog.findViewById(R.id.btAdd);
                    btAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(lotsize.getText().toString()==null||lotsize.getText().toString().equals("")||
                                    amount.getText().toString()==null||amount.getText().toString().equals("")){
                                Toast.makeText(ImportBarcodeActivity.this,"กรุณากรอกข้อมูลให้ครบ",Toast.LENGTH_LONG).show();
                            }else {
                                hmData = new HashMap<String, String>();
                                hmData.put("lotsize", lotsize.getText().toString());
                                hmData.put("amount", amount.getText().toString());
                                hmData.put("remark", remark.getText().toString());
                                alData.add(hmData);
                                adapter.notifyDataSetChanged();

                                    rtlNoData.setVisibility(View.GONE);
                                    listBarcode.setVisibility(View.VISIBLE);

                                dialog.cancel();
                            }
                        }
                    });
                    dialog.show();
                }
            }
        });
        imScan.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
               startScanText();
            }
        });
        imTextScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                Barcodes=edTagBarcode.getText().toString();

                Show_Spiner_WH showWH = new Show_Spiner_WH();
                showWH.execute("http://" + IP + "/Barcode/checkBarcode.php");
            }
        });

        btInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (JobNo.equals("")) {
                    Snackbar.make(v, "กรุณาสแกนบาร์โค้ด", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).setAction("สแกนบาร์โค้ด", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startScan();

                        }
                    }).show();
                } else {
                    dialog.show();
                    showInformation showInfo = new showInformation();
                    showInfo.execute("http://" + IP + "/Barcode/show_information_export.php");
                }
            }
        });
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alData.size() == 0) {
                    Snackbar.make(v, "ยังไม่มีข้อมูลกรุณาเพิ่มข้อมูล", Snackbar.LENGTH_LONG)
                            .show();
                } else {
                    if (etDocname.getText().toString() == null && etDocname.getText().toString().equals("")) {
                        Snackbar.make(v, "กรุณากรอกเลที่อ้างอิงเอกสาร", Snackbar.LENGTH_LONG)
                                .show();
                    } else {
                        Toast.makeText(getApplicationContext(),"test : "+toJSON(),Toast.LENGTH_LONG).show();
                        dialog.show();
                        insertBarcode insert = new insertBarcode();
                        insert.execute("http://" + IP + "/Barcode/insert_Barcode.php");
                    }
                }
            }
        });
        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (JobNo.equals("")) {
                    Snackbar.make(v, "กรุณาสแกนบาร์โค้ด", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).setAction("สแกนบาร์โค้ด", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startScan();
                        }
                    }).show();
                } else {
                    final Dialog dialog = new Dialog(ImportBarcodeActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.dialog_lotsize);//dialog/////////
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

                    final EditText lotsize = (EditText) dialog.findViewById(R.id.lotsize);
                    lotsize.setText("A55");
                    final EditText amount = (EditText) dialog.findViewById(R.id.amount);
                    final EditText remark = (EditText) dialog.findViewById(R.id.remark);
                    final Button btAdd = (Button) dialog.findViewById(R.id.btAdd);
                    final Button btAdds = (Button) dialog.findViewById(R.id.btAdd);
                    btAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(lotsize.getText().toString()==null||lotsize.getText().toString().equals("")||
                                    amount.getText().toString()==null||amount.getText().toString().equals("")){
                                Toast.makeText(ImportBarcodeActivity.this,"กรุณากรอกข้อมูลให้ครบ",Toast.LENGTH_LONG).show();
                            }else {
                                hmData = new HashMap<String, String>();
                                hmData.put("lotsize", lotsize.getText().toString());
                                hmData.put("amount", amount.getText().toString());
                                hmData.put("remark", remark.getText().toString());
                                alData.add(hmData);
                                adapter.notifyDataSetChanged();
                                dialog.cancel();
                            }
                        }
                    });
                    dialog.show();
                }
            }
        });
        listBarcode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                AlertDialog.Builder adb = new AlertDialog.Builder(ImportBarcodeActivity.this);
                adb.setTitle("ลบข้อมูล");
                adb.setMessage("คุณต้องการลบ : " + alData.get(arg2).get("lotsize"));
                final int positionToRemove = arg2;  // set ตำแหน่ง
                adb.setNegativeButton("ไม่ลบ", null);
                adb.setPositiveButton("ลบ", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        alData.remove(positionToRemove);
                        adapter.notifyDataSetChanged();
                        if(alData.size()==0) {
                            rtlNoData.setVisibility(View.VISIBLE);
                            listBarcode.setVisibility(View.GONE);
                        }
                    }
                });
                adb.show();
            }
        });
    }

    public void ClearVar(){
        JobNo = "";
        edTagBarcode.setText("");
        tvJOBNo.setHint("JOB No.  :");
        tvPARTNo.setHint("PART No. :");
        List_WHname.clear();
        List_IDWH.clear();
        spWH.setAdapter(null);// ตรวจสอบ
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

    private void startScan() {
        //ClearVar();  // Set ตัวแปร ค่า
        final MaterialBarcodeScanner materialBarcodeScanner = new MaterialBarcodeScannerBuilder()
                .withActivity(ImportBarcodeActivity.this)
                .withEnableAutoFocus(true)
                .withBleepEnabled(false) // ปิดเสียง หลังสแกนBarcode
                .withBackfacingCamera()
                .withCenterTracker()
                .withText("กำลังสแกน......")
                .withResultListener(new MaterialBarcodeScanner.OnResultListener() {
                    @Override
                    public void onResult(Barcode barcode) {
                        dialog.show();
                        barcodeResult = barcode;
                        Barcodes = barcode.rawValue + "";
                        Show_Spiner_WH showWH = new Show_Spiner_WH();
                        showWH.execute("http://" + IP + "/Barcode/checkBarcode.php");
                    }
                })
                .build();
        materialBarcodeScanner.startScan();
    }

    private void startScanText() {
        final MaterialBarcodeScanner materialBarcodeScanner = new MaterialBarcodeScannerBuilder()
                .withActivity(ImportBarcodeActivity.this)
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
                rtlNoData.setVisibility(View.VISIBLE);
                listBarcode.setVisibility(View.GONE);
                ClearVar();
                alData.clear();
                etDocname.setText("");
                CustomAdapter_listdataBarcodeimport adapter = new CustomAdapter_listdataBarcodeimport(getApplicationContext(), alData); //set listview แบบ Custom
                listBarcode.setAdapter(adapter);
                Snackbar.make(findViewById(android.R.id.content), "บันทึกเรียบร้อยแล้ว", Snackbar.LENGTH_LONG)
                        .show();
            }else {
              showTimeoutDialog();
            }
        }
    }  //ยังไม่ได้ทำการเปลี่ยน ฝั่ง PHP   เหลอ Test ก่อน Upขึ้น Server

    public String toJSON() {
        String jsons = "";
        try {
            ArrayList<JSONObject> MyArrJson = new ArrayList<JSONObject>();
            JSONObject object;
            String docname = etDocname.getText().toString();
            String presonalID = data.getString("PERSONAL_ID", null);
            Object selectedItem = spWH.getSelectedItem();
            String WHNAME = selectedItem.toString();
            int position = List_WHname.indexOf(WHNAME);

            Object selectedItems = spFLG.getSelectedItem();
            String FLGNAME = selectedItems.toString();
            int positions = List_flgName.indexOf(FLGNAME);
            String FLG = List_flgID.get(positions);

            String WHID = List_IDWH.get(position);

            for (int i = 0; i < alData.size(); i++) {
                object = new JSONObject();
                object.put("DocNo", docname);
                object.put("barcode", Barcodes);
                object.put("FLG", FLG);
                object.put("WHID", WHID);
                object.put("lotsize", alData.get(i).get("lotsize"));
                object.put("amount", alData.get(i).get("amount"));
                object.put("remark", alData.get(i).get("remark"));
                object.put("presonalID",presonalID);
                object.put("MSID",MSID);

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

    public class showInformation extends AsyncTask<String, Void, String> {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
        @Override
        protected String doInBackground(String... params) {

            Request.Builder builder = new Request.Builder();
            builder.url(params[0]);
            RequestBody formBody = new FormBody.Builder()
                    .add("job_no",JobNo)
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
                String test = "";
                try {

                    Jobject = new JSONObject(jsonData);


                    final Dialog dialog = new Dialog(ImportBarcodeActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.dialog_information_barcode);//dialog/////////
                    dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));  // ตี่งค่า ให้พื้น หลัง โปร่ง เเสง
                    dialog.setCancelable(false);

                    Button btCancel = (Button) dialog.findViewById(R.id.btCancel);
                    TextView partNo = (TextView) dialog.findViewById(R.id.partNo);
                    TextView partName = (TextView) dialog.findViewById(R.id.partName);
                    TextView dwgNo = (TextView) dialog.findViewById(R.id.dwgNo);
                    TextView weight = (TextView) dialog.findViewById(R.id.weight);
                    TextView width = (TextView) dialog.findViewById(R.id.width);
                    TextView length = (TextView) dialog.findViewById(R.id.length);
                    TextView height = (TextView) dialog.findViewById(R.id.height);

                    partNo.setText("Part No.      : " + JobNo);
                    partName.setText("Part Name  : " + Jobject.getString("PART_NAME"));
                    dwgNo.setText("Dwg No.      : " + Jobject.getString("DWG_NO"));
                    weight.setText("Weight         : " + Jobject.getString("JOB_WEIGHT"));
                    width.setText("Width           : " + Jobject.getString("JOB_WIDTH"));
                    length.setText("Length         : " + Jobject.getString("JOB_LENGTH"));
                    height.setText("Height         : " + Jobject.getString("JOB_HEIGHT"));
                    btCancel.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            dialog.cancel();
                        }
                    });
                    dialog.show();
                } catch (JSONException e) {

                    e.printStackTrace();
                }
            }else{
                showTimeoutDialog();
            }

        }
    }

    public class Show_Spiner_WH extends AsyncTask<String, Void, String> {

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
            ClearVar();
            if(result!=null) {
                String jsonData = result;
                JSONObject Jobject = null;
                String PartNo = "";
                try {
                    Jobject = new JSONObject(jsonData);
                    JSONArray data = Jobject.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject message = data.getJSONObject(i);
                        List_WHname.add(message.getString("WHNAME") + "");
                        List_IDWH.add(message.getString("WHID") + "");
                        JobNo = message.getString("JOB");
                        MSID = message.getString("MSID");
                        PartNo = message.getString("PART");

                    }
                    if (!JobNo.equals("")) {
                        sound.start();
                    } else {
                        miss.start();
                        Snackbar.make(findViewById(android.R.id.content), "ไม่พบข้อมูลกรุณาสแกนบาร์โค้ดใหม่", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).setAction("สแกนบาร์โค้ด", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startScan();
                            }
                        }).show();
                    }
                    ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(ImportBarcodeActivity.this, android.R.layout.simple_spinner_item, List_WHname);
                    myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spWH.setAdapter(myAdapter);
                    edTagBarcode.setText(Barcodes);
                    tvJOBNo.setText("JOB No.  : " + JobNo);
                    tvPARTNo.setText("PART No. : " + PartNo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
               showTimeoutDialog();
            }

        }
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

                    ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(ImportBarcodeActivity.this, android.R.layout.simple_spinner_item, List_flgName);
                    myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spFLG.setAdapter(myAdapter);


                } catch (JSONException e) {

                    e.printStackTrace();
                }
            }else{
                showTimeoutDialog();
            }

        }
    }

    public void showTimeoutDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                ImportBarcodeActivity.this);
        builder.setTitle("แจ้งเตือน");
        builder.setMessage("การเชื่อมต่อมีปัญหากรุณาตรวจสอบIntenet");
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {
                    }
                });
        builder.show();
    } // Test เรื่อง การตรวยสอบ ใหม่ !!

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
            Intent intent = new Intent(ImportBarcodeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(ImportBarcodeActivity.this, settingServerActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_export) {
            Intent intent = new Intent(ImportBarcodeActivity.this, ExportBarcodeActivity.class);
            startActivity(intent);
            finish();
        } else if(id == R.id.nav_check){
            Intent intent = new Intent(ImportBarcodeActivity.this, showCheckBarcodeActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_checkStore){
            Intent intent = new Intent(ImportBarcodeActivity.this, CheckBarcodeActivity.class);
            startActivity(intent);
            finish();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_save){
            if (alData.size() == 0) {
                Snackbar.make(findViewById(android.R.id.content), "ยังไม่มีข้อมูลกรุณาเพิ่มข้อมูล", Snackbar.LENGTH_LONG)
                        .show();
            } else {
                if (etDocname.getText().toString() == null && etDocname.getText().toString().equals("")) {
                    Snackbar.make(findViewById(android.R.id.content), "กรุณากรอกเลที่อ้างอิงเอกสาร", Snackbar.LENGTH_LONG)
                            .show();
                } else {
                   // Toast.makeText(getApplicationContext(),"test : "+toJSON(),Toast.LENGTH_LONG).show();
                    dialog.show();
                    insertBarcode insert = new insertBarcode();
                    insert.execute("http://" + IP + "/Barcode/insert_Barcode.php");
                }
            }
        }else
            if(id== R.id.action_scan){
              startScan();
            }
            else if(id==R.id.action_info){
                if (JobNo.equals("")) {
                    Snackbar.make(findViewById(android.R.id.content), "กรุณาสแกนบาร์โค้ด", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).setAction("สแกนบาร์โค้ด", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startScan();

                        }
                    }).show();
                } else {
                    dialog.show();
                    showInformation showInfo = new showInformation();
                    showInfo.execute("http://" + IP + "/Barcode/show_information_export.php");
                }
            }
        return super.onOptionsItemSelected(item);
    }

}
