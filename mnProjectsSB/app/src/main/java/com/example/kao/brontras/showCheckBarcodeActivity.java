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

public class showCheckBarcodeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Barcode barcodeResult;
    public static final String BARCODE_KEY = "BARCODE";
    ListView listBarcode, listlotsize;
    ArrayList<String>amountData, lotsizeData, jobNo, lotlist, amountlot, lotsizelot, updateBy,updateDate;
    TextView  TagBarcode;

    SharedPreferences data;
    SharedPreferences.Editor editor;
    MediaPlayer sound, miss;
    String Barcodes;
    RelativeLayout rtlNoData;
    ProgressDialog dialog;
    String IP;
    NavigationView navigationView;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_check_barcode);
        data = getSharedPreferences("data", Context.MODE_PRIVATE);
        editor = data.edit();
        IP = data.getString("IP", "");
        Barcodes = "";

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);




        setMenuList();
        sound = MediaPlayer.create(showCheckBarcodeActivity.this, R.raw.pass);
        miss = MediaPlayer.create(showCheckBarcodeActivity.this, R.raw.miss);

        lotsizeData =   new ArrayList<String>();
        amountData =    new ArrayList<String>();

        lotlist =       new ArrayList<String>();
        lotsizelot =    new ArrayList<String>();
        amountlot =     new ArrayList<String>();
        updateBy =      new ArrayList<String>();
        updateDate =    new ArrayList<String>();
        jobNo =         new ArrayList<String>();
        rtlNoData = (RelativeLayout) findViewById(R.id.rtlNoData);
        dialog =        new ProgressDialog(showCheckBarcodeActivity.this); // this = YourActivity
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("กำลังโหลด กรุณารอสักครู่.....");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        listBarcode = (ListView) findViewById(R.id.listlotsize);  //ListView barcode

        TagBarcode = (TextView) findViewById(R.id.tagBarcode);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("เช๊คสต๊อค");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setBackgroundColor(Color.parseColor("#212121"));
        setSupportActionBar(toolbar);
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
    }

    private void startScan() {
        final MaterialBarcodeScanner materialBarcodeScanner = new MaterialBarcodeScannerBuilder()
                .withActivity(showCheckBarcodeActivity.this)
                .withEnableAutoFocus(true)
                .withBleepEnabled(false) // ปิดเสียง หลังสแกนBarcode
                .withBackfacingCamera()
                .withCenterTracker()
                .withText("กำลังสแกน......")
                .withResultListener(new MaterialBarcodeScanner.OnResultListener() {
                    @Override
                    public void onResult(Barcode barcode) {
                        barcodeResult = barcode;
                       // showlotsize(barcode.rawValue);
                        Barcodes = barcode.rawValue + "";
                        dialog.show();
                        Show_flg showfig = new Show_flg();
                        showfig.execute("http://" + IP + "/Barcode/show_checkBarcode.php");

                    }
                })
                .build();
        materialBarcodeScanner.startScan();
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
                        lotsizeData.add(message.getString("LOT_NO") + "");
                        amountData.add(message.getString("QTYACTION") + "");
                        jobNo.add(message.getString("JOB_NO") + "");
                        updateBy.add(message.getString("UPDATED_BY")+"");
                        updateDate.add(message.getString("UPDATE_DATE")+"");
                    }
                    if (lotsizeData.size() == 0) {
                        miss.start();
                        rtlNoData.setVisibility(View.VISIBLE);
                        listBarcode.setVisibility(View.GONE);
                        Snackbar.make(findViewById(android.R.id.content), "ไม่มีข้อมูล กรุณาสแกนบาร์โค้ด", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).setAction("สแกนบาร์โค้ด", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startScan();

                            }
                        }).show();
                    } else
                        sound.start();

                    CustomAdapter_listShowbarcode adapter = new CustomAdapter_listShowbarcode(getApplicationContext(), lotsizeData, amountData, jobNo,updateBy,updateDate); //set listview แบบ Custom
                    listBarcode.setAdapter(adapter);
                    rtlNoData.setVisibility(View.GONE);
                    listBarcode.setVisibility(View.VISIBLE);
                    TagBarcode.setText(Barcodes);

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
                showCheckBarcodeActivity.this);
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
            Intent intent = new Intent(showCheckBarcodeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(showCheckBarcodeActivity.this, settingServerActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_import) {
            Intent intent = new Intent(showCheckBarcodeActivity.this, ImportBarcodeActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_export){
            Intent intent = new Intent(showCheckBarcodeActivity.this, ExportBarcodeActivity.class);
            startActivity(intent);
            finish();
        }else  if (id == R.id.nav_checkStore) {
            Intent intent = new Intent(showCheckBarcodeActivity.this, CheckBarcodeActivity.class);
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
        getMenuInflater().inflate(R.menu.menu_bar_show_check_barcode, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.action_scan){
            lotsizeData.clear();
            amountData.clear();
            jobNo.clear();
            updateBy.clear();
            updateDate.clear();
            startScan();
        }

        return super.onOptionsItemSelected(item);
    }

}
