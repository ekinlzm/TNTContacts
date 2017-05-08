package com.tinnotech.contacts;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PortraitManagerService extends IntentService {
    public static final int ACTION_DOWNLAOD_PORTRAIT = 1;

    private static ArrayList<String> mDownloadList = new ArrayList<>();
    private String mCurDownload = null;
    private OkHttpClient mOkHttpClient = null;
    private File mPortraitCacheDir = null;

    public PortraitManagerService() {
        super("PortraitManagerService");
    }

    public static boolean isDonwloadListEmpty(){
        return (mDownloadList.size() == 0);
    }
    public static void clearDownloadList(){
        mDownloadList.clear();
    }

    public static void addDownloadItem(String url){
        if(url != null)
            mDownloadList.add(url);
    }

    public static String getFileNameFromUrl(String url){
        return (url == null) ? null : url.substring(url.lastIndexOf('/') + 1);
    }

    public static void checkPortraitCache(Context context, boolean cleanUseless){
        int index = -1;
        File portraitCacheDir = context.getDir(Const.portrait_cache_dir,Context.MODE_PRIVATE);
        if (!portraitCacheDir.exists()) return;

        ArrayList<String> nameList = new ArrayList<>();
        ArrayList<String> urlList = new ArrayList<>();

        String RAW_CONTACTS_WHERE = Const.MIMETYPE
                + "='"
                + Const.CONTACT_ITEM_TYPE
                + "' and " + Const.CONTACT_PORTRAIT_URL + "!='null'";

        Cursor cursor = context.getContentResolver().query(Const.DATA_URI, new String[]{Const.CONTACT_PORTRAIT_URL}, RAW_CONTACTS_WHERE, null, null);
        if(cursor != null) {
            while (cursor.moveToNext()) {
                urlList.add(cursor.getString(0));
                nameList.add(getFileNameFromUrl(cursor.getString(0)));
            }
            cursor.close();
        }

        if((nameList.size() == 0) && !cleanUseless) return;

        for (File item : portraitCacheDir.listFiles()) {
            index = nameList.indexOf(item.getName());
            if(index == -1){
                if(cleanUseless)
                    item.delete();
            }
            else{
                nameList.remove(index);
                urlList.remove(index);
            }
        }

        for (int i = 0; i < urlList.size(); i ++){
            addDownloadItem(urlList.get(i));
        }
        if(mDownloadList.size() > 0){
            Intent intent = new Intent(context, PortraitManagerService.class);
            intent.putExtra("type", PortraitManagerService.ACTION_DOWNLAOD_PORTRAIT);
            context.startService(intent);
        }

    }

    private void downloadPortait(){
        if(mCurDownload == null) return;
        final Request request = new Request.Builder().url(mCurDownload).build();
        final Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                startDownloadList();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    is = response.body().byteStream();
                    File portraitFile = new File(mPortraitCacheDir, getFileNameFromUrl(mCurDownload));
                    fos = new FileOutputStream(portraitFile);
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    fos.flush();
                    //更新系统通讯录头像
                    byte[] portraitData = null;
                    FileInputStream io = new FileInputStream(portraitFile);
                    portraitData = new byte[io.available()];
                    io.read(portraitData);
                    io.close();
                    String RAW_CONTACTS_WHERE = Const.MIMETYPE
                            + "='"
                            + Const.CONTACT_ITEM_TYPE
                            + "' and " + Const.CONTACT_PORTRAIT_URL + "='"
                            + mCurDownload
                            + "'";
                    ContentResolver resolver = getApplicationContext().getContentResolver();
                    Cursor rawCursor = resolver.query(Const.DATA_URI, new String[]{Const.RAW_CONTACT_ID}, RAW_CONTACTS_WHERE, null, null);
                    if(rawCursor != null) {
                        while (rawCursor.moveToNext()) {
                            //联系人的头像不存在，所以用insert，否则用update
                            String PHOTO_CONTACTS_WHERE = Const.MIMETYPE
                                    + "='"
                                    + Const.PORTRAIT_ITEM_TYPE
                                    + "' and " + Const.RAW_CONTACT_ID + "="
                                    + rawCursor.getLong(0);
                            Cursor photoCursor = resolver.query(Const.DATA_URI, new String[]{Const.PORTRAIT_PHOTO_ID}, PHOTO_CONTACTS_WHERE, null, null);
                            int photoId = 0;
                            if(photoCursor != null) {
                                if (photoCursor.moveToNext())
                                    photoId = photoCursor.getInt(0);
                                photoCursor.close();
                            }
                            ContentValues values = new ContentValues();
                            values.put(Const.PORTRAIT_PHOTO, portraitData);
                            if (photoId == 0) {
                                values.put(Const.RAW_CONTACT_ID, rawCursor.getLong(0));
                                values.put(Const.MIMETYPE, Const.PORTRAIT_ITEM_TYPE);
                                resolver.insert(Const.DATA_URI, values);
                            } else {
                                resolver.update(Const.DATA_URI, values, PHOTO_CONTACTS_WHERE, null);
                            }
                        }
                        rawCursor.close();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(is != null) is.close();
                    if(fos != null) fos.close();
                    startDownloadList();
                }
            }
        });
    }

    private void startDownloadList(){
        mCurDownload = null;
        while(true) {
            if(mDownloadList.size() == 0)
                break;
            mCurDownload = mDownloadList.remove(0);
            final String fileName = getFileNameFromUrl(mCurDownload);
            final File portraitFile = new File(mPortraitCacheDir, fileName);
            if (!portraitFile.exists())
                break;

            mCurDownload = null;
        }
        if(mCurDownload != null)
            downloadPortait();
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            int type = intent.getIntExtra("type", 0);
            if(mPortraitCacheDir == null)
                mPortraitCacheDir = getDir(Const.portrait_cache_dir, Context.MODE_PRIVATE);
            switch (type){
                case ACTION_DOWNLAOD_PORTRAIT:
                    if(mDownloadList.size() > 0){
                        mOkHttpClient = new OkHttpClient();
                        startDownloadList();
                    }
                    break;

                default:
                    break;
            }
        }
    }

}
