/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 * ANDROID REWRITE - removed libffi dependency.
 */
package org.lwjgl.system;

import org.lwjgl.system.libffi.*;

/**
 * Interface for dynamically created native functions that call into Java code (upcalls).
 * ANDROID STUB: libffi callbacks are not supported at runtime on Android.
 */
public interface CallbackI extends Pointer {

    /**
     * Returns the libffi Call Interface for this callback function.
     * ANDROID STUB: returns null; not called on Android.
     */
    FFICIF getCallInterface();

    @Override
    default long address() {
        return Callback.create(getCallInterface(), this);
    }

    /**
     * The Java method that will be called from native code when the native callback function is invoked.
     */
    void callback(long ret, long args);
}
