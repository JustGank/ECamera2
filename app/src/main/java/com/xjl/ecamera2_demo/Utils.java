package com.xjl.ecamera2_demo;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.view.Surface;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static List<Surface> getSurfaceList(Surface... surfaces) {
        List<Surface> surfacesList = new ArrayList<>();
        for (int i = 0; i < surfaces.length; i++) {
            surfacesList.add(surfaces[i]);
        }
        return surfacesList;
    }

    /***
     * 摄像头传感器方向指的是传感器采集到的画面方向经过顺时针旋转多少度之后才能和局部坐标系的 y 轴正方向一致，也就是通过 CameraCharacteristics.SENSOR_ORIENTATION 获取到的值。
     *
     */

    public static int getDisplayRotation(CameraCharacteristics cameraCharacteristics, Context context) {
        //首先拿到屏幕方向
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        /**
         * 最后我们要考虑一个特殊情况，就是前置摄像头的画面是做了镜像处理的，
         * 也就是所谓的前置镜像操作，这个情况下， orientation 的值并不是实际我们要旋转的角度，
         * 我们需要取它的镜像值才是我们真正要旋转的角度，例如 orientation 为 270°，实际我们要旋转的角度是 90°。
         * */
        if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
            return (360 - (sensorOrientation + degrees) % 360) % 360;
        } else {
            return (360 - degrees + sensorOrientation) % 360;
        }


    }

    public static int getJpegOrientation(CameraCharacteristics cameraCharacteristics,int deviceOrientation) {
        int myDeviceOrientation = deviceOrientation;
        if (myDeviceOrientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN) {
            return 0;
        }
       int sensorOrientation= cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

        sensorOrientation= myDeviceOrientation = (myDeviceOrientation + 45) / 90 * 90;

        // Reverse device orientation for front-facing cameras
        boolean facingFront = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT;
        if (facingFront) {
            myDeviceOrientation = -myDeviceOrientation;
        }

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        return (sensorOrientation + myDeviceOrientation + 360) % 360;
    }


}
