package xyz.pinaki.android.camera;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;

/**
 * Created by pinaki on 8/19/17.
 */

abstract class ViewFinderPreview {
    private Callback mCallback;
    private int width;
    private int height;
    protected void setSize(int w, int h) {
        width = w;
        height = h;
    }
    int getWidth() {
        return width;
    }
    int getHeight() {
        return height;
    }

    interface Callback {
        void onSurfaceChanged();
        void onSurfaceDestroyed();
        void onSurfaceCreated();
    }
    ViewFinderPreview(Callback callback) {
        mCallback = callback;
    }
    abstract Surface getSurface();

    SurfaceHolder getSurfaceHolder() {
        return null;
    }
    SurfaceTexture getSurfaceTexture() {
        return null;
    }
    abstract View getView();
    abstract void start();
    abstract void stop();

    abstract Class gePreviewType();

    protected void dispatchSurfaceChanged() {
        mCallback.onSurfaceChanged();
    }

    protected void dispatchSurfaceDestroyed() {
        mCallback.onSurfaceDestroyed();
    }

    protected void dispatchSurfaceCreated() {
        mCallback.onSurfaceCreated();
    }
}
