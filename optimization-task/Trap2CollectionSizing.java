import java.util.ArrayList;
import java.util.List;

/**
 * Trap 2: Under-sized collection growth.
 *
 * {@code new ArrayList<>()} starts at capacity 10 and grows by ~1.5x, copying
 * the whole backing array with System.arraycopy on every grow. Presizing
 * allocates one contiguous block once.
 */
final class Trap2CollectionSizing {

    private static final int N = 1_000_000;

    private Trap2CollectionSizing() {
    }

    static void run() {
        Bench.header("TRAP 2: Under-sized collection growth (" + N + " items)");
        System.out.println("  Default ArrayList resizes " + countGrows(N) + " times to hold " + N + " items.");

        String[] source = new String[N];
        for (int i = 0; i < N; i++) {
            source[i] = "rec-" + i;
        }

        Bench.Result bad = Bench.run("BAD  new ArrayList<>()", () -> {
            List<String> records = new ArrayList<>();
            for (String s : source) {
                records.add(s);
            }
            return records.size();
        });
        Bench.Result good = Bench.run("GOOD new ArrayList<>(expectedSize)", () -> {
            List<String> records = new ArrayList<>(source.length);
            for (String s : source) {
                records.add(s);
            }
            return records.size();
        });
        Bench.verdict(bad, good);
    }

    /** Mirrors ArrayList.grow(): newCapacity = old + old/2, starting at 10. */
    static int countGrows(int n) {
        int capacity = 10;
        int grows = 1; // the first add() allocates the initial 10-slot array
        while (capacity < n) {
            capacity += capacity >> 1;
            grows++;
        }
        return grows;
    }
}
