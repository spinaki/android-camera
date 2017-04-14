**BufferQueue**

BufferQueue connects something that generates buffers of graphical data (the producer) to something that accepts the data for display or further processing (the consumer).

**Surface**

The Surface represents the producer side of a buffer queue that is often (but not always!) consumed by SurfaceFlinger
. When you render onto a Surface, the result ends up in a buffer that gets shipped to the consumer. A Surface is not simply a raw chunk of memory you can scribble on. A Surface produces a buffer queue that is often consumed by SurfaceFlinger. When rendering onto a Surface, the result ends up in a buffer that gets shipped to the consumer. Canvas APIs provide a software implementation (with hardware-acceleration support) for drawing directly on a Surface (low-level alternative to OpenGL ES).

**SurfaceHolder**
Anything having to do with a View involves a SurfaceHolder, whose APIs enable getting and setting Surface parameters such as size and format.
Used in `camera.setPreviewDisplay()`.

**SurfaceView**

SurfaceView combines a Surface and a View. SurfaceView's View components are composited by SurfaceFlinger (and not
the app), enabling rendering from a separate thread/process and isolation from app UI rendering.
Used in camera: `surfaceHolder = surfaceView.getHolder()`.

_How SurfaceView works_:
All UI elements go through a complicated measurement and layout process that fits them into a rectangular area, and all visible View objects are rendered to a SurfaceFlinger-created Surface that was set up by the WindowManager when the app was brought to the foreground. The app's UI thread performs layout and rendering to a single buffer.

A SurfaceView takes the same parameters as other views, so you can give it a position and size, and fit other elements around it. When it comes time to render, however, the contents are completely transparent; The View part of a SurfaceView is just a see-through placeholder.

When the SurfaceView's View component is about to become visible, the framework asks the WindowManager to ask SurfaceFlinger to create a new Surface. (This doesn't happen synchronously, which is why you should provide a callback that notifies you when the Surface creation finishes.) By default, the new Surface is placed behind the app UI Surface, but the default Z-ordering can be overridden to put the Surface on top.

Whatever you render onto this Surface will be composited by SurfaceFlinger, not by the app. This is the real power of SurfaceView: The Surface you get can be rendered by a separate thread or a separate process, isolated from any rendering performed by the app UI, and the buffers go directly to SurfaceFlinger. The new Surface is the producer side of a BufferQueue, whose consumer is a SurfaceFlinger layer. You can update the Surface with any mechanism that can feed a BufferQueue.

**SurfaceTexture**
SurfaceTexture combines a Surface and GLES texture to create a BufferQueue for which your app is the consumer. When a producer queues a new buffer, it notifies your app, which in turn releases the previously-held buffer, acquires the new buffer from the queue, and makes EGL calls to make the buffer available to GLES as an external texture.

When you create a SurfaceTexture, you are creating a BufferQueue for which your app is the consumer. When a new
buffer is queued by the producer, your app is notified via callback (onFrameAvailable()). Your app calls updateTexImage(), which releases the previously-held buffer, acquires the new buffer from the queue.
Used in camera `camera.setPreviewTexture`

**TextureView**
TextureView combines a View with a SurfaceTexture. TextureView wraps a SurfaceTexture and takes responsibility for responding to callbacks and acquiring new buffers. When drawing, TextureView uses the contents of the most recently received buffer as its data source, rendering wherever and however the View state indicates it should.

A TextureView can be used to display a content stream. Such a content stream can for instance be a video or an OpenGL scene. The content stream can come from the application's process as well as a remote process.

TextureView can only be used in a hardware accelerated window. When rendered in software, TextureView will draw nothing.

Unlike SurfaceView, TextureView does not create a separate window but behaves as a regular View. This key difference allows a TextureView to be moved, transformed, animated, etc. For instance, you can make a TextureView semi-translucent by calling myView.setAlpha(0.5f).

Using a TextureView is simple: all you need to do is get its SurfaceTexture. The SurfaceTexture can then be used to render content. The following example demonstrates how to render the camera preview into a TextureView:
**TextureView vs SurfaceView**

SurfaceView and TextureView fill similar roles, but have very different implementations. To decide which is best requires an understanding of the trade-offs.
Because TextureView is a proper citizen of the View hierarchy, it behaves like any other View, and can overlap or be overlapped by other elements. You can perform arbitrary transformations and retrieve the contents as a bitmap with simple API calls.

The main strike against TextureView is the performance of the composition step. With SurfaceView, the content is written to a separate layer that SurfaceFlinger composites, ideally with an overlay. With TextureView, the View composition is always performed with GLES, and updates to its contents may cause other View elements to redraw as well (e.g. if they're positioned on top of the TextureView). After the View rendering completes, the app UI layer must then be composited with other layers by SurfaceFlinger, so you're effectively compositing every visible pixel twice. For a full-screen video player, or any other application that is effectively just UI elements layered on top of video, SurfaceView offers much better performance.

As noted earlier, DRM-protected video can be presented only on an overlay plane. Video players that support protected content must be implemented with SurfaceView.

**Detailed Comparison**

Both SurfaceView and TextureView are inherited from android.view.View class. They can be drawn and rendered from a separate thread, which is the major difference from other views. The feature of drawing separately is employed by Crosswalk to improve rendering performance greatly by a dedicated GPU thread.

SurfaceView provides a dedicated drawing surface embedded inside of a view hierarchy. You can control the format of this surface and, if you like, its size; the SurfaceView takes care of placing the surface at the correct location on the screen. Its behavior is more or less similar as an onscreen Window on a traditional desktop system, for example, XWindow on X11 system which can be frameless and embedded inside another XWindow.
The followings are two limitations of SurfaceView:

* You can not be animated, transformed and scaled;
* You can not overlay two SurfaceView.

TextureView looks like a general View. You can animate, transform and scale it, just like a TextView.
TextureView can only be used in a hardware accelerated window. However, TextureView will consume much more memory than SurfaceView, and also may have a 1~3 frame latency.

**Further Reading**
* Details on Texture and Surface [Android Docs](https://source.android.com/devices/graphics/arch-tv.html)
* TextureView Demo with old camera API [here](https://github.com/dalinaum/TextureViewDemo/blob/master/src/kr/gdg/android/textureview/CameraActivity.java)
* Great [comparison](https://github.com/crosswalk-project/crosswalk-website/wiki/Android-SurfaceView-vs-TextureView)
* Grafika Test App and [Code](https://github.com/google/grafika)
* Google blog intro-ing [TextureView](https://android-developers.googleblog.com/2011/11/android-40-graphics-and-animations.html)