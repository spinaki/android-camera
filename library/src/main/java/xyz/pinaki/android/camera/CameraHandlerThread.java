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
    /* package */ void openCamera(final int cameraId, final InternalCallback internalCallback) {
        if (workerHandler == null) {
            workerHandler = new Handler(getLooper());
        }
        workerHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    final Camera camera = Camera.open(cameraId);
                    Handler uiHandler = new Handler(Looper.getMainLooper());
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            internalCallback.onCameraOpen(camera);
                        }
                    });
                } catch (Exception e) {
                    Log.i(TAG, "Failed to Open Camera");
                    e.printStackTrace();
                }
            }
        });
    }

    /* package*/ void capturePhoto(final Camera camera, final InternalCallback internalCallback) {
        workerHandler.post(new Runnable() {
            @Override
            public void run() {
                camera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(final byte[] bytes, Camera camera) {
                        final Bitmap bitmap = BitmapUtils.createSampledBitmapFromBytes(bytes, 800);
                        Handler uiHandler = new Handler(Looper.getMainLooper());
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (internalCallback != null) {
                                    internalCallback.onPictureTaken(bitmap, bytes);
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    void processBitmap(final int cameraId, final Bitmap bitmap, final DeviceOrientationListener orientationListener,
                       final RotationEventListener rotationEventListener, final InternalCallback internalCallback) {
        workerHandler.post(new Runnable() {
            @Override
            public void run() {
                // rotating the bitmap without depending on the Camera.Parameters.getRotation attribute
                //
                // more details here: http://www.androidzeitgeist.com/2013/01/fixing-rotation-camera-picture.html
                // https://developer.android.com/reference/android/hardware/Camera.Parameters.html#setRotation(int)

                // The value from OrientationEventListener is relative to the natural orientation of the device.
                // CameraInfo.orientation is the angle between camera orientation and natural device orientation.
                // The sum of the two is the rotation angle for back-facing camera. The difference of the two is
                // the rotation angle for front-facing camera. Note that the JPEG pictures of front-facing cameras
                // are not mirrored as in preview display.
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
                int rotation = rotationEventListener.getCameraDisplayRotation();
//                // following is resp for orientation in landscape mode. without this landscape will be saved as
//                // portraits in the bitmap / jpeg
//                Log.i(TAG, "in onPictureTaken orientationListener : " + orientationListener.getRememberedOrientation());
                rotation = (rotation + orientationListener.getRememberedOrientation() +
                        rotationEventListener.getDeviceDisplayRotation()) % 360;
                matrix.postRotate(rotation);
                // https://developer.android.com/reference/android/graphics/Bitmap.html#createBitmap(android.graphics.Bitmap, int, int, int, int)
                // "https://developer.android.com/reference/android/graphics/Bitmap.html#createBitmap(android.graphics.Bitmap, int, int, int, int)"
                final Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,
                        false);
                Handler uiHandler = new Handler(Looper.getMainLooper());
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (internalCallback != null) {
                            internalCallback.onBitmapProcessed(rotatedBitmap);
                        }
                    }
                });
            }
        });
    }

}
