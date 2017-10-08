package xyz.pinaki.android.camera;

import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.lang.ref.WeakReference;

import xyz.pinaki.android.camera.dimension.AspectRatio;

/**
 * Created by pinaki on 8/11/17.
 * should contain the business logic like starting the
 * a. background thread & handler
 * b. apis for surface / texture which can be fed to the Camera1, CameraFragment -- or should this be in Camera1 /
 * Camera2
 * c. bitmap manipulation -- subsampling
 * d. orientation fix
 * e. How to combine the async from 2 different calls into one.
 * f. How to do video vs image
 */

class Camera1Presenter implements CameraPresenter {
    private BaseCamera camera1;
    private CameraAPI.LensFacing lensFacing = CameraAPI.LensFacing.BACK;
    // TODO: these should be populated by DI / Dependency Injection
    private WeakReference<AppCompatActivity> activity;
    private ViewFinderPreview viewFinderPreview;
    private CameraStatusCallback cameraStatusCallback;
    // TODO: should the threading be implemented by the BasePresenter or something else ?
    private CameraHandlerThread backgroundThread;
    Camera1Presenter(AppCompatActivity a) {
        activity = new WeakReference<>(a);
    }

    @Override
    public void setCameraStatusCallback(CameraStatusCallback c) {
        cameraStatusCallback = c;
    }

    @Override
    public void setDisplayOrientation(int orientation) {
        if (camera1 != null) {
            camera1.setOrientation(orientation);
        }
    }

    @Override
    public void onCreate() {
        Log.i("pinaki-Camera1Presenter", "start thread");
        initThread();
        backgroundThread.start();
        backgroundThread.prepareHandler();
    }

    @Override
    public void onDestroy() {
        Log.i("pinaki-Camera1Presenter", "quit thread");
        backgroundThread.quit();
        backgroundThread.interrupt();
        backgroundThread = null;
    }

    // primarily used to open camera
    @Override
    public boolean onStart() {
        Message m = Message.obtain();
        m.what = Camera1.CAMERA1_ACTION_OPEN;
        // TODO: do you need to pass a real Context object
        camera1 = new Camera1(activity.get());
        camera1.setFacing(lensFacing);
        camera1.setPreview(viewFinderPreview);
        m.obj = camera1;
        backgroundThread.queueMessage(m);
        return true;
    }

    private void initThread() {
        backgroundThread = new CameraHandlerThread("Camera1Handler", cameraStatusCallback);
    }

    // primarily used to stop camera
    @Override
    public void onStop() {
        if (camera1 != null) {
            camera1.stop();
            camera1 = null;
        }
    }

    @Override
    public void setPreview(ViewFinderPreview v) {
        viewFinderPreview = v;
    }

    @Override
    public boolean isCameraOpened() {
        return false;
    }

    @Override
    public void setFacing(CameraAPI.LensFacing l) {
        lensFacing = l;
    }

    @Override
    public int getFacing() {
        return 0;
    }

    @Override
    public void takePicture() {
        // send a new message to the handler to start capturing a photo.
        Message m = Message.obtain();
        m.what = Camera1.CAMERA1_ACTION_TAKE_PICTURE;
        m.obj = camera1;
        backgroundThread.queueMessage(m);
    }

    @Override
    public AspectRatio getAspectRatio() {
        return camera1 != null ? camera1.getAspectRatio() : null;
    }
}
