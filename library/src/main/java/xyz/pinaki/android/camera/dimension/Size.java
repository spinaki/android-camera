package xyz.pinaki.android.camera.dimension;

import android.support.annotation.NonNull;

/**
 * Created by pinaki on 8/21/17.
 */

public class Size implements Comparable<Size> {
    final private int width;
    final private int height;
    public Size(int w, int h) {
        width = w;
        height = h;
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }

    @Override
    public int compareTo(@NonNull Size o) {
        if (equals(o)) {
            return 0;
        }
        return width * height > o.width * o.height ? 1 : -1;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Size)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        Size s = (Size) o;
        return s.height == height && s.width == width;
    }

    @Override
    public String toString() {
        return "(" + width + ", " + height + ")";
    }
}
