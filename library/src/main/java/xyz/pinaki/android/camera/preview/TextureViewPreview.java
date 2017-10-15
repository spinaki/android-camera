package xyz.pinaki.android.camera.preview;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by pinaki on 8/19/17.
 */

public final class TextureViewPreview extends ViewFinderPreview {
    private static final String TAG = TextureViewPreview.class.getName();
    private TextureView textureView;
    ViewGroup parentView;
    Context context;
    private int displayOrientation;
    public TextureViewPreview(Context c, ViewGroup parent, Callback callback) {
        super(callback);
        parentView = parent;
        context = c;
    }

    @Override
    public Surface getSurface() {
        return new Surface(textureView.getSurfaceTexture());
    }

    @Override
    public SurfaceTexture getSurfaceTexture() {
        return textureView.getSurfaceTexture();
    }
    @Override
    public View getView() {
        return textureView;
    }

    @Override
    public void start() {
        textureView = new TextureView(context);
        parentView.addView(textureView, 0);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.i(TAG, "TextureViewPreview onSurfaceTextureAvailable");
                setSize(width, height);
//                configureTransform(); // TODO: necessary ?
//                dispatchSurfaceCreated();
                dispatchSurfaceChanged(); // TODO: check ?
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                Log.i(TAG, "TextureViewPreview onSurfaceTextureSizeChanged");
                setSize(width, height);
//                configureTransform();
                // changes is already triggered from onSurfaceTextureAvailable -- so might not be needed here.
                // TODO verify this
//                dispatchSurfaceChanged();
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                setSize(0, 0);
                dispatchSurfaceDestroyed();
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                // do nothing ??
            }
        });

    }

    // only from camera2 ??
//    @Override
//    void setDisplayOrientation(int d) {
//        displayOrientation = d;
//        configureTransform();
//    }

    // does nothing for camera 1 since displayOrientation is not set
    private void configureTransform() {
        Matrix matrix = new Matrix();
        if (displayOrientation % 180 == 90) {
            final int width = getWidth();
            final int height = getHeight();
            // Rotate the camera preview when the screen is landscape.
            matrix.setPolyToPoly(
                    new float[]{
                            0.f, 0.f, // top left
                            width, 0.f, // top right
                            0.f, height, // bottom left
                            width, height, // bottom right
                    }, 0,
                    displayOrientation == 90 ?
                            // Clockwise
                            new float[]{
                                    0.f, height, // top left
                                    0.f, 0.f, // top right
                                    width, height, // bottom left
                                    width, 0.f, // bottom right
                            } : // displayOrientation == 270
                            // Counter-clockwise
                            new float[]{
                                    width, 0.f, // top left
                                    width, height, // top right
                                    0.f, 0.f, // bottom left
                                    0.f, height, // bottom right
                            }, 0,
                    4);
        } else if (displayOrientation == 180) {
            matrix.postRotate(180, getWidth() / 2, getHeight() / 2);
        }
        textureView.setTransform(matrix);
    }

    @Override
    public void stop() {
        parentView.removeView(textureView);
        textureView = null;
    }

    @Override
    public Class gePreviewType() {
        return SurfaceTexture.class;
    }

    // This method is called only from Camera2.
    @Override
    public void setBufferSize(int width, int height) {
        textureView.getSurfaceTexture().setDefaultBufferSize(width, height);
    }

}
