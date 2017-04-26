package xyz.pinaki.androidcamera.example;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import xyz.pinaki.android.camera.CameraController;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_WRITE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestWritePermission();
        } else  if (null == savedInstanceState) {
            launchCamera();
        }
    }

    private void launchCamera() {
        CameraController.Callback callback = new CameraController.Callback() {
            @Override
            public void onCameraOpened() {
                Log.i(TAG, "onCameraOpened");
            }

            @Override
            public void onCameraClosed() {
                Log.i(TAG, "onCameraClosed");
            }

            @Override
            public void onPhotoTaken(byte[] data) {
                Log.i(TAG, "onPhotoTaken data length" + data.length);
            }

            @Override
            public void onBitmapProcessed(Bitmap bitmap) {
                Log.i(TAG, "onBitmapProcessed dimensions" + bitmap.getWidth() + ", " + bitmap.getHeight());
            }
        };
        CameraController.getInstance().launch(this, R.id.container, callback);
    }

    private void requestWritePermission() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        launchCamera();
    }
}
