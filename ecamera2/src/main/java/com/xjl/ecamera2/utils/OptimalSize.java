package com.xjl.ecamera2.utils;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Log;
import android.util.Size;

import java.util.List;

public class OptimalSize {

    private static final String TAG = "OptimalSize";
    /**
     * ImageReader：常用来拍照或接收 YUV 数据。
     * MediaRecorder：常用来录制视频。
     * MediaCodec：常用来录制视频。
     * Allocation
     * SurfaceHolder：常用来显示预览画面。
     * SurfaceTexture：常用来显示预览画面。
     * 这是因为相机 Sensor 的宽是长边，而高是短边。
     */
    public static Size getOptimalSize(CameraCharacteristics cameraCharacteristics, Class clazz, int maxWidth, int maxHeight) {
        float aspectRatio = maxWidth / (float) maxHeight;
        StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] supportedSizes = streamConfigurationMap.getOutputSizes(clazz);
        if (supportedSizes != null) {
            for (Size size : supportedSizes) {
                Log.e(TAG, "getOptimalSize size width=" + size.getWidth() + "   height=" + size.getHeight());
                if (size.getWidth() / (float) size.getHeight() == aspectRatio &&
                        size.getHeight() <= maxHeight && size.getWidth() <= maxWidth) {
                    return size;
                }
            }
        }
        return null;
    }

    public static Size getOptimalSize(List<Size> supportedSizes, int maxWidth, int maxHeight) {
        float aspectRatio = maxWidth / (float) maxHeight;
        if (supportedSizes != null) {
            for (Size size : supportedSizes) {
                if (size.getWidth() / (float) size.getHeight() == aspectRatio
                        && size.getHeight() <= maxHeight && size.getWidth() <= maxWidth) {
                    return size;
                }
            }
        }
        return null;
    }


}
