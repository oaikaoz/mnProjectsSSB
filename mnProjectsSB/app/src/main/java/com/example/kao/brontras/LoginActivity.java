package com.example.kao.brontras;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Button btLogin;
    SharedPreferences data, server;
    SharedPreferences.Editor editor;
    String IP;
    EditText etUser, etPass;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btLogin = (Button) findViewById(R.id.btLogin);
        etUser = (EditText) findViewById(R.id.etUser);
        etPass = (EditText) findViewById(R.id.etPass);
        data = getSharedPreferences("data", Context.MODE_PRIVATE);
        editor = data.edit();
        server = getSharedPreferences("server", Context.MODE_PRIVATE);
        editor.putString("IP", ""); // set IP


        editor.commit();
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.bgColor));
        }

        IP = data.getString("IP", "");
        dialog = new ProgressDialog(LoginActivity.this); // this = YourActivity
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("กำลังโหลด. กรุณารอสักครู่");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);

        if (data.getString("Username", null) != null) {
           Intent it = new Intent(getApplication(),ImportBarcodeActivity.class);
            startActivity(it);
            finish();
        }

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connectMan = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = connectMan.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnected()) {
                    dialog.show();
                    CheckLogin Login = new CheckLogin();  //okHttp
                    Login.execute("http://" + IP + "/Barcode/select_login.php"); //check Login
                } else {
                    Snackbar.make(v, "ไม่มีการเชื่อมต่อ Internet", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).setAction("ตั้งค่า", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                        }
                    }).show();
                }
            }
        });
        checkInternet();  //ตรวนสอบการเชื่อมต่อ Internet ตอนเปิด Appliction

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public class CheckLogin extends AsyncTask<String, Void, String> {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
        final String user = etUser.getText().toString();
        final String pass = etPass.getText().toString();

        @Override
        protected String doInBackground(String... params) {
            Request.Builder builder = new Request.Builder();
            builder.url(params[0]);
            RequestBody formBody = new FormBody.Builder()
                    .add("username", user)
                    .add("password", pass)
                    .add("nameSer", "nameSer")
                    .add("userSer", "userSer")
                    .add("passSer", "passSer")
                    .add("dbName", "dbName")
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
                    String success = Jobject.getString("success");
                    if (success.equals("1")) { //จะได้ ค่า 1 กรณี ที่ มี username password  ถูก ต้อง ใน ไฟลล์ PHP
                        String nUser = Jobject.getString("username");
                        String PERSONAL_ID = Jobject.getString("PERSONAL_ID");
                        String Menulist = Jobject.getString("MenuList");
                        editor.putString("PERSONAL_ID", PERSONAL_ID); // เก็บ รหัส ผู้ใชงาน
                        editor.putString("nUser", nUser);
                        editor.putString("Username", etUser.getText().toString());
                        editor.putString("MenuList",Menulist);
                        editor.commit();
                        Toast.makeText(getApplicationContext(), "เข้าสู่ระบบแล้ว", Toast.LENGTH_LONG).show();
                        //Snackbar.make(findViewById(android.R.id.content), "เข้าสู่ระบบแล้ว", Snackbar.LENGTH_LONG).show();
                        Intent it = new Intent(getApplication(), ImportBarcodeActivity.class);
                        startActivity(it);
                        finish();
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), "รหัสผ่านผิดกรุณากรอกใหม่", Snackbar.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Snackbar.make(findViewById(android.R.id.content), "Error", Snackbar.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            } else {
                showTimeoutDialog();
            }

        }
    }

    public void showTimeoutDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                LoginActivity.this);
        builder.setTitle("แจ้งเตือน");
        builder.setMessage("หมดเวลาการเชื่อมต่อ");
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {
                    }
                });
        builder.show();
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

        if (id == R.id.nav_manage) { // การทำงาน ตั้งค่า Server
            Intent intent = new Intent(LoginActivity.this, settingServerActivity.class);
            startActivity(intent);

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void checkInternet() {
        ConnectivityManager connectMan = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
        } else {
            Snackbar.make(findViewById(android.R.id.content), "ไม่มีการเชื่อมต่อ Internet", Snackbar.LENGTH_LONG).show();
        }
    }

}
