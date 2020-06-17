package com.xjl.ecamera2_demo;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ImageReader;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.xjl.ecamera2.helper.CameraHelper;
import com.xjl.ecamera2.image_listener.EJPEGAvailableListener;

public class CaptureFragment extends Fragment {

    private static final String TAG = "CaptureFragment";

    public TextureView textureView;
    public View view;
    public Surface previewSurface, imageReaderSurface;
    public Size previewSize;

    public ImageReader imageReader;

    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture previewSurfaceTexture, int width, int height) {
            Log.e(TAG, "onSurfaceTextureAvailable width=" + width + "  height=" + height);

                previewSize = cameraHelper.getPreviewSize(SurfaceTexture.class, width, height);

                if (previewSize == null) {
                    Log.e(TAG, "getOptimalSize previewSize is null");
                    previewSize = new Size(width, height);
                }
                previewSurfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

                previewSurface = new Surface(previewSurfaceTexture);

                if (cameraHelper.isOutputSupportedFor(ejpegAvailableListener.format)) {

                    /**
                     * width：图像数据的宽度。
                     * height：图像数据的高度。
                     * format：图像数据的格式，定义在 ImageFormat 里，例如 ImageFormat.YUV_420_888。
                     * maxImages：最大 Image 个数，可以理解成 Image 对象池的大小。
                     * */
                    imageReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ejpegAvailableListener.format, 3);
                    imageReader.setOnImageAvailableListener(ejpegAvailableListener, cameraHelper.deviceStateHandler);

                    imageReaderSurface = imageReader.getSurface();


                } else {
                    Log.e(TAG, "not support ImageFormat =" + ejpegAvailableListener.format);
                }
                //建立回话时 就要携带需要 有会话的Surface
                cameraHelper.setSurace(previewSurface,imageReaderSurface);


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

    public CameraHelper cameraHelper;

    public CaptureFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_capture, container, false);
        textureView = view.findViewById(R.id.texture);
        return view;
    }

    public int defaultOpenFacing = CameraCharacteristics.LENS_FACING_FRONT;

    private EJPEGAvailableListener ejpegAvailableListener;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cameraHelper = new CameraHelper(getContext());

        if (!cameraHelper.openCameraByFacing(getContext(), defaultOpenFacing)) {
            Log.e(TAG, "无此方向摄像头!");
        }
         textureView.setSurfaceTextureListener(surfaceTextureListener);
         ejpegAvailableListener = new EJPEGAvailableListener(getContext());
    }


    CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);

        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Log.e(TAG, "CaptureCallback onCaptureCompleted");
            ejpegAvailableListener.setCaptureResult(result);
        }
    };


    public void takePhoto(){
        cameraHelper.takePhoto( imageReaderSurface,captureCallback);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraHelper.release();
    }


}
