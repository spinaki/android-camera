package xyz.pinaki.android.camera;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import xyz.pinaki.androidcamera.R;

/**
 * Created by pinaki on 8/11/17.
 */

public class Camera1Fragment extends BaseCameraFragment implements CameraView {
    private static final String TAG = Camera1Fragment.class.getName();
    private CameraPresenter cameraPresenter;
    private ViewFinderPreview viewFinderPreview;
    private int numCallsToChange = 0;
    RelativeLayout parentLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        parentLayout = (RelativeLayout) inflater.inflate(R.layout.camera_fragment, container, false);
        return  parentLayout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        View shutterIcon = view.findViewById(R.id.shutter);
        shutterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Fix this
//                takePicture();
//                cameraPresenter.takePicture();
                shutterClicked();
            }
        });
        View cameraSwitch = view.findViewById(R.id.switch_cam);
        cameraSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: fix this cameraPresenter
//                stopAndRelease();
                switchCameraClicked();
            }
        });

        // captured preview
        // TODO: fix this
        final ViewGroup previewContainer = (ViewGroup) view.findViewById(R.id.preview_container);
        final ImageView previewImage = (ImageView) view.findViewById(R.id.preview_image);
        final ImageView previewCloseButton = (ImageView) view.findViewById(R.id.preview_close_icon);
        previewCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previewContainer.setVisibility(View.INVISIBLE);
            }
        });
        viewFinderPreview = new SurfaceViewPreview(getContext(), parentLayout, new ViewFinderPreview.Callback() {
            @Override
            public void onSurfaceChanged() {
                numCallsToChange++;
                Log.i(TAG, "viewFinderPreview onSurfaceChanged, numCalls: " + numCallsToChange);
                cameraPresenter.setPreview(viewFinderPreview);
                cameraPresenter.onResume();
            }

            @Override
            public void onSurfaceDestroyed() {
                numCallsToChange--;
                Log.i(TAG, "viewFinderPreview onSurfaceDestroyed");
                cameraPresenter.onPause();
            }

            @Override
            public void onSurfaceCreated() {
                Log.i(TAG, "viewFinderPreview onSurfaceCreated");
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: fix this with the correct ViewGroup
//        cameraPresenter.onCreate(getContext(), (ViewGroup) getView());
        // TODO or not use viewgroup here and use in fragment
        cameraPresenter.onCreate();

    }

    @Override
    public void onDestroy() {
        cameraPresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        // TODO: fix this
//        cameraHandlerThreadOld = new CameraHandlerThreadOld();
//        cameraHandlerThreadOld.onResume();
//        openCamera();
//        orientationListener.enable();
//        cameraPresenter.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        // TODO: fix this
//        cameraPresenter.onPause();

//        stopAndRelease();
//        orientationListener.disable();
//        if (cameraHandlerThreadOld != null) {
//            cameraHandlerThreadOld.quit();
//        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        // TODO: hide action bar
//        if (getActivity() instanceof AppCompatActivity &&
//                ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
//            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
//        } else if ( getActivity() !=  null && getActivity().getActionBar() != null) {
//            getActivity().getActionBar().hide();
//        }
        // TODO: enable orientation listener: cameraPresenter helps somehow
//        orientationListener = new DeviceOrientationListener(getActivity());
    }

    @Override
    public void setPresenter(@NonNull CameraPresenter c) {
        cameraPresenter = c;
    }

    @Override
    public void shutterClicked() {
        cameraPresenter.takePicture();
    }

    @Override
    public void switchCameraClicked() {
        // TODO: fix
        cameraPresenter.setFacing(0);
    }

    @Override
    public void switchFlashClicked() {

    }

    @Override
    public void focus() {

    }
}
