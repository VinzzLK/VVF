/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 * ANDROID REWRITE - removed org.lwjgl.system.libc.LibCString dependency.
 */
package org.lwjgl.system;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.system.Pointer.*;

final class MultiReleaseMemCopy {

    private MultiReleaseMemCopy() {}

    static void copy(long src, long dst, long bytes) {
        if (bytes <= 160) {
            // A custom Java loop is fastest at small sizes, approximately up to 160 bytes.
            if (BITS64 && ((src | dst) & 7) == 0) {
                // both src and dst are aligned to 8 bytes
                for (long i = 0; i < bytes - 7; i += 8) {
                    memPutLong(dst + i, memGetLong(src + i));
                }
                // handle remaining bytes
                int remaining = (int)(bytes & 7);
                for (int i = 0; i < remaining; i++) {
                    memPutByte(dst + bytes - remaining + i, memGetByte(src + bytes - remaining + i));
                }
            } else {
                for (long i = 0; i < bytes; i++) {
                    memPutByte(dst + i, memGetByte(src + i));
                }
            }
        } else {
            // For larger copies, delegate to the JNI memcpy native call.
            nmemcpy(dst, src, bytes);
        }
    }

    /** Native memcpy via JNI - provided by Zalith Launcher's native LWJGL at runtime. */
    private static native void nmemcpy(long dst, long src, long bytes);
}
