package xyz.pinaki.android.camera.orientation;

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
// why is this necessary ?
// https://developer.android.com/reference/android/hardware/Camera.Parameters.html#setRotation(int)
// this class is used to make sure the final bitmap created by the camera is in the correct orientation.
// as mentioned in the above link -- the Camera.Parameters.rotation is not reliable.
// Camera.Parameters.setRotation Sets the clockwise rotation angle in degrees relative to the orientation of the camera.
// This affects the pictures returned from JPEG Camera.PictureCallback. The camera driver may set orientation in the
// EXIF header without rotating the picture. Or the driver may rotate the picture and the EXIF thumbnail. If the Jpeg
// picture is rotated, the orientation in the EXIF header will be missing or 1 (row #0 is top and column #0 is left side).
// If applications want to rotate the picture to match the orientation of what users see,
// apps should use OrientationEventListener and Camera.CameraInfo.
//http://stackoverflow.com/questions/15808719/controlling-the-camera-to-take-pictures-in-portrait-doesnt-rotate-the-final-ima
public class DeviceOrientationListener extends OrientationEventListener {
    private int currentNormalizedOrientation = 0;
    private int rememberedNormalizedOrientation = 0;

    public DeviceOrientationListener(Context context) {
        super(context, SensorManager.SENSOR_DELAY_NORMAL);
    }

// Called when the orientation of the device has changed. orientation parameter is in degrees, ranging from 0 to 359.
// orientation is 0 degrees when the device is oriented in its natural position, 90 degrees when its left side is at the
// top, 180 degrees when it is upside down, and 270 degrees when its right side is to the top. ORIENTATION_UNKNOWN is
// returned when the device is close to flat and the orientation cannot be determined.
    @Override
    public void onOrientationChanged(int orientation) {
        if (orientation != ORIENTATION_UNKNOWN) {
            currentNormalizedOrientation = normalize(orientation);
            rememberedNormalizedOrientation = currentNormalizedOrientation;
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

    public void rememberOrientation() {
        rememberedNormalizedOrientation = currentNormalizedOrientation;
    }

    public int getRememberedOrientation() {
        return rememberedNormalizedOrientation;
    }
}
