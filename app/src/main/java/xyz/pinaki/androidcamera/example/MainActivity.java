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
import java.util.List;

import xyz.pinaki.android.camera.CameraAPI;
import xyz.pinaki.android.camera.CameraAPIClient;
import xyz.pinaki.android.camera.dimension.AspectRatio;
import xyz.pinaki.android.camera.dimension.Size;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_WRITE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    CameraAPIClient apiClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // write perm to write the image
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE);
        } else  if (null == savedInstanceState) {
            launchCamera();
        }
    }

    private void launchCamera() {
        AspectRatio aspectRatio = AspectRatio.of(4, 3);
        apiClient = new CameraAPIClient.Builder(this).
                previewType(CameraAPI.PreviewType.TEXTURE_VIEW).
                maxSizeSmallerDimPixels(1000).
                desiredAspectRatio(aspectRatio).
                build();
        CameraAPIClient.Callback callback = new CameraAPIClient.Callback() {
            @Override
            public void onCameraOpened() {
                Log.i(TAG, "onCameraOpened");
            }

            @Override
            public void onAspectRatioAvailable(AspectRatio desired, AspectRatio chosen, List<Size> availableSizes) {
                Log.i(TAG, "onAspectRatio: desired "+ desired + ", chosen: " + chosen + ", sizes" + availableSizes);
            }

            @Override
            public void onCameraClosed() {
                Log.i(TAG, "onCameraClosed");
            }

            @Override
            public void onPhotoTaken(byte[] data) {
                Log.i(TAG, "onPhotoTaken data length: " + data.length);
            }

            @Override
            public void onBitmapProcessed(Bitmap bitmap) {
                Log.i(TAG, "onBitmapProcessed dimensions: " + bitmap.getWidth() + ", " + bitmap.getHeight());
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
                    Log.i("STORAGE", image.getAbsolutePath());
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
        apiClient.start(R.id.container, callback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        launchCamera();
    }

    // https://stackoverflow.com/questions/7575921/illegalstateexception-can-not-perform-this-action-after-onsaveinstancestate-wit?rq=1
    // https://stackoverflow.com/questions/7469082/getting-exception-illegalstateexception-can-not-perform-this-action-after-onsa?rq=1
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    @Override
    public void onDestroy() {
        apiClient.stop();
        super.onDestroy();
    }

}
