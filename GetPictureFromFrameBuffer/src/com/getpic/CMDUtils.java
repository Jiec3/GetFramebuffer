
package com.getpic;

import java.io.DataOutputStream;
import java.io.InputStream;

public class CMDUtils {

    private CMDUtils() {
    }

    public static boolean runWithRoot(String command) {
        int result = -1;

        Process process = null;
        DataOutputStream os = null;
        InputStream is = null;
        InputStream es = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            result = process.waitFor();
            is = process.getInputStream();
            es = process.getErrorStream();
        } catch (Exception e) {
            return false;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }

        return result == 1;
    }

    public static boolean runWithoutRoot(String command) {
        int result = -1;

        Process process = null;
        DataOutputStream os = null;
        InputStream is = null;
        InputStream es = null;
        try {
            process = Runtime.getRuntime().exec("sh");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            result = process.waitFor();
            is = process.getInputStream();
            es = process.getErrorStream();
        } catch (Exception e) {
            return false;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }

        return result == 1;
    }
}
