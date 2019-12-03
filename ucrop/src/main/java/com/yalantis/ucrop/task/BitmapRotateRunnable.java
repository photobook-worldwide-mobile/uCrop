package com.yalantis.ucrop.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import com.yalantis.ucrop.callback.BitmapRotateCallback;
import com.yalantis.ucrop.util.ImageHeaderParser;

import java.io.File;
import java.io.IOException;

/**
 * @author azriazmi
 * <p>
 * First image is downscaled if max size was set and if resulting image is larger that max size.
 * Image is then rotated accordingly.
 * Finally a new Bitmap object is created and saved to file.
 */
public class BitmapRotateRunnable implements Runnable {

    private static final String TAG = "BitmapCropTask";

    static {
        System.loadLibrary("ucrop");
    }

    private final Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;
    private final int compressionQuality = 85;

    private final String mImageInputPath, mImageOutputPath;
    private final BitmapRotateCallback mRotateCallback;

    private float mCurrentScale = 1f;
    private float mCurrentAngle;
    private int mRotatedImageWidth, mRotatedImageHeight;
    private final int mMaxResultImageSizeX, mMaxResultImageSizeY;

    @SuppressWarnings("SuspiciousNameCombination")
    public BitmapRotateRunnable(
            int imageWidth,
            int imageHeight,
            float rotationAngle,
            int maxResultImageSizeX,
            int maxResultImageSizeY,
            @NonNull String imageInputPath,
            @NonNull String imageOutputPath,
            @Nullable BitmapRotateCallback cropCallback
    ) {
        mCurrentAngle = rotationAngle;
        if (mCurrentAngle == 90 || mCurrentAngle == 270) {
            // swap for rotation
            mRotatedImageWidth = imageHeight;
            mRotatedImageHeight = imageWidth;
        } else {
            mRotatedImageWidth = imageWidth;
            mRotatedImageHeight = imageHeight;
        }

        mMaxResultImageSizeX = maxResultImageSizeX;
        mMaxResultImageSizeY = maxResultImageSizeY;

        mImageInputPath = imageInputPath;
        mImageOutputPath = imageOutputPath;

        mRotateCallback = cropCallback;
    }

    private float getResizeScale() {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mImageInputPath, options);

        float resizeScale = 1;
        if (mMaxResultImageSizeX > 0 && mMaxResultImageSizeY > 0) {

            if (mRotatedImageWidth > mMaxResultImageSizeX || mRotatedImageHeight > mMaxResultImageSizeY) {

                float scaleX = mMaxResultImageSizeX / (float) mRotatedImageWidth;
                float scaleY = mMaxResultImageSizeY / (float) mRotatedImageHeight;
                resizeScale = Math.min(scaleX, scaleY);

                mCurrentScale /= resizeScale;
            }
        }
        return resizeScale;
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean rotate(float resizeScale) throws IOException {
        ExifInterface originalExif = new ExifInterface(mImageInputPath);

        mRotatedImageWidth = Math.round(mRotatedImageWidth / mCurrentScale);
        mRotatedImageHeight = Math.round(mRotatedImageHeight / mCurrentScale);

        Log.d(TAG, "Creating rotated image with width " + mRotatedImageWidth + " and height " + mRotatedImageHeight);

        boolean cropped = BitmapCropTask.cropCImg(mImageInputPath, mImageOutputPath,
                0, 0, mRotatedImageWidth, mRotatedImageHeight,
                mCurrentAngle, resizeScale, mCompressFormat.ordinal(), compressionQuality,
                0, 1);

        if (cropped && mCompressFormat.equals(Bitmap.CompressFormat.JPEG)) {
            ImageHeaderParser.copyExif(originalExif, mRotatedImageWidth, mRotatedImageHeight, mImageOutputPath);
        }
        return cropped;
    }

    @Override
    public void run() {
        float resizeScale = getResizeScale();

        try {
            rotate(resizeScale);
            if (mRotateCallback != null) {
                Uri uri = Uri.fromFile(new File(mImageOutputPath));
                mRotateCallback.onBitmapRotated(mImageOutputPath, uri, mRotatedImageWidth, mRotatedImageHeight);
            }
        } catch (Throwable throwable) {
            if (mRotateCallback != null) mRotateCallback.onRotateFailure(throwable);
        }
    }
}
