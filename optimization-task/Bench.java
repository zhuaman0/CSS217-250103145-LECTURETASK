import java.lang.management.ManagementFactory;
import java.util.function.LongSupplier;

/**
 * Tiny measuring helper: best-of-N wall time + bytes allocated on the heap.
 *
 * This is NOT JMH. It warms the code up so the JIT compiles it, keeps results
 * alive in {@link #sink} so the JIT cannot delete the work, and reports the
 * fastest run. Good enough to show order-of-magnitude differences in class.
 */
final class Bench {

    /** Every benchmark result is folded in here, so the work is never "dead". */
    static long sink;

    private static final com.sun.management.ThreadMXBean THREADS =
            (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();

    private static final int WARMUPS = 5;
    private static final int RUNS = 7;

    private Bench() {
    }

    record Result(double millis, long bytes) {
    }

    static Result run(String label, LongSupplier body) {
        for (int i = 0; i < WARMUPS; i++) {
            sink += body.getAsLong();
        }
        long bestNanos = Long.MAX_VALUE;
        long bestBytes = Long.MAX_VALUE;
        long threadId = Thread.currentThread().threadId();
        for (int i = 0; i < RUNS; i++) {
            long bytesBefore = THREADS.getThreadAllocatedBytes(threadId);
            long t0 = System.nanoTime();
            sink += body.getAsLong();
            long elapsed = System.nanoTime() - t0;
            long bytes = THREADS.getThreadAllocatedBytes(threadId) - bytesBefore;
            bestNanos = Math.min(bestNanos, elapsed);
            bestBytes = Math.min(bestBytes, bytes);
        }
        Result r = new Result(bestNanos / 1e6, bestBytes);
        System.out.printf("  %-44s %9.3f ms  %12s allocated%n", label, r.millis(), human(r.bytes()));
        return r;
    }

    static void verdict(Result bad, Result good) {
        double speedup = bad.millis() / Math.max(good.millis(), 1e-6);
        System.out.printf("  => GOOD is %.1fx faster, allocates %s less%n%n",
                speedup, human(Math.max(0, bad.bytes() - good.bytes())));
    }

    static void header(String title) {
        System.out.println("------------------------------------------------------------------------");
        System.out.println(" " + title);
        System.out.println("------------------------------------------------------------------------");
    }

    static String human(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
