package xyz.pinaki.android.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.List;

import xyz.pinaki.androidcamera.R;


/**
 * Created by pinaki.
 * references:
 * 1. https://android.googlesource.com/platform/development/+/master/samples/ApiDemos/src/com/example
 * /android/apis/graphics/CameraPreview.java
 * 2. https://github.com/pikanji/CameraPreviewSample/blob/master/src/net/pikanji/camerapreviewsample/MainActivity.java
 * 3. https://github.com/dm77/barcodescanner/blob/master/core/src/main/java/me/dm7/barcodescanner/core/CameraPreview.java
 *
 * Details about old code: https://docs.google.com/document/d/14uhODZIqGdz1hZZR1Nz7ineOiS0_zM-SfFE9XeJRT54/edit#
 */

@SuppressWarnings("deprecation")
public class CameraFragment extends Fragment {
    private static final int COMPRESS_IMAGE_MAX_DIMENSION = 800;
    private static final String TAG = CameraFragment.class.getSimpleName();
    private static final int REQUEST_CAMERA = 0;
    private Camera camera = null;
    // necessary for correctly rotating the captured bitmap so that exported image / view has the correct orientation--
    // this is especially if the phone is in landscape mode while capturing.
    private DeviceOrientationListener orientationListener;
    private RotationEventListener rotationEventListener = new RotationEventListener();;
    private int cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private boolean shouldCreateLowresImage = true;
    CenteredCameraPreviewHolder previewHolder;
    RelativeLayout parentLayout;
    View previewContainer;
    ImageView previewImage;
    private CameraHandlerThread cameraHandlerThread;
    private CameraController.Callback callback;
    private final InternalCallback internalCallback = new InternalCallback() {
        @Override
        public void onPictureTaken(Bitmap bitmap, byte[] bytes) {
            if (callback != null) {
                callback.onPhotoTaken(bytes);
            }
            cameraHandlerThread.processBitmap(cameraId, bitmap,  orientationListener, rotationEventListener, this);
        }

        @Override
        public void onCameraOpen(Camera cam) {
            camera = cam;
            previewHolder = createCenteredCameraPreview(getActivity());
            previewHolder.addSurfaceView();
            parentLayout.addView(previewHolder, 0);
            previewHolder.setCamera(camera, cameraId);
            if (callback != null) {
                callback.onCameraOpened();
            }
        }

        @Override
        public void onBitmapProcessed(Bitmap bitmap) {
            previewContainer.setVisibility(View.VISIBLE);
            previewImage.setImageBitmap(bitmap);
            previewHolder.startCameraPreview();
            if (callback != null) {
                callback.onBitmapProcessed(bitmap);
            }
        }
    };

    public CameraFragment() {
        // empty constructor
    }

    /* package */ static CameraFragment newInstance() {
        return new CameraFragment();
    }

    /* package */ void setCallback(CameraController.Callback callback) {
        this.callback = callback;
    }

