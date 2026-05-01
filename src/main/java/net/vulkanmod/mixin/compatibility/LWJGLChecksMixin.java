package net.vulkanmod.mixin.compatibility;

import org.lwjgl.system.Checks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Disables LWJGL struct validation checks on Android Java 21.
 *
 * Root cause: LWJGL 3.3.3 structs use a static field `UNSAFE` of type
 * sun.misc.Unsafe inside each generated struct class. On Android Java 21
 * with LWJGL 3.3.6-snapshot (used by Zalith), that field does not exist,
 * causing NoSuchFieldError on every getter/setter and inside validate().
 *
 * The safest fix without patching LWJGL bytecode is to force
 * Checks.CHECKS = false so validate() is never called.
 */
@Mixin(value = Checks.class, remap = false)
public class LWJGLChecksMixin {

    /**
     * Force CHECKS to false at class initialization time.
     * This is equivalent to passing -Dorg.lwjgl.util.NoChecks=true
     * but works even when JVM args cannot be changed.
     */
    static {
        try {
            java.lang.reflect.Field checksField = Checks.class.getDeclaredField("CHECKS");
            checksField.setAccessible(true);
            // CHECKS is a final boolean - need Unsafe to override
            java.lang.reflect.Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
            long offset = unsafe.staticFieldOffset(checksField);
            Object base = unsafe.staticFieldBase(checksField);
            unsafe.putBoolean(base, offset, false);
            System.out.println("[VulkanMod] LWJGL Checks.CHECKS disabled for Android Java 21 compatibility");
        } catch (Exception e) {
            System.err.println("[VulkanMod] Warning: Could not disable LWJGL checks: " + e.getMessage());
        }
    }
}
