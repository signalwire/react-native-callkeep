package io.wazo.callkeep;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * onHostDestroy must not kill the process: the end-call event it posts would
 * never reach JavaScript, and the app's own call leg would stay up. Ending
 * the calls is the default and can be turned off.
 */
public class HostDestroyTest {
    public static void main(String[] args) throws Exception {
        String classFile;
        try (InputStream in = HostDestroyTest.class.getResourceAsStream("/io/wazo/callkeep/RNCallKeepModule.class")) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            for (int n; (n = in.read(buffer)) > 0; ) {
                out.write(buffer, 0, n);
            }
            classFile = new String(out.toByteArray(), StandardCharsets.ISO_8859_1);
        }
        if (classFile.contains("killProcess")) {
            throw new AssertionError("RNCallKeepModule calls Process.killProcess");
        }
        if (!CallKeepSettings.endCallsOnHostDestroy(null)) {
            throw new AssertionError("calls end on host destroy by default");
        }
        if (!CallKeepSettings.endCallsOnHostDestroy(true) || CallKeepSettings.endCallsOnHostDestroy(false)) {
            throw new AssertionError("endCallsOnHostDestroy follows the setting");
        }
        System.out.println("HostDestroyTest: ok");
    }
}
