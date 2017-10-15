package xyz.pinaki.android.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xyz.pinaki.android.camera.dimension.AspectRatio;
import xyz.pinaki.android.camera.dimension.Size;

/**
 * Created by pinaki on 8/11/17.
 */
@SuppressWarnings("MissingPermission")
class Camera2 extends BaseCamera {
    private static String TAG = Camera2.class.getSimpleName();
    private int lensFacing = CameraCharacteristics.LENS_FACING_BACK;
    private CameraManager cameraManager;
    private String cameraId;
    private CameraCharacteristics cameraCharacteristics;
    private ImageReader imageReader;
    private Size previewImageSize;
    private Size capturedPictureSize;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder previewRequestBuilder;
    private HandlerThread backgroundThread;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    Camera2(AppCompatActivity a) {
        super(a);
    }
    Camera2(AppCompatActivity a, HandlerThread h) {
        super(a);
        backgroundThread = h;
    }

    @Override
    public boolean start() {
        if (!super.start()) {
            return false;
        }
        deviceOrientationListener.enable();
        cameraManager = (CameraManager) activity.get().getSystemService(Context.CAMERA_SERVICE);
        // choose camera id by lens
        if (!chooseCameraIdByLensFacing()) {
            return false;
        }
        // collect preview and picture sizes based on the query aspect ratio
        StreamConfigurationMap info = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (info == null) {
            throw new IllegalStateException("Failed to get configuration map: " + cameraId);
        }
        android.util.Size[] sizes = info.getOutputSizes(viewFinderPreview.gePreviewType());
        AspectRatio desiredAspectRatio = AspectRatio.of(aspectRatio.getWidth(), aspectRatio.getHeight());
        List<Size> availableSizes = convertSizes(sizes);
        previewImageSize = chooseOptimalSize(availableSizes);
        cameraStatusCallback.onAspectRatioAvailable(desiredAspectRatio, aspectRatio, availableSizes);
        sizes = info.getOutputSizes(ImageFormat.JPEG);
        capturedPictureSize = chooseOptimalSize(convertSizes(sizes));
        // prepare image reader
        prepareImageReader(capturedPictureSize);
        // open the camera and relayout the surface based on the chosen size
        startOpeningCamera();
        return true;
    }

    private static List<Size> convertSizes(android.util.Size[] aSizes ) {
        List<Size> sizes = new ArrayList<>();
        for (android.util.Size s : aSizes) {
            sizes.add(new Size(s.getWidth(), s.getHeight()));
        }
        return sizes;
    }

    private void startOpeningCamera() {
        try {
            cameraManager.openCamera(cameraId, cameraDeviceCallback, new Handler(backgroundThread.getLooper()));
        } catch (CameraAccessException e) {
            throw new RuntimeException("Failed to open camera: " + cameraId, e);
        }
    }

