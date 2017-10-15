* DeviceOrientationListener uses the accelerometer info and normalizes
them to get the rotation.
* Important stuff on camera orientation are in this [blog](https://www.captechconsulting.com/blogs/android-camera-orientation-made-simple)
and in the [Google+ post](https://plus.google.com/+AndroidDevelopers/posts/jXNFNKWxsc3)
* Since we are using `Activity.setRequestedOrientation` to fix orientation to portrait,
the `Display.getRotation` does not change -- it is fixed to 0 no matter how you rotate the device.
Hence `DisplayOrientationDetector` does not work -- since it gets the rotation from `Display`
* The angles reported by accelerometer in `OrientationEventListener` is complement of `Display.getRotation`.
 Consider the following scenarios.
 * `OrientationEventListener` changes angles when you rotate the device clockwise.
 So if the left side is on top, the orientation angle is 90. However, the display rotation is
 complement (270) -- since it measures the rotation of the drawn graphics
 on the screen which is in opposite direction of the physical rotation of the device.
 As the `Display.getRotation` ref says [here](https://developer.android.com/reference/android/view/Display.html#getRotation())
 ````
For example, if a device has a naturally tall screen, and the user has
turned it on its side to go into a landscape orientation, the value returned
here may be either Surface.ROTATION_90 or Surface.ROTATION_270 depending on
the direction it was turned. The angle is the rotation of the drawn
graphics on the screen, which is the opposite direction of the physical
rotation of the device. For example, if the device is rotated 90 degrees
counter-clockwise, to compensate rendering will be rotated by 90 degrees
clockwise and thus the returned value here will be Surface.ROTATION_90.
 ````

 * Hence if we are using the normalized raw angles from Accelerometer -- then complement them.
 * For Camera1 -- `setRotation` does not work in certain cases like `MotoG`.
 Possibly some manufacturers rotate the image based on setRotation and you wont have to rotate it anymore ? Check this hypothesis in motog.
 * For Selfie Camera / Front Facing camera you will have to handle special case. Understand why.
 * As per the google post -- the preview feed handles mirroring of image in the selfie cam.
 However, for the JPEG data -- you have to manually rotate it.
* https://developer.android.com/reference/android/hardware/Camera.html#setDisplayOrientation(int)
