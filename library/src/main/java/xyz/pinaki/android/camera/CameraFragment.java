package xyz.pinaki.android.camera;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.List;

import xyz.pinaki.android.camera.dimension.AspectRatio;
import xyz.pinaki.android.camera.dimension.Size;
import xyz.pinaki.android.camera.orientation.DisplayOrientationDetector;
import xyz.pinaki.android.camera.preview.SurfaceViewPreview;
import xyz.pinaki.android.camera.preview.TextureViewPreview;
import xyz.pinaki.android.camera.preview.ViewFinderPreview;
import xyz.pinaki.androidcamera.R;

/**
 * Created by pinaki on 8/11/17.
 */

public class CameraFragment extends BaseCameraFragment implements CameraView {
    private static final String TAG = CameraFragment.class.getName();
    private CameraPresenter cameraPresenter;
    private ViewFinderPreview viewFinderPreview;
    private CameraAPI.PreviewType previewType;
    private CameraAPI.LensFacing currentFacing = CameraAPI.LensFacing.BACK;
    private RelativeLayout parentView;
    private AdjustableLayout autoFitCameraView;
    private DisplayOrientationDetector displayOrientationDetector;
    ViewGroup previewContainer;
    ImageView previewImage;
    private CameraAPIClient.Callback apiCallback;
    private CameraStatusCallback cameraStatusCallback = new CameraStatusCallback() {
        @Override
        public void onCameraOpen() {
            autoFitCameraView.setPreview(viewFinderPreview);
            autoFitCameraView.setAspectRatio(cameraPresenter.getAspectRatio());
            autoFitCameraView.requestLayout();
            apiCallback.onCameraOpened();
        }

        @Override
        public void onPhotoTaken(byte[] data) {
            apiCallback.onPhotoTaken(data);
        }

        @Override
        public void onBitmapProcessed(Bitmap bitmap) {
            previewContainer.setVisibility(View.VISIBLE);
            previewImage.setImageBitmap(bitmap);
            apiCallback.onBitmapProcessed(bitmap);
        }

        @Override
        public void onCameraClosed() {
            apiCallback.onCameraClosed();
        }

        @Override
        public void onAspectRatioAvailable(AspectRatio desired, AspectRatio chosen, List<Size> availablePreviewSizes) {
            apiCallback.onAspectRatioAvailable(desired, chosen, availablePreviewSizes);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = (RelativeLayout) inflater.inflate(R.layout.camera_view_main, container, false);
        return parentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        View shutterIcon = view.findViewById(R.id.shutter);
        shutterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shutterClicked();
            }
        });
        View cameraSwitch = view.findViewById(R.id.switch_cam);
        cameraSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchCameraClicked();
            }
        });
        previewContainer = (ViewGroup) view.findViewById(R.id.preview_container);
        previewImage = (ImageView) view.findViewById(R.id.preview_image);
        final ImageView previewCloseButton = (ImageView) view.findViewById(R.id.preview_close_icon);
        previewCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previewContainer.setVisibility(View.INVISIBLE);
            }
        });
        autoFitCameraView = (AdjustableLayout) view.findViewById(R.id.camera_adjust);
        ViewFinderPreview.Callback viwefinderCallback = new ViewFinderPreview.Callback() {
            @Override
            public void onSurfaceChanged() {
                cameraPresenter.setPreview(viewFinderPreview);
                cameraPresenter.onStart(); // starts the camera
            }

            @Override
            public void onSurfaceDestroyed() {
                cameraPresenter.onStop();
            }

            @Override
            public void onSurfaceCreated() {
            }
        };
        if(previewType == CameraAPI.PreviewType.TEXTURE_VIEW) {
            viewFinderPreview = new TextureViewPreview(getContext(), autoFitCameraView, viwefinderCallback);
        } else {
            viewFinderPreview = new SurfaceViewPreview(getContext(), autoFitCameraView, viwefinderCallback);
        }
        viewFinderPreview.start();

        if (displayOrientationDetector == null) {
            // the constructor has to be within one of the lifecycle event to make sure the context is not null;
            displayOrientationDetector = new DisplayOrientationDetector(getContext()) {
                @Override
                public void onDisplayOrientationChanged(int displayOrientation) {
                    // update listeners
                    cameraPresenter.setDisplayOrientation(displayOrientation);
                    autoFitCameraView.setDisplayOrientation(displayOrientation);
                }
            };
        }
        displayOrientationDetector.enable(getActivity().getWindowManager().getDefaultDisplay());
    }

    @Override
    public void onDestroyView() {
        displayOrientationDetector.disable();
        super.onDestroyView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraPresenter.setCameraStatusCallback(cameraStatusCallback);
        cameraPresenter.onCreate();
    }

    @Override
    public void onDestroy() {
        cameraPresenter.onDestroy();
        cameraStatusCallback.onCameraClosed();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof AppCompatActivity &&
                ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        } else if ( getActivity() !=  null && getActivity().getActionBar() != null) {
            getActivity().getActionBar().hide();
        }
        // TODO: enable orientation listener: cameraPresenter helps somehow
//        orientationListener = new DeviceOrientationListener(getActivity());
    }

    @Override
    public void setPresenter(@NonNull CameraPresenter c) {
        cameraPresenter = c;
    }

    @Override
    public void setPreviewType(CameraAPI.PreviewType p) {
        previewType = p;
    }

    public void setCallback(CameraAPIClient.Callback c) {
        apiCallback = c;
    }

    @Override
    public void shutterClicked() {
        cameraPresenter.takePicture();
    }

    @Override
    public void switchCameraClicked() {
        currentFacing = currentFacing == CameraAPI.LensFacing.BACK ? CameraAPI.LensFacing.FRONT : CameraAPI
                .LensFacing.BACK;
        viewFinderPreview.stop();
        cameraPresenter.setFacing(currentFacing);
        viewFinderPreview.start();
    }

    @Override
    public void switchFlashClicked() {

    }

    @Override
    public void focus() {

    }
}
