package xyz.pinaki.android.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

/**
 * Created by pinaki on 3/20/17.
 */
@SuppressWarnings("deprecation")
/* package */ class CameraHandlerThread extends HandlerThread {
    private static final String TAG = CameraHandlerThread.class.getSimpleName();
    private Handler workerHandler = null;
    private final Handler uiHandler; // responseHandler
    private final CameraCallback cameraCallback;

    /* package */ CameraHandlerThread(Handler responseHandler, CameraCallback callback) {
        super(TAG);
        uiHandler = responseHandler;
        cameraCallback = callback;
    }

    //
    /* package */ void openCamera() {
        if (workerHandler == null) {
            workerHandler = new Handler(getLooper());
        }
        workerHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "workerHandler run thread: " + Thread.currentThread().getId());
                final Camera camera = Camera.open(0);
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
            }
        });
    }
}
