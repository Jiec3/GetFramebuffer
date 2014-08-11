
package com.syouquan.script;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * 描述:
 * 描述:如果存在/dev/graphics/fb0目录文件，就直接读取该文件，如果没有该文件就通过（命令行）system/bin/screencap截屏
 * ；一般来说，4.0以下的读取dev/graphics/fb0文件截屏，是4.0以上系统，通过命令行截图
 * 
 * @author ljc
 * @since 2014-7-2 下午12:02:10
 */
public class CaptureScreenShot {

    private static final String FB0_FILE_PATH = "/dev/graphics/fb0";

    private String mSrcFilePath = FB0_FILE_PATH;

    int mWidth; // 屏幕宽（像素，如：480px）

    int mHeight; // 屏幕高（像素，如：800p）

    private int mDeepth;

    private int mPixelformat; // fb0文件中存储的数据的类型，例如BGRA_8888，RGBA_8888

    boolean mIsCapture = false;// 是否正在截图

    private ICaptureScreenshot mICaptureScreenshot;

    private boolean isFixedWidth = false;

    private Context context;

    private Bitmap mBitmap = null;

    private void setICaptureScreenshot(ICaptureScreenshot iCapture) {
        mICaptureScreenshot = iCapture;
    }

    private String[] mCapturePathArray = {
            "/sbin/screencap", "/system/bin/screencap", "/system/xbin/screencap",
            "/data/local/xbin/screencap", "/data/local/bin/screencap", "/system/sd/xbin/screencap"
    };

    public CaptureScreenShot(Context context, ICaptureScreenshot iCapture) {
        init(context, iCapture);
    }

    /**
     * 初始化基本信息
     * 
     * @param activity
     */
    public void init(Context context, ICaptureScreenshot iCapture) {
        this.context = context;
        setICaptureScreenshot(iCapture);

        dm = new DisplayMetrics();
        android.view.Display display = ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        display.getMetrics(dm);

        if (dm.widthPixels > dm.heightPixels) {
            mWidth = dm.heightPixels; // 屏幕宽（像素，如：480px）
            mHeight = dm.widthPixels; // 屏幕高（像素，如：800p）
        } else {
            mWidth = dm.widthPixels; // 屏幕宽（像素，如：480px）
            mHeight = dm.heightPixels; // 屏幕高（像素，如：800p）
        }

        mPixelformat = display.getPixelFormat();
        PixelFormat localPixelFormat1 = new PixelFormat();
        PixelFormat.getPixelFormatInfo(mPixelformat, localPixelFormat1);
        mDeepth = localPixelFormat1.bytesPerPixel;// 位深

        // 初始化原始图片转换成像素的参数
        fbinfo = new FbInfoEntity();
        fbinfo.width = mWidth;
        fbinfo.height = mHeight;

        switch (mPixelformat) {
            case 1: /* RGBA_8888 */
                fbinfo.bpp = 32;
                fbinfo.red_offset = 0;
                fbinfo.red_length = 8;
                fbinfo.green_offset = 8;
                fbinfo.green_length = 8;
                fbinfo.blue_offset = 16;
                fbinfo.blue_length = 8;
                fbinfo.alpha_offset = 24;
                fbinfo.alpha_length = 8;
                break;
            case 2: /* RGBX_8888 */
                fbinfo.bpp = 32;
                fbinfo.red_offset = 0;
                fbinfo.red_length = 8;
                fbinfo.green_offset = 8;
                fbinfo.green_length = 8;
                fbinfo.blue_offset = 16;
                fbinfo.blue_length = 8;
                fbinfo.alpha_offset = 24;
                fbinfo.alpha_length = 0;
                break;
            case 3: /* RGB_888 */
                fbinfo.bpp = 24;
                fbinfo.red_offset = 0;
                fbinfo.red_length = 8;
                fbinfo.green_offset = 8;
                fbinfo.green_length = 8;
                fbinfo.blue_offset = 16;
                fbinfo.blue_length = 8;
                fbinfo.alpha_offset = 24;
                fbinfo.alpha_length = 0;
                break;
            case 4: /* RGB_565 */
                fbinfo.bpp = 16;
                fbinfo.red_offset = 11;
                fbinfo.red_length = 5;
                fbinfo.green_offset = 5;
                fbinfo.green_length = 6;
                fbinfo.blue_offset = 0;
                fbinfo.blue_length = 5;
                fbinfo.alpha_offset = 0;
                fbinfo.alpha_length = 0;
                break;
            case 5: /* BGRA_8888 */
                fbinfo.bpp = 32;
                fbinfo.red_offset = 16;
                fbinfo.red_length = 8;
                fbinfo.green_offset = 8;
                fbinfo.green_length = 8;
                fbinfo.blue_offset = 0;
                fbinfo.blue_length = 8;
                fbinfo.alpha_offset = 24;
                fbinfo.alpha_length = 8;
                break;
            default:
                fbinfo.bpp = 32;
                fbinfo.red_offset = 0;
                fbinfo.red_length = 8;
                fbinfo.green_offset = 8;
                fbinfo.green_length = 8;
                fbinfo.blue_offset = 16;
                fbinfo.blue_length = 8;
                fbinfo.alpha_offset = 24;
                fbinfo.alpha_length = 8;
                break;
        }

    }

