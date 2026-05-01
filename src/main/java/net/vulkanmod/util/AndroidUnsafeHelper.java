package net.vulkanmod.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Android Java 21 compatible Unsafe access helper for LWJGL struct initialization.
 * Handles the sun.misc.Unsafe field access issues that occur on Android with Java 21.
 */
public class AndroidUnsafeHelper {

    private static final Unsafe UNSAFE;
    private static volatile boolean initialized = false;

    static {
        try {
            // Try standard field access first
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            UNSAFE = (Unsafe) unsafeField.get(null);
            initialized = true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Unsafe", e);
        }
    }

    /**
     * Get the Unsafe instance. This is safe to call from any context.
     */
    public static Unsafe getUnsafe() {
        if (!initialized) {
            throw new RuntimeException("Unsafe not initialized");
        }
        return UNSAFE;
    }

    /**
     * Initialize LWJGL struct type field safely.
     * This handles the issue where LWJGL tries to access Unsafe.UNSAFE field directly.
     */
    public static void initStructType(Object struct, int sType) {
        try {
            // For LWJGL structs, the sType is typically at offset 0
            // We use Unsafe to write directly to avoid the field access issue
            long address = getStructAddress(struct);
            if (address != 0) {
                UNSAFE.putInt(address, sType);
            }
        } catch (Exception e) {
            // If Unsafe fails, try reflection on the struct directly
            try {
                Class<?> structClass = struct.getClass();
                Field sTypeField = structClass.getDeclaredField("sType");
                sTypeField.setAccessible(true);
                sTypeField.setInt(struct, sType);
            } catch (Exception ex) {
                // Last resort: use the public setter through exception handling
                throw new RuntimeException("Failed to initialize struct type", ex);
            }
        }
    }

    /**
     * Get the native address of an LWJGL struct.
     */
    public static long getStructAddress(Object struct) {
        try {
            // LWJGL structs typically have an address() method
            if (struct != null) {
                java.lang.reflect.Method addressMethod = struct.getClass().getMethod("address");
                return (long) addressMethod.invoke(struct);
            }
        } catch (Exception e) {
            // Struct might not have address method
        }
        return 0;
    }

    /**
     * Check if Unsafe is properly initialized and accessible.
     */
    public static boolean isUnsafeAvailable() {
        return initialized && UNSAFE != null;
    }
}
