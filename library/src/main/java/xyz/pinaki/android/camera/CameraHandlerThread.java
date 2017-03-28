package xyz.pinaki.android.camera;

import android.graphics.Bitmap;
import android.graphics.Matrix;
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

    void processBitmap(final int cameraId, final Bitmap bitmap, final CenteredCameraPreviewHolder previewHolder,
                       final CameraOrientationListener orientationListener, final CameraCallback cameraCallback) {
        workerHandler.post(new Runnable() {
            @Override
            public void run() {
                Matrix matrix = new Matrix();
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//                 Why is this necessary ?
//                    http://stackoverflow.com/questions/8332312/android-front-and-back-camera-orientation-landscape/8347956
                    // without this front facnig camera image is inverted. this basically inverts the x coordinates
                    // across the Y axis
                    float[] mirrorY = {-1, 0, 0, 0, 1, 0, 0, 0, 1};
                    Matrix matrixMirrorY = new Matrix();
                    matrixMirrorY.setValues(mirrorY);
                    matrix.postConcat(matrixMirrorY);
                }
                // this is necessary for fixing the orientation of the captured bitmap
                int rotation = previewHolder.getDisplayOrientation();
//                // following is resp for orientation in landscape mode. without this landscape will be saved as
//                // portraits in the bitmap / jpeg
//                Log.i(TAG, "in onPictureTaken orientationListener : " + orientationListener.getRememberedOrientation());
                rotation = (rotation + orientationListener.getRememberedOrientation() + previewHolder.getLayoutOrientation()) % 360;
                matrix.postRotate(rotation);
                final Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,
                        false);
                bitmap.recycle();
                Handler uiHandler = new Handler(Looper.getMainLooper());
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (cameraCallback != null) {
                            cameraCallback.onBitmapProcessed(rotatedBitmap);
                        }
                    }
                });
            }
        });
    }

}