    private final CameraDevice.StateCallback cameraDeviceCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            startCaptureSession();
        }

        @Override
        public void onClosed(@NonNull CameraDevice camera) {
//            mCallback.onCameraClosed(); // TODO fix on closed
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice = null; // TODO fix on closed
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "onError: " + camera.getId() + " (" + error + ")");
            cameraDevice = null;
        }
    };

    private void startCaptureSession() {
        // TODO: do you need this ?
        viewFinderPreview.setBufferSize(previewImageSize.getWidth(), previewImageSize.getHeight());
        Surface surface = viewFinderPreview.getSurface();
        try {
            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()),
                    sessionCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private final CameraCaptureSession.StateCallback sessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            captureSession = session;
            try {
                previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                updateAutoFocus();
//            updateFlash(); TODO: add later
                previewRequestBuilder.addTarget(viewFinderPreview.getSurface());
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cameraStatusCallback.onCameraOpen();
                    }
                });
                // set repeating request for preview
                session.setRepeatingRequest(previewRequestBuilder.build(), mCaptureCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

        }
    };

    private void updateAutoFocus() {
        int[] modes = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
        if (modes == null || modes.length == 0 ||
                (modes.length == 1 && modes[0] == CameraCharacteristics.CONTROL_AF_MODE_OFF)) {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
        } else {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        }
    }

    private static abstract class PictureCaptureCallback extends CameraCaptureSession.CaptureCallback {
        static final int STATE_PREVIEW = 0;
        static final int STATE_LOCKING = 1;
        static final int STATE_LOCKED = 2;
        static final int STATE_PRECAPTURE = 3;
        static final int STATE_WAITING = 4;
        static final int STATE_CAPTURING = 5;
        private int mState;
        void setState(int state) {
            mState = state;
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            onPreviewReady(); // TODO: check this
            process(result);
        }

        private void process(@NonNull CaptureResult result) {
            switch (mState) {
                case STATE_LOCKING: {
                    Integer af = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (af == null) {
                        break;
                    }
                    if (af == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
                            af == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                        Integer ae = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (ae == null || ae == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            setState(STATE_CAPTURING);
                            onReady();
                        } else {
                            setState(STATE_LOCKED);
                            onPrecaptureRequired();
                        }
                    }
                    break;
                }
                case STATE_PRECAPTURE: {
                    Integer ae = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (ae == null || ae == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            ae == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED ||
                            ae == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                        setState(STATE_WAITING);
                    }
                    break;
                }
                case STATE_WAITING: {
                    Integer ae = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (ae == null || ae != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        setState(STATE_CAPTURING);
                        onReady();
                    }
                    break;
                }
            }
        }

        abstract void onPrecaptureRequired();

        abstract void onReady();
        abstract void onPreviewReady();
    };
    private PictureCaptureCallback mCaptureCallback = new PictureCaptureCallback() {
        @Override
        void onReady() {
            captureStillPicture();
        }

        @Override
        void onPreviewReady() {
        }

        @Override
        public void onPrecaptureRequired() {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            setState(STATE_PRECAPTURE);
            try {
                captureSession.capture(previewRequestBuilder.build(), this, null);
                previewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                        CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE);
            } catch (CameraAccessException e) {
                Log.e(TAG, "Failed to run precapture sequence.", e);
            }
        }
    };

    void captureStillPicture() {
        deviceOrientationListener.rememberOrientation();
        try {
            CaptureRequest.Builder picCaptureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice
                    .TEMPLATE_STILL_CAPTURE);
            picCaptureRequestBuilder.addTarget(imageReader.getSurface());
            picCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    previewRequestBuilder.get(CaptureRequest.CONTROL_AF_MODE));
            // fix the orientation
            picCaptureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, getJpegOrientation(cameraCharacteristics));
            captureSession.stopRepeating(); // Stop preview while capturing a still picture.
            captureSession.capture(picCaptureRequestBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    restartPreview(); // restart the preview etc.
                }
            }, null);

        } catch (CameraAccessException e) {
            Log.e(TAG, "Cannot capture a still picture.", e);
        }
    }

    private int getJpegOrientation(CameraCharacteristics c) {
        int sensorOrientation = c.get(CameraCharacteristics.SENSOR_ORIENTATION);
        Log.i(TAG, "sensorOrientation: " + sensorOrientation);
        // Round device orientation to a multiple of 90
//        deviceOrientation = (deviceOrientation + 45) / 90 * 90;
        int deviceOrientation = deviceOrientationListener.getRememberedOrientation();
        Log.i(TAG, "getRememberedOrientation.: " + deviceOrientation);
        Log.i(TAG, "displayOrientation.: " + displayOrientation);
        // Reverse device orientation for front-facing cameras
        boolean facingFront = c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT;
        if (facingFront) {
            deviceOrientation = -deviceOrientation;
        }
        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        int jpegOrientation = (sensorOrientation + deviceOrientation + 360) % 360;
        return jpegOrientation;
    }

    void restartPreview() {
        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);
        try {
            captureSession.capture(previewRequestBuilder.build(), mCaptureCallback, null);
            updateAutoFocus();
//            updateFlash();
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
            captureSession.setRepeatingRequest(previewRequestBuilder.build(), mCaptureCallback, null);
            mCaptureCallback.setState(PictureCaptureCallback.STATE_PREVIEW);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to restart camera preview.", e);
        }
    }

    private final ImageReader.OnImageAvailableListener onImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            try (Image image = reader.acquireNextImage()) {
                Image.Plane[] planes = image.getPlanes();
                if (planes.length > 0) {
                    ByteBuffer buffer = planes[0].getBuffer();
                    final byte[] data = new byte[buffer.remaining()];
                    buffer.get(data);
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cameraStatusCallback.onPhotoTaken(data);
                        }
                    });
                    final Bitmap bitmap = BitmapUtils.createSampledBitmapFromBytes(data, getMaxWidthSize());
                    final Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap
                            .getHeight(), getImageTransformMatrix(), false);
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cameraStatusCallback.onBitmapProcessed(rotatedBitmap);
                        }
                    });
                }
            }
        }

    };
    private Matrix getImageTransformMatrix() {
        Matrix matrix = new Matrix();
        if (lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
            float[] mirrorY = {-1, 0, 0, 0, 1, 0, 0, 0, 1};
            Matrix matrixMirrorY = new Matrix();
            matrixMirrorY.setValues(mirrorY);
            matrix.postConcat(matrixMirrorY);
        }
        return matrix;
    }
    private void prepareImageReader(Size size) {
        // TODO: hack have the correct size;
        imageReader = ImageReader.newInstance(size.getWidth(), size.getHeight(), ImageFormat.JPEG, /* maxImages */ 2);
        imageReader.setOnImageAvailableListener(onImageAvailableListener, null);
    }

    private boolean chooseCameraIdByLensFacing() {
        final String[] ids;
        try {
            ids = cameraManager.getCameraIdList();
            if (ids.length == 0) { // No camera
                throw new RuntimeException("No camera available.");
            }
            for (String id : ids) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Integer lFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (lFacing == null) {
                    throw new NullPointerException("Unexpected state: LENS_FACING null");
                }
                if (lensFacing == lFacing) {
                    cameraId = id;
                    cameraCharacteristics = characteristics;
                    return true;
                }
            }
            // cameraId not found -- choose the default camera ??
            cameraId = ids[0];
            cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
            Integer level = cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            if (level == null || level == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                return false;
            }
            if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == null) {
                return false;
            }
            lensFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
            return true;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void stop() {
        deviceOrientationListener.disable();
        // stop the camera and release resources
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
    }

    @Override
    public boolean isCameraOpened() {
        return false;
    }

    @Override
    public void setFacing(CameraAPI.LensFacing l) {
        if (l == CameraAPI.LensFacing.BACK) {
            lensFacing = CameraCharacteristics.LENS_FACING_BACK;
        } else if (l == CameraAPI.LensFacing.FRONT) {
            lensFacing = CameraCharacteristics.LENS_FACING_FRONT;
        } else {
            throw new RuntimeException("Unknown Facing Camera!");
        }
    }
    @Override
    public int getFacing() {
        return 0;
    }

    @Override
    public void takePicture(PhotoTakenCallback photoTakenCallback) {
        captureStillPicture();
    }
}
