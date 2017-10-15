package xyz.pinaki.android.camera;

/**
 * Created by pinaki on 8/11/17.
 * View interface in the MVP Model
 */

interface CameraView {
    // the fragments for camera1 and 2 should implement this.
    void setPresenter(CameraPresenter c);
    void setPreviewType(CameraAPI.PreviewType v);
    void shutterClicked();
    void switchCameraClicked();
    void switchFlashClicked();
    void focus();
}
