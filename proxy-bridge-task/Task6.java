// Task 6: RPG Game Weapon System (Bridge)

// ===== Implementor hierarchy =====
interface Weapon {
    void use();
}

class Sword implements Weapon {
    @Override
    public void use() {
        System.out.println("Slash! Dealing 20 physical damage.");
    }
}

class MagicWand implements Weapon {
    @Override
    public void use() {
        System.out.println("Zap! Dealing 35 magic damage.");
    }
}

// ===== Abstraction hierarchy =====
abstract class Hero {
    protected Weapon weapon; // bridge slot

    public Hero(Weapon weapon) {
        this.weapon = weapon;
    }

    public abstract void attack();
}

class Knight extends Hero {
    public Knight(Weapon weapon) {
        super(weapon);
    }

    @Override
    public void attack() {
        weapon.use();
    }
}

class Wizard extends Hero {
    public Wizard(Weapon weapon) {
        super(weapon);
    }

    @Override
    public void attack() {
        weapon.use();
    }
}

public class Task6 {
    public static void main(String[] args) {
        Hero knightWithSword = new Knight(new Sword());
        knightWithSword.attack();

        Hero knightWithWand = new Knight(new MagicWand());
        knightWithWand.attack();
    }
}