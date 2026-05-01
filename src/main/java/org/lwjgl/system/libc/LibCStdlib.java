/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 * ANDROID STUB
 */
package org.lwjgl.system.libc;

/** Stub for Android port. */
public class LibCStdlib {

    private LibCStdlib() {}

    public static long nmalloc(long size)                        { throw new UnsupportedOperationException("Not available on Android"); }
    public static long ncalloc(long num, long size)              { throw new UnsupportedOperationException("Not available on Android"); }
    public static long nrealloc(long ptr, long size)             { throw new UnsupportedOperationException("Not available on Android"); }
    public static void nfree(long ptr)                           { throw new UnsupportedOperationException("Not available on Android"); }
    public static long naligned_alloc(long alignment, long size) { throw new UnsupportedOperationException("Not available on Android"); }
    public static void naligned_free(long ptr)                   { throw new UnsupportedOperationException("Not available on Android"); }
}
