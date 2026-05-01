/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 * ANDROID STUB - libffi not available on Android, stubs for compile-time compatibility only.
 */
package org.lwjgl.system.libffi;

import java.nio.ByteBuffer;

/** Stub for Android port. libffi is not used at runtime; Zalith Launcher handles native calls. */
public class FFICIF {

    public static final int SIZEOF = 8;

    private final long address;

    private FFICIF(long address) {
        this.address = address;
    }

    public long address() {
        return address;
    }

    public static FFICIF create(long address) {
        return new FFICIF(address);
    }

    public static FFICIF calloc(org.lwjgl.system.MemoryStack stack) {
        return new FFICIF(0L);
    }
}
