package xyz.pinaki.android.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
    private ViewFinderPreview viewFinderPreview;
    WeakReference<AppCompatActivity> activity; // TODO: set this ?
    Camera1(AppCompatActivity a) {
        activity = new WeakReference<>(a);
    }

    @Override
    public boolean start() { // TODO: should this take the cameraID ?
        if (!isCameraPresent(activity.get())) {
            return false;
        }
        if (Looper.getMainLooper().isCurrentThread()) {
            throw new RuntimeException("Camera Cannot Start in Main UI Thread");
        }
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

    private int calcCameraRotation() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        Log.i(TAG, "cameraInfo.orientation: " + cameraInfo.orientation + ", displayOrientation: " + displayOrientation);
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return (360 - (cameraInfo.orientation + displayOrientation) % 360) % 360;
        } else {  // back-facing
            return (cameraInfo.orientation - displayOrientation + 360) % 360;
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
        Size s = chooseOptimalSize(parameters.getSupportedPreviewSizes());
        Log.i(TAG, "OptimalPreviewSize: " + s.getWidth() + ", " + s.getHeight() + ", AspectRatio: " +
                aspectRatio);
        parameters.setPreviewSize(s.getWidth(), s.getHeight());
        // TODO: picture and preview sizes might be different
        s = chooseOptimalSize(parameters.getSupportedPictureSizes());
        parameters.setPictureSize(s.getWidth(), s.getHeight());
        setAutoFocusInternal(parameters); // how to set focus at the correct point ?
        // setRotation is commented since this has not been implemented correctly in many device (e.g., Samsung
        // Galaxy Prime). We compensate for the rotation manually using getImageTransformMatrix
//        parameters.setRotation(calcCameraRotation());
        // TODO: set flash
//        setFlashInternal(mFlash);
        camera.setParameters(parameters);
    }

    private void setAutoFocusInternal(Camera.Parameters parameters) {
        final List<String> modes = parameters.getSupportedFocusModes();
        if (modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (modes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
        } else if (modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        } else {
            parameters.setFocusMode(modes.get(0));
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private Size chooseOptimalSize(List<Camera.Size> cameraSizes) {
        Map<AspectRatio, SortedSet<Size>> aspectRatioSortedSizesMap = new HashMap<>();
        // get supporting preview sizes
        for (Camera.Size csize : cameraSizes) {
            AspectRatio a = AspectRatio.of(csize.width, csize.height);
            SortedSet<Size>sizes = aspectRatioSortedSizesMap.get(a);
            if (sizes == null) {
                sizes = new TreeSet<>();
                aspectRatioSortedSizesMap.put(a, sizes);
            }
            sizes.add(new Size(csize.width, csize.height));
        }
        Log.i(TAG, "getSupportedPreviewSizes all: " + aspectRatioSortedSizesMap.values());
        // aspect ratio should always be populated either with default or user input values.
        // find the sizes that have the aspect ratio as above

        // if sizes found: chooseOptimalSize to find the optimal size
        // using the surface width and height compensated by the orientation

        // if sizes not found: find the aspect ratio of the input sizes
        // choose the largest aspect ratio from the list.
//        aspectRatio = AspectRatio.of(352, 288); // HACK
        SortedSet<Size> sizes = aspectRatioSortedSizesMap.get(aspectRatio);
        if (sizes == null) {
            aspectRatio = chooseAspectRatio(aspectRatioSortedSizesMap.keySet());
            Log.i(TAG, "choosing AR : " + aspectRatio);
            sizes = aspectRatioSortedSizesMap.get(aspectRatio);
        }
        final int surfaceWidth = viewFinderPreview.getWidth();
        final int surfaceHeight = viewFinderPreview.getHeight();
        int desiredWidth = surfaceWidth;
        int desiredHeight = surfaceHeight;
        if (displayOrientation == 90 || displayOrientation == 270) {
            desiredWidth = surfaceHeight;
            desiredHeight = surfaceWidth;
        }
        Size result = null;
        for (Size s: sizes) {
            if (desiredWidth <= s.getWidth() && desiredHeight <= s.getHeight()) {
                return s;
            }
            result = s;
        }
        return result;
    }

    private AspectRatio chooseAspectRatio(Set<AspectRatio> aspectRatioSet) {
        if (aspectRatioSet.contains(aspectRatio)) {
            return aspectRatio;
        }
        SortedSet<AspectRatio> aspectRatios = new TreeSet<>(aspectRatioSet);
        return aspectRatios.last();
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
        if (Looper.getMainLooper().isCurrentThread()) {
            throw new RuntimeException("Camera Cannot Take Picture in Main UI Thread");
        }
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                // send to the presenter maybe via the thread ?
                photoTakenCallback.onPhotoTaken(data);
            }
        });
    }

    private void stopAndRelease() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private static boolean isCameraPresent(Context context) {
        // this device has a camera
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }
}