    /**
     * 有些手机的fb0中的width不是dm.widthPixels，有些差距，例如我的天语手机的dm.widthPixels =
     * 540,实际上fb0中的width=544
     */
    private void fixWidth() {
        if (isFixedWidth)
            return;
        else
            isFixedWidth = true;
        long fbFilelength = calSrcFileLength();
        if (fbFilelength <= 0) {
            return;
        }
        long perIconSize = mWidth * mHeight * (fbinfo.bpp / 8);
        int iconNum = (int) ((fbFilelength + perIconSize / 2) / perIconSize);
        long perIconSizeFixed = fbFilelength / iconNum;
        int temWidth = (int) (perIconSizeFixed / (mHeight * (fbinfo.bpp / 8)));
        long temPerIconSizeFixed = temWidth * mHeight * (fbinfo.bpp / 8);
        if (temPerIconSizeFixed == perIconSizeFixed)
            mWidth = temWidth;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public int delay;

    public void setDelay(int secondTime) {
        delay = secondTime;
    }

    public void startCaptureScreenShot() {
        if (!exeScreencap()) {
            return;
        }

        mBitmap = null;

        // 下面分三步，第一步从src中取出原始数据，第二步将原始数据转成像素数据，第三步把像素数据转成Bitmap

        byte[] srcDataArray = new byte[mHeight * mWidth * mDeepth];// 存储从fb0中读取出的原始数据

        FileInputStream stream = null;
        DataInputStream dStream = null;
        try {
            // 第一步 从fb0中读取出一帧图片的原始数据
            File fbFile = new File(mSrcFilePath);
            stream = new FileInputStream(fbFile);
            dStream = new DataInputStream(stream);
            dStream.readFully(srcDataArray);

            // 第二步 转换
            int pixelArray[] = srcData2Pixel(srcDataArray, mPixelformat);

            // 第三步 像素 生成bitmap
            if (pixelArray != null) {
                Bitmap bitmap = Bitmap.createBitmap(pixelArray, mWidth, mHeight,
                        Bitmap.Config.ARGB_8888);
                if (dm.widthPixels > dm.heightPixels) {
                    // 横屏的时候
                    Matrix matrix = new Matrix();
                    matrix.reset();
                    matrix.setRotate(270);
                    Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                            bitmap.getHeight(), matrix, true);
                    if (bitmap != null && !bitmap.isRecycled() && bitmap != newbm) {
                        bitmap.recycle();
                        bitmap = null;
                        System.gc();
                    }
                    captureSucceed(newbm);
                    mBitmap = newbm;
                } else {
                    captureSucceed(bitmap);
                    mBitmap = bitmap;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            captureFailed(ScreenshotErrorCode.ERRORCODE_IO);
        } catch (IOException e) {
            e.printStackTrace();
            captureFailed(ScreenshotErrorCode.ERRORCODE_IO);
        } finally {
            try {
                if (stream != null)
                    stream.close();
                if (dStream != null)
                    dStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 保存bitmap到文件
     * 
     * @param bitmap
     * @param bitName
     * @throws IOException
     */
    private void saveMyBitmap(Bitmap bitmap, String bitName) throws IOException {
        File f = new File(bitName);
        f.createNewFile();
        FileOutputStream fOut = new FileOutputStream(f);

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        fOut.flush();
        fOut.close();
    }

    public boolean exeScreencap() {
        Process rootPP = null;
        File file;
        String capturePath = null;
        for (String str : mCapturePathArray) {
            file = new File(str);
            if (file.exists()) {
                capturePath = str;
                break;
            }

        }
        if (capturePath == null) {
            mSrcFilePath = FB0_FILE_PATH;
            if (chmodFb0()) {
                fixWidth();
                return true;
            }
            return false;
        }
        try {
            String cmd = null;
            cmd = capturePath + " " + FB0_FILE_PATH;
            rootPP = Runtime.getRuntime().exec("su", null, null);
            OutputStream os = rootPP.getOutputStream();

            os.write(cmd.getBytes());
            os.flush();
            os.close();
            if (rootPP.waitFor() == 0) {
                return true;
            } else {
                captureFailed(ScreenshotErrorCode.ERRORCODE_NO_ROOT_PERMISSION);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
            captureFailed(ScreenshotErrorCode.ERRORCODE_IO);
        } catch (InterruptedException e) {
            e.printStackTrace();
            captureFailed(ScreenshotErrorCode.ERRORCODE_INTERRUPTED_EXCEPTION);
        } finally {
            if (rootPP != null)
                rootPP.destroy();
            fixWidth();
        }
        return false;
    }

    public boolean chmodFb0() {
        Process rootPP = null;

        int result = -1;
        try {

            rootPP = Runtime.getRuntime().exec("su", null, null);
            OutputStream os = rootPP.getOutputStream();
            File fbFile = new File(FB0_FILE_PATH);
            os.write(("chmod 777 " + fbFile.getAbsolutePath()).getBytes());
            os.flush();
            os.close();
            result = rootPP.waitFor();

            if (result == 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rootPP != null)
                rootPP.destroy();
        }
        captureFailed(ScreenshotErrorCode.ERRORCODE_NO_ROOT_PERMISSION);
        return false;
    }

    public void captureFailed(int errorCode) {
        if (mICaptureScreenshot != null) {
            mICaptureScreenshot.captureFailed(errorCode);
        }
        mIsCapture = false;
    }

    public void captureSucceed(Bitmap bitmap) {
        if (mICaptureScreenshot != null) {
            mICaptureScreenshot.captureSucceed(bitmap);
        }
        mIsCapture = false;
    }

    public Bitmap getShotBitmap() {
        return mBitmap;
    }

    /**
     * 把从fb中读取的原始数据解析成像素数据
     * 
     * @param piex
     * @param pixelformat
     * @return
     */
    private int[] srcData2Pixel(byte[] piex, int pixelformat) {
        // 1byte = 8位
        int itemLength = fbinfo.bpp / 8;
        int count = mWidth * mHeight;
        int colors[] = new int[count];
        int r = 0, g = 0, b = 0, a = 0;
        switch (pixelformat) {
            case 1: /* RGBA_8888 */
                for (int i = 0; i < count; i++) {
                    r = piex[i * itemLength];
                    g = piex[i * itemLength + 1];
                    b = piex[i * itemLength + 2];
                    a = piex[i * itemLength + 3];
                    colors[i] = ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8)
                            | ((b & 0xFF) << 0);
                }
                break;
            case 2: /* RGBX_8888 */
                for (int i = 0; i < count; i++) {
                    r = piex[i * itemLength];
                    g = piex[i * itemLength + 1];
                    b = piex[i * itemLength + 2];
                    a = 0;
                    colors[i] = ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8)
                            | ((b & 0xFF) << 0);
                }
                break;
            case 3: /* RGB_888 */
                for (int i = 0; i < count; i++) {
                    r = piex[i * itemLength];
                    g = piex[i * itemLength + 1];
                    b = piex[i * itemLength + 2];
                    a = 0;
                    colors[i] = ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8)
                            | ((b & 0xFF) << 0);
                }
                break;
            case 4: /* RGB_565 */
                for (int i = 0; i < count; i++) {
                    byte[] srcByte = new byte[itemLength];
                    for (int j = 0; j < itemLength; j++) {
                        srcByte[j] = piex[i * itemLength + j];
                    }

                    r = transfToInt(srcByte, fbinfo.red_length, fbinfo.red_offset);
                    g = transfToInt(srcByte, fbinfo.green_length, fbinfo.green_offset);
                    b = transfToInt(srcByte, fbinfo.blue_length, fbinfo.blue_offset);
                    a = transfToInt(srcByte, fbinfo.alpha_length, fbinfo.alpha_offset);
                    colors[i] = ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8)
                            | ((b & 0xFF) << 0);
                }
                break;
            case 5: /* BGRA_8888 */
                for (int i = 0; i < count; i++) {
                    if (FB0_FILE_PATH.equals(mSrcFilePath)) {
                        r = piex[i * itemLength + 2];
                        g = piex[i * itemLength + 1];
                        b = piex[i * itemLength];
                        a = piex[i * itemLength + 3];
                    } else {
                        r = piex[i * itemLength];
                        g = piex[i * itemLength + 1];
                        b = piex[i * itemLength + 2];
                        a = piex[i * itemLength + 3];
                    }

                    colors[i] = ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8)
                            | ((b & 0xFF) << 0);
                }
                break;
            default:
                for (int i = 0; i < count; i++) {
                    byte[] srcByte = new byte[itemLength];
                    for (int j = 0; j < itemLength; j++) {
                        srcByte[j] = piex[i * itemLength + j];
                    }

                    r = transfToInt(srcByte, fbinfo.red_length, fbinfo.red_offset);
                    g = transfToInt(srcByte, fbinfo.green_length, fbinfo.green_offset);
                    b = transfToInt(srcByte, fbinfo.blue_length, fbinfo.blue_offset);
                    a = transfToInt(srcByte, fbinfo.alpha_length, fbinfo.alpha_offset);
                    colors[i] = ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8)
                            | ((b & 0xFF) << 0);
                }
                break;
        }
        return colors;
    }

