package xyz.pinaki.android.camera;

import android.content.Context;
import android.view.ViewGroup;

/**
 * Created by pinaki on 8/11/17.
 * Presenter in the MVP pattern
 */
interface CameraPresenter {
    void onCreate(Context context, ViewGroup viewGroup);
    void onDestroy();
    boolean onResume();
    void onPause();
    boolean isCameraOpened();
    void setFacing(int facing);
    int getFacing();
    void takePicture();
}
