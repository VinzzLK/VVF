/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 * ANDROID STUB
 */
package org.lwjgl.system.libc;

/** Stub for Android port. */
public class LibCString {

    private LibCString() {}

    public static long nmemcpy(long dst, long src, long count) {
        throw new UnsupportedOperationException("Not available on Android");
    }

    public static long nmemmove(long dst, long src, long count) {
        throw new UnsupportedOperationException("Not available on Android");
    }

    public static void nmemset(long dst, int value, long count) {
        throw new UnsupportedOperationException("Not available on Android");
    }
}
