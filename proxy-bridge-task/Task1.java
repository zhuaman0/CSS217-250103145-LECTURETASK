// Task 1: The Bouncer (Protection Proxy)
interface Image {
    void show();
}

class RealImage implements Image {
    private final String fileName;

    public RealImage(String fileName) {
        this.fileName = fileName;
        loadFromDisk();
    }

    private void loadFromDisk() {
        System.out.println("--> [SLOW] Reading " + fileName + " from hard drive...");
    }

    @Override
    public void show() {
        System.out.println("Displaying " + fileName + " on screen!");
    }
}

class ProxyImage implements Image {
    private final String fileName;
    private final String userRole;
    private RealImage realImage;

    public ProxyImage(String fileName, String userRole) {
        this.fileName = fileName;
        this.userRole = userRole;
    }

    @Override
    public void show() {
        // Проверка доступа ДО того, как трогаем дорогой объект
        if (!"ADMIN".equals(userRole)) {
            System.out.println("[ACCESS DENIED] You lack permission to view: " + fileName);
            return;
        }
        if (realImage == null) {
            realImage = new RealImage(fileName); // lazy loading
        }
        realImage.show();
    }
}

public class Task1 {
    public static void main(String[] args) {
        Image guestImage = new ProxyImage("salary_sheet.png", "GUEST");
        Image adminImage = new ProxyImage("salary_sheet.png", "ADMIN");

        guestImage.show();
        adminImage.show();
    }
}