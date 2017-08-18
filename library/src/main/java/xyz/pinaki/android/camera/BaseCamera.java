package xyz.pinaki.android.camera;

/**
 * Created by pinaki on 8/11/17.
 */

abstract class BaseCamera {
    public abstract boolean start();
    public abstract void stop();
    public abstract boolean isCameraOpened();
    public abstract void setFacing(int facing);
    public abstract int getFacing();
    public abstract void takePicture(PhotoTakenCallback p);
    // add any call backs

    interface PhotoTakenCallback {
        void onPhotoTaken(byte[] data);
    }
}
