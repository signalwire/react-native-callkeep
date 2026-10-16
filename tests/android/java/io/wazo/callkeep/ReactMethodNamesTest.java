package io.wazo.callkeep;

import com.facebook.react.bridge.ReactMethod;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * The New Architecture's interop layer refuses a module that exports two
 * {@code @ReactMethod}s with one name (overloads), at the first
 * {@code NativeModules.RNCallKeep} access. Reads the annotations without
 * initialising the class (android.jar is a stub).
 */
public class ReactMethodNamesTest {
    public static void main(String[] args) throws Exception {
        Class<?> module = Class.forName("io.wazo.callkeep.RNCallKeepModule", false,
                ReactMethodNamesTest.class.getClassLoader());
        Set<String> names = new HashSet<>();
        int count = 0;
        for (Method method : module.getDeclaredMethods()) {
            if (method.getAnnotation(ReactMethod.class) == null) {
                continue;
            }
            count++;
            if (!names.add(method.getName())) {
                throw new AssertionError("two @ReactMethods named " + method.getName());
            }
        }
        if (count == 0) {
            throw new AssertionError("no @ReactMethod found");
        }
        System.out.println("ReactMethodNamesTest: " + count + " @ReactMethods, names unique");
    }
}
