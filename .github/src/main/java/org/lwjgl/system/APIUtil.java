/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 * ANDROID REWRITE - removed libffi, platform-specific, and javax.annotation dependencies
 */
package org.lwjgl.system;

import org.lwjgl.*;
import org.lwjgl.system.libffi.*;

import java.io.*;
import java.lang.reflect.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;

import static org.lwjgl.system.Checks.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.wrap;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Utility class useful to API bindings. [INTERNAL USE ONLY]
 */
public final class APIUtil {

    public static final PrintStream DEBUG_STREAM = getDebugStream();

    private static final Pattern API_VERSION_PATTERN;

    static {
        String PREFIX         = "[^\\d\\n\\r]*";
        String VERSION        = "(\\d+)[.](\\d+)(?:[.](\\S+))?";
        String IMPLEMENTATION = "(?:\\s+(.+?))?\\s*";
        API_VERSION_PATTERN = Pattern.compile("^" + PREFIX + VERSION + IMPLEMENTATION + "$", Pattern.DOTALL);
    }

    @SuppressWarnings({"unchecked", "UseOfSystemOutOrSystemErr"})
    private static PrintStream getDebugStream() {
        PrintStream debugStream = System.err;
        Object state = Configuration.DEBUG_STREAM.get();
        if (state instanceof String) {
            try {
                Supplier<PrintStream> factory = (Supplier<PrintStream>) Class.forName((String) state).getConstructor().newInstance();
                debugStream = factory.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (state instanceof Supplier<?>) {
            debugStream = ((Supplier<PrintStream>) state).get();
        } else if (state instanceof PrintStream) {
            debugStream = (PrintStream) state;
        }
        return debugStream;
    }

    private APIUtil() {}

    public static void apiLog(CharSequence msg) {
        if (DEBUG) {
            DEBUG_STREAM.print("[LWJGL] " + msg + "\n");
        }
    }

    public static void apiLogMore(CharSequence msg) {
        if (DEBUG) {
            DEBUG_STREAM.print("\t" + msg + "\n");
        }
    }

    public static void apiLogMissing(String api, ByteBuffer functionName) {
        if (DEBUG) {
            String function = memASCII(functionName, functionName.remaining() - 1);
            DEBUG_STREAM.print("[LWJGL] Failed to locate address for " + api + " function " + function + "\n");
        }
    }

    public static String apiFindLibrary(String start, String name) {
        String libName = Platform.get().mapLibraryName(name);
        try (Stream<Path> paths = Files.find(
            Paths.get(start).toAbsolutePath(),
            Integer.MAX_VALUE,
            (path, attributes) -> attributes.isRegularFile() && path.getFileName().toString().equals(libName)
        )) {
            return paths.findFirst().map(Path::toString).orElse(name);
        } catch (IOException e) {
            return name;
        }
    }

    public static SharedLibrary apiCreateLibrary(String name) {
        // On Android, Zalith Launcher handles native library loading at runtime.
        throw new UnsupportedOperationException("apiCreateLibrary not supported on Android; use Zalith Launcher runtime.");
    }

    public static long apiGetFunctionAddress(FunctionProvider provider, String functionName) {
        long a = provider.getFunctionAddress(functionName);
        if (a == NULL) {
            requiredFunctionMissing(functionName);
        }
        return a;
    }

    private static void requiredFunctionMissing(String functionName) {
        if (!Configuration.DISABLE_FUNCTION_CHECKS.get(false)) {
            throw new NullPointerException("A required function is missing: " + functionName);
        }
    }

    public static long apiGetFunctionAddressOptional(SharedLibrary library, String functionName) {
        long a = library.getFunctionAddress(functionName);
        if (DEBUG_FUNCTIONS && a == NULL) {
            optionalFunctionMissing(library, functionName);
        }
        return a;
    }

    private static void optionalFunctionMissing(SharedLibrary library, String functionName) {
        if (DEBUG) {
            DEBUG_STREAM.print("[LWJGL] Failed to locate address for " + library.getName() + " function " + functionName + "\n");
        }
    }

    public static ByteBuffer apiGetMappedBuffer(ByteBuffer buffer, long mappedAddress, int capacity) {
        if (buffer != null && memAddress(buffer) == mappedAddress && buffer.capacity() == capacity) {
            return buffer;
        }
        return mappedAddress == NULL ? null : wrap(BUFFER_BYTE, mappedAddress, capacity).order(NATIVE_ORDER);
    }

    public static long apiGetBytes(int elements, int elementShift) {
        return (elements & 0xFFFF_FFFFL) << elementShift;
    }

    public static long apiCheckAllocation(int elements, long bytes, long maxBytes) {
        if (DEBUG) {
            if (elements < 0) {
                throw new IllegalArgumentException("Invalid number of elements");
            }
            if ((maxBytes + Long.MIN_VALUE) < (bytes + Long.MIN_VALUE)) {
                throw new IllegalArgumentException("The request allocation is too large");
            }
        }
        return bytes;
    }

    /** A data class for API versioning information. */
    public static class APIVersion implements Comparable<APIVersion> {

        public final int major;
        public final int minor;
        public final String revision;
        public final String implementation;

        public APIVersion(int major, int minor) {
            this(major, minor, null, null);
        }

        public APIVersion(int major, int minor, String revision, String implementation) {
            this.major = major;
            this.minor = minor;
            this.revision = revision;
            this.implementation = implementation;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(16);
            sb.append(major).append('.').append(minor);
            if (revision != null) sb.append('.').append(revision);
            if (implementation != null) sb.append(" (").append(implementation).append(')');
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof APIVersion)) return false;
            APIVersion that = (APIVersion) o;
            return this.major == that.major && this.minor == that.minor &&
                   Objects.equals(this.revision, that.revision) &&
                   Objects.equals(this.implementation, that.implementation);
        }

        @Override
        public int hashCode() {
            int result = major;
            result = 31 * result + minor;
            result = 31 * result + (revision != null ? revision.hashCode() : 0);
            result = 31 * result + (implementation != null ? implementation.hashCode() : 0);
            return result;
        }

        @Override
        public int compareTo(APIVersion other) {
            if (this.major != other.major) return Integer.compare(this.major, other.major);
            if (this.minor != other.minor) return Integer.compare(this.minor, other.minor);
            return 0;
        }
    }

