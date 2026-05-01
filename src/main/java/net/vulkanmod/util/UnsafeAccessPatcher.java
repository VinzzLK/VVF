package net.vulkanmod.util;

import java.lang.reflect.Field;

/**
 * Global Unsafe access patcher for LWJGL compatibility.
 * This class attempts to patch sun.misc.Unsafe field access at the JVM level
 * to work around issues on Android Java 21 where internal field access is restricted.
 * 
 * This should be called very early in the mod initialization process.
 */
public class UnsafeAccessPatcher {

    private static volatile boolean patched = false;

    /**
     * Attempt to patch Unsafe access for LWJGL compatibility.
     * Safe to call multiple times.
     */
    public static void patch() {
        if (patched) {
            return;
        }

        synchronized (UnsafeAccessPatcher.class) {
            if (patched) {
                return;
            }

            try {
                patchUnsafeAccess();
                patched = true;
            } catch (Exception e) {
                System.err.println("Warning: Failed to patch Unsafe access: " + e.getMessage());
                e.printStackTrace();
                // Continue anyway - not a fatal error
            }
        }
    }

    /**
     * Perform the actual Unsafe patching.
     * This makes the Unsafe.UNSAFE field accessible to LWJGL.
     */
    private static void patchUnsafeAccess() throws Exception {
        // Get the Unsafe class
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        
        // Get the UNSAFE field
        Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
        
        // Make it accessible
        unsafeField.setAccessible(true);
        
        // Get the Unsafe instance
        Object unsafe = unsafeField.get(null);
        
        // Verify it's accessible
        if (unsafe != null) {
            System.out.println("Unsafe access patched successfully");
        } else {
            throw new RuntimeException("Unsafe instance is null after patching");
        }
    }

    /**
     * Check if Unsafe patching was successful.
     */
    public static boolean isPatched() {
        return patched;
    }

    /**
     * Manual Unsafe field access for LWJGL structs.
     * Use this when normal sType() calls fail.
     */
    public static void setStructType(Object struct, int sType) throws Exception {
        Class<?> structClass = struct.getClass();
        java.lang.reflect.Method addressMethod = structClass.getMethod("address");
        long address = (long) addressMethod.invoke(struct);

        // Get Unsafe
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Object unsafe = unsafeField.get(null);

        // Get the putInt method
        java.lang.reflect.Method putIntMethod = unsafeClass.getMethod("putInt", long.class, int.class);
        
        // Write the type
        putIntMethod.invoke(unsafe, address, sType);
    }
}
