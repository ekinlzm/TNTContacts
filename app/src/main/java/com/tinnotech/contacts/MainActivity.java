package com.tinnotech.contacts;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.alibaba.fastjson.JSON;
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

        //test
        Intent intent = new Intent(this, NetworkService.class);
        startService(intent);

        Button btn = (Button)findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject obj = new JSONObject();
                obj.put(Const.SP_KEY_CONTACT, 0);
                obj.put(Const.SP_KEY_FAMILY, 0);
                obj.put(Const.SP_KEY_FRIEND, 0);
                Intent intent = new Intent(MainActivity.this, NetworkService.class);
                intent.putExtra("type", 20);
                intent.putExtra("json", obj.toString());
                startService(intent);
            }
        });
    }
}
