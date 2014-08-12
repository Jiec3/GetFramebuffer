
package com.syouquan.script;


public class ColorEngine {
    public static native int nativeScreenShot(int width, int height, String fileName);

    public static native int nativeGetColor(int pointx, int pointy);
}
