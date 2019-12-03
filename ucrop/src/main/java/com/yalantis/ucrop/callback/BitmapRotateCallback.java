package com.yalantis.ucrop.callback;

import android.net.Uri;

import androidx.annotation.NonNull;

public interface BitmapRotateCallback {

    void onBitmapRotated(@NonNull String outputPath, @NonNull Uri resultUri, int imageWidth, int imageHeight);

    void onRotateFailure(@NonNull Throwable t);

}