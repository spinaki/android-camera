package xyz.pinaki.android.camera;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Message;
import android.util.Log;

/**
 * Created by pinaki on 8/11/17.
 * should contain the business logic like starting the
 * a. background thread & handler
 * b. apis for surface / texture which can be fed to the Camera1, Camera1Fragment -- or should this be in Camera1 /
 * Camera2
 * c. bitmap manipulation -- subsampling
 * d. orientation fix
 * e. How to combine the async from 2 different calls into one.
 * f. How to do video vs image
 */

class Camera1Presenter implements CameraPresenter {
    private CameraView cameraView;
    // TODO: should this be implemented by the Presenter or something else ?
    private CameraHandlerThread backgroundThread;
//    Handler backgroundHandler;
    Camera1Presenter(CameraView c) {
        cameraView = c;
    }
    @Override
    public boolean start() {
        Log.i("pinaki-Camera1Preseter", "start thread");
        initThread();
        backgroundThread.start();
//        backgroundHandler = new Handler(backgroundThread.getLooper());
        Message m = Message.obtain();
        m.what = Camera1.CAMERA1_ACTION_OPEN;
        // TODO: do you need to pass a real Context object
        m.obj = new Camera1(null);
        backgroundThread.queueMessage(m);
        return true;
    }
    private void initThread() {
        backgroundThread = new CameraHandlerThread("Camera1Handler", new InternalCallback(){
            @Override
            public void onPictureTaken(Bitmap bitmap, byte[] bytes) {

            }
            @Override
            public void onCameraOpen(Camera camera) {
                // TODO should camera be used ?
                // add the surface / textureview
                // listen on the callback for the surfaceview / textureview creation
            }

            @Override
            public void onBitmapProcessed(Bitmap bitmap) {
                // process the bitmap
            }
        });
    }

    @Override
    public void stop() {
        Log.i("pinaki-Camera1Presenter", "stop thread");
        backgroundThread.quit();
        backgroundThread.interrupt();
        backgroundThread = null;
    }

    @Override
    public boolean isCameraOpened() {
        return false;
    }

    @Override
    public void setFacing(int facing) {

    }

    @Override
    public int getFacing() {
        return 0;
    }

    @Override
    public void takePicture() {

    }
}
