package xyz.pinaki.android.camera;

import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import xyz.pinaki.android.camera.dimension.AspectRatio;
import xyz.pinaki.android.camera.dimension.Size;

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
    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private final AtomicBoolean isPictureCaptureInProgress = new AtomicBoolean(false);
    Camera1(AppCompatActivity a) {
        super(a);
    }

    @Override
    public boolean start() { // TODO: should this take the cameraID ?
        if (!super.start()) {
            return false;
        }
        if (Looper.getMainLooper().isCurrentThread()) {
            throw new RuntimeException("Camera Cannot Start in Main UI Thread");
        }
        deviceOrientationListener.enable();
        // TODO: choose the camera based on ID
        // start the camera
        try {
            if (camera != null) {
                stopAndRelease();
            }
            Log.i(TAG, "start camera with ID: " + cameraId);
            camera = Camera.open(cameraId);
            return true;
        } catch (RuntimeException exception) {
            Log.i(TAG, "Cannot open camera with id " + cameraId, exception);
        }
        return false;
    }

    void configureParameters() {
        adjustCameraParameters(camera.getParameters());
        camera.setDisplayOrientation(calcCameraRotation());
    }

    /**
     * links:
     * https://developer.android.com/reference/android/hardware/Camera.html#setDisplayOrientation(int)
     * https://www.captechconsulting.com/blogs/android-camera-orientation-made-simple
     * http://www.androidzeitgeist.com/2012/10/displaying-camera-preview-instant.html
     * https://plus.google.com/+AndroidDevelopers/posts/jXNFNKWxsc3
     * https://stackoverflow.com/questions/9055460/is-androids-camerainfo-orientation-correctly-documented-incorrectly-implemente
     * https://developer.android.com/reference/android/hardware/Camera.html#setDisplayOrientation%28int%29
     * @return
     */
    private int calcCameraRotation() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        int rememberedOr = (360 - deviceOrientationListener.getRememberedOrientation()) % 360;
        Log.i(TAG, "cameraInfo.orientation: " + cameraInfo.orientation +
                ", displayOrientation: " + displayOrientation + ", rememberedOr: " + rememberedOr);
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//            return (360 - (cameraInfo.orientation + displayOrientation) % 360) % 360;
            return (360 - (cameraInfo.orientation + rememberedOr) % 360) % 360;
        } else {  // back-facing
//            return (cameraInfo.orientation - displayOrientation + 360) % 360;
            return (cameraInfo.orientation - rememberedOr + 360) % 360;
        }
    }

    Matrix getImageTransformMatrix() {
        Matrix matrix = new Matrix();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            float[] mirrorY = {-1, 0, 0, 0, 1, 0, 0, 0, 1};
            Matrix matrixMirrorY = new Matrix();
            matrixMirrorY.setValues(mirrorY);
            matrix.postConcat(matrixMirrorY);
        }
        matrix.postRotate(calcCameraRotation());
        return matrix;
    }

    private void adjustCameraParameters(Camera.Parameters parameters) {
        AspectRatio desiredAspectRatio = AspectRatio.of(aspectRatio.getWidth(), aspectRatio.getHeight()); // original AR
        List<Size> availableSizes = convertSizes(parameters.getSupportedPreviewSizes());
        Size s = chooseOptimalSize(availableSizes);
        Log.i(TAG, "OptimalPreviewSize: " + s.getWidth() + ", " + s.getHeight() + ", AspectRatio: " + aspectRatio);
        cameraStatusCallback.onAspectRatioAvailable(desiredAspectRatio, aspectRatio, availableSizes);

        parameters.setPreviewSize(s.getWidth(), s.getHeight());
        // TODO: picture and preview sizes might be different
        s = chooseOptimalSize(convertSizes(parameters.getSupportedPictureSizes()));
        parameters.setPictureSize(s.getWidth(), s.getHeight());
        setAutoFocusInternal(parameters);
        // setRotation is commented since this has not been implemented correctly in many device (e.g., Samsung
        // Galaxy Prime). We compensate for the rotation manually using getImageTransformMatrix
//        parameters.setRotation(calcCameraRotation());
        // TODO: set flash
//        setFlashInternal(mFlash);
        camera.setParameters(parameters);
    }
    private static List<Size> convertSizes(List<Camera.Size> cameraSizes) {
        List<Size> sizes = new ArrayList<>();
        for (Camera.Size csize : cameraSizes) {
            sizes.add(new Size(csize.width, csize.height));
        }
        return sizes;
    }

    private void setAutoFocusInternal(Camera.Parameters parameters) {
        final List<String> modes = parameters.getSupportedFocusModes();
        if (modes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        } else if (modes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
        } else if (modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        } else {
            parameters.setFocusMode(modes.get(0));
        }
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
        camera.startPreview();
    }

    @Override
    public void stop() {
        deviceOrientationListener.disable();
        stopAndRelease();
    }

    @Override
    public boolean isCameraOpened() {
        return false;
    }

    @Override
    public void setFacing(CameraAPI.LensFacing lensFacing) {
        if (lensFacing == CameraAPI.LensFacing.BACK) {
            cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else if (lensFacing == CameraAPI.LensFacing.FRONT) {
            cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            throw new RuntimeException("Unknown Facing Camera!");
        }
    }

    @Override
    public int getFacing() {
        return 0;
    }

    @Override
    public void takePicture(final PhotoTakenCallback photoTakenCallback) {
        deviceOrientationListener.rememberOrientation();
        if (Looper.getMainLooper().isCurrentThread()) {
            throw new RuntimeException("Camera Cannot Take Picture in Main UI Thread");
        }
        if (camera.getParameters() != null && camera.getParameters().getFocusMode() != null) {
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera cam) {
                    takePictureInternal(cam, photoTakenCallback);
                }
            });
        } else {
            takePictureInternal(camera, photoTakenCallback);
        }

    }
    private void takePictureInternal(Camera camera, final PhotoTakenCallback photoTakenCallback) {
        if (!isPictureCaptureInProgress.getAndSet(true)) {
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    isPictureCaptureInProgress.set(false);
                    // send to the presenter maybe via the thread ?
                    photoTakenCallback.onPhotoTaken(data);
                }
            });
        }

    }

    private void stopAndRelease() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
}
