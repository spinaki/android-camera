package xyz.pinaki.android.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

/**
 * Created by pinaki on 3/20/17.
 */
@SuppressWarnings("deprecation")
/* package */ class CameraHandlerThread extends HandlerThread {
    private static final String TAG = CameraHandlerThread.class.getSimpleName();
    CenteredCameraPreviewHolder previewHolder;
    Camera camera;
    public CameraHandlerThread(CenteredCameraPreviewHolder previewHolder) {
        super("CameraHandlerThread");
        this.previewHolder = previewHolder;
        start();
    }

    public void startCamera(final int cameraId) {
        Handler localHandler = new Handler(getLooper());
        localHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (camera != null) {
                        stopAndRelease();
                    }
                    Log.i(TAG, "open camera in thread");
                    camera = Camera.open(cameraId);
                    previewHolder.setCamera(camera, cameraId);
                } catch (RuntimeException exception) {
                    Log.i(TAG, "Cannot open camera with id " + cameraId, exception);
                }

                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        previewHolder.setCamera(camera, cameraId);
                    }
                });
            }
        });
    }

    private void stopAndRelease() {
        if (previewHolder != null) {
            previewHolder.stopCameraPreview();
            previewHolder.unsetCamera();
        }
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}
