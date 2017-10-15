package xyz.pinaki.android.camera;

import android.graphics.Bitmap;
import android.hardware.Camera;

/* package */ interface InternalCallback {
    void onPictureTaken(final Bitmap bitmap, final byte[] bytes);
    void onCameraOpen(Camera camera);
    void onBitmapProcessed(Bitmap bitmap);
}
