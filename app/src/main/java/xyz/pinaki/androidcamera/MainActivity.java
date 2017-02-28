package xyz.pinaki.androidcamera;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

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
    }
}
