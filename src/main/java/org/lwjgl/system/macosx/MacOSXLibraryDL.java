/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 * ANDROID STUB - macOS platform not applicable.
 */
package org.lwjgl.system.macosx;

import org.lwjgl.system.*;
import java.nio.ByteBuffer;

/** Stub for Android port. DynamicLinkLoader (macOS) is not available on Android. */
public class MacOSXLibraryDL extends MacOSXLibrary {

    public MacOSXLibraryDL(String name) {
        super(name, 0L);
        throw new UnsupportedOperationException("MacOSXLibraryDL not available on Android");
    }

    public MacOSXLibraryDL(String name, long handle) {
        super(name, handle);
    }

    @Override
    public String getPath() { return SharedLibraryUtil.getLibraryPath(address()); }

    @Override
    public long getFunctionAddress(ByteBuffer functionName) { return 0L; }

    @Override
    public void free() {}
}
