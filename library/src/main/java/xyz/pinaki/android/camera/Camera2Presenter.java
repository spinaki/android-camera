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
    public void onCreate() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public boolean onResume() {
        return false;
    }

    @Override
    public void onPause() {

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
