package xyz.pinaki.android.camera;

import android.support.annotation.NonNull;

/**
 * Created by pinaki on 8/11/17.
 */

class Camera2 extends BaseCamera {
    Camera2(@NonNull CameraView c) {
        super(c);
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
