package com.example.demopython;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private String ImagePath = null;
    private Uri imageUri;
    private int Photo_ALBUM = 1, CAMERA = 2;
    private Bitmap bp = null;
    byte[] imagebyte, puff;

    Button btn_take, btn_select,btn_check,btn_defog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_take = (Button) findViewById(R.id.takephoto);
        btn_take.setOnClickListener(this);
        btn_select = (Button) findViewById(R.id.selectphoto);
        btn_select.setOnClickListener(this);
        btn_check = (Button) findViewById(R.id.check);
        btn_check.setOnClickListener(this);
        btn_defog = (Button) findViewById(R.id.defog);
        btn_defog.setOnClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("NewApi")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.takephoto:
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                builder.detectFileUriExposure();            //7.0拍照必加
                File outputImage = new File(Environment.getExternalStorageDirectory() + File.separator + "face.jpg");     //临时照片存储地

                try {                                                                                   //文件分割符
                    if (outputImage.exists()) {   //如果临时地址有照片，先清除
                        outputImage.delete();
                    }
                    outputImage.createNewFile();    ///创建临时地址
                } catch (IOException e) {
                    e.printStackTrace();
                }

                imageUri = Uri.fromFile(outputImage);              //获取Uri
                ImagePath = outputImage.getAbsolutePath();
                Log.i("拍照图片路径", ImagePath);         //，是传递你要保存的图片的路径，打开相机后，点击拍照按钮，系统就会根据你提供的地址进行保存图片
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);    //跳转相机
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);                          //相片输出路径
                startActivityForResult(intent, CAMERA);
                break;
            case R.id.selectphoto:
                Intent in = new Intent(Intent.ACTION_PICK);      //选择数据
                in.setType("image/*");                     //选择的数据为图片
                startActivityForResult(in, Photo_ALBUM);
                break;
            case R.id.check:
                Intent ck = new Intent(MainActivity.this,Check.class);
                startActivity(ck);
                break;
            case R.id.defog:
                if(bp == null)
                    showDialog();
                else {
                    //puff = Bitmap2Bytes(bp);
                    Intent df = new Intent(MainActivity.this, Defog.class);
                    df.putExtra("path", ImagePath);
                    startActivity(df);
                }
                break;
            default:
        }
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("NO PHOTO");
        builder.setPositiveButton("Know", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView image = findViewById(R.id.photo);
        // 相册选择图片
        if (requestCode == Photo_ALBUM) {
            if (data != null) {       //开启了相册，但是没有选照片
                Uri uri = data.getData();
                //从uri获取内容的cursor
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);cursor.moveToNext();
                ImagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));   //获得图片的绝对路径
                cursor.close();
                Log.i("图片路径", ImagePath);
                bp = decodeimage(ImagePath,1280,720);

                imagebyte = Bitmap2Bytes(bp);
                image.setImageBitmap(bp);
            }
        } else if (requestCode == CAMERA) {
            bp = decodeimage(ImagePath,1920,1080);
            imagebyte = Bitmap2Bytes(bp);
            image.setImageBitmap(bp);
        }
    }

    private byte[] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    private Bitmap decodeimage(String imagePath, int reqWidth, int reqHeight) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imagePath, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }


}
