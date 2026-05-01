/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 * ANDROID STUB - libffi not available on Android.
 */
package org.lwjgl.system.libffi;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

/**
 * Stub for Android port.
 * At runtime Zalith Launcher provides its own native LWJGL Android that replaces these.
 * This class exists only so javac can compile VulkanMod successfully.
 */
public class LibFFI {

    // FFI ABI constants
    public static final int FFI_DEFAULT_ABI = 1;
    public static final int FFI_STDCALL     = 5;

    // FFI type constants
    public static final int FFI_TYPE_VOID       = 0;
    public static final int FFI_TYPE_INT        = 1;
    public static final int FFI_TYPE_FLOAT      = 2;
    public static final int FFI_TYPE_DOUBLE     = 3;
    public static final int FFI_TYPE_UINT8      = 5;
    public static final int FFI_TYPE_SINT8      = 6;
    public static final int FFI_TYPE_UINT16     = 7;
    public static final int FFI_TYPE_SINT16     = 8;
    public static final int FFI_TYPE_UINT32     = 9;
    public static final int FFI_TYPE_SINT32     = 10;
    public static final int FFI_TYPE_UINT64     = 11;
    public static final int FFI_TYPE_SINT64     = 12;
    public static final int FFI_TYPE_STRUCT     = 13;
    public static final int FFI_TYPE_POINTER    = 14;

    // FFI result constants
    public static final int FFI_OK             = 0;
    public static final int FFI_BAD_TYPEDEF    = 1;
    public static final int FFI_BAD_ABI        = 2;

    // Predefined FFI types (stubs - zero address, never called natively on Android)
    public static final FFIType ffi_type_void    = FFIType.create(0L);
    public static final FFIType ffi_type_uint8   = FFIType.create(0L);
    public static final FFIType ffi_type_sint8   = FFIType.create(0L);
    public static final FFIType ffi_type_uint16  = FFIType.create(0L);
    public static final FFIType ffi_type_sint16  = FFIType.create(0L);
    public static final FFIType ffi_type_uint32  = FFIType.create(0L);
    public static final FFIType ffi_type_sint32  = FFIType.create(0L);
    public static final FFIType ffi_type_uint64  = FFIType.create(0L);
    public static final FFIType ffi_type_sint64  = FFIType.create(0L);
    public static final FFIType ffi_type_float   = FFIType.create(0L);
    public static final FFIType ffi_type_double  = FFIType.create(0L);
    public static final FFIType ffi_type_pointer = FFIType.create(0L);

    private LibFFI() {}

    public static int ffi_prep_cif(FFICIF cif, int abi, FFIType rtype, PointerBuffer atypes) {
        return FFI_OK;
    }

    public static int ffi_prep_cif(FFICIF cif, int abi, FFIType rtype, FFIType atypes) {
        return FFI_OK;
    }

    public static int ffi_prep_cif_var(FFICIF cif, int abi, int nfixedargs, FFIType rtype, PointerBuffer atypes) {
        return FFI_OK;
    }

    public static void ffi_call(FFICIF cif, long fn, long rvalue, PointerBuffer avalues) {
        throw new UnsupportedOperationException("libffi not available on Android");
    }

    public static FFIClosure ffi_closure_alloc(long size, PointerBuffer code) {
        throw new UnsupportedOperationException("libffi not available on Android");
    }

    public static void ffi_closure_free(FFIClosure closure) {
        throw new UnsupportedOperationException("libffi not available on Android");
    }

    public static int ffi_prep_closure_loc(FFIClosure closure, FFICIF cif, long fun, long user_data, long codeloc) {
        throw new UnsupportedOperationException("libffi not available on Android");
    }
}
