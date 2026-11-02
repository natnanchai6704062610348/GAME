/**
 * เลือกชื่อและสร้างบอสตามหมายเลขด่าน
 */
public final class BossFactory {

    private static final String[] NAMES = {
        "IRON MAW",
        "FURNACE BUTCHER",
        "STORM EEL",
        "THORN STALKER",
        "ROADBREAKER",
        "THE SURGEON",
        "PLAGUE BELLY",
        "WAR COMMANDER",
        "MAGMA TITAN",
        "HIVE QUEEN"
    };

    // คืนชื่อบอส โดยจำกัดหมายเลขด่านให้อยู่ระหว่าง 1–10
    public static String name(int level) {
        int stage = Math.max(1, Math.min(10, level));

        return NAMES[stage - 1];
    }

    // สร้างบอสตามหมายเลขด่าน
    public static Boss create(int x, int level, int worldWidth) {
        if (level == 1) {
            return new MutantBoss(x, 1, worldWidth);
        }

        return new StageBoss(x, level, worldWidth);
    }
}