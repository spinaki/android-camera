package xyz.pinaki.android.camera.orientation;

import android.app.Activity;
import android.hardware.Camera;
import android.view.Surface;

/**
 * Created by pinaki on 3/28/17.
 */

// why is this necessary ?
// The screen can be rotated in four different angles (0°, 90°, 180°, 270°). In addition to that the camera can also
// be built into the device in four different angles by the manufacturer. Finally the camera can be on the front or on
// the back of the device. We will need to get all these angles and tell the camera object the display orientation so
// that the preview will be drawn on the surface using the right rotation.
@SuppressWarnings("deprecation")
/* package */ class RotationEventListener {
    // rotation of the screen from its "natural" orientation at capture time
    private int deviceDisplayRotation = 0;
    // how the camera sensor is rotated compared to the natural orientation of the device
    // https://www.captechconsulting.com/blogs/android-camera-orientation-made-simple
    // will be useful for showing preview in the correct orientation
    private int cameraDisplayRotation = 0;

    /* package */ void onRotationChanged(Activity activity, Camera.CameraInfo cameraInfo) {
        this.deviceDisplayRotation = deviceDisplayRotationInDegrees(activity);
        this.cameraDisplayRotation = cameraDisplayRotation(cameraInfo, this.deviceDisplayRotation);
    }

    /* package */ int getDeviceDisplayRotation() {
        return deviceDisplayRotation;
    }

    /* package */ int getCameraDisplayRotation() {
        return cameraDisplayRotation;
    }

// https://developer.android.com/reference/android/hardware/Camera.CameraInfo.html#orientation
// cameraInfo.orientation: The orientation of the camera image. The value is the angle that the camera image needs to
// be rotated clockwise so it shows correctly on the display in its natural orientation. It should be 0, 90, 180, or
// 270.
// For example, suppose a device has a naturally tall screen. The back-facing camera sensor is mounted in landscape.
// You are looking at the screen. If the top side of the camera sensor is aligned with the right edge of the screen in
// natural orientation, the value should be 90. If the top side of a front-facing camera sensor is aligned with the
// right of the screen, the value should be 270.

    /**
     * this method computes the orientation that may be used in Camera.setDisplayOrientation so that the preview
     * shows up correctly
     * https://developer.android.com/reference/android/hardware/Camera.html#setDisplayOrientation(int)
     * More reading: http://www.androidzeitgeist.com/2012/10/displaying-camera-preview-instant.html
     * https://www.captechconsulting.com/blogs/android-camera-orientation-made-simple
     * https://plus.google.com/+AndroidDevelopers/posts/jXNFNKWxsc3
     * http://stackoverflow.com/questions/9055460/is-androids-camerainfo-orientation-correctly-documented-incorrectly-implemente
     * @param cameraInfo
     * @param deviceDisplayRotationDegrees
     * @return
     */
    private static int cameraDisplayRotation(Camera.CameraInfo cameraInfo, int deviceDisplayRotationDegrees) {
        int cameraDisplayRotation = 0;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            cameraDisplayRotation = (cameraInfo.orientation + deviceDisplayRotationDegrees) % 360;
            cameraDisplayRotation = (360 - cameraDisplayRotation) % 360;
        } else {
            cameraDisplayRotation = (cameraInfo.orientation - deviceDisplayRotationDegrees + 360) % 360;
        }
        return cameraDisplayRotation;
    }

    // changes the display rotation from enum values to raw degrees.
    private static int deviceDisplayRotationInDegrees(Activity activity) {
        // Records the rotation of the screen from its "natural" orientation at capture time. For example,
// if a device has a naturally tall screen, and the user has turned it on its side to go into a landscape orientation,
// the value returned here may be either Surface.ROTATION_90 or Surface.ROTATION_270 depending on the direction it was
// turned. The angle is the rotation of the drawn graphics on the screen, which is the opposite direction of the
// physical rotation of the device. For example, if the device is rotated 90 degrees counter-clockwise, to compensate
// rendering will be rotated by 90 degrees clockwise and thus the returned value here will be Surface.ROTATION_90.
        // https://developer.android.com/reference/android/view/Display.html#getRotation()
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees  = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;

            case Surface.ROTATION_90:
                degrees = 90;
                break;

            case Surface.ROTATION_180:
                degrees = 180;
                break;

            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        return degrees;
    }
}
