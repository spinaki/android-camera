package xyz.pinaki.android.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * BitmapUtils provides all the util function required for bitmap modifications.
 *
 * @author Rekha
 */
/* package */ class BitmapUtils {
    private static final int JPEG_COMPRESSION_QUALITY = 70;
    private static final String TAG = BitmapUtils.class.getSimpleName();

    public static GradientDrawable getRoundedRectangleShape(Context context, int color, boolean isSelected) {
        GradientDrawable shape = new GradientDrawable();
        float cornerRadius = 30.0f;
        if (isSelected) {
            shape.setShape(GradientDrawable.RECTANGLE);
            int radius = (int) (cornerRadius * context.getResources().getDisplayMetrics().density);
            shape.setCornerRadius(radius);
            shape.setColor(color);
        } else {
            shape.setColor(Color.TRANSPARENT);
        }
        return shape;
    }

    // to find the correct file via adb pull: http://stackoverflow.com/questions/15641848/cannot-find-storage-emulated-0-folder-of-nexus-7-in-eclipse
    /* package */ static String compressedImageFromBitmap(Bitmap bitmap, String fileName) {
        try {
            File file = createTempFile(fileName);
            if (file == null) {
                return null;
            }
            Log.i(TAG, file.getAbsolutePath());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_COMPRESSION_QUALITY, bos);
            byte[] bitmapData = bos.toByteArray();
            //write the bytes in file
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(bitmapData);
            fileOutputStream.flush();
            fileOutputStream.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static File createTempFile(String fileName) {
        try {
            File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/pinaki_camera");
            folder.mkdirs();
            if (TextUtils.isEmpty(fileName)) {
                fileName = "tmp_img123.jpeg";
            }

            File file = new File(folder, fileName);
            if (file.exists()) {
                file.delete();
            }

            if (file.createNewFile()) {
                file.createNewFile();
            }

            return file;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ref - https://developer.android.com/training/displaying-bitmaps/load-bitmap.html
    /* package */ static Bitmap createSampledBitmapFromBytes(byte[] jpegByteArray, int maxDimensionSize) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(jpegByteArray, 0, jpegByteArray.length, options);
        Log.i("CAMERA-Bitmap", "Original width, height: " + options.outWidth + ", " + options.outHeight);
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
        Log.i("CAMERA-Bitmap", "target width, height: " + targetWidth + ", " + targetHeight);
        options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);
        Log.i("CAMERA-Bitmap", "sample size: " + options.inSampleSize);
        options.inJustDecodeBounds = false;
        Bitmap bitmap =  BitmapFactory.decodeByteArray(jpegByteArray, 0, jpegByteArray.length, options);
        Log.i("CAMERA-Bitmap", "Changed width, height: " + options.outWidth + ", " + options.outHeight);
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
