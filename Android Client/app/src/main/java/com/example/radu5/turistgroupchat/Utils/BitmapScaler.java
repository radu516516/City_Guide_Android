package com.example.radu5.turistgroupchat.Utils;

import android.graphics.Bitmap;

/**
 * Created by radu5 on 4/29/2018.
 */

//todo CLASS TO RESIZE A BITMAP AND PRESERVE ASPECT RATIO
public class BitmapScaler {

    //todo RESIZE BITMAP  Bitmap bMapScaled = Bitmap.createScaledBitmap(bMap, 150, 100, true); (DOESNT KEEP ASPECT RATIO)

    // Scale and maintain aspect ratio given a desired width
    // BitmapScaler.scaleToFitWidth(bitmap, 100);
    public static Bitmap scaleToFitWidth(Bitmap b, int width)
    {
        float factor = width / (float) b.getWidth();
        return Bitmap.createScaledBitmap(b, width, (int) (b.getHeight() * factor), true);
    }


    // Scale and maintain aspect ratio given a desired height
    // BitmapScaler.scaleToFitHeight(bitmap, 100);
    public static Bitmap scaleToFitHeight(Bitmap b, int height)
    {
        float factor = height / (float) b.getHeight();
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factor), height, true);
    }

    //todo EXAMPLE USE (RESIZE IMAGE TO KEEP ASPECT RATIO)

    //todo IN cazul in care imaginea e tot ecranu pe width
    /*// Get height or width of screen at runtime
    int screenWidth = DeviceDimensionsHelper.getDisplayWidth(this);
    // Resize a Bitmap maintaining aspect ratio based on screen width
    BitmapScaler.scaleToFitWidth(bitmap, screenWidth);*/
}
