package xyz.pinaki.android.camera;

import android.graphics.Bitmap;

/**
 * Created by pinaki on 9/4/17.
 */

interface CameraStatusCallback {
    void onCameraOpen();
    void onImageCaptured(Bitmap bitmap);
}
