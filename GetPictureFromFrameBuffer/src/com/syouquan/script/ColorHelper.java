
package com.syouquan.script;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

public class ColorHelper {

    private static final String TEMP_PATH = "/sdcard/mypi.bmp";

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
        // DisplayMetrics dm = new DisplayMetrics();
        // dm =
        // context.getApplicationContext().getResources().getDisplayMetrics();
        // int width = dm.widthPixels;
        // int height = dm.heightPixels;

        // int i = ScriptEngine.nativeScreenShot(width, height, TEMP_PATH);
        // int color = -1;
        // if (i != -1) {
        // try {
        // Bitmap bitmap = BitmapFactory.decodeStream(new
        // FileInputStream(TEMP_PATH));
        // color = bitmap.getPixel(x, y);
        // Log.i("test", "color : " + color);
        // } catch (FileNotFoundException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // }
        int color = ScriptEngine.nativeScreenShot(x, y, TEMP_PATH);
        Log.e("test", "color = " + color);
        return color;
    }

    public static int getColorValue(Context context, final int x, final int y) {
        CaptureScreenShot css = new CaptureScreenShot(context, null);
        css.startCaptureScreenShot();
        return css.getShotBitmap().getPixel(x, y);
    }
}
