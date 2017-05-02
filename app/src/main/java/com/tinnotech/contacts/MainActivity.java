package com.tinnotech.contacts;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

        //Intent intent1 = new Intent(MainActivity.this, ActionIntentService.class);
       // intent1.putExtra("type", -1);
        //startService(intent1);

        Button btn1 = (Button)findViewById(R.id.button_login);
        btn1.setOnClickListener(new View.OnClickListener() {
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
                testgroup();
            }
        });
    }

    private void testgroup(){
        String[] RAW_PROJECTION = new String[] { ContactsContract.Data.RAW_CONTACT_ID, };

        String RAW_CONTACTS_WHERE = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
                + "=? or "
                + ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
                +"=?"
                + " and "
                + ContactsContract.Data.MIMETYPE
                + "="
                + "'"
                + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
                + "'";

        // 通过分组的id 查询得到RAW_CONTACT_ID
        Cursor groupContactCursor = getContentResolver().query(
                ContactsContract.Data.CONTENT_URI, RAW_PROJECTION,
                RAW_CONTACTS_WHERE, new String[]{Const.CONTACTS_GROUP_FAMILY_PROFILE, Const.CONTACTS_GROUP_FAMILY}, "data1 asc");

// Second, query the corresponding name of the raw_contact_id
        while(groupContactCursor.moveToNext())
        {
            Cursor contactCursor = getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                    new String[]{ContactsContract.Data.RAW_CONTACT_ID, ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME},
                    ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE + "' AND " + ContactsContract.Data.RAW_CONTACT_ID + "=" + groupContactCursor.getInt(0),
                    null,
                    null);
            contactCursor.moveToNext();
            Log.e("Test", "Member name is: " + contactCursor.getString(1) + " " + contactCursor.getString(2));
            contactCursor.close();
        }
        groupContactCursor.close();
    }
}
