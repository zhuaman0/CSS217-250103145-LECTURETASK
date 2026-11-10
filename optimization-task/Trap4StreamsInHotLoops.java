import java.util.List;

/**
 * Trap 4: Streams in hot loops.
 *
 * The expensive case is not one big stream — it is a small stream built
 * millions of times inside a hot path. Every call builds a pipeline
 * (source, spliterator, one object per stage) plus boxing for
 * {@code List<Integer>}. A counted for-loop over {@code int[]} builds nothing.
 */
final class Trap4StreamsInHotLoops {

    private static final int CALLS = 1_000_000;
    private static final int READINGS_PER_CALL = 16;

    private Trap4StreamsInHotLoops() {
    }

    static void run() {
        Bench.header("TRAP 4: Streams in a hot loop (" + CALLS + " calls x " + READINGS_PER_CALL + " readings)");

        int[] readings = new int[READINGS_PER_CALL];
        Integer[] boxed = new Integer[READINGS_PER_CALL];
        for (int i = 0; i < READINGS_PER_CALL; i++) {
            readings[i] = 150 + i * 7;
            boxed[i] = readings[i];
        }
        List<Integer> readingList = List.of(boxed);

        Bench.Result bad = Bench.run("BAD  list.stream().filter().map().sum()", () -> {
            long total = 0;
            for (int c = 0; c < CALLS; c++) {
                total += sumAboveThresholdStream(readingList, c & 0xFF);
            }
            return total;
        });
        Bench.Result good = Bench.run("GOOD counted for-loop over int[]", () -> {
            long total = 0;
            for (int c = 0; c < CALLS; c++) {
                total += sumAboveThresholdLoop(readings, c & 0xFF);
            }
            return total;
        });
        Bench.verdict(bad, good);
    }

    // Readable, perfectly fine in business logic that runs a few times per request.
    static int sumAboveThresholdStream(List<Integer> values, int threshold) {
        return values.stream()
                .filter(v -> v > threshold) // captures 'threshold' -> new lambda object per call
                .mapToInt(v -> v * 2)
                .sum();
    }

    // What you write once a profiler proves this method is the hot path.
    static int sumAboveThresholdLoop(int[] values, int threshold) {
        int sum = 0;
        for (int i = 0; i < values.length; i++) {
            int v = values[i];
            if (v > threshold) {
                sum += v * 2;
            }
        }
        return sum;
    }
}
