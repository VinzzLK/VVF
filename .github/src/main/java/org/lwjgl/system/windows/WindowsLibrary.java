/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 * ANDROID STUB - Windows platform not applicable.
 */
package org.lwjgl.system.windows;

import org.lwjgl.system.*;
import java.nio.ByteBuffer;

/** Stub for Android port. Windows library loading is not available. */
public class WindowsLibrary extends SharedLibrary.Default {

    public static final long HINSTANCE = 0L;

    public WindowsLibrary(String name) {
        super(name, 0L);
        throw new UnsupportedOperationException("WindowsLibrary not available on Android");
    }

    public WindowsLibrary(String name, long handle) {
        super(name, handle);
    }

    @Override
    public String getPath() { return null; }

    @Override
    public long getFunctionAddress(ByteBuffer functionName) { return 0L; }

    @Override
    public void free() {}
}
