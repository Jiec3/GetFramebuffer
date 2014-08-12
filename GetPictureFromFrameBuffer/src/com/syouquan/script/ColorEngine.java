
package com.syouquan.script;

public class ColorEngine {
    /**
     * 截图
     * 
     * @param fileName 截图保存图片的路径
     * @return
     */
    public static native int nativeScreenShot(String fileName);

    /**
     * 获取坐标颜色
     * 
     * @param pointx
     * @param pointy
     * @return
     */
    public static native int nativeGetColor(int pointx, int pointy);
}
