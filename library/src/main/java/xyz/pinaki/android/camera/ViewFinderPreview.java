package xyz.pinaki.android.camera;

import android.view.Surface;
import android.view.View;

/**
 * Created by pinaki on 8/19/17.
 */

abstract class ViewFinderPreview {
    private Callback mCallback;
    interface Callback {
        void onSurfaceChanged();
    }
    ViewFinderPreview(Callback callback) {
        mCallback = callback;
    }
    abstract Surface getSurface();

    abstract View getView();

    protected void dispatchSurfaceChanged() {
        mCallback.onSurfaceChanged();
    }
}
