package xyz.pinaki.android.camera;

import android.graphics.Bitmap;

/* package */ interface CameraCallback {
    void onPictureTaken(final Bitmap bitmap);
}
