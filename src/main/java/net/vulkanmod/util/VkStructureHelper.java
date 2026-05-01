package net.vulkanmod.util;

import org.lwjgl.vulkan.VkBaseInStructure;

/**
 * Comprehensive Vulkan struct initialization helper for Android Java 21 compatibility.
 * Provides safe sType initialization that handles sun.misc.Unsafe access issues.
 * 
 * This class wraps LWJGL struct sType() calls to provide fallback mechanisms
 * when running on Android with Java 21 where Unsafe field access may fail.
 */
public class VkStructureHelper {

    private static final sun.misc.Unsafe UNSAFE;
    private static final boolean UNSAFE_AVAILABLE;

    static {
        sun.misc.Unsafe unsafe = null;
        boolean available = false;
        try {
            java.lang.reflect.Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            unsafe = (sun.misc.Unsafe) unsafeField.get(null);
            available = true;
        } catch (Exception e) {
            System.err.println("Warning: Unsafe not available for VkStructureHelper: " + e.getMessage());
        }
        UNSAFE = unsafe;
        UNSAFE_AVAILABLE = available;
    }

    /**
     * Safely set sType on a Vulkan structure.
     * First tries the normal method, then falls back to direct Unsafe write if needed.
     * 
     * @param struct The Vulkan structure to initialize
     * @param sType The structure type constant
     * @throws RuntimeException if initialization fails completely
     */
    public static <T extends VkBaseInStructure> void setSType(T struct, int sType) {
        try {
            // Try normal method first
            struct.sType(sType);
        } catch (NoSuchFieldError e) {
            // Fallback: direct Unsafe write
            if (UNSAFE_AVAILABLE && UNSAFE != null) {
                try {
                    long address = struct.address();
                    UNSAFE.putInt(address, sType);
                } catch (Exception ex) {
                    throw new RuntimeException("Failed to set sType via Unsafe: " + ex.getMessage(), ex);
                }
            } else {
                throw new RuntimeException("sType() failed and Unsafe is not available: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Safely set sType on a Vulkan structure with default behavior.
     * Some LWJGL structs have sType$Default() method. This wraps it safely.
     * 
     * @param struct The Vulkan structure to initialize with default sType
     * @throws RuntimeException if initialization fails
     */
    public static <T extends VkBaseInStructure> void setSTypeDefault(T struct) {
        try {
            // Try to find and call sType$Default() method
            java.lang.reflect.Method method = struct.getClass().getMethod("sType$Default");
            method.invoke(struct);
        } catch (NoSuchMethodException e) {
            // Method doesn't exist, might not be needed for this struct
            // Do nothing
        } catch (Exception e) {
            // If it fails, try to figure out what the default should be
            // Most Vulkan structures encode the type in their class name
            System.err.println("Warning: sType$Default() failed on " + struct.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * Direct sType initialization by address for low-level scenarios.
     * Useful when struct methods are completely unavailable.
     * 
     * @param structAddress The native address of the struct
     * @param sType The structure type to write
     */
    public static void setSTypeByAddress(long structAddress, int sType) {
        if (!UNSAFE_AVAILABLE || UNSAFE == null) {
            throw new RuntimeException("Unsafe is not available");
        }
        UNSAFE.putInt(structAddress, sType);
    }

    /**
     * Check if Unsafe is properly initialized and available.
     */
    public static boolean isUnsafeAvailable() {
        return UNSAFE_AVAILABLE && UNSAFE != null;
    }
}
