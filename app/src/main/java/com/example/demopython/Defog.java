package com.example.demopython;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Defog extends AppCompatActivity {

    Bitmap src,res;
    String Dir = "/sdcard/tmphoto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_defog);

        ImageView srcimage = findViewById(R.id.srcimage);
        ImageView resimage = findViewById(R.id.resimage);

        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
        // byte[] puff = intent.getByteArrayExtra("image");
        //src = BitmapFactory.decodeByteArray(puff,0,puff.length);
        Uri uri = Uri.fromFile(new File(path));
        //srcimage.setImageURI(uri);
        try {
            src = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        srcimage.setImageBitmap(src);
        svaeBitmap(src);

        initPython();
        callpythoncode(path);

    }

    private void svaeBitmap(Bitmap src) {
        try{
            File dirfile = new File(Dir);
            if(!dirfile.exists()){
                dirfile.mkdir();
            }
            File file = new File(Dir,"src.jpg");
            FileOutputStream fos = new FileOutputStream(file);
            src.compress(Bitmap.CompressFormat.JPEG,100,fos);
            fos.flush();
            fos.close();
            Log.i("ok",Dir);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void callpythoncode(String path) {
        Python py = Python.getInstance();
        py.getModule("defog").callAttr("defog",path);
    }

    private void initPython() {
        if(!Python.isStarted()){
            Python.start(new AndroidPlatform(this));
        }
    }
}