    public static APIVersion apiParseVersion(Configuration<?> option) {
        Object state = option.get();
        if (state instanceof String)     return apiParseVersion((String) state);
        if (state instanceof APIVersion) return (APIVersion) state;
        return null;
    }

    public static APIVersion apiParseVersion(String version) {
        Matcher matcher = API_VERSION_PATTERN.matcher(version);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(String.format("Malformed API version string [%s]", version));
        }
        return new APIVersion(
            Integer.parseInt(matcher.group(1)),
            Integer.parseInt(matcher.group(2)),
            matcher.group(3),
            matcher.group(4)
        );
    }

    public static void apiFilterExtensions(Set<String> extensions, Configuration<Object> option) {
        Object value = option.get();
        if (value == null) return;
        if (value instanceof String) {
            String s = (String) value;
            if (s.indexOf('.') != -1) {
                try {
                    @SuppressWarnings("unchecked") Predicate<String> predicate =
                        (Predicate<String>) Class.forName(s).newInstance();
                    extensions.removeIf(predicate);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                for (String extension : s.split(",")) {
                    extensions.remove(extension);
                }
            }
        } else if (value instanceof List<?>) {
            @SuppressWarnings("unchecked") List<String> list = (List<String>) value;
            extensions.removeAll(list);
        } else if (value instanceof Predicate<?>) {
            @SuppressWarnings("unchecked") Predicate<String> predicate = (Predicate<String>) value;
            extensions.removeIf(predicate);
        } else {
            throw new IllegalStateException("Unsupported " + option.getProperty() + " value specified.");
        }
    }

    public static String apiUnknownToken(int token) {
        return apiUnknownToken("Unknown", token);
    }

    public static String apiUnknownToken(String description, int token) {
        return String.format("%s [0x%X]", description, token);
    }

    public static Map<Integer, String> apiClassTokens(BiPredicate<Field, Integer> filter,
                                                       Map<Integer, String> target,
                                                       Class<?>... tokenClasses) {
        if (target == null) target = new HashMap<>(64);
        int TOKEN_MODIFIERS = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
        for (Class<?> tokenClass : tokenClasses) {
            if (tokenClass == null) continue;
            for (Field field : tokenClass.getDeclaredFields()) {
                if ((field.getModifiers() & TOKEN_MODIFIERS) == TOKEN_MODIFIERS && field.getType() == int.class) {
                    try {
                        Integer value = field.getInt(null);
                        if (filter != null && !filter.test(field, value)) continue;
                        String name = target.get(value);
                        target.put(value, name == null ? field.getName() : name + "|" + field.getName());
                    } catch (IllegalAccessException e) {
                        // Ignore
                    }
                }
            }
        }
        return target;
    }

    public static long apiArray(MemoryStack stack, long... addresses) {
        PointerBuffer pointers = memPointerBuffer(stack.nmalloc(POINTER_SIZE, addresses.length << POINTER_SHIFT), addresses.length);
        for (long address : addresses) pointers.put(address);
        return pointers.address;
    }

    public static long apiArray(MemoryStack stack, ByteBuffer... buffers) {
        PointerBuffer pointers = memPointerBuffer(stack.nmalloc(POINTER_SIZE, buffers.length << POINTER_SHIFT), buffers.length);
        for (ByteBuffer buffer : buffers) pointers.put(buffer);
        return pointers.address;
    }

    public static long apiArrayp(MemoryStack stack, ByteBuffer... buffers) {
        long pointers = apiArray(stack, buffers);
        PointerBuffer lengths = stack.mallocPointer(buffers.length);
        for (ByteBuffer buffer : buffers) lengths.put(buffer.remaining());
        return pointers;
    }

    public interface Encoder {
        ByteBuffer encode(CharSequence text, boolean nullTerminated);
    }

    public static long apiArray(MemoryStack stack, Encoder encoder, CharSequence... strings) {
        PointerBuffer pointers = stack.mallocPointer(strings.length);
        for (CharSequence s : strings) pointers.put(encoder.encode(s, true));
        return pointers.address;
    }

    public static long apiArrayi(MemoryStack stack, Encoder encoder, CharSequence... strings) {
        PointerBuffer pointers = stack.mallocPointer(strings.length);
        IntBuffer lengths = stack.mallocInt(strings.length);
        for (CharSequence s : strings) {
            ByteBuffer buffer = encoder.encode(s, false);
            pointers.put(buffer);
            lengths.put(buffer.capacity());
        }
        return pointers.address;
    }

    public static long apiArrayp(MemoryStack stack, Encoder encoder, CharSequence... strings) {
        PointerBuffer pointers = stack.mallocPointer(strings.length);
        PointerBuffer lengths  = stack.mallocPointer(strings.length);
        for (CharSequence s : strings) {
            ByteBuffer buffer = encoder.encode(s, false);
            pointers.put(buffer);
            lengths.put(buffer.capacity());
        }
        return pointers.address;
    }

    public static void apiArrayFree(long pointers, int length) {
        for (int i = length; --i >= 0; ) {
            nmemFree(memGetAddress(pointers + Integer.toUnsignedLong(i) * POINTER_SIZE));
        }
    }

    // ----------------------------------------
    // FFI stub methods - not functional on Android, exist for compile compatibility only.
    // At runtime these are never called; Zalith Launcher replaces the entire lwjgl-android module.

    public static FFIType apiCreateStruct(FFIType... members) {
        throw new UnsupportedOperationException("libffi not available on Android");
    }

    public static FFIType apiCreateUnion(FFIType... members) {
        throw new UnsupportedOperationException("libffi not available on Android");
    }

    public static FFIType apiCreateArray(FFIType type, int length) {
        throw new UnsupportedOperationException("libffi not available on Android");
    }

    public static FFICIF apiCreateCIF(int abi, FFIType rtype, FFIType... atypes) {
        throw new UnsupportedOperationException("libffi not available on Android");
    }

    public static FFICIF apiCreateCIFVar(int abi, int nfixedargs, FFIType rtype, FFIType... atypes) {
        throw new UnsupportedOperationException("libffi not available on Android");
    }

    public static int apiStdcall() {
        return LibFFI.FFI_DEFAULT_ABI;
    }

    public static void apiClosureRet(long ret, boolean __result) { memPutAddress(ret, __result ? 1L : 0L); }
    public static void apiClosureRet(long ret, byte __result)    { memPutAddress(ret, __result & 0xFFL); }
    public static void apiClosureRet(long ret, short __result)   { memPutAddress(ret, __result & 0xFFFFL); }
    public static void apiClosureRet(long ret, int __result)     { memPutAddress(ret, __result & 0xFFFF_FFFFL); }
    public static void apiClosureRetL(long ret, long __result)   { memPutLong(ret, __result); }
    public static void apiClosureRetP(long ret, long __result)   { memPutAddress(ret, __result); }
    public static void apiClosureRet(long ret, float __result)   { memPutFloat(ret, __result); }
    public static void apiClosureRet(long ret, double __result)  { memPutDouble(ret, __result); }
}
