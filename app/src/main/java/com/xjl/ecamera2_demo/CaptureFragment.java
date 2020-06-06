package com.xjl.ecamera2_demo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class CaptureFragment extends Fragment {

    private static final String TAG = "CaptureFragment";

    //摄像头管理类，用于检测、打开系统摄像头，通过getCameraCharacteristics(cameraId)可以获取摄像头特征。
    CameraManager cameraManager;
    //相机特性类，例如，是否支持自动调焦，是否支持zoom，是否支持闪光灯一系列特征。
    CameraCharacteristics cameraCharacteristics;
    //相机设备,类似早期的camera类
    CameraDevice cameraDevice;
    //用于创建预览、拍照的Session类。通过它的setRepeatingRequest()方法控制预览界面 , 通过它的capture()方法控制拍照动作或者录像动作。
    CameraCaptureSession cameraCaptureSession;
    //一次捕获的请求，可以设置一些列的参数，用于控制预览和拍照参数，例如：对焦模式，曝光模式，zoom参数等等。
    CameraCaptureSession mCaptureSession;
    CaptureRequest captureRequest;
    String[] cameraList;
    TextureView textureView;
    View view;
    String frontCameraId, backCameraId, exteralCameraId, currentCameraId;
    Surface surface, imageReaderSurface;
    Size previewSize;
    ImageReader imageReader;

    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture previewSurfaceTexture, int width, int height) {
            Log.e(TAG, "onSurfaceTextureAvailable width=" + width + "  height=" + height);

            try {
                previewSize = getOptimalSize(cameraManager.getCameraCharacteristics(currentCameraId), SurfaceTexture.class, width, height);

                if (previewSize == null) {
                    Log.e(TAG, "getOptimalSize previewSize is null");
                    previewSize = new Size(1920, 1080);
                }
                previewSurfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
                surface = new Surface(previewSurfaceTexture);

                int format = ImageFormat.JPEG;
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(currentCameraId);
                StreamConfigurationMap configurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (configurationMap.isOutputSupportedFor(format)) {

                    /**
                     * width：图像数据的宽度。
                     * height：图像数据的高度。
                     * format：图像数据的格式，定义在 ImageFormat 里，例如 ImageFormat.YUV_420_888。
                     * maxImages：最大 Image 个数，可以理解成 Image 对象池的大小。
                     * */
                    imageReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), format, 3);
                    imageReader.setOnImageAvailableListener(onImageAvailableListener, deviceStateHandler);

                    imageReaderSurface = imageReader.getSurface();


                } else {
                    Log.e(TAG, "not support ImageFormat =" + format);
                }
                //建立回话时 就要携带需要 有会话的Surface
                cameraDevice.createCaptureSession(Utils.getSurfaceList(surface, imageReaderSurface), captureSessionCallback, deviceStateHandler);

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.e(TAG, "onSurfaceTextureSizeChanged width=" + width + "  height=" + height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.e(TAG, "onSurfaceTextureDestroyed ");
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            Log.e(TAG, "onSurfaceTextureUpdated ");
        }
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_capture, container, false);
        textureView = view.findViewById(R.id.texture);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            cameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
            cameraList = cameraManager.getCameraIdList();

            if (cameraList == null || cameraList.length == 0) {
                Toast.makeText(getContext(), "你的设备当前没有可用的摄像头", Toast.LENGTH_SHORT);
                return;
            }

            Log.e(TAG, "cameraList.length=" + cameraList.length);

            for (int i = 0; i < cameraList.length; i++) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraList[i]);
                int INFO_SUPPORTED_HARDWARE_LEVEL = cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                Log.e(TAG, "INFO_SUPPORTED_HARDWARE_LEVEL=" + INFO_SUPPORTED_HARDWARE_LEVEL);
                if (INFO_SUPPORTED_HARDWARE_LEVEL == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                    Toast.makeText(getContext(), "您的设备无法使用Camera2", Toast.LENGTH_SHORT);
                    return;
                }

                int LENS_FACING = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                Log.e(TAG, "LENS_FACING=" + LENS_FACING);
                if (LENS_FACING == CameraCharacteristics.LENS_FACING_FRONT) {
                    currentCameraId = frontCameraId = cameraList[i];
                } else if (LENS_FACING == CameraCharacteristics.LENS_FACING_BACK) {
                    backCameraId = cameraList[i];
                } else if (LENS_FACING == CameraCharacteristics.LENS_FACING_EXTERNAL) {
                    exteralCameraId = cameraList[i];
                }
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        try {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(currentCameraId, cameraDeviceStateCallback, deviceStateHandler);
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        textureView.setSurfaceTextureListener(surfaceTextureListener);

        myOrientationEventListener = new MyOrientationEventListener(getContext(), SensorManager.SENSOR_DELAY_NORMAL);

        myOrientationEventListener.enable();


    }

    private DeviceStateHandler deviceStateHandler = new DeviceStateHandler();

    private class DeviceStateHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Log.e(TAG, "DeviceStateHandler msg.what=" + msg.what);
            switch (msg.what) {

            }
        }
    }


    CameraDevice.StateCallback cameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.e(TAG, "CameraDevice.StateCallback onOpened");
            cameraDevice = camera;
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
            cameraDevice = null;
        }
    };


    MyOrientationEventListener myOrientationEventListener;

    public class MyOrientationEventListener extends OrientationEventListener {

        public int orientation;

        public MyOrientationEventListener(Context context, int rate) {
            super(context, rate);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            Log.e(TAG, "MyOrientationEventListener orientation=" + orientation);
            this.orientation = orientation;
        }
    }

    ;

    CameraCaptureSession.StateCallback captureSessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.e(TAG, "captureSessionCallback onConfigured");
            cameraCaptureSession = session;

            try {
                CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                builder.addTarget(surface);
                builder.addTarget(imageReaderSurface);
                int or = Utils.getJpegOrientation(cameraManager.getCameraCharacteristics(currentCameraId), myOrientationEventListener.orientation);

                builder.set(CaptureRequest.JPEG_ORIENTATION, or);

                captureRequest = builder.build();
                cameraCaptureSession.setRepeatingRequest(captureRequest, captureCallback, deviceStateHandler);


            } catch (CameraAccessException e) {
                e.printStackTrace();
            }


        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

        }

        @Override
        public void onClosed(@NonNull CameraCaptureSession session) {
            super.onClosed(session);
        }
    };

    BlockingDeque<CaptureResult> captureResults = new LinkedBlockingDeque<>();

    CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);

        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Log.e(TAG, "CaptureCallback onCaptureCompleted");
            try {
                captureResults.put(result);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };


    //拿到了一帧的数据
    ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.e(TAG, "OnImageAvailableListener onImageAvailable");
            Image image = reader.acquireNextImage();

            try {
                CaptureResult captureResult = captureResults.take();

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

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };


    /**
     * ImageReader：常用来拍照或接收 YUV 数据。
     * MediaRecorder：常用来录制视频。
     * MediaCodec：常用来录制视频。
     * Allocation
     * SurfaceHolder：常用来显示预览画面。
     * SurfaceTexture：常用来显示预览画面。
     * 这是因为相机 Sensor 的宽是长边，而高是短边。
     */
    private Size getOptimalSize(CameraCharacteristics cameraCharacteristics, Class clazz, int maxWidth, int maxHeight) {
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

    private Size getOptimalSize(List<Size> supportedSizes, int maxWidth, int maxHeight) {
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


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraCaptureSession != null) {
            try {
                cameraCaptureSession.stopRepeating();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        if (cameraDevice != null) {
            cameraDevice.close();
        }
    }



}
