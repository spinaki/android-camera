package xyz.pinaki.android.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import junit.framework.Assert;

import xyz.pinaki.androidcamera.R;

/**
 * Created by pinaki on 3/29/17.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Camera2Fragment extends Fragment {
    private static final String TAG = Camera2Fragment.class.getSimpleName();
    CameraHandlerThread cameraHandlerThread;
    Handler cameraHandler, uiHandler;
    private CameraDevice camera;
    CenteredCameraPreviewHolder previewHolder;
    RelativeLayout parentLayout;
    private RotationEventListener rotationEventListener = new RotationEventListener();
    View previewContainer;
    ImageView previewImage;
    private final CameraCallback cameraCallback = new CameraCallback() {

        @Override
        public void onPictureTaken(Bitmap bitmap) {
            Assert.fail("Stub!");
        }

        @Override
        public void onCameraOpen(Camera camera) {
            Assert.fail("Stub!");
        }

        @Override
        public void onBitmapProcessed(Bitmap bitmap) {
            Log.i(TAG, "onBitmapProcessed");
            previewContainer.setVisibility(View.VISIBLE);
            previewImage.setImageBitmap(bitmap);
        }
    };

    public Camera2Fragment() {
        // empty constructor
    }

    /* package */ static Camera2Fragment newInstance() {
        return new Camera2Fragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof AppCompatActivity &&
                ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        } else if (getActivity() != null && getActivity().getActionBar() != null) {
            getActivity().getActionBar().hide();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        parentLayout = (RelativeLayout)inflater.inflate(R.layout.camera_fragment, container, false);
//        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
//                != PackageManager.PERMISSION_GRANTED) {
//            Log.i(TAG, "Requesting Camera Permissions from onCreateView ");
//            requestCameraPermission();
//        }
        return parentLayout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        View cameraSwitch = view.findViewById(R.id.switch_cam);
        cameraSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchCamera();
            }
        });

        View shutterIcon = view.findViewById(R.id.shutter);
        shutterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "shutter clicked");
                if ( previewHolder != null && previewHolder.getCaptureSession() != null && previewHolder
                        .getImageReader() != null) {
                    Log.i(TAG, "shutter clicked if block");
                    try {
                        CaptureRequest.Builder requester = camera.createCaptureRequest(CameraDevice
                                .TEMPLATE_STILL_CAPTURE);
                        requester.addTarget(previewHolder.getImageReader().getSurface());
                        CameraCaptureSession.CaptureCallback captureCallback
                                = new CameraCaptureSession.CaptureCallback() {

                            @Override
                            public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                                           @NonNull CaptureRequest request,
                                                           @NonNull TotalCaptureResult result) {
                                Log.d(TAG, "onCaptureCompleted");
                            }
                        };
                        previewHolder.getCaptureSession().capture(requester.build(), captureCallback, cameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
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
    public void onResume() {
        super.onResume();
        cameraHandlerThread = new CameraHandlerThread();
        cameraHandlerThread.start();
        cameraHandler = new Handler(cameraHandlerThread.getLooper());
        uiHandler = new Handler(Looper.getMainLooper());
        openCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (camera != null) {
            camera.close();
            camera = null;
        }
        if (parentLayout != null) {
            parentLayout.removeView(previewHolder);
        }
        cameraHandlerThread.quitSafely();
        try {
            cameraHandlerThread.join();
        } catch (InterruptedException ex) {
            Log.e(TAG, "Background worker thread was interrupted while joined", ex);
        }
    }

    private void openCamera() {
        openCamera(CameraCharacteristics.LENS_FACING_BACK);
    }


    private void openCamera(int lensFacing) {
        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);

                // We don't use a front facing camera in this sample.
                // this needs to change.
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing == null || facing != lensFacing) {
                    continue;
                }
                // add permissions
                // check if this is allowed.
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
//                    requestCameraPermission();
                } else {
                    manager.openCamera(cameraId, mStateCallback, cameraHandler);
                }
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
//            mCameraOpenCloseLock.release();
            camera = cameraDevice;
//            createCameraPreviewSession();
            // create the surfaceView
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    previewHolder = createCenteredCameraPreview(getActivity());
                    previewHolder.addSurfaceView();
                    parentLayout.addView(previewHolder, 0);
                    previewHolder.setCamera(camera);
                }
            });
        }

        private CenteredCameraPreviewHolder createCenteredCameraPreview(Activity activity) {
            CenteredCameraPreviewHolder previewHolder = new CenteredCameraPreviewHolder(activity,
                    rotationEventListener, true, cameraHandler, cameraCallback);
            previewHolder.setBackgroundColor(Color.BLACK);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams
                    .MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            previewHolder.setLayoutParams(layoutParams);
            return previewHolder;
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
//            mCameraOpenCloseLock.release();
            cameraDevice.close();
            camera = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
//            mCameraOpenCloseLock.release();
            cameraDevice.close();
            camera = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }
    };

    private void switchCamera() {
        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(camera.getId());
            int lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING) ==  CameraCharacteristics
                    .LENS_FACING_FRONT ? CameraCharacteristics.LENS_FACING_BACK : CameraCharacteristics
                    .LENS_FACING_FRONT;
            if (camera != null) {
                camera.close();
                camera = null;
            }
            if (parentLayout != null ) {
                parentLayout.removeView(previewHolder);
            }
            openCamera(lensFacing);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
