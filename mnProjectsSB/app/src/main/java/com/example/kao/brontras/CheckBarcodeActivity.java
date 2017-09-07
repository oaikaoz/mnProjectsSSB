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
import android.icu.util.CurrencyAmount;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.edwardvanraak.materialbarcodescanner.MaterialBarcodeScanner;
import com.edwardvanraak.materialbarcodescanner.MaterialBarcodeScannerBuilder;
import com.google.android.gms.vision.barcode.Barcode;
import com.squareup.okhttp.OkHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CheckBarcodeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener { //ตรวจสอบ Barcode
    private Barcode barcodeResult;
    public static final String BARCODE_KEY = "BARCODE";

    ArrayList<HashMap<String, String>> alData; //ข้อมูลในList Barcode
    HashMap<String, String> hmData;
    ArrayList<String> WHNAME, WHID;
    ArrayList<String> DateCheck;
    ListView LvdataBarcode;
    NavigationView navigationView;
    TextView tvBarcode, tvQTY;
    Button btSave;
    SharedPreferences data;
    SharedPreferences.Editor editor;
    MediaPlayer sound, miss;
    Spinner spWHNAME;
    Spinner spDATE;
    String Barcodes;
    Button btScan;
    String IP;
    ProgressDialog dialog;
    RelativeLayout rtlNoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_barcode);
        alData = new ArrayList<HashMap<String, String>>();
        btScan = (Button) findViewById(R.id.scanBarcode);
        btSave = (Button) findViewById(R.id.btSave);
        data = getSharedPreferences("data", Context.MODE_PRIVATE);
        editor = data.edit();
        IP = data.getString("IP", "");

        sound = MediaPlayer.create(CheckBarcodeActivity.this, R.raw.pass);
        miss = MediaPlayer.create(CheckBarcodeActivity.this, R.raw.miss);
        WHNAME = new ArrayList<String>();
        DateCheck = new ArrayList<String>();
        WHID = new ArrayList<String>();

        spDATE = (Spinner) findViewById(R.id.spDATE);
        spWHNAME = (Spinner) findViewById(R.id.spWHNAME);
        LvdataBarcode = (ListView) findViewById(R.id.list_show_qty);
        tvBarcode = (TextView) findViewById(R.id.tvBarcode);
        tvQTY = (TextView) findViewById(R.id.tvQTY);

        dialog = new ProgressDialog(CheckBarcodeActivity.this); // this = YourActivity
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("กำลังโหลด กรุณารอสักครู่.....");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Show_flg showflg = new Show_flg();
        showflg.execute("http://" + IP + "/Barcode/check_barcode.php");

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        rtlNoData = (RelativeLayout) findViewById(R.id.rtlNoData);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("เช๊คบาร์โค้ด");
        //setSubtitle("รับเข้าสินค้า");
        toolbar.setTitleTextColor(Color.WHITE);
        //toolbar.setSubtitleTextColor(Color.BLACK);
        toolbar.setBackgroundColor(Color.parseColor("#212121"));
        setSupportActionBar(toolbar);

        setMenuList();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.bgColor));
        }
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alData.size() > 0) {
                    dialog.show();
                    insertBarcode insert = new insertBarcode();
                    insert.execute("http://" + IP + "/Barcode/insert_check_barcode.php");

                } else {
                    Snackbar.make(v, "ไม่มีข้อมูล กรุณาสแกนบาร์โค้ด", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).setAction("สแกนบาร์โค้ด", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startScan();

                        }
                    }).show();
                }

            }
        });
        btScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });
        LvdataBarcode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
                final Dialog dialog = new Dialog(CheckBarcodeActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_showcheckbarcode); // lotsize
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));  // ตี่งค่า ให้พื้น หลัง โปร่ง เเสง
                dialog.setCancelable(false);
                Button btCancel = (Button) dialog.findViewById(R.id.btCancel);
                Button btSave = (Button) dialog.findViewById(R.id.btAdd);
                final EditText edTagNo = (EditText) dialog.findViewById(R.id.TagNo);
                final EditText edMSID = (EditText) dialog.findViewById(R.id.edMSID);
                final EditText edQTY = (EditText) dialog.findViewById(R.id.edAmounts);
                final EditText edSerial = (EditText) dialog.findViewById(R.id.EdSerial);

                edTagNo.setEnabled(false);
                edTagNo.setText(alData.get(arg2).get("TagNo"));

                edMSID.setEnabled(false);
                edMSID.setText(alData.get(arg2).get("MSid"));

                edQTY.setText(alData.get(arg2).get("Amount"));

                edSerial.setEnabled(false);
                edSerial.setText(alData.get(arg2).get("LotNo"));

                btCancel.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext()
                                , "Close dialog", Toast.LENGTH_SHORT);
                        dialog.cancel();
                    }
                });
                btSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alData.get(arg2).put("Amount", edQTY.getText().toString());
                        CustomAdapter_show_check_barcode adapter = new CustomAdapter_show_check_barcode(getApplicationContext(), alData); //set listview แบบ Custom
                        LvdataBarcode.setAdapter(adapter);
                        dialog.cancel();
                    }
                });
                dialog.show();
            }
        });
    }

    private void startScan() {
        final MaterialBarcodeScanner materialBarcodeScanner = new MaterialBarcodeScannerBuilder()
                .withActivity(CheckBarcodeActivity.this)
                .withEnableAutoFocus(true)
                .withBleepEnabled(false) // ปิดเสียง หลังสแกนBarcode
                .withBackfacingCamera()
                .withCenterTracker()
                .withText("กำลังสแกน......")
                .withResultListener(new MaterialBarcodeScanner.OnResultListener() {
                    @Override
                    public void onResult(Barcode barcode) {
                        barcodeResult = barcode;
                        // show_datalist(barcode.rawValue + "");
                        tvBarcode.setText(" " + barcode.rawValue + "");
                        Barcodes = barcode.rawValue + "";
                        dialog.show();
                        if(checkVar()==true){
                            dialog.dismiss();
                            Snackbar.make(findViewById(android.R.id.content), "บาร์โค้ดนี้ถูกสแกนแล้ว", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).setAction("สแกนบาร์โค้ด", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startScan();
                                }
                            }).show();
                        }else {
                            Show_datalist showdatalist = new Show_datalist();
                            showdatalist.execute("http://" + IP + "/Barcode/show_check_Barcode.php");
                        }
                    }
                })
                .build();
        materialBarcodeScanner.startScan();

    }
    public Boolean checkVar(){

        Boolean CHECK=false;
        for(int position =0; position< alData.size();position++) {
           if(alData.get(position).get("TagNo").equals(Barcodes)){
                        CHECK = true;
           }
        }
        return  CHECK;
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

    public class Show_flg extends AsyncTask<String, Void, String> {
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();

        @Override
        protected String doInBackground(String... params) {
            // dialog.show();
            Request.Builder builder = new Request.Builder();
            builder.url(params[0]);
            RequestBody formBody = new FormBody.Builder()
                    .add("barcode", "Barcodes")
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
            if (result != null) {
                String jsonData = result;
                JSONObject Jobject = null;
                String PartNo = "";
                try {
                    Jobject = new JSONObject(jsonData);
                    JSONArray data = Jobject.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject message = data.getJSONObject(i);
                        DateCheck.add(message.getString("STOCKH_DATE"));
                        if (!WHNAME.contains(message.getString("WH_NAME"))) {
                            WHNAME.add(message.getString("WH_NAME"));
                            WHID.add(message.getString("WHID"));
                        }
                    }

                    ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(CheckBarcodeActivity.this, android.R.layout.simple_spinner_item, DateCheck);
                    myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spDATE.setAdapter(myAdapter);
                    ArrayAdapter<String> myAdapters = new ArrayAdapter<String>(CheckBarcodeActivity.this, android.R.layout.simple_spinner_item, WHNAME);
                    myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spWHNAME.setAdapter(myAdapters);


                } catch (JSONException e) {

                    e.printStackTrace();
                }
            } else {
                showTimeoutDialog("show_flg");
            }

        }
    }

    public class Show_datalist extends AsyncTask<String, Void, String> {
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
        Object selectedItem = spWHNAME.getSelectedItem();
        String WHNAMEs = selectedItem.toString();

        int position = WHNAME.indexOf(WHNAMEs);
        String WHIDs = WHID.get(position);

        Object selectedItems = spDATE.getSelectedItem();
        String STdates = selectedItems.toString();


        @Override
        protected String doInBackground(String... params) {


            Request.Builder builder = new Request.Builder();
            builder.url(params[0]);
            RequestBody formBody = new FormBody.Builder()
                    .add("barcode", Barcodes)
                    .add("WHID", WHIDs)
                    .add("STDATE", STdates)
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
            if (result != null) {
                String jsonData = result;
                JSONObject Jobject = null;

                try {
                    Jobject = new JSONObject(jsonData);
                    JSONArray data = Jobject.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject message = data.getJSONObject(i);
                        if (message.getString("TAG_NO").equals(Barcodes)) {   //ตรวจสอบ Status Barcode
                            sound.start();
                            MyTTS.getInstance(CheckBarcodeActivity.this)
                                    .speak("สินค้าจำนวน " + message.getString("QTYACTION") + "ชิ้น");

                            hmData = new HashMap<String, String>();
                            hmData.put("TagNo", message.getString("TAG_NO"));
                            hmData.put("Amount", message.getString("QTYACTION"));
                            hmData.put("MSid", message.getString("MS_ID"));
                            hmData.put("LotNo", message.getString("LOT_NO"));
                            hmData.put("Stockd_ID", message.getString("STOCKD_ID"));
                            alData.add(0, hmData);
                        } else if(message.getString("TAG_NO").equals("Doble")){
                            Snackbar.make(findViewById(android.R.id.content), "บาร์โค้ดนี้ถูกเช็คแล้ว", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).setAction("สแกนบาร์โค้ด", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startScan();
                                }
                            }).show();
                            miss.start();
                        }else {
                            Snackbar.make(findViewById(android.R.id.content), "ไม่พบข้อมูลบาร์โค้ดนี้", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).setAction("สแกนบาร์โค้ด", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startScan();
                                }
                            }).show();
                            miss.start();
                        }

                    }
                    tvQTY.setText(" " + alData.size() + "");
                    if(alData.size() < 0 ) {

                        rtlNoData.setVisibility(View.GONE);
                        LvdataBarcode.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {

                    e.printStackTrace();
                }
                if (alData.size() > 0) {
                    spDATE.setEnabled(false);
                    spWHNAME.setEnabled(false);
                    CustomAdapter_show_check_barcode adapter = new CustomAdapter_show_check_barcode(getApplicationContext(), alData); //set listview แบบ Custom
                    LvdataBarcode.setAdapter(adapter);
                    rtlNoData.setVisibility(View.GONE);
                    LvdataBarcode.setVisibility(View.VISIBLE);
                }
            } else {
                showTimeoutDialog("Show_datalist");
            }

        }
    }

    public class insertBarcode extends AsyncTask<String, Void, String> {
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();

        String json = toJSON();

        @Override
        protected String doInBackground(String... params) {

            Request.Builder builder = new Request.Builder();
            builder.url(params[0]);
            RequestBody formBody = new FormBody.Builder()
                    .add("JSON", json)
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
            if (result != null || result.equals("")) {
                alData.clear();
                spDATE.setEnabled(true);
                spWHNAME.setEnabled(true);
                CustomAdapter_show_check_barcode adapter = new CustomAdapter_show_check_barcode(getApplicationContext(), alData); //set listview แบบ Custom
                LvdataBarcode.setAdapter(adapter);
                rtlNoData.setVisibility(View.VISIBLE);
                LvdataBarcode.setVisibility(View.GONE);
                Snackbar.make(findViewById(android.R.id.content), "บันทึกเรียบร้อยแล้ว", Snackbar.LENGTH_LONG)
                        .show();
            } else {
                showTimeoutDialog("insertBarcode");
            }
        }
    }

    public String showTimeoutDialog(final String check) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                CheckBarcodeActivity.this);
        builder.setTitle("แจ้งเตือน");
        builder.setMessage("การเชื่อมต่อมีปัญหากรุณาตรวจสอบ Intenet ");
        builder.setPositiveButton("ลองอีกครั้ง",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogs,
                                        int which) {
                        dialog.show();
                        if (check.equals("show_flg")) {
                            Show_flg showflg = new Show_flg();
                            showflg.execute("http://" + IP + "/Barcode/check_barcodes.php");
                        } else if (check.equals("Show_datalist")) {
                            Show_datalist showdatalist = new Show_datalist();
                            showdatalist.execute("http://" + IP + "/Barcode/show_check_Barcode.php");
                        } else if (check.equals("insertBarcode")) {
                            insertBarcode insert = new insertBarcode();
                            insert.execute("http://" + IP + "/Barcode/insert_check_barcode.php");
                        }
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

    public String toJSON() {
        String jsons = "";
        try {
            ArrayList<JSONObject> MyArrJson = new ArrayList<JSONObject>();
            JSONObject object;

            String presonalID = data.getString("PERSONAL_ID", null);


            for (int i = 0; i < alData.size(); i++) {
                object = new JSONObject();
                object.put("TagNo", alData.get(i).get("TagNo"));
                object.put("Amount", alData.get(i).get("Amount"));
                object.put("MSid", alData.get(i).get("MSid"));
                object.put("LotNo", alData.get(i).get("LotNo"));
                object.put("Stockd_ID", alData.get(i).get("Stockd_ID"));
                object.put("presonalID", presonalID);

                MyArrJson.add(object);
            }
            JSONArray json = new JSONArray(MyArrJson);

            jsons = json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsons;
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
            Intent intent = new Intent(CheckBarcodeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(CheckBarcodeActivity.this, settingServerActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_import) {
            Intent intent = new Intent(CheckBarcodeActivity.this, ImportBarcodeActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_export){
            Intent intent = new Intent(CheckBarcodeActivity.this, ExportBarcodeActivity.class);
            startActivity(intent);
            finish();
        }else if(id == R.id.nav_check){
            Intent intent = new Intent(CheckBarcodeActivity.this, showCheckBarcodeActivity.class);
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
        getMenuInflater().inflate(R.menu.menu_bar_export, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_save) {
            if (alData.size() > 0) {
                dialog.show();

                insertBarcode insert = new insertBarcode();
                insert.execute("http://" + IP + "/Barcode/insert_check_barcode.php");

            } else {
                Snackbar.make(findViewById(android.R.id.content), "ไม่มีข้อมูล กรุณาสแกนบาร์โค้ด", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).setAction("สแกนบาร์โค้ด", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startScan();

                    }
                }).show();
            }

        }
        else if(id== R.id.action_scan){
            startScan();
        }

        return super.onOptionsItemSelected(item);
    }

}
