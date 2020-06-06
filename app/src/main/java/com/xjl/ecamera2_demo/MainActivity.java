package com.xjl.ecamera2_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private int CAMERA_PERMISSION_REQUEST_CODE = 1;

    private String[] REQUEST_PERMISSONS = {
            android.Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE};

    CaptureFragment captureFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;

        Window window = this.getWindow();

        window.setFlags(flag, flag);

        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(REQUEST_PERMISSONS, CAMERA_PERMISSION_REQUEST_CODE);
        }
        captureFragment = (CaptureFragment) getSupportFragmentManager().findFragmentById(R.id.capture_fragment);

        findViewById(R.id.ratio).setOnClickListener(this);
        findViewById(R.id.flashlight).setOnClickListener(this);
        findViewById(R.id.colors).setOnClickListener(this);
        findViewById(R.id.switch_camera).setOnClickListener(this);
        findViewById(R.id.capture).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ratio:
                break;
            case R.id.flashlight:
                break;
            case R.id.colors:
                break;
            case R.id.switch_camera:
                break;
            case R.id.capture:
                break;
        }
    }
}
