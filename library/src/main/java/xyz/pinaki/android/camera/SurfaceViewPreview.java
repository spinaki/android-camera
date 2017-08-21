package xyz.pinaki.android.camera;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import xyz.pinaki.androidcamera.R;

/**
 * Created by pinaki on 8/19/17.
 */

final class SurfaceViewPreview extends ViewFinderPreview {
    private static final String TAG = SurfaceViewPreview.class.getName();
    SurfaceView surfaceView;
    SurfaceViewPreview(Context context, ViewGroup parent, Callback callback) {
        super(callback);
        final View view = View.inflate(context, R.layout.surface_view, parent);
        surfaceView = (SurfaceView) view.findViewById(R.id.surface_view);
        final SurfaceHolder holder = surfaceView.getHolder();
        //noinspection deprecation
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder h) {
                Log.i(TAG, "SurfaceViewPreview created");
                dispatchSurfaceCreated();
            }

            @Override
            public void surfaceChanged(SurfaceHolder h, int format, int width, int height) {
                Log.i(TAG, "SurfaceViewPreview changed");
                // TODO: dow we need a setSize ?
//                setSize(width, height);
                if (!ViewCompat.isInLayout(surfaceView)) {
                    dispatchSurfaceChanged();
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder h) {
                Log.i(TAG, "SurfaceViewPreview destroyed");
                dispatchSurfaceDestroyed();
//                setSize(0, 0);
            }
        });
    }

    @Override
    Surface getSurface() {
        return surfaceView.getHolder().getSurface();
    }

    @Override
    SurfaceHolder getSurfaceHolder() {
        return surfaceView.getHolder();
    }

    @Override
    View getView() {
        return null;
    }

    @Override
    Class gePreviewType() {
        return SurfaceHolder.class;
    }
}
