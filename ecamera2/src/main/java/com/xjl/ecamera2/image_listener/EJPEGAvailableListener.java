package com.xjl.ecamera2.image_listener;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CaptureResult;
import android.location.Location;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class EJPEGAvailableListener extends EImageAvaliableListener {

    private static final String TAG = "EJPEGAvailableListener";

    private Context context;

    public int format = ImageFormat.JPEG;

    public EJPEGAvailableListener(Context context) {
        this.context = context;
    }


    @Override
    public void onImageAvailable(ImageReader imageReader) {
        if (getCaptureResult() != null) {
            Image image = imageReader.acquireNextImage();
            if (image != null) {
                ByteBuffer jpegByteBuffer = image.getPlanes()[0].getBuffer();
                byte[] jpegByteArray = new byte[jpegByteBuffer.remaining()];
                jpegByteBuffer.get(jpegByteArray);

                int imageWidth = image.getWidth();
                int imageHeight = image.getHeight();

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
                long date = System.currentTimeMillis();
                String title = "IMG_" + simpleDateFormat.format(date);
                String displayName = title + ".jpeg";

                Log.e(TAG, displayName);

                int orientation = getCaptureResult().get(CaptureResult.JPEG_ORIENTATION);
                Location location = getCaptureResult().get(CaptureResult.JPEG_GPS_LOCATION);
                double longitude = 0, latitude = 0;
                if (location != null) {
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                }

                File file = new File(filePath + File.separator + displayName);

                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(file, true);
                    fileOutputStream.write(jpegByteArray);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.ImageColumns.TITLE, title);
                values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, displayName);
                values.put(MediaStore.Images.ImageColumns.DATA, file.getAbsolutePath());
                values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, date);
                values.put(MediaStore.Images.ImageColumns.WIDTH, imageWidth);
                values.put(MediaStore.Images.ImageColumns.HEIGHT, imageHeight);
                values.put(MediaStore.Images.ImageColumns.ORIENTATION, orientation);
                values.put(MediaStore.Images.ImageColumns.LONGITUDE, longitude);
                values.put(MediaStore.Images.ImageColumns.LATITUDE, latitude);
                this.context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                image.close();
            }
        }
        //此次逻辑处理完毕 将返回的result 置空
        setCaptureResult(null);
    }


}
