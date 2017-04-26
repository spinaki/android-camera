# android-camera
Lightweight Library for integrating the Camera sensor on your Android apps. The library internally takes care of
invoking the correct API --  the deprecated `android.hardware.Camera` or the new `android.hardware.Camera2`. You get a
generated image in the bitmap format which you can save (as JPEGs etc) or render in your apps. The returned bitmap is
 guaranteed to have the correct orientation and resolution.
## Getting Started
Add the library dependency to your `build.gradle`.
~~~~
compile 'xyz.pinaki.android:camera:1.0.1'
~~~~
The main entry point to this library is the singleton `CameraController` class. You can use the `getInstance()`
method to get an instance object of the class. Start the camera using the `launch` method of this class which takes
three arguments:
```java
launch(AppCompatActivity activity, int containerID, Callback callback)
```
* As the first arguement, pass the `Activity` from which this is launched.
* The Camera is launched inside a `Fragment`. You will have to define a place in your layout file to launch the
`Fragment`. The ID of this node in the layout file is the second argument. For instance, in the layout file within the
demo app, we define a `FrameLayout` where the camera will be displayed.
```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
              android:layout_height="match_parent" android:layout_width="match_parent"
                android:orientation="vertical">
    <FrameLayout android:layout_width="match_parent"
                 android:layout_height="match_parent"
                 android:id="@+id/container">
    </FrameLayout>
</LinearLayout>
```
* The third argument is a callback object which notifies the caller about various lifecycle events of the `Camera`.
It is an instance of the interface CAmeraController.Callback and you can add your own code in the object about what
you wich to do when these lifecycle events are triggered.
```java
public interface Callback {
        void onCameraOpened();
        void onCameraClosed();
        void onPhotoTaken(byte[] data);
        void onBitmapProcessed(Bitmap bitmap);
    }
```

Checkout the example app built using this library `app/src/main/java/xyz/pinaki/androidcamera/example`

## Why This Library
There are multiple issues with the camera libraries on Android.
* Not all hardware supports Camera2.
Event if they are post Android 21, the cameras might not support the new `Camera2` API. Or even if one of the cameras
 support it -- the other might not. During my search, I was not able to find a lightweight library, which enables
 developer capture an image from either of the cameras and easily save it. Hence, I created this library.
* Orientation issues.
* Rear vs Front Camera.
* Handling the callbacks correctly in background thread.
* Correct Aspect Ratio for Camera and Preview.
## Functionality of this library
* Most [examples](https://developer.android.com/guide/topics/media/camera.html#custom-camera) for the android camera, used the main
 UI thread to open the camera. However, this is discouraged in the [docs](https://developer.android.com/training/camera/cameradirect.html#TaskOpenCamera). The idea is to use a background thread to invoke it -- since
  this operation might take a while and block the UI thread. However, the caveat is all camera callbacks (e.g., preview, focus, photo capture)
  happen on the [same thread where Camera.open was invoked](https://developer.android.com/reference/android/hardware/Camera.html).
  If the callback handling is not correctly implemented -- the android platform throws an exception that callbacks
  should be handled in the same thread as open. This library takes care of this using looper,message queue and handlers.
* The preview size and aspect ratio: The camera library does not support arbitrary aspect ratios and sizes. The goal
of this library to open up an API so that user can specify a size or an aspect ratio and the library can choose a
size that best matches the query ratio.
* Correct Orientation of the preview image: The orientation of the preview image (while recording) depends on
multiple factors : natural orientation of the device, how the camera sensor is oriented in relation to the device,
how the user holds while capturing a photo (portrait or landscape). Because of these factors, its easy to get the
orientation of the preview image wrong. See [here](https://www.captechconsulting.com/blogs/android-camera-orientation-made-simple) and [here](https://www.captechconsulting.com/blogs/android-camera-orientation-made-simple) .
This library takes care of such scenarios and generates the correct orientation of the preview image.
* Even if you fix the orientation of the preview image -- the orientation of the final image which you want to
display or save in the disk (JPEG etc) is still not fixed. The angle used in `Camera.setDisplayOrientation` can also
be used in `Camera.Parameters.setRotation`. However, according to the [Android docs](https://developer.android.com/reference/android/hardware/Camera.Parameters.html#setRotation(int))
there is no guarantee that the pixels returned from the library will be correctly oriented. It depends on the
hardware manufacturer on how they interpret this value. This library takes care of this too.
* Option to lock the orientation: In many camera libraries, if you change the orientation of the camera, the view is
destroyed and regenerated. This creates a bad user experience since on most devices, one can see a lag. This library
has an option to fix the orientation -- so that if orientation is changed -- the views are not regenerated.
* SurfaceView instead of TextureView. In many libraries, e.g., the one from [Google](https://github.com/googlesamples/android-Camera2Basic)
TextureView is used to render the live Camera preview. However, SurfaceView is much more efficient as mentioned
[in Android Docs](https://source.android.com/devices/graphics/arch-tv.html) and
[here](https://github.com/crosswalk-project/crosswalk-website/wiki/Android-SurfaceView-vs-TextureView) .
This library uses SurfaceView.
* Work In Progress: Tap to focus.
* Work In Progress: Pinch to Zoom.
