package xyz.pinaki.android.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

import xyz.pinaki.android.camera.dimension.AspectRatio;

/**
 * Created by pinaki on 9/3/17.
 */

public class AdjustableLayout extends RelativeLayout {
    private static final String TAG = AdjustableLayout.class.getName();
    private ViewFinderPreview viewFinderPreview;
    public AdjustableLayout(Context context) {
        super(context);
    }
    public AdjustableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    void setPreview (ViewFinderPreview v) {
        viewFinderPreview = v;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i(TAG, "onMeasure");
        if (isInEditMode()){
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final AspectRatio ratio = BaseCamera.DEFAULT_ASPECT_RATIO; // getAspectRatio(); HACK TODO fix this
        assert ratio != null;
        if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
            int height = (int) (MeasureSpec.getSize(widthMeasureSpec) * ratio.toDouble());
            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
            }
            super.onMeasure(widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        } else if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            int width = (int) (MeasureSpec.getSize(heightMeasureSpec) * ratio.toDouble());
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
        // adjust the surface or texture view views
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        // TODO fix this
//        if (mDisplayOrientationDetector.getLastKnownDisplayOrientation() % 180 == 0) {
//            ratio = ratio.inverse();
//        }
        Log.i(TAG, "onMeasure changing viewFinderPreview");
        if (height < width * ratio.getHeight() / ratio.getWidth()) {
            viewFinderPreview.getView().measure(
                    MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(width * ratio.getHeight() / ratio.getWidth(),
                            MeasureSpec.EXACTLY));
        } else {
            viewFinderPreview.getView().measure(
                    MeasureSpec.makeMeasureSpec(height * ratio.getWidth() / ratio.getHeight(),
                            MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }

    }
}
