package xyz.pinaki.android.camera;

/**
 * Created by pinaki on 8/11/17.
 * Presenter in the MVP pattern
 */
interface CameraPresenter {
    void onCreate();
    void onDestroy();
    boolean start();
    void stop();
    boolean isCameraOpened();
    void setFacing(int facing);
    int getFacing();
    void takePicture();
}
