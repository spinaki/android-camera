package xyz.pinaki.android.camera.dimension;

import android.support.annotation.NonNull;

/**
 * Created by pinaki on 8/21/17.
 */

public final class AspectRatio implements Comparable<AspectRatio> {
    private final int width;
    private final int height;
    private AspectRatio(int w, int h) {
        width = w;
        height = h;
    }
    public static AspectRatio of(int w, int h) {
        int gcd = gcd(w, h);
        return new AspectRatio(w/gcd, h/gcd);
    }

    private static int gcd(int a, int b) {
        while (b != 0) {
            int c = b;
            b = a % b;
            a = c;
        }
        return a;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public AspectRatio inverse() {
        //noinspection SuspiciousNameCombination
        return AspectRatio.of(height, width);
    }

    @Override
    public int compareTo(@NonNull AspectRatio o) {
        if (equals(o)) {
            return 0;
        }
        return toDouble() > o.toDouble() ? 1 : -1;
    }

    public double toDouble() {
        return  (double) width / height;
    }

    @Override
    public int hashCode() {
        // assuming most sizes are <2^16, doing a rotate will give us perfect hashing
        return height ^ ((width << (Integer.SIZE / 2)) | (width >>> (Integer.SIZE / 2)));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof AspectRatio)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        AspectRatio a = (AspectRatio)o;
        return a.width == width && a.height == height;
    }

    @Override
    public String toString() {
        return width + "/" + height;
    }
}
