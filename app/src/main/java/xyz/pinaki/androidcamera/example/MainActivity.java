package xyz.pinaki.androidcamera.example;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
                // save the image for testing
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_";
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                FileOutputStream fos = null;


                try {
                    File image = File.createTempFile(
                            imageFileName,  /* prefix */
                            ".jpg",         /* suffix */
                            storageDir      /* directory */
                    );
                    Log.i("PIANKI-STORAGE", image.getAbsolutePath());
                    fos = new FileOutputStream(image);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

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

    @Override
    public void onDestroy() {
        CameraController.getInstance().stop();
        super.onDestroy();
    }

}