    // check if this device has a camera
    private static boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public void takePicture() {
        if (previewHolder != null) {
            Log.i(TAG, "safeToTakePicture: " + previewHolder.getSafeToTakePicture());
            orientationListener.rememberOrientation();
            if (previewHolder.getSafeToTakePicture()) {
                previewHolder.setSafeToTakePicture(false);
                cameraHandlerThread.capturePhoto(camera, internalCallback);
            }
        } else {
            Log.i(TAG, "previewHolder is NULL");
        }
    }

//    @Override
//    public void onPictureTaken(byte[] data, Camera camera) {
//        if (internalCallback != null) {
//            Bitmap bitmap;
//            if (shouldCreateLowresImage) {
//                bitmap = BitmapUtils.createSampledBitmapFromBytes(data, COMPRESS_IMAGE_MAX_DIMENSION);
//            } else {
//                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//            }
//            int rotation = (previewHolder.getDisplayOrientation() + orientationListener.getRememberedOrientation() +
//                    previewHolder.getLayoutOrientation()) % 360;
////            Log.i(TAG, "onPictureTaken displayOrientation: " + previewHolder.getDisplayOrientation() + ", " +
////                    "orientationListener: " + orientationListener.currentNormalizedOrientation + ", " +
////                    orientationListener.rememberedNormalizedOrientation +
////                    ", layoutOrientation: " + previewHolder.getLayoutOrientation() + ", rotation: " + rotation);
//            Matrix matrix = new Matrix();
//            if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//                float[] mirrorY = {-1, 0, 0, 0, 1, 0, 0, 0, 1};
//                Matrix matrixMirrorY = new Matrix();
//                matrixMirrorY.setValues(mirrorY);
//                matrix.postConcat(matrixMirrorY);
//            }
//            matrix.postRotate(rotation);
//            Log.i(TAG, "createBitmap: width: " + bitmap.getWidth() + ", height: " + bitmap.getHeight());
//            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
//            internalCallback.onPictureTaken(bitmap);
//            previewHolder.startCameraPreview(); // start preview in the background
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();
        cameraHandlerThread = new CameraHandlerThread();
        cameraHandlerThread.start();
        openCamera();
        orientationListener.enable();
    }

    private void openCamera() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        } else {
            if (checkCameraHardware(getContext())) {
                try {
                    if (camera != null) {
                        stopAndRelease();
                    }
                    cameraHandlerThread.openCamera(cameraId, internalCallback);
                } catch (RuntimeException exception) {
                    Log.i(TAG, "Cannot open camera with id " + cameraId, exception);
                }
            } else {
                Log.i(TAG, "Camera Not Present");
            }
        }
    }

    private void stopAndRelease() {
        if (previewHolder != null) {
            previewHolder.stopCameraPreview();
            previewHolder.unsetCamera();
        }
        if (camera != null) {
            camera.release();
            camera = null;
        }
        if (callback != null) {
            callback.onCameraClosed();
        }
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        stopAndRelease();
        orientationListener.disable();
        if (cameraHandlerThread != null) {
            cameraHandlerThread.quit();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof  AppCompatActivity &&
                ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        } else if ( getActivity() !=  null && getActivity().getActionBar() != null) {
            getActivity().getActionBar().hide();
        }
        orientationListener = new DeviceOrientationListener(getActivity());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        parentLayout = (RelativeLayout)inflater.inflate(R.layout.camera_fragment, container, false);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Requesting Camera Permissions from onCreateView ");
            requestCameraPermission();
        }
        return parentLayout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        View shutterIcon = view.findViewById(R.id.shutter);
        shutterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });
        View cameraSwitch = view.findViewById(R.id.switch_cam);
        cameraSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopAndRelease();
                switchCamera();
            }
        });

        previewContainer = view.findViewById(R.id.preview_container);
        previewImage = (ImageView) view.findViewById(R.id.preview_image);
        final ImageView previewCloseButton = (ImageView) view.findViewById(R.id.preview_close_icon);
        previewCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previewContainer.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView");
        super.onDestroyView();
    }

    private CenteredCameraPreviewHolder createCenteredCameraPreview(Activity activity) {
        CenteredCameraPreviewHolder previewHolder = new CenteredCameraPreviewHolder(activity, rotationEventListener);
        previewHolder.setBackgroundColor(Color.BLACK);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams
                .MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        previewHolder.setLayoutParams(layoutParams);
        return previewHolder;
    }

    private void switchCamera() {
        cameraId = (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) ? Camera.CameraInfo.CAMERA_FACING_FRONT :
                Camera.CameraInfo.CAMERA_FACING_BACK;
        Log.i(TAG, "switching to camera: " + cameraId);
        parentLayout.removeView(previewHolder);
        openCamera();
    }

//    protected boolean isFrontFacingCamera() {
//        if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//            return true;
//        }
//        return false;
//    }

    protected boolean hasFlash() {
        Camera.Parameters params = camera.getParameters();
        List<String> flashModes = params.getSupportedFlashModes();
        if (flashModes == null) {
            return false;
        }

        for (String flashMode : flashModes) {
            if (Camera.Parameters.FLASH_MODE_ON.equals(flashMode)) {
                return true;
            }
        }
        return false;
    }

    protected void setFlashMode(CameraFlashMode mode) {
        if (camera == null || !hasFlash()) {
            return;
        }
        Camera.Parameters parameters = camera.getParameters();
        switch (mode) {
            case AUTO:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                break;
            case OFF:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                break;
            case ON:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                break;
            default:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                break;
        }
        camera.setParameters(parameters);
    }

    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            // add some message here
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onReq permissions result");
        if (requestCode == REQUEST_CAMERA) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for camera permission.
            Log.i(TAG, "Received response for Camera permission request.");
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission has been granted, preview can be displayed
                Log.i(TAG, "CAMERA permission has now been granted. Showing preview.");
                // requestPermissionFollowUp();
            } else {
                Log.i(TAG, "CAMERA permission was NOT granted.");
            }
        }
    }
}

