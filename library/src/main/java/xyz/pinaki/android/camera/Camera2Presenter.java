package xyz.pinaki.android.camera;

/**
 * Created by pinaki on 8/11/17.
 */

class Camera2Presenter implements CameraPresenter {
    CameraView cameraView;
    Camera2Presenter(CameraView c) {
        cameraView = c;
    }

    @Override
    public boolean start() {
        return false;
    }

    @Override
    public void stop() {

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
    public void takePicture() {

    }
}
