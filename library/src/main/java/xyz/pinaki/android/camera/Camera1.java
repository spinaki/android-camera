package xyz.pinaki.android.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Looper;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by pinaki on 8/11/17.
 * logic related to camera1 -- should not contain other business stuff like threading, handler, bitmap processing etc
 */
@SuppressWarnings("deprecation")
class Camera1 extends BaseCamera {
    private static String TAG = Camera1.class.getSimpleName();
    static final int CAMERA1_ACTION_OPEN = 1;
    static final int CAMERA1_ACTION_TAKE_PICTURE = 2;
    private Camera camera;
    private int cameraId = 0;
    WeakReference<Context> context; // TODO: set this
    Camera1(Context c) {
//        context = new WeakReference<>(c);
    }

    @Override
    public boolean start() { // TODO: should this take the cameraID ?
//        if (!isCameraPresent(context.get())) {
//            return false;
//        }
        if (Looper.getMainLooper().isCurrentThread()) {
            throw new RuntimeException("Camera Cannot Start in Main UI Thread");
        }
        // start the camera
        try {
            if (camera != null) {
                // TODO: fix
                stopAndRelease();
            }
            // TODO
            Log.i(TAG, "start camera with ID: " + cameraId);
            camera = Camera.open(cameraId);
        } catch (RuntimeException exception) {
            Log.i(TAG, "Cannot open camera with id " + cameraId, exception);
        }
        return false;
    }

    @Override
    public void stop() {
        stopAndRelease();
    }

    @Override
    public boolean isCameraOpened() {
        return false;
    }

    @Override
    public void setFacing(int facing) {

    }

    @Override
    public int getFacing() {
        return 0;
    }

    @Override
    public void takePicture(final PhotoTakenCallback photoTakenCallback) {
        if (Looper.getMainLooper().isCurrentThread()) {
            throw new RuntimeException("Camera Cannot Take Picture in Main UI Thread");
        }
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                // do something about taking the picture
//                final Bitmap bitmap = BitmapUtils.createSampledBitmapFromBytes(data, 800);
                // send to the presenter maybe via the thread ?
                photoTakenCallback.onPhotoTaken(data);
            }
        });
    }

    private void stopAndRelease() {
//        if (camera != null) {
//            previewHolder.stopCameraPreview();
//            previewHolder.unsetCamera();
//        }
        // TODO: fix this
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
//        if (callback != null) {
//            callback.onCameraClosed();
//        }
    }

    private static boolean isCameraPresent(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
}
