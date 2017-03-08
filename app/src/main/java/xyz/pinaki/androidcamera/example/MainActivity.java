package xyz.pinaki.androidcamera.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import xyz.pinaki.androidcamera.CameraManager;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (null == savedInstanceState) {
            CameraManager.getInstance().launch(this, R.id.container);
        }
    }
}
