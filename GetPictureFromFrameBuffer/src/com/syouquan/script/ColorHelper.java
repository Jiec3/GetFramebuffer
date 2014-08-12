
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
        int color = ColorEngine.nativeGetColor(x, y);
        Log.e("test", "color = " + color);
        return color;
    }

}
