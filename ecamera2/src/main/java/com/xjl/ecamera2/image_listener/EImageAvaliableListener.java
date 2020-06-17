package com.xjl.ecamera2.image_listener;


import android.hardware.camera2.CaptureResult;
import android.media.ImageReader;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public abstract class EImageAvaliableListener implements ImageReader.OnImageAvailableListener {

    public String fileName;

    public String displayTitle;

    public String filePath=Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ECamera2";

    public EImageAvaliableListener() {

    }
    /**
     * 可以拿到缩率图
     */
    private CaptureResult captureResult;

    public CaptureResult getCaptureResult() {
        return captureResult;
    }

    public void setCaptureResult(CaptureResult captureResult) {
        this.captureResult = captureResult;
    }

    public String getFileName() {
        long date = System.currentTimeMillis();

        SimpleDateFormat dateFormat = new SimpleDateFormat();

        return "IMG_" + dateFormat.format(new Date());

    }

    public void getDisPlayName() {

    }


}
