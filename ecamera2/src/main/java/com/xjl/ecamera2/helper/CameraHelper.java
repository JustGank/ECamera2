package com.xjl.ecamera2.helper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.xjl.ecamera2.utils.OptimalSize;
import com.xjl.ecamera2.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CameraHelper {

    private static final String TAG = "CameraHelper";

    //摄像头管理类，用于检测、打开系统摄像头，通过getCameraCharacteristics(cameraId)可以获取摄像头特征。
    private CameraManager cameraManager;
    //相机特性类，例如，是否支持自动调焦，是否支持zoom，是否支持闪光灯一系列特征。
    private CameraCharacteristics currentCameraCharacteristics;
    //相机设备,类似早期的camera类
    private CameraDevice currentCameraDevice;
    //用于创建预览、拍照的Session类。通过它的setRepeatingRequest()方法控制预览界面 , 通过它的capture()方法控制拍照动作或者录像动作。
    private CameraCaptureSession currentCameraCaptureSession;
    //一次捕获的请求，可以设置一些列的参数，用于控制预览和拍照参数，例如：对焦模式，曝光模式，zoom参数等等。
    private CameraCaptureSession currentCaptureSession;
    private CaptureRequest captureRequest;

    Context context;

    HashMap<Integer, ArrayList<String>> cameraFaceMap;

    String currentOpenedId = "";

    DeviceOrientationListener deviceOrientationListener;

    public DeviceStateHandler deviceStateHandler = new DeviceStateHandler();

    private class DeviceStateHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Log.e(TAG, "DeviceStateHandler msg.what=" + msg.what);
            switch (msg.what) {

            }
        }
    }


    public CameraHelper(Context context) {
        this.context = context;
        cameraManager = (CameraManager) this.context.getSystemService(Context.CAMERA_SERVICE);

        try {
            String[] cameraList = cameraManager.getCameraIdList();

            cameraFaceMap = new HashMap<>();

            for (int i = 0; i < cameraList.length; i++) {
                initCameraFaceMap(cameraList[i]);
            }

            for (Integer facingType : cameraFaceMap.keySet()) {
                Log.e(TAG, "facingType=" + facingType + "    camera num=" + cameraFaceMap.get(facingType).size());
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        deviceOrientationListener = new DeviceOrientationListener(this.context, SensorManager.SENSOR_DELAY_NORMAL);

        deviceOrientationListener.enable();

    }

    private void initCameraFaceMap(String id) {
        try {
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(id);
            int INFO_SUPPORTED_HARDWARE_LEVEL = cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            Log.e(TAG, "INFO_SUPPORTED_HARDWARE_LEVEL=" + INFO_SUPPORTED_HARDWARE_LEVEL);
            if (INFO_SUPPORTED_HARDWARE_LEVEL == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                Log.e(TAG, "camera id =" + id + " cant user camera2!");
                return;
            }

            int facing = cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING);
            if (cameraFaceMap.get(facing) == null) {
                cameraFaceMap.put(facing, new ArrayList<String>());
            }
            cameraFaceMap.get(facing).add(id);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public List<String> getCameraIdByFacing(int facing) {
        return cameraFaceMap.get(facing);
    }

    public boolean openCameraByFacing(Context context, int facing) {
        ArrayList<String> facingList = cameraFaceMap.get(facing);
        if (facingList != null && facingList.size() > 0) {
            OpenCameraById(context, facingList.get(0));
            return true;
        } else {
            return false;
        }
    }

    public void OpenCameraById(Context context, String cameraId) {
        this.currentOpenedId = cameraId;
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                cameraManager.openCamera(cameraId, cameraDeviceStateCallback, deviceStateHandler);
            }
            currentCameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    //捕捉静态图片
    public void takePhoto(Surface surface, CameraCaptureSession.CaptureCallback captureCallback) {
        try {
            CaptureRequest.Builder builder = currentCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            builder.set(CaptureRequest.JPEG_ORIENTATION, Utils.getJpegOrientation(currentCameraCharacteristics, deviceOrientationListener.orientation));
            byte QUALITY = 100;
            builder.set(CaptureRequest.JPEG_QUALITY, QUALITY);
            builder.addTarget(surface);
            for (int i = 0; i < buildSurface.size(); i++) {
                builder.addTarget(buildSurface.get(i));
            }
            CaptureRequest captureRequest = builder.build();
            currentCameraCaptureSession.capture(captureRequest, captureCallback, deviceStateHandler);


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    public CameraCharacteristics getCurrentCameraCharacteristics() {
        return currentCameraCharacteristics;
    }


    private List<Surface> buildSurface;

    public void setSurace(Surface... surfaces) {
        buildSurface = Utils.getSurfaceList(surfaces);
        try {
            currentCameraDevice.createCaptureSession(buildSurface, captureSessionCallback, deviceStateHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    CameraDevice.StateCallback cameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.e(TAG, "CameraDevice.StateCallback onOpened");
            currentCameraDevice = camera;
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.e(TAG, "CameraDevice.StateCallback onDisconnected");
        }

        /**
         * 当相机被成功开启的时候会通过 CameraStateCallback.onOpened() 方法回调一个 CameraDevice
         * 实例给你，否则的话会通过 CameraStateCallback.onError() 方法回调一个 CameraDevice
         * 实例和一个错误码给你。onOpened() 和 onError() 其实都意味着相机已经被开启了，唯一的区别是 onError()
         * 表示开启过程中出了问题，你必须把传递给你的 CameraDevice 关闭，而不是继续使用它，
         * 具体的 API 介绍可以自行查看文档
         * */
        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            if (camera != null) {
                camera.close();
            }
            currentCameraDevice = null;
        }
    };


    CameraCaptureSession.StateCallback captureSessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.e(TAG, "captureSessionCallback onConfigured session is null="+(session==null));
            currentCameraCaptureSession = session;

            try {
                CaptureRequest.Builder builder = currentCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                for (int i = 0; i < buildSurface.size(); i++) {
                    builder.addTarget(buildSurface.get(i));
                }

                int or = Utils.getJpegOrientation(cameraManager.getCameraCharacteristics(currentOpenedId), deviceOrientationListener.orientation);

                builder.set(CaptureRequest.JPEG_ORIENTATION, or);

                captureRequest = builder.build();
                if(currentCameraCaptureSession!=null)
                {
                    currentCameraCaptureSession.setRepeatingRequest(captureRequest, captureCallback, deviceStateHandler);
                }

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.e(TAG, "captureSessionCallback onConfigureFailed" );
        }

        @Override
        public void onClosed(@NonNull CameraCaptureSession session) {
            super.onClosed(session);
            Log.e(TAG, "captureSessionCallback onClosed" );
        }
    };

    CameraCaptureSession.CaptureCallback captureCallback;

    public void setCameraCaptureCallback(CameraCaptureSession.CaptureCallback captureCallback) {
        this.captureCallback = captureCallback;
    }

    /**
     * 工具类型的方法
     */
    public Size getPreviewSize(Class clazz, int maxWidth, int maxHeight) {
        return OptimalSize.getOptimalSize(currentCameraCharacteristics, clazz, maxWidth, maxHeight);
    }

    public boolean isOutputSupportedFor(int format) {
        return currentCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).isOutputSupportedFor(format);
    }

    public CameraDevice getCurrentCameraDevice() {
        return currentCameraDevice;
    }


    public void release() {
        if (currentCaptureSession != null) {
            try {
                currentCaptureSession.stopRepeating();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        if (currentCameraDevice != null) {
            currentCameraDevice.close();
        }
    }


}
