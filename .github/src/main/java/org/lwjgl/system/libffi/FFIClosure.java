/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 * ANDROID STUB - libffi not available on Android.
 */
package org.lwjgl.system.libffi;

/** Stub for Android port. libffi is not used at runtime. */
public class FFIClosure {

    public static final int SIZEOF = 24;

    private final long address;

    private FFIClosure(long address) {
        this.address = address;
    }

    public long address()    { return address; }
    public long user_data()  { return 0L; }

    public static FFIClosure create(long address) {
        return new FFIClosure(address);
    }
}
