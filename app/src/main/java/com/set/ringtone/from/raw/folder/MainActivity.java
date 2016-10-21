package com.set.ringtone.from.raw.folder;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private TextView txtRingtone;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private String fNmae = "kiss_me.mp3";
    private String fPAth = "android.resource://com.set.ringtone.from.raw.folder/raw/kiss_me";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtRingtone= (TextView) findViewById(R.id.txtRingtone);
        txtRingtone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (checkPermission()) {
                        if (Settings.System.canWrite(MainActivity.this))
                        {
                            setRingtone();
                        }
                        else
                        {
                            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS)
                                    .setData(Uri.parse("package:" + getPackageName()))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }


                        Log.e("value", "Permission already Granted, Now you can save image.");
                    } else {
                        requestPermission();
                    }
                } else {
                    setRingtone();
                    Log.e("value", "Not required for requesting runtime permission");
                }

            }
        });
    }


    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can save image .");
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (Settings.System.canWrite(MainActivity.this)) {
                            setRingtone();
                        } else {
                            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS)
                                    .setData(Uri.parse("package:" + getPackageName()))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }
                } else {
                    Log.e("value", "Permission Denied, You cannot save image.");
                }
                break;
        }
    }



    private void setRingtone(){
        AssetFileDescriptor openAssetFileDescriptor;
        ((AudioManager) getSystemService(AUDIO_SERVICE)).setRingerMode(2);
        File file = new File(Environment.getExternalStorageDirectory() + "/appkeeda", this.fNmae);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Uri parse = Uri.parse(this.fPAth);
        ContentResolver contentResolver = getContentResolver();
        try {
            openAssetFileDescriptor = contentResolver.openAssetFileDescriptor(parse, "r");
        } catch (FileNotFoundException e2) {
            openAssetFileDescriptor = null;
        }
        try {
            byte[] bArr = new byte[1024];
            FileInputStream createInputStream = openAssetFileDescriptor.createInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            for (int read = createInputStream.read(bArr); read != -1; read = createInputStream.read(bArr)) {
                fileOutputStream.write(bArr, 0, read);
            }
            fileOutputStream.close();
        } catch (IOException e3) {
            e3.printStackTrace();
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("_data", file.getAbsolutePath());
        contentValues.put("title", "nkDroid ringtone");
        contentValues.put("mime_type", "audio/mp3");
        contentValues.put("_size", Long.valueOf(file.length()));
        contentValues.put("artist", Integer.valueOf(R.string.app_name));
        contentValues.put("is_ringtone", Boolean.valueOf(true));
        contentValues.put("is_notification", Boolean.valueOf(false));
        contentValues.put("is_alarm", Boolean.valueOf(false));
        contentValues.put("is_music", Boolean.valueOf(false));
        try {
            Toast.makeText(this, new StringBuilder().append("Ringtone set successfully"), Toast.LENGTH_LONG).show();
            RingtoneManager.setActualDefaultRingtoneUri(getBaseContext(), 1, contentResolver.insert(MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath()), contentValues));
        } catch (Throwable th) {
            Toast.makeText(this, new StringBuilder().append("Ringtone feature is not working"), Toast.LENGTH_LONG).show();
        }
    }
}
