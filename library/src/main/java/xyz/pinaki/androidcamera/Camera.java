package xyz.pinaki.androidcamera;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by pinaki on 3/6/17.
 */

public class Camera {
    public void launch(AppCompatActivity activity, int containerID) {
        activity.getSupportFragmentManager().beginTransaction().replace(
                containerID, CameraFragment.newInstance(), "CameraFragment").commit();
    }
}
