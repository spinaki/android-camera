package xyz.pinaki.androidcamera;

/**
 * Created by pinaki on 8/16/16.
 */

import android.content.Context;
import android.hardware.SensorManager;
import android.view.OrientationEventListener;

/**
 * Orientation listener to remember the device's orientation when the user presses
 * the shutter button.
 *
 * The orientation will be normalized to return it in steps of 90 degrees
 * (0, 90, 180, 270).
 */
/* package */ class CameraOrientationListener extends OrientationEventListener {
    /* package */ int currentNormalizedOrientation;
    /* package */ int rememberedNormalizedOrientation;
    private CameraFragment cameraFragment;

    /* package */ CameraOrientationListener(Context context) {
        super(context, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /* package */ void setCamera1Fragment (CameraFragment camera1Fragment) {
        this.cameraFragment = camera1Fragment;
    }
    @Override
    public void onOrientationChanged(int orientation) {
        if (orientation != ORIENTATION_UNKNOWN) {
            currentNormalizedOrientation = normalize(orientation);
        }
        if (this.cameraFragment != null ) {
            cameraFragment.rotateUI(currentNormalizedOrientation);
        }
    }

    private int normalize(int degrees) {
        if (degrees > 315 || degrees <= 45) {
            return 0;
        }

        if (degrees > 45 && degrees <= 135) {
            return 90;
        }

        if (degrees > 135 && degrees <= 225) {
            return 180;
        }

        if (degrees > 225 && degrees <= 315) {
            return 270;
        }

        throw new RuntimeException("The physics as we know them are no more. Watch out for anomalies.");
    }

    /* package */ void rememberOrientation() {
        rememberedNormalizedOrientation = currentNormalizedOrientation;
    }

    /* package */ int getRememberedOrientation() {
        return rememberedNormalizedOrientation;
    }
}
