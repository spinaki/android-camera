package xyz.pinaki.android.camera;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by pinaki on 3/6/17.
 */
// singleton manager class
// TODO: Add the runtime permissions here or in BaseCamera ?
public class CameraController {
    private static String TAG = CameraController.class.getSimpleName();
    private final boolean shouldFixOrientation = true;
    private Camera2FragmentOld camera2FragmentOld;
    private CameraFragmentOld cameraFragmentOld;
    private static class SingletonHolder {
        private static final CameraController INSTANCE = new CameraController();
    }
    private CameraController() {
    }

    public static CameraController getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public interface Callback {
        void onCameraOpened();
        void onCameraClosed();
        void onPhotoTaken(byte[] data);
        void onBitmapProcessed(Bitmap bitmap);
    }

    public void launch(AppCompatActivity activity, int containerID, Callback callback) {
        if (shouldFixOrientation) {
            int orientation = activity.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            else {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
        Camera1Fragment cameraView = new Camera1Fragment();
//        CameraPresenter presenter = new Camera1Presenter(activity);
        CameraPresenter presenter = new Camera2Presenter(activity);
        cameraView.setPresenter(presenter);
        activity.getSupportFragmentManager().beginTransaction().replace(
                containerID, cameraView, "Camera1FragmentNew").commit();

//        presenter.start();

//        if (isCamera2Supported(activity) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Log.i(TAG, "Camera2 Supported");
//            camera2FragmentOld = Camera2FragmentOld.newInstance();
//            camera2FragmentOld.setCallback(callback);
//            activity.getSupportFragmentManager().beginTransaction().replace(
//                    containerID, camera2FragmentOld, "Camera2FragmentOld").commit();
//        } else {
//            Log.i(TAG, "Camera2 NOT Supported");
//            cameraFragmentOld = CameraFragmentOld.newInstance();
//            cameraFragmentOld.setCallback(callback);
//            activity.getSupportFragmentManager().beginTransaction().replace(
//                    containerID, cameraFragmentOld, "Camera1Fragment").commit();
//        }
    }

    public void stop() {
        if (camera2FragmentOld != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            camera2FragmentOld.setCallback(null);
        }
        if (cameraFragmentOld != null) {
            cameraFragmentOld.setCallback(null);
        }
    }


    private static boolean isCamera2Supported(Context context) {
        if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ) {
            return false;
        }
        CameraManager cameraManager = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (cameraManager.getCameraIdList().length == 0) {
                return false;
            }
            for (String cameraIdStr : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraIdStr);
                Integer hardwareSupport = cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                if ( hardwareSupport == null ) {
                    return false;
                }
                if (hardwareSupport == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY ) {
                    return false;
                }
                if (hardwareSupport == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED) {
                    return false;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        return true;
    }
}
