package xyz.pinaki.android.camera;

/**
 * Created by pinaki on 8/11/17.
 */

public class CameraAPI {
    public static final class LensFacing {
        private final String name;
        private LensFacing(String s) {
            name = s;
        }
        public static final LensFacing BACK = new LensFacing("back");
        public static final LensFacing FRONT = new LensFacing("front");
    }

    public static final class FlashStatus {
        public static final int OFF = 0;
        public static final int ON = 0;
    }

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
        baseCamera.takePicture(null);
    }
//    abstract void setLivePreview();
//    abstract void setCapturePreview();
}
