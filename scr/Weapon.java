/**
 * จัดการข้อมูลอาวุธ กระสุน การรีโหลด และการอัปเกรด
 */
public class Weapon {

    public final String name;

    public final int damage;
    public final int delay;
    public final int magazine;
    public final int reloadMs;
    public final int pellets;

    private int upgradeLevel;
    private int ammo;

    private long lastShot = 0;
    private long reloadUntil = 0;

    // ==================== Constructor ====================

    public Weapon(
            String name,
            int damage,
            int delay,
            int magazine,
            int reloadMs,
            int pellets) {

        this.name = name;
        this.damage = damage;
        this.delay = delay;
        this.magazine = magazine;
        this.reloadMs = reloadMs;
        this.pellets = pellets;

        ammo = magazine;
    }

    // ==================== การยิง ====================

    /**
     * ตรวจเงื่อนไขและใช้กระสุนหนึ่งนัด
     * now และ delay ใช้หน่วยมิลลิวินาที
     */
    public boolean shoot(long now) {
        if (reloadUntil > 0
                || ammo <= 0
                || now - lastShot < delay) {

            return false;
        }

        ammo--;
        lastShot = now;

        return true;
    }

    // ==================== การรีโหลด ====================

    public void reload(long now) {
        if (ammo < magazine && reloadUntil == 0) {
            reloadUntil = now + reloadMs;
        }
    }

    /**
     * เติมกระสุนเมื่อครบเวลารีโหลด
     */
    public void update(long now) {
        if (reloadUntil > 0 && now >= reloadUntil) {
            ammo = magazine;
            reloadUntil = 0;
        }
    }

    public boolean isReloading() {
        return reloadUntil > 0;
    }

    public int ammo() {
        return ammo;
    }

    /**
     * เติมกระสุนทันทีและยกเลิกสถานะรีโหลด
     */
    public void refillAmmo() {
        ammo = magazine;
        reloadUntil = 0;
    }

    // ==================== การอัปเกรด ====================

    /**
     * เพิ่มความเสียหายครั้งละ 25% ของค่าพื้นฐาน
     * ผลลัพธ์เป็นจำนวนเต็ม
     */
    public int getDamage() {
        return damage + damage * upgradeLevel / 4;
    }

    public int getUpgradeLevel() {
        return upgradeLevel;
    }

    public boolean upgrade() {
        if (upgradeLevel >= 3) {
            return false;
        }

        upgradeLevel++;

        return true;
    }

    // ==================== สร้างชุดอาวุธ ====================

    public static Weapon[] create() {
        // ลำดับพารามิเตอร์:
        // ชื่อ, ความเสียหาย, เว้นช่วงยิง, ความจุ,
        // เวลารีโหลด, จำนวนกระสุนย่อยต่อการยิง
        return new Weapon[] {
            new Weapon(
                "PISTOL",
                20, 320, 14, 1050, 1
            ),
            new Weapon(
                "SMG",
                11, 90, 40, 1400, 1
            ),
            new Weapon(
                "SHOTGUN",
                16, 680, 8, 1900, 5
            ),
            new Weapon(
                "ASSAULT RIFLE",
                24, 165, 30, 1600, 1
            ),
            new Weapon(
                "SNIPER",
                115, 1050, 5, 2200, 1
            )
        };
    }
}