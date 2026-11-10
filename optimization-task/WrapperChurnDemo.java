import java.util.ArrayList;
import java.util.List;

/**
 * Section 1.2C + pattern table (Adapter row): heap allocation and GC churn.
 *
 * BAD wraps a legacy sensor in a brand-new Adapter on every reading and keeps
 * the adapter (e.g. handing it to a listener / list), so it really reaches the
 * heap and fills Eden. GOOD builds the adapter once and reuses it — which is
 * exactly what Main.java in the Adapter task already does with
 * BulbAdapter / ThermostatAdapter.
 *
 * Note: if the wrapper never escapes the method, HotSpot's escape analysis can
 * remove the allocation entirely. That is the handout's Section 5 lesson —
 * measure, because the JIT is often smarter than intuition.
 */
final class WrapperChurnDemo {

    /** Legacy API: Fahrenheit only. */
    static final class LegacySensor {
        private int fahrenheit = 68;

        int readFahrenheit() {
            fahrenheit = 60 + (fahrenheit * 31 + 7) % 40; // deterministic "noise"
            return fahrenheit;
        }
    }

    interface CelsiusSensor {
        double celsius();
    }

    static final class SensorAdapter implements CelsiusSensor {
        private final LegacySensor legacy;

        SensorAdapter(LegacySensor legacy) {
            this.legacy = legacy;
        }

        @Override
        public double celsius() {
            return (legacy.readFahrenheit() - 32) * 5.0 / 9.0;
        }
    }

    private static final int READINGS = 2_000_000;
    private static final int BATCH = 1_000; // readings kept per dashboard refresh

    private WrapperChurnDemo() {
    }

    static void run() {
        Bench.header("SECTION 1: Adapter churn in a tight loop (" + READINGS + " readings)");

        LegacySensor sensor = new LegacySensor();
        List<CelsiusSensor> recent = new ArrayList<>(BATCH);

        Bench.Result bad = Bench.run("BAD  new SensorAdapter(...) per reading", () -> {
            double total = 0;
            for (int i = 0; i < READINGS; i++) {
                CelsiusSensor adapter = new SensorAdapter(sensor); // escapes into 'recent'
                recent.add(adapter);
                total += adapter.celsius();
                if (recent.size() == BATCH) recent.clear();
            }
            recent.clear();
            return (long) total;
        });

        CelsiusSensor shared = new SensorAdapter(sensor);
        Bench.Result good = Bench.run("GOOD one adapter, reused", () -> {
            double total = 0;
            for (int i = 0; i < READINGS; i++) {
                recent.add(shared);
                total += shared.celsius();
                if (recent.size() == BATCH) recent.clear();
            }
            recent.clear();
            return (long) total;
        });
        Bench.verdict(bad, good);
    }
}
