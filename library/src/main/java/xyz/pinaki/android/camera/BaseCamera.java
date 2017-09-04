package xyz.pinaki.android.camera;

import xyz.pinaki.android.camera.dimension.AspectRatio;

/**
 * Created by pinaki on 8/11/17.
 */

abstract class BaseCamera {
    static final AspectRatio DEFAULT_ASPECT_RATIO = AspectRatio.of(16, 9);
    public abstract boolean start();
    public abstract void stop();
    public abstract boolean isCameraOpened();
    public abstract void setFacing(CameraAPI.LensFacing lensFacing);
    public abstract int getFacing();
    public abstract void takePicture(PhotoTakenCallback p);
    protected AspectRatio aspectRatio = DEFAULT_ASPECT_RATIO;
    // add any call backs

    interface PhotoTakenCallback {
        void onPhotoTaken(byte[] data);
    }
    public AspectRatio getAspectRatio() {
        return aspectRatio;
    }
}
