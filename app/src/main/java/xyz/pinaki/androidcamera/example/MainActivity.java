package xyz.pinaki.androidcamera.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import xyz.pinaki.androidcamera.Camera;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (null == savedInstanceState) {
            Camera camera = new Camera();
            camera.launch(this, R.id.container);
//            getSupportFragmentManager().beginTransaction().replace(
//                    R.id.container, CameraFragment.newInstance(), "CameraFragment").commit();
        }
    }
}
