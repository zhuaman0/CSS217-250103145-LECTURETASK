/**
 * Trap 3: String concatenation in loops.
 *
 * Strings are immutable, so {@code log += token} copies the whole log so far
 * on every iteration: 1 + 2 + ... + n characters = O(n^2). (Since Java 9 the
 * {@code +} operator compiles to invokedynamic/StringConcatFactory instead of a
 * StringBuilder, but the full copy per iteration is still there.)
 * One StringBuilder with an estimated capacity appends in amortised O(n).
 */
final class Trap3StringConcat {

    private static final int N = 10_000;

    private Trap3StringConcat() {
    }

    static void run() {
        Bench.header("TRAP 3: String concatenation in loops (" + N + " tokens)");

        String[] tokens = new String[N];
        for (int i = 0; i < N; i++) {
            tokens[i] = "tok" + i;
        }

        Bench.Result bad = Bench.run("BAD  log += token + \", \"", () -> {
            String log = "";
            for (String token : tokens) {
                log += token + ", ";
            }
            return log.length();
        });
        Bench.Result good = Bench.run("GOOD StringBuilder(tokens.length * 16)", () -> {
            StringBuilder log = new StringBuilder(tokens.length * 16);
            for (String token : tokens) {
                log.append(token).append(", ");
            }
            return log.length();
        });
        Bench.verdict(bad, good);
    }
}
