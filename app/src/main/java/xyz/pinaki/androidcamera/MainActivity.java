package xyz.pinaki.androidcamera;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final CameraFragment cameraFragment = CameraFragment.newInstance();
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction().replace(
                    R.id.container, cameraFragment).commit();
        }
        View shutterIcon = findViewById(R.id.shutter);
        shutterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraFragment.takePicture();
            }
        });
        final View previewContainer = findViewById(R.id.preview_container);
        final ImageView previewImage = (ImageView) findViewById(R.id.preview_image);
        cameraFragment.setCameraCallback(new CameraCallback() {
            @Override
            public void onPictureTaken(Bitmap bitmap) {
                previewContainer.setVisibility(View.VISIBLE);
                previewImage.setImageBitmap(bitmap);
            }
        });
        final ImageView previewCloseButton = (ImageView) findViewById(R.id.preview_close_icon);
        previewCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previewContainer.setVisibility(View.INVISIBLE);
            }
        });

    }
}
