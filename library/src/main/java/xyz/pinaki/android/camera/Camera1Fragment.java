package xyz.pinaki.android.camera;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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

public class Camera1Fragment extends Fragment implements CameraView {
    private static final String TAG = Camera1Fragment.class.getName();
    private CameraPresenter cameraPresenter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        RelativeLayout parentLayout = (RelativeLayout) inflater.inflate(R.layout.camera_fragment, container, false);
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
                cameraPresenter.takePicture();
            }
        });
        View cameraSwitch = view.findViewById(R.id.switch_cam);
        cameraSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: fix this cameraPresenter
//                stopAndRelease();
//                switchCamera();
            }
        });

        // captured preview
        // TODO: fix this
        final View previewContainer = view.findViewById(R.id.preview_container);
        final ImageView previewImage = (ImageView) view.findViewById(R.id.preview_image);
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
        // TODO: fix this
//        cameraHandlerThread = new CameraHandlerThread();
//        cameraHandlerThread.start();
//        openCamera();
//        orientationListener.enable();
        cameraPresenter.start();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        // TODO: fic this
        cameraPresenter.stop();
//        stopAndRelease();
//        orientationListener.disable();
//        if (cameraHandlerThread != null) {
//            cameraHandlerThread.quit();
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
}
