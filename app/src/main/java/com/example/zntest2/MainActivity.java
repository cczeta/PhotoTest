package com.example.zntest2;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    //返回码常量
    private static final int RESULT_LOAD_IMAGE = 1;
    private Button bt_column;
    private Button bt_camera;
    private ImageView iv_img;
    //图片存储路径
    private String mFilePath;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //根据时间信息动态生成图片文件名
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String str = format.format(date);
        mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/DCIM/100ANDRO/"+"IMG_"+str+".jpg";
        initView();

        bt_camera.setOnClickListener(this);
        bt_column.setOnClickListener(this);
    }



    private void initView() {
        bt_column = (Button)findViewById(R.id.bt_column);
        bt_camera = (Button)findViewById(R.id.bt_camera);
        iv_img = (ImageView)findViewById(R.id.iv_img);
    }


    //处理页面跳转后的返回数据
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //照相
        if(requestCode==RESULT_LOAD_IMAGE) {
            if(!(resultCode==RESULT_OK)){
                Toast.makeText(MainActivity.this, "拍照取消", Toast.LENGTH_SHORT).show();
                new File(mFilePath).delete();
            }else {
                Log.i("测试", "发送广播");

                iv_img.setImageBitmap(getBitmap(mFilePath));
                Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri imageUri = Uri.fromFile(new File(mFilePath));
                intentBc.setData(imageUri);
                //发送广播通知系统相册更新所拍图片
                this.sendBroadcast(intentBc);
                Toast.makeText(MainActivity.this, "图片已存储到系统相册", Toast.LENGTH_SHORT).show();
            }

            //查看相册
        }else if(requestCode==RESULT_LOAD_IMAGE+1){
            if(resultCode==RESULT_OK && data!=null) {
                Uri select_image = data.getData();
                //获取系统图片路径
                String[] file_path_column = {MediaStore.Images.Media.DATA};
                //获取游标对象
                Cursor cursor = getContentResolver().query(select_image,
                        file_path_column, null, null, null);
                //重置游标对象
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(file_path_column[0]);
                String picture_path = cursor.getString(columnIndex);
                Log.i("用户所选照片的路径:",picture_path);
                iv_img.setImageBitmap(getBitmap(picture_path));
                cursor.close();


                Toast.makeText(MainActivity.this, "查看相册", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.bt_column:
                //访问系统相册
                Intent column = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(column, RESULT_LOAD_IMAGE + 1);
                break;
            case R.id.bt_camera:
                //调用系统照相机
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File newFile = new File(mFilePath);
                try {
                    //创建图片存储文件
                    newFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Uri uri = Uri.fromFile(newFile);
                camera.putExtra(MediaStore.EXTRA_OUTPUT,uri);
                startActivityForResult(camera, RESULT_LOAD_IMAGE);
                break;
        }
    }


    //根据指定路径得到位图对象
    private Bitmap getBitmap(String path){
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inSampleSize = 10;
        return BitmapFactory.decodeFile(path,options);
    }
}
