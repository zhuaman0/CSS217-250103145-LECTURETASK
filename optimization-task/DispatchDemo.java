/**
 * Section 1.2B: JIT inlining and call-site polymorphism.
 *
 * The same loop body {@code sum += shapes[i].area()} is timed with 1, 2 and 4
 * receiver types. HotSpot profiles each call site separately:
 *   1 type  -> monomorphic: inlined, no dispatch at all
 *   2 types -> bimorphic:   inlined behind one type check
 *   4 types -> megamorphic: no inlining, vtable/itable lookup on every call
 *
 * IMPORTANT: each scenario has its OWN copy of the loop. If all three shared
 * one method, they would share one call-site profile, and the megamorphic run
 * would "pollute" the monomorphic one.
 */
final class DispatchDemo {

    interface Shape {
        double area();
    }

    record Circle(double r) implements Shape {
        public double area() { return 3.14159 * r * r; }
    }

    record Square(double side) implements Shape {
        public double area() { return side * side; }
    }

    record Rect(double w, double h) implements Shape {
        public double area() { return w * h; }
    }

    record Triangle(double b, double h) implements Shape {
        public double area() { return 0.5 * b * h; }
    }

    private static final int N = 2_000_000;
    private static final int PASSES = 10;

    private DispatchDemo() {
    }

    static void run() {
        Bench.header("SECTION 1: Call-site polymorphism (" + N + " shapes x " + PASSES + " passes)");

        Shape[] mono = new Shape[N];
        Shape[] bi = new Shape[N];
        Shape[] mega = new Shape[N];
        for (int i = 0; i < N; i++) {
            double x = 1 + (i % 10);
            mono[i] = new Circle(x);
            bi[i] = (i % 2 == 0) ? new Circle(x) : new Square(x);
            mega[i] = switch (i % 4) {
                case 0 -> new Circle(x);
                case 1 -> new Square(x);
                case 2 -> new Rect(x, x + 1);
                default -> new Triangle(x, x + 1);
            };
        }

        Bench.Result m1 = Bench.run("Monomorphic (1 impl)", () -> (long) sumMono(mono));
        Bench.run("Bimorphic   (2 impls)", () -> (long) sumBi(bi));
        Bench.Result m4 = Bench.run("Megamorphic (4 impls)", () -> (long) sumMega(mega));
        System.out.printf("  => megamorphic call site is %.1fx slower than monomorphic%n",
                m4.millis() / Math.max(m1.millis(), 1e-6));
        System.out.println("     (same arithmetic per element; the difference is dispatch + lost inlining)");
        System.out.println();
    }

    static double sumMono(Shape[] shapes) {
        double sum = 0;
        for (int p = 0; p < PASSES; p++)
            for (int i = 0; i < shapes.length; i++) sum += shapes[i].area();
        return sum;
    }

    static double sumBi(Shape[] shapes) {
        double sum = 0;
        for (int p = 0; p < PASSES; p++)
            for (int i = 0; i < shapes.length; i++) sum += shapes[i].area();
        return sum;
    }

    static double sumMega(Shape[] shapes) {
        double sum = 0;
        for (int p = 0; p < PASSES; p++)
            for (int i = 0; i < shapes.length; i++) sum += shapes[i].area();
        return sum;
    }
}
