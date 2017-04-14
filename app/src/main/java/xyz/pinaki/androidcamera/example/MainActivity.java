package xyz.pinaki.androidcamera.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import xyz.pinaki.android.camera.CameraController;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (null == savedInstanceState) {
            CameraController.getInstance().launch(this, R.id.container);
        }
    }
}
