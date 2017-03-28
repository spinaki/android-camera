package xyz.pinaki.android.camera;

import android.graphics.Bitmap;
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
    private Handler workerHandler = null;

    /* package */ CameraHandlerThread() {
        super(TAG);
    }

    //
    /* package */ void openCamera(final int cameraId, final Handler uiHandler, final CameraCallback cameraCallback) {
        if (workerHandler == null) {
            workerHandler = new Handler(getLooper());
        }
        workerHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "workerHandler run thread: " + Thread.currentThread().getId());
                try {
                    final Camera camera = Camera.open(cameraId);
                    Log.i(TAG, "opened cam in background");
                    if (uiHandler != null) {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG, "onCameraOpen in ui");
                                cameraCallback.onCameraOpen(camera);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.i(TAG, "Failed to Open Camera");
                    e.printStackTrace();
                }
            }
        });
    }

    /* package*/ void capturePhoto(final Camera camera, final CameraCallback cameraCallback) {
        workerHandler.post(new Runnable() {
            @Override
            public void run() {
                camera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes, Camera camera) {
                        final Bitmap bitmap = BitmapUtils.createSampledBitmapFromBytes(bytes, 800);
                        Handler uiHandler = new Handler(Looper.getMainLooper());
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (cameraCallback != null) {
                                    cameraCallback.onPictureTaken(bitmap);
                                }
                            }
                        });
                    }
                });
            }
        });

    }
}
