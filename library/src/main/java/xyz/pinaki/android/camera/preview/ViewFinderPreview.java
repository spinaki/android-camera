package xyz.pinaki.android.camera.preview;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;

/**
 * Created by pinaki on 8/19/17.
 */

public abstract class ViewFinderPreview {
    private Callback mCallback;
    private int width;
    private int height;
    protected void setSize(int w, int h) {
        width = w;
        height = h;
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }

    public interface Callback {
        void onSurfaceChanged();
        void onSurfaceDestroyed();
        void onSurfaceCreated();
    }
    public ViewFinderPreview(Callback callback) {
        mCallback = callback;
    }
    public abstract Surface getSurface();

    public SurfaceHolder getSurfaceHolder() {
        return null;
    }
    public SurfaceTexture getSurfaceTexture() {
        return null;
    }
    public abstract View getView();
    public abstract void start();
    public abstract void stop();

    public abstract Class gePreviewType();

    protected void dispatchSurfaceChanged() {
        mCallback.onSurfaceChanged();
    }

    public void setBufferSize(int width, int height) {
    }
    protected void dispatchSurfaceDestroyed() {
        mCallback.onSurfaceDestroyed();
    }

    protected void dispatchSurfaceCreated() {
        mCallback.onSurfaceCreated();
    }
}
