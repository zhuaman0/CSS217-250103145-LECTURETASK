/**
 * CSS 217 — "Beyond Design Patterns" lab.
 *
 * Measures every claim in the handout instead of trusting intuition
 * (Section 5: "Never optimize based on intuition").
 *
 * Run from this folder:
 *   javac -d out *.java && java -cp out OptimizationLab
 */
public class OptimizationLab {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println(" CSS 217: JVM OPTIMIZATION LAB  (Java " + Runtime.version() + ")");
        System.out.println(" Best of 7 runs after 5 warm-ups. Numbers vary by machine; ratios matter.");
        System.out.println("========================================================================");

        DispatchDemo.run();
        WrapperChurnDemo.run();
        Trap1Autoboxing.run();
        Trap2CollectionSizing.run();
        Trap3StringConcat.run();
        Trap4StreamsInHotLoops.run();

        System.out.println("(checksum " + Bench.sink + " — printed so the JIT cannot discard the work)");
    }
}
