package xyz.pinaki.androidcamera;

import android.graphics.Bitmap;

/* package */ interface CameraCallback {
    void onPictureTaken(final Bitmap bitmap);
}
