package xyz.pinaki.android.camera;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;


/**
 * Created by pinaki on 8/16/17.
 */

final class CameraHandlerThread extends HandlerThread {
    private static String TAG = CameraHandlerThread.class.getSimpleName();
//    private final WeakReference<CameraStatusCallback> uiCallback;
    private Handler handler;

    CameraHandlerThread(String name) {
        super(name);
//        uiCallback = new WeakReference<>(i);
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
//        handler = new CameraHandler(getLooper(), uiCallback);
    }

    void prepareHandler() {
        if (handler == null) {
            handler = new CameraHandler(getLooper());
        }
    }

    void queueMessage(Message msg) {
        if (handler != null && isAlive()) {
            handler.sendMessage(msg);
        }
    }

    private static final class CameraHandler extends Handler {
        private final Handler uiHandler = new Handler(Looper.getMainLooper());
        CameraHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            final Camera1 camera1 = (Camera1) msg.obj;
            super.handleMessage(msg);
            switch (msg.what) {
                case Camera1.CAMERA1_ACTION_OPEN:
                    camera1.start();
                    camera1.setUpPreview();
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            camera1.configureParameters();
                            camera1.startPreview();
                            camera1.cameraStatusCallback.onCameraOpen();
                        }
                    });
                    break;
                case Camera1.CAMERA1_ACTION_TAKE_PICTURE:
                    camera1.takePicture(new BaseCamera.PhotoTakenCallback() {
                        @Override
                        public void onPhotoTaken(final byte[] data) {
                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    camera1.cameraStatusCallback.onPhotoTaken(data);
                                }
                            });
                            processBitmap(camera1, data);
                        }
                    });
                    break;
                default:
                    throw new RuntimeException("Unknown Action In CameraHandlerThred with what: " + msg.what);
            }
        }
        private void processBitmap(final Camera1 camera1, byte[] data) {
            final Bitmap bitmap = BitmapUtils.createSampledBitmapFromBytes(data, camera1.getMaxWidthSize());
            Matrix matrix = camera1.getImageTransformMatrix();
            final Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap
                    .getHeight(), matrix, false);
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    camera1.startPreview();
                    camera1.cameraStatusCallback.onBitmapProcessed(rotatedBitmap);
                }
            });
        }
    }
}
