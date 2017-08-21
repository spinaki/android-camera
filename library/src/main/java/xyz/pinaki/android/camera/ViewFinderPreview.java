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
