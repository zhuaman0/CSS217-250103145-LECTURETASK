# CSS 217 — Beyond Design Patterns: Optimization Lab

This lab measures each claim in the handout *Practical Java Optimization, the JVM, and Clean Code Realities* instead of taking it on trust. That is the handout's own rule in Section 5: never optimize based on intuition.

```bash
cd optimization-task
javac -d out *.java && java -cp out OptimizationLab
```

| File | Handout section |
|---|---|
| `DispatchDemo.java` | 1.2B: monomorphic, bimorphic and megamorphic call sites |
| `WrapperChurnDemo.java` | 1.2C and the Adapter row of the pitfalls table: creating a wrapper on every call |
| `Trap1Autoboxing.java` | Trap 1: `Integer compute(T)` vs `ToIntFunction<T>` |
| `Trap2CollectionSizing.java` | Trap 2: `new ArrayList<>()` vs `new ArrayList<>(n)` |
| `Trap3StringConcat.java` | Trap 3: `+=` in a loop vs `StringBuilder` |
| `Trap4StreamsInHotLoops.java` | Trap 4: a stream pipeline per call vs a counted `for` loop |
| `Bench.java` | Timing helper: warm-up, best of 7 runs, and heap bytes allocated per run (through `com.sun.management.ThreadMXBean`) |

## Results (OpenJDK 26, Apple Silicon, best of 7)

| Experiment | BAD | GOOD | Speed-up | Allocation saved |
|---|---|---|---|---|
| Call site, 1 vs 4 implementations | 32.5 ms (megamorphic) | 13.3 ms (monomorphic) | **2.4×** | n/a |
| Bimorphic (2 implementations) | n/a | 14.6 ms | about the same as monomorphic | n/a |
| New adapter on every reading | 7.0 ms | 6.9 ms | 1.0× | **30.5 MB** |
| Autoboxing `Integer` | 6.7 ms | 0.87 ms | **7.8×** | 30.5 MB |
| Default-size `ArrayList` (30 resizes) | 1.8 ms | 1.1 ms | **1.7×** | 10.1 MB |
| `String +=` (10k tokens) | 17.0 ms | 0.08 ms | **~200×** | **419 MB** |
| Stream per call in a hot loop | 20.9 ms | 4.1 ms | **5.0×** | 275 MB |

### What the numbers show

1. **Dispatch.** The bimorphic case costs almost nothing compared with the monomorphic one, as the handout says ("a conditional branch"). The jump happens at 3 or more types, when the JIT stops inlining. Each scenario has its own copy of the loop because HotSpot profiles each *call site* separately. A shared loop would let the 4-type run pollute the 1-type profile.
2. **Wrapper churn.** Allocating in the TLAB is so cheap that the time barely changes. The real cost is the 30 MB of short-lived garbage per 2M calls, which fills Eden and triggers young-generation GC pauses. A single-run benchmark does not show those pauses, but a server running for hours does. If the wrapper never escapes the method, escape analysis can remove the allocation completely. That is why the demo stores it in a list.
3. **Trap 3 is quadratic.** It allocates 419 MB to build a string of about 150 KB. Since Java 9, `+` compiles to `invokedynamic`/`StringConcatFactory` instead of a `StringBuilder`, but the whole string is still copied on every iteration.
4. **Trap 4 is about *small streams built many times*.** One large stream over 10M elements performs close to a loop. The cost comes from building a new pipeline, plus a capturing lambda and boxed `Integer`s, a million times.

> These are micro-benchmarks for teaching. For real decisions, use JMH, and use VisualVM or async-profiler (Section 5) to find the 10% of code that is actually hot first.

## "Before You Abstract" checklist applied to our earlier tasks

| Code | >1 real implementation? | Changes at runtime? | Stdlib type would do? | Verdict |
|---|---|---|---|---|
| `SmartDevice` + `BulbAdapter` / `ThermostatAdapter` (Adapter task) | Yes, 2 | Yes: `ModernHub` iterates over a mixed list | No: the interface has 4 methods | **Justified.** The `ModernHub` loops are bimorphic, so they stay inlinable. `Main` builds each adapter **once** and reuses it, so there is no churn. `calculateAveragePowerUsage` uses a plain counted loop with an `int` sum, so it has no boxing and no stream. |
| `Image` / `ProxyImage` (Task 1) | Yes: real and proxy | Yes: role-based access | No | **Justified.** The proxy adds a real behaviour (an access check before the expensive load). |
| `Video` / `ProxyVideo` (Task 2) | Yes | Yes: lazy download | No | **Justified.** Watch for the "hidden I/O" trap: a caller of `play()` cannot tell that the first call downloads a file. |
| `Internet` / `SchoolProxyInternet` (Task 3) | Yes | Yes | Could be a `Predicate<String>` for the block list | **Acceptable.** The block list could be a `Set.of(...)` instead of chained `equals` checks if it grows. |
| `Device` + remotes (Bridge, Tasks 4–5) | Yes: Radio, SmartLight; 3 remotes | Yes | No | **Justified.** Two independent axes of change (remote × device) are exactly what Bridge is for. |
| `Weapon` + `Hero` (Bridge, Task 6) | Yes: 2 weapons, 2 heroes | Yes | `Weapon` has one method, so `Runnable` would also work | **Borderline.** `Knight.attack()` and `Wizard.attack()` are identical, so the `Hero` hierarchy adds no behaviour yet. That is the "complex class hierarchy for simple domain problems" trap. Keep it only if heroes will gain different behaviour. |

**Takeaway.** Every pattern we used earlier solves a requirement that exists today, and none of them sits in a hot loop. They are worth their cost. The lab shows where the cost would start to matter: megamorphic call sites, wrappers created on every call, boxing, and streams inside a tight loop.
