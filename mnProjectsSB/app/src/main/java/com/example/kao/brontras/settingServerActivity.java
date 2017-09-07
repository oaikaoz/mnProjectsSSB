package com.example.kao.brontras;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.EditText;

public class settingServerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    SharedPreferences data, server;
    SharedPreferences.Editor editor, editorser;
    Button btSave;
    EditText nameSer, userSer, passSer, dbName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_server);
        data = getSharedPreferences("data", Context.MODE_PRIVATE);
        editor = data.edit();
        server = getSharedPreferences("server", Context.MODE_PRIVATE);
        editorser = server.edit();
///////////ทำการตรวจสอบ Server ณ หน้านี้เลย

        btSave = (Button) findViewById(R.id.btSave);
        nameSer = (EditText) findViewById(R.id.nameSer);
        userSer = (EditText) findViewById(R.id.userSer);
        passSer = (EditText) findViewById(R.id.passSer);
        dbName = (EditText) findViewById(R.id.dbName);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if(server.getString("nameSer", null)==null||server.getString("nameSer",null).equals("")){
     
	 
	 
	 
	 
        }else {
            nameSer.setText(server.getString("nameSer", null));
            userSer.setText(server.getString("userSer", null));
            passSer.setText(server.getString("passSer", null));
            dbName.setText(server.getString("dbName", null));
        }
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editorser.putString("nameSer", nameSer.getText().toString());
                editorser.putString("userSer", userSer.getText().toString());
                editorser.putString("passSer", passSer.getText().toString());
                editorser.putString("dbName", dbName.getText().toString());
                editorser.commit();
                Snackbar.make(v, "บันทึกเรียบร้อยแล้ว", Snackbar.LENGTH_LONG).show();
            }
        });
        navigationView.setNavigationItemSelectedListener(this);
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
        //Handle navigation view item clicks here.
        int id = item.getItemId();
       if (id == R.id.nav_logout) {
            editor.clear();
            editor.commit();
            Intent intent = new Intent(settingServerActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
