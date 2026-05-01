/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 * ANDROID STUB - macOS platform not applicable.
 */
package org.lwjgl.system.macosx;

import org.lwjgl.system.*;
import java.nio.ByteBuffer;

/** Stub for Android port. CoreFoundation is not available on Android. */
public class MacOSXLibraryBundle extends MacOSXLibrary {

    public MacOSXLibraryBundle(String name, long bundleRef) {
        super(name, bundleRef);
    }

    public static MacOSXLibraryBundle getWithIdentifier(String bundleID) {
        throw new UnsupportedOperationException("MacOSXLibraryBundle not available on Android");
    }

    public static MacOSXLibraryBundle create(String path) {
        throw new UnsupportedOperationException("MacOSXLibraryBundle not available on Android");
    }

    @Override
    public String getPath() { return null; }

    @Override
    public long getFunctionAddress(ByteBuffer functionName) { return 0L; }

    @Override
    public void free() {}
}
