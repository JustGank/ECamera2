package com.xjl.ecamera2.image_listener;


import android.media.ImageReader;
import android.os.Environment;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public abstract class EImageAvaliableListener implements ImageReader.OnImageAvailableListener {

    public String fileName;

    public String displayTitle;

    public String filePath=Environment.getExternalStorageDirectory().getPath() + File.separator + "ECamera2";

    public EImageAvaliableListener() {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }


    public String getFileName() {
        long date = System.currentTimeMillis();

        SimpleDateFormat dateFormat = new SimpleDateFormat();

        return "IMG_" + dateFormat.format(new Date());

    }

    public void getDisPlayName() {

    }


}
