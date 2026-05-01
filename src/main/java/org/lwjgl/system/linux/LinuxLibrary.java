/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 * ANDROID STUB - Linux platform library replaced by Zalith Launcher native runtime.
 */
package org.lwjgl.system.linux;

import org.lwjgl.*;
import org.lwjgl.system.*;
import java.nio.ByteBuffer;

/** Stub for Android port. DynamicLinkLoader is provided by Zalith Launcher's native LWJGL at runtime. */
public class LinuxLibrary extends SharedLibrary.Default {

    public LinuxLibrary(String name) {
        super(name, 0L);
        throw new UnsupportedOperationException("LinuxLibrary stub - Zalith Launcher handles native loading at runtime");
    }

    public LinuxLibrary(String name, long handle) {
        super(name, handle);
    }

    @Override
    public String getPath() { return SharedLibraryUtil.getLibraryPath(address()); }

    @Override
    public long getFunctionAddress(ByteBuffer functionName) { return 0L; }

    @Override
    public void free() {}
}
