package xyz.pinaki.androidcamera;

import android.graphics.Bitmap;

public interface CameraCallback {
    void onPictureTaken(final Bitmap bitmap);
}
