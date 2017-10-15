package xyz.pinaki.android.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import xyz.pinaki.android.camera.dimension.AspectRatio;
import xyz.pinaki.android.camera.preview.ViewFinderPreview;

/**
 * Created by pinaki on 9/3/17.
 */

public class AdjustableLayout extends FrameLayout {
    private static final String TAG = AdjustableLayout.class.getName();
    private ViewFinderPreview viewFinderPreview;
    private AspectRatio aspectRatio;
    private int displayOrientation;
    public AdjustableLayout(Context context) {
        super(context);
    }
    public AdjustableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    void setPreview (ViewFinderPreview v) {
        viewFinderPreview = v;
    }
    void setAspectRatio(AspectRatio a) {
        aspectRatio = a;
    }
    void setDisplayOrientation(int orientation) {
        displayOrientation = orientation;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i(TAG, "onMeasure");
        if (isInEditMode()){
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        if (aspectRatio == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
            int height = (int) (MeasureSpec.getSize(widthMeasureSpec) * aspectRatio.toDouble());
            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
            }
            super.onMeasure(widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        } else if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            int width = (int) (MeasureSpec.getSize(heightMeasureSpec) * aspectRatio.toDouble());
            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(width, MeasureSpec.getSize(widthMeasureSpec));
            }
            super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    heightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        if (viewFinderPreview == null) {
            return;
        }
        Log.i(TAG, "onMeasure changing viewFinderPreview: " + aspectRatio.toString());
        // adjust the surface or texture view views
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        AspectRatio previewAspectRatio = aspectRatio;
        if (displayOrientation % 180 == 0) {
            previewAspectRatio = aspectRatio.inverse();
        }
        if (height < width * previewAspectRatio.getHeight() / previewAspectRatio.getWidth()) {
            viewFinderPreview.getView().measure(
                    MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(width * previewAspectRatio.getHeight() / previewAspectRatio.getWidth(),
                            MeasureSpec.EXACTLY));
        } else {
            viewFinderPreview.getView().measure(
                    MeasureSpec.makeMeasureSpec(height * previewAspectRatio.getWidth() / previewAspectRatio.getHeight(),
                            MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }

    }
}
