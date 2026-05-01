/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 * ANDROID REWRITE - removed libffi, libc, and javax.annotation dependencies.
 */
package org.lwjgl.system;

import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import static org.lwjgl.system.APIUtil.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.system.StackWalkUtil.*;

/** Provides {@link MemoryAllocator} implementations for {@link MemoryUtil} to use. */
final class MemoryManage {

    private MemoryManage() {}

    static MemoryAllocator getInstance() {
        Object allocator = Configuration.MEMORY_ALLOCATOR.get();
        if (allocator instanceof MemoryAllocator) {
            return (MemoryAllocator) allocator;
        }

        if (!"system".equals(allocator)) {
            String className;
            if (allocator == null || "jemalloc".equals(allocator)) {
                className = "org.lwjgl.system.jemalloc.JEmallocAllocator";
            } else if ("rpmalloc".equals(allocator)) {
                className = "org.lwjgl.system.rpmalloc.RPmallocAllocator";
            } else {
                className = allocator.toString();
            }

            try {
                Class<?> allocatorClass = Class.forName(className);
                return (MemoryAllocator) allocatorClass.getConstructor().newInstance();
            } catch (Throwable t) {
                if (Checks.DEBUG && allocator != null) {
                    t.printStackTrace(DEBUG_STREAM);
                }
                apiLog(String.format("Warning: Failed to instantiate memory allocator: %s. Using the system default.", className));
            }
        }

        return new StdlibAllocator();
    }

    /** stdlib memory allocator. Uses JNI-backed native methods provided by Zalith Launcher runtime. */
    private static class StdlibAllocator implements MemoryAllocator {

        @Override public long getMalloc()                              { return MemoryAccessJNI.malloc; }
        @Override public long getCalloc()                              { return MemoryAccessJNI.calloc; }
        @Override public long getRealloc()                             { return MemoryAccessJNI.realloc; }
        @Override public long getFree()                                { return MemoryAccessJNI.free; }
        @Override public long getAlignedAlloc()                        { return MemoryAccessJNI.aligned_alloc; }
        @Override public long getAlignedFree()                         { return MemoryAccessJNI.aligned_free; }

        @Override public long malloc(long size)                        { return nmalloc(size); }
        @Override public long calloc(long num, long size)              { return ncalloc(num, size); }
        @Override public long realloc(long ptr, long size)             { return nrealloc(ptr, size); }
        @Override public void free(long ptr)                           { nfree(ptr); }
        @Override public long aligned_alloc(long alignment, long size) { return naligned_alloc(alignment, size); }
        @Override public void aligned_free(long ptr)                   { naligned_free(ptr); }
    }

    /** Wraps a MemoryAllocator to track allocations and detect memory leaks. */
    static class DebugAllocator implements MemoryAllocator {

        private static final ConcurrentMap<Allocation, Allocation> ALLOCATIONS = new ConcurrentHashMap<>();
        private static final ConcurrentMap<Long, String>           THREADS     = new ConcurrentHashMap<>();

        private final MemoryAllocator allocator;

