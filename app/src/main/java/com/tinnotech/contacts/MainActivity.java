package com.tinnotech.contacts;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.alibaba.fastjson.JSONObject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check permission for modis
        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 1);
        }

        //Intent intent1 = new Intent(MainActivity.this, ActionIntentService.class);
       // intent1.putExtra("type", -1);
        //startService(intent1);

        Button btn1 = (Button)findViewById(R.id.button_login);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sp = getSharedPreferences(Const.SP_FILE_NAME, Context.MODE_PRIVATE);
                final int contact_version = sp.getInt(Const.SP_KEY_CONTACT, 0);
                final int family_version = sp.getInt(Const.SP_KEY_FAMILY, 0);
                final int friend_version = sp.getInt(Const.SP_KEY_FRIEND, 0);
                JSONObject obj = new JSONObject();
                obj.put(Const.SP_KEY_CONTACT, contact_version);
                obj.put(Const.SP_KEY_FAMILY, family_version);
                obj.put(Const.SP_KEY_FRIEND, friend_version);

                Intent intent = new Intent(MainActivity.this, NetworkService.class);
                intent.putExtra("type", 20);
                intent.putExtra("json", obj.toString());
                startService(intent);
            }
        });

        Button btn_bind = (Button)findViewById(R.id.button_bind);
        btn_bind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ActionIntentService.class);
                intent.putExtra("type", 2);
                startService(intent);
            }
        });

        Button btn2 = (Button)findViewById(R.id.button_unbind);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ActionIntentService.class);
                intent.putExtra("type", 3);
                startService(intent);
            }
        });

        Button btn3 = (Button)findViewById(R.id.button_test);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActionIntentService.getContactsGroupMember(MainActivity.this, new int[]{Const.GROUP_ID_FAMILY_PROFILE, Const.GROUP_ID_FAMILY});
            }
        });
    }


}
