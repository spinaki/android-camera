package xyz.pinaki.android.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;


/**
 * Created by pinaki on 8/16/17.
 */

final class CameraHandlerThread extends HandlerThread {
    private static String TAG = CameraHandlerThread.class.getSimpleName();
    private final WeakReference<CameraStatusCallback> uiCallback;
    private Handler handler;

    CameraHandlerThread(String name, CameraStatusCallback i) {
        super(name);
        uiCallback = new WeakReference<>(i);
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
//        handler = new CameraHandler(getLooper(), uiCallback);
    }

    void prepareHandler() {
        if (handler == null) {
            handler = new CameraHandler(getLooper(), uiCallback);
        }
    }

    void queueMessage(Message msg) {
        if (handler != null && isAlive()) {
            handler.sendMessage(msg);
        }
    }

    private static final class CameraHandler extends Handler {
        private final WeakReference<CameraStatusCallback> uiCallback;
        private final Handler uiHandler = new Handler(Looper.getMainLooper());
        CameraHandler(Looper looper, WeakReference<CameraStatusCallback> i) {
            super(looper);
            uiCallback = i;
        }
        @Override
        public void handleMessage(Message msg) {
            final Camera1 camera1 = (Camera1) msg.obj;
            super.handleMessage(msg);
            switch (msg.what) {
                case Camera1.CAMERA1_ACTION_OPEN:
                    Log.i(TAG,"in thread to open cam");
                    camera1.start(); // TODO: fix this -- is camera return required ?
                    camera1.setUpPreview();
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            camera1.configureParameters();
                            camera1.startPreview();
                            uiCallback.get().onCameraOpen();
                        }
                    });
                    break;
                case Camera1.CAMERA1_ACTION_TAKE_PICTURE:
                    camera1.takePicture(new BaseCamera.PhotoTakenCallback() {
                        @Override
                        public void onPhotoTaken(byte[] data) {
                            // do something about taking the picture
//                            final Bitmap bitmap = BitmapUtils.createSampledBitmapFromBytes(data, 800);
                            final BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = false;
                            final Bitmap bitmap =  BitmapFactory.decodeByteArray(data, 0, data.length, options);
                            Log.i(TAG, "Original width, height: " + options.outWidth + ", " + options.outHeight);
                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    camera1.startPreview();
                                    uiCallback.get().onImageCaptured(bitmap);
                                }
                            });
                        }
                    });
                    break;
                default:
                    throw new RuntimeException("Unknown Action In CameraHandlerThred with what: " + msg.what);
            }
        }
    }
}
