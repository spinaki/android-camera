package xyz.pinaki.android.camera;

import android.graphics.Bitmap;

import java.util.List;

import xyz.pinaki.android.camera.dimension.AspectRatio;
import xyz.pinaki.android.camera.dimension.Size;

/**
 * Created by pinaki on 9/4/17.
 * Internal Interfaces used by the library
 */
interface CameraStatusCallback {
    void onCameraOpen();
    void onPhotoTaken(byte[] data);
    void onBitmapProcessed(Bitmap bitmap);
    void onCameraClosed();
    void onAspectRatioAvailable(AspectRatio desired, AspectRatio chosen, List<Size> available);
}