    /**
     * 从byte数组中读取出指定位置（单位是位）开始的指定长度（单位是位）的int值
     * 
     * @param srcByte
     * @param bitLength 指定长度
     * @param bitOffset 指定位置开始
     * @return
     */
    public int transfToInt(byte[] srcByte, int bitLength, int bitOffset) {
        int allARGB = byteArray2int(srcByte);
        int rightMove = fbinfo.bpp - bitOffset - bitLength;
        int leftMove = fbinfo.bpp - bitLength;
        int result = (allARGB >> rightMove) << leftMove;
        return result;
    }

    /**
     * 将4字节的byte数组转成一个int值
     * 
     * @param b
     * @return
     */
    private int byteArray2int(byte[] b) {
        byte[] a = new byte[4];
        int i = a.length - 1, j = b.length - 1;
        for (; i >= 0; i--, j--) {// 从b的尾部(即int值的低位)开始copy数据
            if (j >= 0)
                a[i] = b[j];
            else
                a[i] = 0;// 如果b.length不足4,则将高位补0
        }
        int v0 = (a[0] & 0xff) << 24;// &0xff将byte值无差异转成int,避免Java自动类型提升后,会保留高位的符号位
        int v1 = (a[1] & 0xff) << 16;
        int v2 = (a[2] & 0xff) << 8;
        int v3 = (a[3] & 0xff);
        return v0 + v1 + v2 + v3;
    }

