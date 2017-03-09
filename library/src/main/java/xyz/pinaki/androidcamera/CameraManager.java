package xyz.pinaki.androidcamera;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by pinaki on 3/6/17.
 */
// singleton manager class
public class CameraManager {
    private static class SingletonHolder {
        private static final CameraManager INSTANCE = new CameraManager();
    }
    private CameraManager() {
    }

    public static CameraManager getInstance() {
        return SingletonHolder.INSTANCE;
    }
    public void launch(AppCompatActivity activity, int containerID) {
        activity.getSupportFragmentManager().beginTransaction().replace(
                containerID, CameraFragment.newInstance(), "CameraFragment").commit();
    }
}