        DebugAllocator(MemoryAllocator allocator) {
            this.allocator = allocator;

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (ALLOCATIONS.isEmpty()) return;

                boolean missingStacktrace = false;
                for (Allocation allocation : ALLOCATIONS.keySet()) {
                    StringBuilder sb = new StringBuilder(512);
                    sb.append("[LWJGL] ")
                      .append(allocation.size)
                      .append(" bytes leaked, thread ")
                      .append(allocation.threadId)
                      .append(" (")
                      .append(THREADS.get(allocation.threadId))
                      .append("), address: 0x")
                      .append(Long.toHexString(allocation.address).toUpperCase())
                      .append("\n");

                    StackTraceElement[] stackTrace = allocation.getElements();
                    if (stackTrace != null) {
                        for (Object el : stackTrace) {
                            sb.append("\tat ").append(el.toString()).append("\n");
                        }
                    } else {
                        missingStacktrace = true;
                    }
                    DEBUG_STREAM.print(sb);
                }
                if (missingStacktrace) {
                    DEBUG_STREAM.print("[LWJGL] Reminder: disable Configuration.DEBUG_MEMORY_ALLOCATOR_FAST to get stacktraces.\n");
                }
            }));
        }

        // NOTE: callbacks[] field removed - libffi not available on Android.
        // getMalloc/getCalloc etc. return 0L (null function pointers).
        // Zalith Launcher runtime replaces this entire module anyway.
        @Override public long getMalloc()       { return 0L; }
        @Override public long getCalloc()       { return 0L; }
        @Override public long getRealloc()      { return 0L; }
        @Override public long getFree()         { return 0L; }
        @Override public long getAlignedAlloc() { return 0L; }
        @Override public long getAlignedFree()  { return 0L; }

        @Override public long malloc(long size)                        { return track(allocator.malloc(size), size); }
        @Override public long calloc(long num, long size)              { return track(allocator.calloc(num, size), num * size); }
        @Override public long realloc(long ptr, long size) {
            long oldSize = untrack(ptr);
            long address = allocator.realloc(ptr, size);
            if (address != NULL)        track(address, size);
            else if (size != 0L)        track(ptr, oldSize);
            return address;
        }
        @Override public void free(long ptr)                           { untrack(ptr); allocator.free(ptr); }
        @Override public long aligned_alloc(long alignment, long size) { return track(allocator.aligned_alloc(alignment, size), size); }
        @Override public void aligned_free(long ptr)                   { untrack(ptr); allocator.aligned_free(ptr); }

        static long track(long address, long size) {
            if (address != NULL) {
                Thread t = Thread.currentThread();
                THREADS.putIfAbsent(t.getId(), t.getName());

                Allocation allocationNew = new Allocation(
                    address, size, t.getId(),
                    Configuration.DEBUG_MEMORY_ALLOCATOR_FAST.get(false) ? null : stackWalkGetTrace()
                );

                Allocation allocationOld = ALLOCATIONS.put(allocationNew, allocationNew);
                if (allocationOld != null) {
                    trackAbort(address, allocationOld, allocationNew);
                }
            }
            return address;
        }

        private static void trackAbort(long address, Allocation allocationOld, Allocation allocationNew) {
            String addressHex = Long.toHexString(address).toUpperCase();
            trackAbortPrint(allocationOld, "Old", addressHex);
            trackAbortPrint(allocationNew, "New", addressHex);
            throw new IllegalStateException("The memory address specified is already being tracked: 0x" + addressHex);
        }

        private static void trackAbortPrint(Allocation allocation, String name, String address) {
            StringBuilder sb = new StringBuilder(512);
            sb.append("[LWJGL] ").append(name)
              .append(" allocation with size ").append(allocation.size)
              .append(", thread ").append(allocation.threadId)
              .append(" (").append(THREADS.get(allocation.threadId))
              .append("), address: 0x").append(address).append("\n");
            StackTraceElement[] stackTrace = allocation.getElements();
            if (stackTrace != null) {
                for (Object el : stackTrace) sb.append("\tat ").append(el.toString()).append("\n");
            }
            DEBUG_STREAM.print(sb);
        }

        static long untrack(long address) {
            if (address == NULL) return 0L;
            Allocation allocation = ALLOCATIONS.remove(new Allocation(address, 0L, NULL, null));
            if (allocation == null) {
                throw new IllegalStateException("The memory address specified is not being tracked: 0x"
                    + Long.toHexString(address).toUpperCase());
            }
            return allocation.size;
        }

        private static class Allocation {
            final long address;
            final long size;
            final long threadId;
            private final Object[] stacktrace;

            Allocation(long address, long size, long threadId, Object[] stacktrace) {
                this.address   = address;
                this.size      = size;
                this.threadId  = threadId;
                this.stacktrace = stacktrace;
            }

            private StackTraceElement[] getElements() {
                return stacktrace == null ? null : stackWalkArray(stacktrace);
            }

            @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
            @Override public boolean equals(Object other) { return this.address == ((Allocation) other).address; }
            @Override public int hashCode()               { return Long.hashCode(address); }
        }

        static void report(MemoryAllocationReport report) {
            for (Allocation allocation : ALLOCATIONS.keySet()) {
                report.invoke(allocation.address, allocation.size, allocation.threadId,
                    THREADS.get(allocation.threadId), allocation.getElements());
            }
        }

        private static <T> void aggregate(T t, long size, Map<T, AtomicLong> map) {
            AtomicLong node = map.computeIfAbsent(t, k -> new AtomicLong());
            node.set(node.get() + size);
        }

        static void report(MemoryAllocationReport report,
                           MemoryAllocationReport.Aggregate groupByStackTrace,
                           boolean groupByThread) {
            switch (groupByStackTrace) {
                case ALL:                 reportAll(report, groupByThread); break;
                case GROUP_BY_METHOD:     reportByMethod(report, groupByThread); break;
                case GROUP_BY_STACKTRACE: reportByStacktrace(report, groupByThread); break;
            }
        }

        private static void reportAll(MemoryAllocationReport report, boolean groupByThread) {
            if (groupByThread) {
                Map<Long, AtomicLong> mapThread = new HashMap<>();
                for (Allocation a : ALLOCATIONS.values()) aggregate(a.threadId, a.size, mapThread);
                for (Entry<Long, AtomicLong> e : mapThread.entrySet())
                    report.invoke(NULL, e.getValue().get(), e.getKey(), THREADS.get(e.getKey()), (StackTraceElement[]) null);
            } else {
                long total = 0L;
                for (Allocation a : ALLOCATIONS.values()) total += a.size;
                report.invoke(NULL, total, NULL, null, (StackTraceElement[]) null);
            }
        }

        private static void reportByMethod(MemoryAllocationReport report, boolean groupByThread) {
            if (groupByThread) {
                Map<Long, Map<StackTraceElement, AtomicLong>> map = new HashMap<>();
                for (Allocation a : ALLOCATIONS.keySet()) {
                    StackTraceElement[] elems = a.getElements();
                    if (elems != null) aggregate(elems[0], a.size, map.computeIfAbsent(a.threadId, k -> new HashMap<>()));
                }
                for (Entry<Long, Map<StackTraceElement, AtomicLong>> te : map.entrySet()) {
                    long threadId = te.getKey();
                    for (Entry<StackTraceElement, AtomicLong> me : te.getValue().entrySet())
                        report.invoke(NULL, me.getValue().get(), threadId, THREADS.get(threadId), me.getKey());
                }
            } else {
                Map<StackTraceElement, AtomicLong> mapMethod = new HashMap<>();
                for (Allocation a : ALLOCATIONS.keySet()) {
                    StackTraceElement[] elems = a.getElements();
                    if (elems != null) aggregate(elems[0], a.size, mapMethod);
                }
                for (Entry<StackTraceElement, AtomicLong> e : mapMethod.entrySet())
                    report.invoke(NULL, e.getValue().get(), NULL, null, e.getKey());
            }
        }

        private static void reportByStacktrace(MemoryAllocationReport report, boolean groupByThread) {
            if (groupByThread) {
                Map<Long, Map<AllocationKey, AtomicLong>> map = new HashMap<>();
                for (Allocation a : ALLOCATIONS.keySet()) {
                    StackTraceElement[] elems = a.getElements();
                    if (elems != null) aggregate(new AllocationKey(elems), a.size, map.computeIfAbsent(a.threadId, k -> new HashMap<>()));
                }
                for (Entry<Long, Map<AllocationKey, AtomicLong>> te : map.entrySet()) {
                    long threadId = te.getKey();
                    for (Entry<AllocationKey, AtomicLong> se : te.getValue().entrySet())
                        report.invoke(NULL, se.getValue().get(), threadId, THREADS.get(threadId), se.getKey().elements);
                }
            } else {
                Map<AllocationKey, AtomicLong> mapStack = new HashMap<>();
                for (Allocation a : ALLOCATIONS.keySet()) {
                    StackTraceElement[] elems = a.getElements();
                    if (elems != null) aggregate(new AllocationKey(elems), a.size, mapStack);
                }
                for (Entry<AllocationKey, AtomicLong> e : mapStack.entrySet())
                    report.invoke(NULL, e.getValue().get(), NULL, null, e.getKey().elements);
            }
        }

        private static class AllocationKey {
            final StackTraceElement[] elements;
            AllocationKey(StackTraceElement[] elements) { this.elements = elements; }
            @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
            @Override public boolean equals(Object o) { return this == o || Arrays.equals(elements, ((AllocationKey) o).elements); }
            @Override public int hashCode()            { return Arrays.hashCode(elements); }
        }
    }
}
