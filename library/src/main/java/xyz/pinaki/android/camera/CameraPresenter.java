package xyz.pinaki.android.camera;

import xyz.pinaki.android.camera.dimension.AspectRatio;
import xyz.pinaki.android.camera.preview.ViewFinderPreview;

/**
 * Created by pinaki on 8/11/17.
 * Presenter in the MVP pattern
 */
interface CameraPresenter {
    void onCreate();
    void onDestroy();
    boolean onStart();
    void onStop();
    void setPreview(ViewFinderPreview v);
    void setMaxWidthSizePixels(int s);
    void setDesiredAspectRatio(AspectRatio a);
    boolean isCameraOpened();
    void setFacing(CameraAPI.LensFacing l);
    int getFacing();
    void takePicture();
    AspectRatio getAspectRatio();
    void setCameraStatusCallback(CameraStatusCallback c);
    void setDisplayOrientation(int o);
}
