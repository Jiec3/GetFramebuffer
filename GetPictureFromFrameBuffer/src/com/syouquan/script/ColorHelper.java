
package com.syouquan.script;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;

public class ColorHelper {

    public static String getColor(Context context, int x, int y) {
        return Integer.toHexString(getColorValueByNative(context, x, y));
    }

    public static int[] getColorRGB(Context context, int x, int y) {
        int color = getColorValueByNative(context, x, y);
        int rgb[] = {
                Color.red(color), Color.green(color), Color.blue(color)
        };
        return rgb;
    }

    public static int getColorValueByNative(Context context, int x, int y) {

        DisplayMetrics dm = new DisplayMetrics();
        dm = context.getApplicationContext().getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        int i = ScriptEngine.nativeScreenShot(width, height, "/sdcard/mypi.bmp");
        int color = -1;
        if (i != -1) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream("/sdcard/mypi.bmp"));
                color = bitmap.getPixel(x, y);
                Log.i("test", "color : " + color);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return color;
    }

    public static int getColorValue(Context context, final int x, final int y) {
        int color = -1;
        DisplayMetrics dm = new DisplayMetrics();
        dm = context.getApplicationContext().getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        // int i = ScriptEngine.nativeScreenShot(width, height,
        // "/sdcard/mypic.bmp");
        CaptureScreenShot css = new CaptureScreenShot(context, null);
        css.startCaptureScreenShot();
        return css.getShotBitmap().getPixel(x, y);
    }
}
