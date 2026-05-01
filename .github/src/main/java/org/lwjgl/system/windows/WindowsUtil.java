/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 * ANDROID STUB
 */
package org.lwjgl.system.windows;

/** Stub for Android port. */
public final class WindowsUtil {

    private WindowsUtil() {}

    public static void windowsThrowException(String msg) {
        throw new UnsupportedOperationException("Windows not available on Android: " + msg);
    }
}
