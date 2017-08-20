package xyz.pinaki.android.camera;

import android.view.Surface;
import android.view.TextureView;
import android.view.View;

/**
 * Created by pinaki on 8/19/17.
 */

final class TextureViewPreview extends ViewFinderPreview {
    private TextureView textureView;

    TextureViewPreview(Callback callback) {
        super(callback);
    }

    @Override
    Surface getSurface() {
        return null;
    }

    @Override
    View getView() {
        return null;
    }
}
