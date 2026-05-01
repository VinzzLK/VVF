/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 * ANDROID REWRITE - removed javax.annotation dependency not available in Java 21
 */
package org.lwjgl.system;

import java.lang.annotation.*;

/**
 * Marks all fields, methods, and parameters as @Nonnull by default.
 * Stripped version for Android/Java 21 - javax.annotation.meta not available.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
public @interface NonnullDefault {}
