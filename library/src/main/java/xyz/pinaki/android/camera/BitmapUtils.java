package xyz.pinaki.android.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/* package */ class BitmapUtils {
    // ref - https://developer.android.com/training/displaying-bitmaps/load-bitmap.html
    /* package */ static Bitmap createSampledBitmapFromBytes(byte[] jpegByteArray, int maxDimensionSize) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(jpegByteArray, 0, jpegByteArray.length, options);
        double aspectRatio = options.outWidth * 1.0 / options.outHeight ;
        // compute the target width and height
        int targetWidth = 0 ; int targetHeight = 0;
        if (options.outWidth > options.outHeight) {
            targetWidth = maxDimensionSize;
            targetHeight =  (int) (targetWidth / aspectRatio);
        } else {
            targetHeight = maxDimensionSize;
            targetWidth = (int) (targetHeight * aspectRatio);
        }
        options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);
        options.inJustDecodeBounds = false;
        Bitmap bitmap =  BitmapFactory.decodeByteArray(jpegByteArray, 0, jpegByteArray.length, options);
        return bitmap;
    }

    /**
     * calculates the feasible size for the bitmap.
     * https://developer.android.com/training/displaying-bitmaps/load-bitmap.html
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize val8ue that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        // A power of two value is calculated because the decoder uses a final value by rounding down to the nearest
        // power of two, as per the inSampleSize documentation.
        return inSampleSize;
    }
}