    private FbInfoEntity fbinfo;

    private DisplayMetrics dm;

    private int calSrcFileLength() {
        int bytesum = 0;
        try {
            int byteread = 0;
            File fbFile = new File(mSrcFilePath);
            if (fbFile.exists()) { // 文件存在时
                InputStream inStream = new FileInputStream(fbFile); // 读入原文件
                byte[] buffer = new byte[4 * 1024];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; // 字节数 文件大小
                }
                inStream.close();
            } else {
            }
        } catch (Exception e) {
        }
        return bytesum;
    }

    /**
     * 描述:截屏功能的回调接口
     * 
     * @author ljc
     * @since 2014-7-2 下午1:59:54
     */
    public interface ICaptureScreenshot {

        void captureSucceed(Bitmap bitmap);

        /**
         * 截图失败
         * 
         * @param errorCode {@link com.syouquan.utils.CaptureScreenShot.ScreenshotErrorCode}
         */
        void captureFailed(int errorCode);
    }

    /**
     * 描述:截图错误类型
     * 
     * @author ljc
     * @since 2014-7-2 下午3:36:17
     */
    public final class ScreenshotErrorCode {
        /** 未取得权限错误 */
        public static final int ERRORCODE_NO_ROOT_PERMISSION = 1;

        /** 获取权限时，线程被打断 */
        public static final int ERRORCODE_INTERRUPTED_EXCEPTION = 2;

        /** IO错误 */
        public static final int ERRORCODE_IO = 3;

        /** 未知错误 */
        public static final int ERRORCODE_UNKNOW = 4;
    }
}
