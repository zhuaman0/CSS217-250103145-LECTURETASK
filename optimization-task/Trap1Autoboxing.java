import java.util.function.ToIntFunction;

/**
 * Trap 1: Autoboxing in generic interfaces.
 *
 * BAD returns {@code Integer}: every score > 127 is a fresh heap object
 * (Integer.valueOf only caches -128..127). GOOD returns a primitive {@code int}
 * through the standard {@link ToIntFunction} — no custom interface needed
 * (checklist item 3).
 */
final class Trap1Autoboxing {

    // ❌ BAD
    interface BoxedScoreCalculator<T> {
        Integer compute(T record);
    }

    // ✅ GOOD: ToIntFunction<T> is the stdlib version of "int compute(T)".

    record Student(int exam, int homework) {
    }

    private static final int N = 2_000_000;

    private Trap1Autoboxing() {
    }

    static void run() {
        Bench.header("TRAP 1: Autoboxing in generic interfaces (" + N + " records)");

        Student[] students = new Student[N];
        for (int i = 0; i < N; i++) {
            students[i] = new Student(200 + i % 50, 100 + i % 30); // scores > 127 -> not cached
        }

        BoxedScoreCalculator<Student> boxed = s -> s.exam() * 2 + s.homework();
        ToIntFunction<Student> primitive = s -> s.exam() * 2 + s.homework();

        // The boxed values are kept in an array, the way real code stores results
        // (e.g. a List<Integer> of scores). Kept results cannot be optimised away.
        Integer[] boxedOut = new Integer[N];
        int[] primitiveOut = new int[N];

        Bench.Result bad = Bench.run("BAD  Integer compute(T)", () -> {
            long sum = 0;
            for (int i = 0; i < N; i++) {
                boxedOut[i] = boxed.compute(students[i]);
                sum += boxedOut[i];
            }
            return sum;
        });
        Bench.Result good = Bench.run("GOOD ToIntFunction<T>.applyAsInt", () -> {
            long sum = 0;
            for (int i = 0; i < N; i++) {
                primitiveOut[i] = primitive.applyAsInt(students[i]);
                sum += primitiveOut[i];
            }
            return sum;
        });
        Bench.verdict(bad, good);
    }
}
