
package com.getpic;

import java.io.DataOutputStream;

public class CMDUtils {

    private CMDUtils() {
    }

    public static boolean runWithRoot(String command) {
        int result = -1;

        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            result = process.waitFor();
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
