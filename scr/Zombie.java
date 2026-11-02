/**
 * คลาสพื้นฐานของศัตรู
 * รองรับ Walker, Runner และ Brute
 * และเป็นคลาสแม่ของ Boss
 */
public class Zombie {

    private boolean dying = false;

    protected double x;
    protected final int y = 368;

    protected int hp;
    protected int maxHp;
    protected int damage;

    protected int width = 55;
    protected int height = 102;

    protected double speed;
    protected String kind;

    private long lastHit = 0;

    // ==================== Constructor ====================

    /**
     * สร้าง Walker ระดับ 1
     * พารามิเตอร์ y ยังไม่ได้ถูกนำมาใช้ในโค้ดเดิม
     */
    public Zombie(int x, int y) {
        this(x, "Walker", 1);
    }

    public Zombie(int x, String kind, int level) {
        this.x = x;
        this.kind = kind;

        maxHp = 34 + level * 7;
        speed = 1.35 + level * 0.09;
        damage = 10 + level;

        // Runner เคลื่อนที่เร็ว แต่พลังชีวิตน้อยลง
        if (kind.equals("Runner")) {
            speed *= 2.1;
            maxHp = (int) (maxHp * 0.75);
        }

        // Brute ตัวใหญ่ อึด และโจมตีแรง แต่เดินช้า
        if (kind.equals("Brute")) {
            speed *= 0.65;
            maxHp *= 3;
            damage *= 2;

            width = 75;
            height = 118;
        }

        hp = maxHp;
    }

    // ==================== การเคลื่อนที่ ====================

    /**
     * เดินเข้าหาผู้เล่น โดยเว้นระยะตามแนวนอน 25 หน่วย
     */
    public void update(Player player) {
        if (x > player.getX() + 25) {
            x -= speed;
        } else if (x < player.getX() - 25) {
            x += speed;
        }
    }

    public void move() {
        x -= speed;
    }

    /**
     * ผลักศัตรูออกจากจุดกำเนิดแรง
     * และจำกัดตำแหน่งไม่ให้ออกจากด่าน
     */
    public void knockBackFrom(
            double origin,
            int distance,
            int worldWidth) {

        double direction =
            x + width / 2.0 >= origin ? 1 : -1;

        x = Math.max(
            0,
            Math.min(
                worldWidth - width,
                x + direction * distance
            )
        );
    }

    // ==================== การต่อสู้ ====================

    /**
     * อนุญาตให้โจมตีเมื่อผ่านไปมากกว่า 820 มิลลิวินาที
     * และบันทึกเวลาทันทีเมื่อคืนค่า true
     */
    public boolean canHit(long now) {
        if (now - lastHit > 820) {
            lastHit = now;
            return true;
        }

        return false;
    }

    public void hurt(int damage) {
        hp -= damage;
    }

    public boolean dead() {
        return hp <= 0;
    }

    // ==================== ตำแหน่งและขนาด ====================

    public int getX() {
        return (int) x;
    }

    // ให้เท้าศัตรูอยู่ที่ระดับพื้น y = 470
    public int getY() {
        return 470 - height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // ==================== ข้อมูลศัตรู ====================

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getDamage() {
        return damage;
    }

    public String getKind() {
        return kind;
    }

    // ==================== รางวัลและไอเทม ====================

    public int getReward() {
        if (kind.equals("Brute")) {
            return 75;
        }

        if (kind.equals("Runner")) {
            return 35;
        }

        return 25;
    }

    public double getDropChance() {
        return 0.12;
    }

    // ==================== สถานะแอนิเมชันตาย ====================

    public boolean isDying() {
        return dying;
    }

    public void startDeath() {
        dying = true;
    }
}