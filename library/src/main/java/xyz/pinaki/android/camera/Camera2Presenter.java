package xyz.pinaki.android.camera;

import xyz.pinaki.android.camera.dimension.AspectRatio;

/**
 * Created by pinaki on 8/11/17.
 */

class Camera2Presenter implements CameraPresenter {
    CameraView cameraView;
    Camera2Presenter(CameraView c) {
        cameraView = c;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public boolean onStart() {
        return false;
    }

    @Override
    public void onStop() {

    }

    @Override
    public void setPreview(ViewFinderPreview v) {

    }

    @Override
    public boolean isCameraOpened() {
        return false;
    }

    @Override
    public void setFacing(CameraAPI.LensFacing l) {

    }

    @Override
    public int getFacing() {
        return 0;
    }

    @Override
    public void takePicture() {

    }

    @Override
    public AspectRatio getAspectRatio() {
        return null;
    }
}
