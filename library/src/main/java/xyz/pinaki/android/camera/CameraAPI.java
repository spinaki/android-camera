package xyz.pinaki.android.camera;

/**
 * Created by pinaki on 8/11/17.
 */

class CameraAPI {
    private final BaseCamera baseCamera;

    CameraAPI(BaseCamera b) {
        baseCamera = b;
    }
    protected void start() {
        baseCamera.start();
    }
    protected void stop() {
        baseCamera.stop();
    }
    protected void capture() {
        baseCamera.takePicture();
    }
//    abstract void setLivePreview();
//    abstract void setCapturePreview();
}
