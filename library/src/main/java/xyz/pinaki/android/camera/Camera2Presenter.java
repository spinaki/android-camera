package xyz.pinaki.android.camera;

import android.support.v7.app.AppCompatActivity;

import java.lang.ref.WeakReference;

import xyz.pinaki.android.camera.dimension.AspectRatio;
import xyz.pinaki.android.camera.preview.ViewFinderPreview;

/**
 * Created by pinaki on 8/11/17.
 */

class Camera2Presenter implements CameraPresenter {
    private BaseCamera camera2;
    private int maxWidthSize = CameraAPI.DEFAULT_MAX_IMAGE_WIDTH;
    private CameraAPI.LensFacing lensFacing = CameraAPI.LensFacing.BACK;
    private CameraStatusCallback cameraStatusCallback;
    private WeakReference<AppCompatActivity> activity;
    private ViewFinderPreview viewFinderPreview;
    private AspectRatio desiredAspectRatio;
    private CameraHandlerThread backgroundThread;

    @Override
    public void setCameraStatusCallback(CameraStatusCallback c) {
        cameraStatusCallback = c;
    }

    @Override
    public void setDisplayOrientation(int orientation) {
        if (camera2 != null) {
            camera2.setOrientation(orientation);
        }
    }
    @Override
    public void setDesiredAspectRatio(AspectRatio a) {
        desiredAspectRatio = a;
    }
    Camera2Presenter(AppCompatActivity a) {
        activity = new WeakReference<AppCompatActivity>(a);
    }


    @Override
    public void onCreate() {
        backgroundThread = new CameraHandlerThread("Camera1Handler");
        backgroundThread.start();
        backgroundThread.prepareHandler();
    }

    @Override
    public void onDestroy() {
        backgroundThread.quit();
        backgroundThread.interrupt();
        backgroundThread = null;
    }

    @Override
    public boolean onStart() {
        camera2 = new Camera2(activity.get(), backgroundThread);
        camera2.setPreview(viewFinderPreview);
        camera2.setFacing(lensFacing);
        camera2.setMaxWidthSize(maxWidthSize);
        camera2.setCameraStatusCallback(cameraStatusCallback);
        camera2.setDesiredAspectRatio(desiredAspectRatio);
        camera2.start();
        return true;
    }

    @Override
    public void onStop() {
        if (camera2 != null) {
            camera2.stop();
            camera2 = null;
        }
    }

    @Override
    public void setPreview(ViewFinderPreview v) {
        viewFinderPreview = v;
    }

    @Override
    public void setMaxWidthSizePixels(int s) {
        maxWidthSize = s;
    }

    @Override
    public boolean isCameraOpened() {
        return false;
    }

    @Override
    public void setFacing(CameraAPI.LensFacing l) {
        lensFacing = l;
    }

    @Override
    public int getFacing() {
        return 0;
    }

    @Override
    public void takePicture() {
        camera2.takePicture(new BaseCamera.PhotoTakenCallback() {
            @Override
            public void onPhotoTaken(byte[] data) {

            }
        });
    }

    @Override
    public AspectRatio getAspectRatio() {
        return camera2 != null ? camera2.getAspectRatio() : null;
    }
}
