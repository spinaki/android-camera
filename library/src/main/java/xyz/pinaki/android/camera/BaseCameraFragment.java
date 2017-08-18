package xyz.pinaki.android.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

/**
 * Created by pinaki on 8/18/17.
 */

public class BaseCameraFragment extends Fragment {
    private static final int REQUEST_CAMERA = 0;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA) {
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission has been granted, preview can be displayed
//                Log.i(TAG, "CAMERA permission has now been granted. Showing preview.");
                // requestPermissionFollowUp();
            } else {
//                Log.i(TAG, "CAMERA permission was NOT granted.");
            }
        }
    }



    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        }
    }

    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            // TODO: Add a DialogFragment to Show the details.
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
