package xyz.pinaki.android.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
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
    private ViewFinderPreview viewFinderPreview;
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
        // TODO: choose the camera based on ID
        // start the camera
        try {
            if (camera != null) {
                // TODO: fix
                stopAndRelease();
            }
            // TODO
            Log.i(TAG, "start camera with ID: " + Camera.CameraInfo.CAMERA_FACING_BACK);
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        } catch (RuntimeException exception) {
            Log.i(TAG, "Cannot open camera with id " + Camera.CameraInfo.CAMERA_FACING_BACK, exception);
        }
        return false;
    }

    void configureParameters() {
        Camera.Parameters parameters = camera.getParameters();
        // get supporting preview sizes
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
//            mPictureSizes.add(new Size(size.width, size.height));
        }
        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
//            mPictureSizes.add(new Size(size.width, size.height));
        }
        adjustCameraParameters(parameters);
        // fix orientation
//        camera.setDisplayOrientation(calcCameraRotation(mDisplayOrientation));

    }

    private void adjustCameraParameters(Camera.Parameters parameters) {
        Camera.Size s = parameters.getSupportedPreviewSizes().get(0);
        Log.i(TAG, "getSupportedPreviewSizes: " + s.width + ", " + s.height);
        parameters.setPreviewSize(s.width, s.height);
        Camera.Size s1 = parameters.getSupportedPictureSizes().get(0);
        Log.i(TAG, "getSupportedPictureSizes: " + + s1.width + ", " + s1.height);
        parameters.setPictureSize(s.width, s.height);
//        parameters.setRotation(calcCameraRotation(mDisplayOrientation));
//        setAutoFocusInternal(mAutoFocus);
//        setFlashInternal(mFlash);
        camera.setParameters(parameters);
        // TODO: is the following required ? -- not sure
//        viewFinderPreview.getSurfaceHolder().setFixedSize(s.height, s.width);
    }

    void setPreview(ViewFinderPreview v) {
        viewFinderPreview = v;
    }
    void setUpPreview() {
        try {
            if (viewFinderPreview.gePreviewType() == SurfaceHolder.class) {
                Log.i(TAG, "setPreviewDisplay");
                camera.setPreviewDisplay(viewFinderPreview.getSurfaceHolder());
            } else if (viewFinderPreview.gePreviewType() == SurfaceTexture.class) {
                camera.setPreviewTexture(viewFinderPreview.getSurfaceTexture());
            } else {
                throw new RuntimeException("Unknown Preview Surface Type");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void startPreview() {
        Log.i(TAG, "startPreview");
        camera.startPreview();
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

    // TODO: fix this
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
