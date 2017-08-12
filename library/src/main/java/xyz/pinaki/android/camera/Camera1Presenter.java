package xyz.pinaki.android.camera;

import android.os.HandlerThread;

/**
 * Created by pinaki on 8/11/17.
 */

class Camera1Presenter implements CameraPresenter {
    CameraView cameraView;
    HandlerThread backgroundThread = new HandlerThread("background");
    Camera1Presenter(CameraView c) {
        cameraView = c;
    }
    @Override
    public boolean start() {
        backgroundThread.start();
        return false;
    }

    @Override
    public void stop() {
        backgroundThread.quit();
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
