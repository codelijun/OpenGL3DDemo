package com.example.lijun.opengl3ddemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.example.lijun.opengl3ddemo.util.WallpaperUtil;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_SET_WALLPAPER = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!WallpaperUtil.supportGlEs20(this)){
            Toast.makeText(this, "不支持openGL 2.0", Toast.LENGTH_SHORT).show();
            return;
        }
        WallpaperUtil.setLiveWallpaper(this, MainActivity.this, REQUEST_CODE_SET_WALLPAPER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SET_WALLPAPER) {
            if (resultCode == RESULT_OK) {
                // TODO: 2017/3/13 设置动态壁纸成功
                finish();
            } else {
                // TODO: 2017/3/13 取消设置动态壁纸
                finish();
            }
        }
    }
}
