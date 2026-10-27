// Task 4 & Task 5: Bridge (Remote <-> Device)

// ===== Implementor =====
interface Device {
    void turnOn();

    void turnOff();

    void setVolume(int level);
}

class Radio implements Device {
    @Override
    public void turnOn() {
        System.out.println("Radio starts playing static/music.");
    }

    @Override
    public void turnOff() {
        System.out.println("Radio turns off.");
    }

    @Override
    public void setVolume(int level) {
        System.out.println("Radio volume knob turned to: " + level);
    }
}

// Task 4: новое устройство — код пультов НЕ меняется
class SmartLight implements Device {
    @Override
    public void turnOn() {
        System.out.println("Smart Light glows bright white.");
    }

    @Override
    public void turnOff() {
        System.out.println("Smart Light shuts off.");
    }

    @Override
    public void setVolume(int level) {
        System.out.println("Smart Light brightness set to: " + level + "%");
    }
}

// ===== Abstraction =====
class BasicRemote {
    protected Device device; // мост

    public BasicRemote(Device device) {
        this.device = device;
    }

    public void powerOn() {
        device.turnOn();
    }

    public void powerOff() {
        device.turnOff();
    }
}

class AdvancedRemote extends BasicRemote {
    public AdvancedRemote(Device device) {
        super(device);
    }

    public void mute() {
        System.out.println("[Remote] Mute button pressed!");
        device.setVolume(0);
    }
}

// Task 5: новый пульт — код устройств НЕ меняется
class PartyRemote extends BasicRemote {
    public PartyRemote(Device device) {
        super(device);
    }

    public void partyMode() {
        System.out.println("[Party Remote] Pumping up the party!");
        device.turnOn();
        device.setVolume(100);
    }
}

public class BridgeRemotes {
    public static void main(String[] args) {
        System.out.println("=== Task 4 ===");
        Device light = new SmartLight();
        AdvancedRemote remote = new AdvancedRemote(light);
        remote.powerOn();
        remote.mute();

        System.out.println();
        System.out.println("=== Task 5 ===");
        Device radio = new Radio();
        PartyRemote partyRemote = new PartyRemote(radio);
        partyRemote.partyMode();
    }
}