/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 * ANDROID STUB - libffi not available on Android.
 */
package org.lwjgl.system.libffi;

import java.nio.ByteBuffer;

/** Stub for Android port. libffi is not used at runtime. */
public class FFIType {

    public static final int SIZEOF = 16;

    private final long address;
    private long   size;
    private short  alignment;
    private short  type;

    private FFIType(long address) {
        this.address = address;
    }

    public long address() {
        return address;
    }

    public long size()          { return size; }
    public short alignment()    { return alignment; }
    public short type()         { return type; }

    public FFIType size(long size)          { this.size = size; return this; }
    public FFIType alignment(short align)   { this.alignment = align; return this; }
    public FFIType type(short type)         { this.type = type; return this; }
    public FFIType elements(org.lwjgl.PointerBuffer buf) { return this; }

    public static FFIType create(long address) {
        return new FFIType(address);
    }
}
