package com.xjl.ecamera2.image_listener;

import android.hardware.camera2.CaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;

public class EYUVAvaliableListener extends EImageAvaliableListener {

    private static final String TAG = "EYUVAvaliableListener";

    @Override
    public void onImageAvailable(ImageReader reader) {
        Log.e(TAG, "OnImageAvailableListener onImageAvailable");
        Image image = reader.acquireNextImage();

        CaptureResult captureResult = getCaptureResult();

        if (image != null && captureResult != null) {
//                    Image.Plane[] planes = image.getPlanes();
//                    Image.Plane yPlane = planes[0];
//                    Image.Plane uPlane = planes[1];
//                    Image.Plane vPlane = planes[2];
//                    ByteBuffer yBuffer = yPlane.getBuffer();// Data from Y channel
//                    ByteBuffer uBuffer = uPlane.getBuffer(); // Data from U channel
//                    ByteBuffer vBuffer = vPlane.getBuffer(); // Data from V channel
        }
        image.close();


    }
}
