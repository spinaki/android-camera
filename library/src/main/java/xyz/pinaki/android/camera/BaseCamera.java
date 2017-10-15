package xyz.pinaki.android.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import xyz.pinaki.android.camera.dimension.AspectRatio;
import xyz.pinaki.android.camera.dimension.Size;
import xyz.pinaki.android.camera.orientation.DeviceOrientationListener;
import xyz.pinaki.android.camera.preview.ViewFinderPreview;

/**
 * Created by pinaki on 8/11/17.
 */

abstract class BaseCamera {
    private static String TAG = BaseCamera.class.getSimpleName();
    WeakReference<AppCompatActivity> activity;
    DeviceOrientationListener deviceOrientationListener;
    private int maxWidthSize = CameraAPI.DEFAULT_MAX_IMAGE_WIDTH;
    CameraStatusCallback cameraStatusCallback;
    BaseCamera(AppCompatActivity a) {
        activity = new WeakReference<>(a);
        deviceOrientationListener = new DeviceOrientationListener(a);
    }
    protected ViewFinderPreview viewFinderPreview;
    void setCameraStatusCallback(CameraStatusCallback c) {
        cameraStatusCallback = c;
    }
    public boolean start() {
        if (!isCameraPresent(activity.get())) {
            return false;
        }
        return true;
    }
    void setMaxWidthSize(int s) {
        maxWidthSize = s;
    }
    int getMaxWidthSize() {
        return maxWidthSize;
    }
    public abstract void stop();
    public abstract boolean isCameraOpened();
    public abstract void setFacing(CameraAPI.LensFacing lensFacing);
    public abstract int getFacing();
    public abstract void takePicture(PhotoTakenCallback p);
    protected AspectRatio aspectRatio = CameraAPI.DEFAULT_ASPECT_RATIO;
    protected int displayOrientation;
    // add any call backs

    interface PhotoTakenCallback {
        void onPhotoTaken(byte[] data);
    }
    AspectRatio getAspectRatio() {
        return aspectRatio;
    }
    void setDesiredAspectRatio(AspectRatio a) {
        aspectRatio = a;
    }
    void setOrientation(int orientation) {
        displayOrientation = orientation;
        // TODO: do you need to stop and restart camera1 after orientation is set ?
    }
    void setPreview(ViewFinderPreview v) {
        viewFinderPreview = v;
    }
    static boolean isCameraPresent(Context context) {
        // this device has a camera
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }
    @SuppressWarnings("SuspiciousNameCombination")
    protected Size chooseOptimalSize(List<Size> cameraSizes) {
        Map<AspectRatio, SortedSet<Size>> aspectRatioSortedSizesMap = new HashMap<>();
        // get supporting preview sizes
        for (Size csize : cameraSizes) {
            AspectRatio a = AspectRatio.of(csize.getWidth(), csize.getHeight());
            SortedSet<Size>sizes = aspectRatioSortedSizesMap.get(a);
            if (sizes == null) {
                sizes = new TreeSet<>();
                aspectRatioSortedSizesMap.put(a, sizes);
            }
            sizes.add(csize);
        }
        // aspect ratio should always be populated either with default or user input values.
        // find the sizes that have the aspect ratio as above

        // if sizes found: chooseOptimalSize to find the optimal size
        // using the surface width and height compensated by the orientation

        // if sizes not found: find the aspect ratio of the input sizes
        // choose the largest aspect ratio from the list.
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
        Log.i(TAG, "displayOrientation in : chooseOptimalSize " + displayOrientation);
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

}
