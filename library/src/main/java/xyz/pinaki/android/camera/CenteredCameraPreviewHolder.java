package xyz.pinaki.android.camera;

/**
 * Created by pinaki on 9/14/16.
 */

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple wrapper around a Camera and a SurfaceView that renders a centered preview of the Camera
 * to the surface. We need to center the SurfaceView because not all devices have cameras that
 * support preview sizes at the same aspect ratio as the device's display.
 */
@SuppressWarnings("deprecation")
/* package */ class CenteredCameraPreviewHolder extends ViewGroup implements SurfaceHolder.Callback {
    private static final String TAG = CenteredCameraPreviewHolder.class.getSimpleName();
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    Size previewSize;
    Size pictureSize;
    android.util.Size optimalSize;
    Camera camera;
    CameraDevice cameraDevice; // camera2
    Activity activity;
    int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    RotationEventListener rotationEventListener;
    DeviceOrientationListener orientationListener;
    private boolean isCamera2 = false;
    CameraCaptureSession mCaptureSession;
    Handler cameraHandler;
    private ImageReader mImageReader;
    InternalCallback internalCallback;
    CameraController.Callback callback;
    // This flag is required to handle the case when the capture icon is tapped twice simultaneously.
    // Without the flag capture will be invoke again before the previous onPictureTaken call completed.
    // resulting in "RuntimeException takePicture failed" in android.hardware.Camera.takePicture(Camera.java:1436)
    // http://stackoverflow.com/questions/21723557/java-lang-runtimeexception-takepicture-failed
    private boolean safeToTakePicture = false;
    public CenteredCameraPreviewHolder(Context context) {
        super(context);
    }

    /* package */ CenteredCameraPreviewHolder(Activity activity, RotationEventListener rListener) {
        super(activity);
        this.activity = activity;
        this.rotationEventListener = rListener;
    }

    /* package */ CenteredCameraPreviewHolder(Activity activity, RotationEventListener rListener,
                                              DeviceOrientationListener dOrientationListener, boolean isCamera2,
                                              Handler backgroundHandler, InternalCallback internalCallback,
                                              CameraController.Callback callback) {
        this(activity, rListener);
        this.isCamera2 = isCamera2;
        cameraHandler = backgroundHandler;
        orientationListener = dOrientationListener;
        this.internalCallback = internalCallback;
        this.callback = callback;
    }

    /* package */ void addSurfaceView() {
        Log.i(TAG, "addSurfaceView");
        if (surfaceView != null) {
            removeView(surfaceView);
            surfaceView = null;
        }
        surfaceView = new SurfaceView(activity);
        addView(surfaceView);
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /* package */ void unsetCamera() {
        camera = null;
        cameraId = 0;
    }

    /* package */ void setCamera(Camera camera, int cameraId) {
        this.camera = camera;
        this.cameraId = cameraId;
    }

    /* package */ void setCamera(CameraDevice camera) {
        this.cameraDevice = camera;
    }

    SurfaceView getSurfaceView() {
        return surfaceView;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        Log.i(TAG, "onMeasure, width:" + width + ", height:" + height);
        if (camera != null) {
            try {
                Camera.Parameters parameters = camera.getParameters();
                List<Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
                List<Size> supportedPictureSize = parameters.getSupportedPictureSizes();
                previewSize = getOptimalPreviewSize(supportedPreviewSizes, width, height);
                pictureSize = getOptimalPreviewSize(supportedPictureSize, width, height);
                Log.i(TAG, "getOptimalPreviewSize: width:" + previewSize.width + ", height:" + previewSize.height);
                Log.i(TAG, "getOptimalPictureSize: width:" + pictureSize.width + ", height:" + pictureSize.height);
            } catch (RuntimeException exception) {
                Log.i(TAG, "RuntimeException caused by getParameters in onMeasure", exception);
            }
        } else if (cameraDevice != null && isCamera2) {
            CameraManager cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
            try {
                CameraCharacteristics cameraCharacteristics =
                        cameraManager.getCameraCharacteristics(cameraDevice.getId());
                StreamConfigurationMap info = cameraCharacteristics
                        .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                android.util.Size[] sizes = info.getOutputSizes(SurfaceHolder.class);
                Point displaySize = new Point();
                android.util.Size largest = Collections.max(
                        Arrays.asList(info.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea());
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;
                Log.i(TAG, "onMeasure, DisplaySize.width:" + displaySize.x + ", DisplaySize.height:" + displaySize.y);

                if (isPortrait()) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }
                optimalSize = chooseOptimalSize(sizes,
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth, maxPreviewHeight, largest);
                Log.i(TAG, "onMeasure, cameraDevice  width:" + optimalSize.getWidth() + ", height:" + optimalSize.getHeight());

                if (isPortrait()) {
                    setMeasuredDimension(optimalSize.getHeight(), optimalSize.getWidth());
                } else {
                    setMeasuredDimension(optimalSize.getWidth(), optimalSize.getHeight());
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     */
    private static android.util.Size chooseOptimalSize(android.util.Size[] choices, int textureViewWidth,
                                                       int textureViewHeight, int maxWidth, int maxHeight, android.util.Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<android.util.Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<android.util.Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (android.util.Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.max(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.i(TAG, "onLayout r - l = " + (r - l) + ", b - t = " + (b -t));
        if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);
            final int availableWidth = r - l;
            final int availableHeight = b - t;
            int previewWidth = availableWidth;
            int previewHeight = availableHeight;
            if (previewSize != null) {
                if (isPortrait()) {
                    previewWidth = previewSize.height;
                    previewHeight = previewSize.width;
                } else {
                    previewWidth = previewSize.width;
                    previewHeight = previewSize.height;
                }
            } else if (optimalSize != null) {
                if (isPortrait()) {
                    previewWidth = optimalSize.getHeight();
                    previewHeight = optimalSize.getWidth();
                } else {
                    previewWidth = optimalSize.getWidth();
                    previewHeight = optimalSize.getHeight();
                }
            }
            float factH = (float) availableHeight  / previewHeight ;
            float factW = (float) availableWidth / previewWidth;
            float fact = factH < factW ? factH : factW;
            int layoutHeight = (int) (previewHeight * fact);
            int layoutWidth = (int) (previewWidth * fact);
            Log.i(TAG, "onLayout Width = " + layoutWidth + ", Height = " + layoutHeight);
            child.layout( (availableWidth - layoutWidth ) / 2 ,
                    (availableHeight - layoutHeight) / 2,
                    (availableWidth + layoutWidth ) / 2,
                    (availableHeight + layoutHeight) / 2); // left, top, right, bottom
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        surfaceHolder = holder;
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        try {
            if (camera != null) {
                camera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Log.i(TAG, "IOException caused by setPreviewDisplay()", exception);
        }

        if  (isCamera2 && cameraDevice != null ) {
            mImageReader = ImageReader.newInstance(optimalSize.getWidth(), optimalSize.getHeight(),
                    ImageFormat.JPEG, /*maxImages*/2);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, cameraHandler);
            List<Surface> outputs = Arrays.asList(holder.getSurface(), mImageReader.getSurface());
            try {
                cameraDevice.createCaptureSession(outputs, mCaptureSessionListener, cameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    final CameraCaptureSession.StateCallback mCaptureSessionListener =
            new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    Log.i(TAG, "Finished configuring camera outputs");
                    mCaptureSession = session;
                    if (surfaceHolder != null) {
                        try {
                            // Build a request for preview footage
                            CaptureRequest.Builder requestBuilder =
                                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                            requestBuilder.addTarget(surfaceHolder.getSurface());
                            CaptureRequest previewRequest = requestBuilder.build();
                            // Start displaying preview images
                            try {
                                session.setRepeatingRequest(previewRequest, /*listener*/null,
                                /*handler*/null);
                            } catch (CameraAccessException ex) {
                                Log.e(TAG, "Failed to make repeating preview request", ex);
                            }
                        } catch (CameraAccessException ex) {
                            Log.e(TAG, "Failed to build preview request", ex);
                        }
                    }
                    else {
                        Log.e(TAG, "Holder didn't exist when trying to formulate preview request");
                    }
                }
                @Override
                public void onClosed(CameraCaptureSession session) {
                    mCaptureSession = null;
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.e(TAG, "Configuration error on device '" + cameraDevice.getId());
                }
            };

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        // Surface will be destroyed when we return, so stop the preview.
        if (camera != null) {
            stopCameraPreview();
        }
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged, width: " + width + ", height: " + height);
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        if (camera != null) {
            Log.i(TAG, "surfaceChanged " + previewSize.width + ", " + previewSize.height + "|| " +
                    pictureSize.width + ", " + pictureSize.height);
            try { // handle RuntimeException: getParameters failed (empty parameters)
                Camera.Parameters parameters = camera.getParameters();
                if (previewSize!= null) {
                    parameters.setPreviewSize(previewSize.width, previewSize.height);
                }
                if (pictureSize != null) {
                    parameters.setPictureSize(pictureSize.width, pictureSize.height);
                }
                camera.setParameters(parameters);
                // this is necessary  for the preview to have correct orientation / aspect ratio.
                configureOrientationParams();
                requestLayout(); // triggers the onMeasure ?
                startCameraPreview();
            } catch (RuntimeException exception) {
                Log.i(TAG, "RuntimeException caused by getParameters in surfaceChanged", exception);
            }
        }
    }

    // best size based on aspect ratio
    private android.util.Size findBestSize(android.util.Size[] outputSizes) {
        // Find a good size for output - largest 16:9 aspect ratio that's less than 720p
        final int MAX_WIDTH = 1280;
        final float TARGET_ASPECT = 16.f / 9.f;
        final float ASPECT_TOLERANCE = 0.1f;

        android.util.Size outputSize = outputSizes[0];
        float outputAspect = (float) outputSize.getWidth() / outputSize.getHeight();
        for (android.util.Size candidateSize : outputSizes) {
            if (candidateSize.getWidth() > MAX_WIDTH) continue;
            float candidateAspect = (float) candidateSize.getWidth() / candidateSize.getHeight();
            boolean goodCandidateAspect =
                    Math.abs(candidateAspect - TARGET_ASPECT) < ASPECT_TOLERANCE;
            boolean goodOutputAspect =
                    Math.abs(outputAspect - TARGET_ASPECT) < ASPECT_TOLERANCE;
            if ((goodCandidateAspect && !goodOutputAspect) ||
                    candidateSize.getWidth() > outputSize.getWidth()) {
                outputSize = candidateSize;
                outputAspect = candidateAspect;
            }
        }
        Log.i(TAG, "Resolution chosen: " + outputSize);
        return outputSize;
    }

    // http://stackoverflow.com/questions/19577299/android-camera-preview-stretched
    private Size getOptimalPreviewSize(List<Size> sizes, int targetWidth, int targetHeight) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) targetWidth / targetHeight;
        if (sizes == null) return null;
        Size optimalPreviewSize = null;
        double minDiff = Double.MAX_VALUE;
        boolean isPortrait = isPortrait();
        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            int previewWidth = size.width;
            int previewHeight = size.height;
            if (isPortrait) {
                previewWidth = size.height;
                previewHeight = size.width;
            }
            double ratio = (double) previewWidth / previewHeight;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }
            if (Math.abs(previewHeight - targetHeight) < minDiff) {
                optimalPreviewSize = size;
                minDiff = Math.abs(previewHeight - targetHeight);
            }
        }
        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalPreviewSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                int previewWidth = size.width;
                int previewHeight = size.height;
                if (isPortrait) {
                    previewWidth = size.height;
                    previewHeight = size.width;
                }
                double curRatio = ((double) previewWidth) / previewHeight;
                double deltaRatio = Math.abs(targetRatio - curRatio);
                if (deltaRatio < minDiff) {
                    optimalPreviewSize = size;
                    minDiff = deltaRatio;
                }
            }
        }
        return optimalPreviewSize;
    }

    /* package */ void startCameraPreview() {
        Log.i(TAG, "startCameraPreview");
        if (camera != null ) {
            Log.i(TAG, "startCameraPreview in If Block");
            camera.startPreview();
            this.safeToTakePicture = true;
        }
    }

    /* package */ void stopCameraPreview() {
        if (camera != null) {
            camera.stopPreview();
        }
    }

    private boolean isPortrait() {
        return (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
    }

    // https://developer.android.com/reference/android/hardware/Camera.html#setDisplayOrientation(int)
    private void configureOrientationParams() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        rotationEventListener.onRotationChanged(activity, cameraInfo);
        camera.setDisplayOrientation(rotationEventListener.getCameraDisplayRotation());
    }

    /*package*/ final boolean getSafeToTakePicture() {
        return safeToTakePicture;
    }

    /*package*/ void setSafeToTakePicture(boolean safeToTakePicture) {
        this.safeToTakePicture = safeToTakePicture;
    }

    private static android.util.Size chooseBigEnoughSize(android.util.Size[] choices, int width, int height) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<android.util.Size> bigEnough = new ArrayList<android.util.Size>();
        for (android.util.Size option : choices) {
            if (option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            Log.i(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }
    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<android.util.Size> {
        @Override
        public int compare(android.util.Size lhs, android.util.Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }


    /* package */ CameraCaptureSession getCaptureSession() {
        return mCaptureSession;
    }

    /* package */ ImageReader getImageReader() {
        return mImageReader;
    }

    final ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    // Save the image once we get a chance
                    cameraHandler.post(new CapturedImageSaver(reader.acquireNextImage()));
                    // Control flow continues in CapturedImageSaver#run()
                }};
    class CapturedImageSaver implements Runnable {
        /**
         * The image to save.
         */
        private Image image;

        CapturedImageSaver(Image capture) {
            image = capture;
        }

        @Override
        public void run() {
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            Log.i(TAG, "CapturedImageSaver, bytes size: " + bytes.length);
            image.close();
            if (callback != null) {
                callback.onPhotoTaken(bytes);
            }
            // this should be sent to the handler to do the correct thing.
            final Bitmap bitmap = BitmapUtils.createSampledBitmapFromBytes(bytes, 800);
            // fix the bitmap
            Matrix matrix = new Matrix();
            // check if camera facing front:
            CameraManager cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
            boolean facingFront = false;
            try {
                CameraCharacteristics c = cameraManager.getCameraCharacteristics(cameraDevice.getId());
                facingFront = c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT;
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            // FIX THIS
            if (facingFront) {
                float[] mirrorY = {-1, 0, 0, 0, 1, 0, 0, 0, 1};
                Matrix matrixMirrorY = new Matrix();
                matrixMirrorY.setValues(mirrorY);
                matrix.postConcat(matrixMirrorY);
            }
            final Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,
                    false);
//            bitmap.recycle(); // WHY IS THIS FAILING
            Handler uiHandler = new Handler(Looper.getMainLooper());
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
//                     render the camera image back to the UI.
                    if (internalCallback != null) {
                        internalCallback.onBitmapProcessed(rotatedBitmap);
                    }
                }
            });
        }
    }
}
