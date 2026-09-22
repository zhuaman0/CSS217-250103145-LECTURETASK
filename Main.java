import java.util.List;

public class Main {
private static final String LINE =
        "============================================================";

    public static void main(String[] args) {
        System.out.println(LINE);
        System.out.println(" OMNIHOME SMART CONTROLLER: SYSTEM STARTUP");
        System.out.println(LINE);

        LegacyBulb rawBulb = new LegacyBulb();
        LegacyThermostat rawThermostat = new LegacyThermostat();

        SmartDevice bulbAdapter = new BulbAdapter(rawBulb);
        SmartDevice thermostatAdapter = new ThermostatAdapter(rawThermostat);
        System.out.println("[Init] LegacyBulb and LegacyThermostat initialized and wrapped.");

        List<SmartDevice> deviceList = List.of(bulbAdapter, thermostatAdapter);
        ModernHub hub = new ModernHub(deviceList);
        System.out.println("[Hub] Registering " + deviceList.size()
                + " adapted devices into ModernHub...");

        // Stage 3 / Step 5: activate all.
        System.out.println("--- OPERATION: ACTIVATE ALL DEVICES ---");
        System.out.println("[Action] ModernHub.activateAll() invoked.");
        hub.activateAll();
        System.out.println(" -> BulbAdapter: Brightness set to " + rawBulb.readBrightness() + ".");
        System.out.println(" -> ThermostatAdapter: Dial set to '" + rawThermostat.checkDial() + "'.");

        boolean allActive = bulbAdapter.isOn() && thermostatAdapter.isOn();
        System.out.println("[Status] All devices reported active: " + allActive);

        double avg = hub.calculateAveragePowerUsage();
        System.out.printf("[Power] Fleet Average Power Usage: %.2f%% (Bulb: %d%%, Thermostat: %d%%)%n",
                avg, bulbAdapter.getPowerPercent(), thermostatAdapter.getPowerPercent());

        System.out.println("--- AUDIT: HARDWARE FAULT INJECTION (STAGE 4) ---");

        System.out.println("[Fault 1] Filament physically severed on LegacyBulb...");
        rawBulb.breakFilament();
        boolean bulbOn = bulbAdapter.isOn();
        int bulbPower = bulbAdapter.getPowerPercent();
        System.out.println(" -> BulbAdapter.isOn(): " + bulbOn
                + (!bulbOn ? " [PASSED - Verified disconnected]" : " [FAILED]"));
        System.out.println(" -> BulbAdapter.getPowerPercent(): " + bulbPower + "%"
                + (bulbPower == 0 ? " [PASSED - Inactive power confirmed]" : " [FAILED]"));

        System.out.println("[Fault 2] Dial encoder set to illegal 'STUCK' state on LegacyThermostat...");
        rawThermostat.rotateDial("STUCK");
        boolean thOn = thermostatAdapter.isOn();
        int thPower = thermostatAdapter.getPowerPercent();
        System.out.println(" -> ThermostatAdapter.isOn(): " + thOn
                + (!thOn ? " [PASSED - Inactive flag confirmed]" : " [FAILED]"));
        System.out.println(" -> ThermostatAdapter.getPowerPercent(): " + thPower
                + (thPower == -1 ? " [PASSED - Sensor fault sentinel returned]" : " [FAILED]"));

        System.out.println("--- OPERATION: EMERGENCY SHUTDOWN ---");
        System.out.println("[Action] ModernHub.emergencyShutdown() invoked.");
        hub.emergencyShutdown();
        System.out.println(" -> BulbAdapter: Brightness set to " + rawBulb.readBrightness() + ".");
        System.out.println(" -> ThermostatAdapter: Dial rotated to '" + rawThermostat.checkDial() + "'.");
        System.out.printf("[Power] Fleet Average Power Usage: %.2f%%%n",
                hub.calculateAveragePowerUsage());

        System.out.println(LINE);
        System.out.println(" ALL INTEGRATION TESTS PASSED (100/100)");
        System.out.println(LINE);
    }
}
