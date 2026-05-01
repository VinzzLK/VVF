/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 * ANDROID STUB
 */
package org.lwjgl.system.jni;

/** Stub for Android port. JNI interface is handled natively by Zalith Launcher at runtime. */
public class JNINativeInterface {

    private JNINativeInterface() {}

    public static long NewGlobalRef(Object obj) {
        throw new UnsupportedOperationException("Not available on Android");
    }

    public static void DeleteGlobalRef(long globalRef) {
        throw new UnsupportedOperationException("Not available on Android");
    }

    public static <T> T memGlobalRefToObject(long globalRef) {
        throw new UnsupportedOperationException("Not available on Android");
    }
}
