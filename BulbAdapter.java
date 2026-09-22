public class BulbAdapter implements SmartDevice {

    private static final int K = 4;

    private final LegacyBulb bulb;

    public BulbAdapter(LegacyBulb bulb) {
        if (bulb == null) {
            throw new IllegalArgumentException("LegacyBulb must not be null");
        }
        this.bulb = bulb;
    }

    @Override
    public void turnOn() {
        bulb.setBrightness(255);
    }

    @Override
    public void turnOff() {
        bulb.setBrightness(0);
    }

    @Override
    public boolean isOn() {
        if (!bulb.hasPower()) {
            return false;
        }
        return bulb.readBrightness() > 0;
    }

    @Override
    public int getPowerPercent() {
        if (!bulb.hasPower()) {
            return 0;
        }

        int raw = bulb.readBrightness();

        if (raw == 0) {
            return 0;
        }
        int base = (raw * 100) / 255;
        int calibrated = base + K;

        return Math.min(100, calibrated);
    }
}
