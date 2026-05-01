/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 * ANDROID REWRITE - removed libffi and JNINativeInterface; not available on Android.
 */
package org.lwjgl.system;

import org.lwjgl.*;
import org.lwjgl.system.libffi.*;

import java.lang.reflect.*;

import static org.lwjgl.system.APIUtil.*;
import static org.lwjgl.system.Checks.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Base class for dynamically created native functions that call into Java code.
 * ANDROID STUB: Callback creation via libffi is not supported on Android.
 * Zalith Launcher provides its own runtime implementation.
 */
public abstract class Callback implements Pointer, NativeResource {

    private long address;

    /**
     * Creates a callback instance using the specified libffi CIF.
     * On Android this always throws UnsupportedOperationException.
     */
    protected Callback(FFICIF cif) {
        throw new UnsupportedOperationException("Callback via libffi not supported on Android");
    }

    /**
     * Creates a callback instance using the specified function address.
     */
    protected Callback(long address) {
        if (CHECKS) {
            check(address);
        }
        this.address = address;
    }

    @Override
    public long address() {
        return address;
    }

    @Override
    public void free() {
        free(address());
    }

    private static native long getCallbackHandler(Method callback);

    /**
     * Creates a native function that delegates to the specified instance when called.
     * ANDROID STUB: throws UnsupportedOperationException at runtime.
     */
    static long create(FFICIF cif, Object instance) {
        throw new UnsupportedOperationException("Callback via libffi not supported on Android");
    }

    public static <T extends CallbackI> T get(long functionPointer) {
        throw new UnsupportedOperationException("Callback not supported on Android");
    }

    public static <T extends CallbackI> T getSafe(long functionPointer) {
        return functionPointer == NULL ? null : get(functionPointer);
    }

    public static void free(long functionPointer) {
        // no-op stub; Zalith Launcher handles cleanup at runtime
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Callback)) return false;
        return address == ((Callback) o).address();
    }

    @Override
    public int hashCode() {
        return (int) (address ^ (address >>> 32));
    }

    @Override
    public String toString() {
        return String.format("%s pointer [0x%X]", getClass().getSimpleName(), address);
    }
}
