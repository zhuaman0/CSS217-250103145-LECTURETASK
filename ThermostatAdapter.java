
public class ThermostatAdapter implements SmartDevice {

    private final LegacyThermostat thermostat; 

    public ThermostatAdapter(LegacyThermostat thermostat) {
        if (thermostat == null) {
            throw new IllegalArgumentException("LegacyThermostat must not be null");
        }
        this.thermostat = thermostat;
    }

    @Override
    public void turnOn() {
        String dial = thermostat.checkDial();
        if ("IDLE".equals(dial)) {
            thermostat.rotateDial("LOW");
        }
    }

    @Override
    public void turnOff() {
        thermostat.rotateDial("IDLE");
    }

    @Override
    public boolean isOn() {
        String dial = thermostat.checkDial();
        return "LOW".equals(dial)
            || "MEDIUM".equals(dial)
            || "MAX".equals(dial);
    }

    @Override
    public int getPowerPercent() {
        String dial = thermostat.checkDial();
        if ("IDLE".equals(dial))   return 0;
        if ("LOW".equals(dial))    return 33;
        if ("MEDIUM".equals(dial)) return 66;
        if ("MAX".equals(dial))    return 100;

        return -1;
    }
}
