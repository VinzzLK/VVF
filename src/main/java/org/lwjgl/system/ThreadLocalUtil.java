/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 * ANDROID REWRITE - removed org.lwjgl.system.jni.JNINativeInterface dependency.
 */
package org.lwjgl.system;

import org.lwjgl.*;

import static org.lwjgl.system.APIUtil.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.system.Pointer.*;

/** This class supports bindings with thread-local data. [INTERNAL USE ONLY] */
public final class ThreadLocalUtil {

    private ThreadLocalUtil() {}

    /** Returns the pointer size in bytes. */
    public static int tlsGetState(long tls) {
        return (int) memGetAddress(tls);
    }

    public static void tlsSetState(long tls, long value) {
        memPutAddress(tls, value);
    }

    public static void setCapabilities(long tls, long caps) {
        memPutAddress(tls, caps);
    }

    public static long getCapabilities(long tls) {
        return memGetAddress(tls);
    }

    /**
     * Called by LWJGL internally to attach the capabilities pointer to a thread-local slot.
     * On Android, Zalith Launcher's native LWJGL handles this via JNI at runtime.
     */
    public static native void setFunctionMissingAddresses(int index);
}
