import java.awt.Color;
import java.awt.Graphics2D;

/**
 * จัดการพื้นที่โจมตีและกระสุนสกิลของบอส
 * ตรวจสอบการชน และส่งข้อมูลให้ BossSkillEffects วาดเอฟเฟกต์
 */
public final class BossHazard {

    // ตำแหน่ง ความเร็ว และแรงโน้มถ่วง
    double x;
    double y;
    double vx;
    double vy;
    double gravity;

    // ขนาดพื้นที่โจมตี
    final int width;
    final int height;

    // เวลานับเป็นจำนวนรอบอัปเดต
    private int delay;
    private int life;
    private int age;
    private int skill = 1;
    private int impactTicks;

    private final boolean projectile;
    final Color color;

    /**
     * สร้างพื้นที่โจมตีทั่วไป
     */
    public BossHazard(
            double x,
            double y,
            int width,
            int height,
            int delay,
            int life,
            Color color) {

        this(x, y, width, height, delay, life, color, false);
    }

    /**
     * สร้างพื้นที่โจมตี โดยระบุได้ว่าเป็นกระสุนหรือไม่
     */
    public BossHazard(
            double x,
            double y,
            int width,
            int height,
            int delay,
            int life,
            Color color,
            boolean projectile) {

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.delay = delay;
        this.life = life;
        this.color = color;
        this.projectile = projectile;
    }

    // กำหนดรูปแบบสกิล และคืนออบเจ็กต์เดิมเพื่อเรียกต่อได้
    public BossHazard withSkill(int level) {
        skill = level;
        return this;
    }

    /**
     * อัปเดตเวลาและการเคลื่อนที่
     */
    public void update() {
        age++;

        // ช่วงแสดงเอฟเฟกต์กระทบ
        if (impactTicks > 0) {
            impactTicks--;
            return;
        }

        // รอให้ครบเวลาก่อนเริ่มโจมตี
        if (delay > 0) {
            delay--;
            return;
        }

        x += vx;
        y += vy;
        vy += gravity;

        life--;
    }

    // ตรวจว่าหมดอายุหรือออกนอกขอบเขตที่กำหนดหรือไม่
    public boolean expired() {
        return (life <= 0 && impactTicks <= 0)
            || y > 750
            || x < -600;
    }

    // ตรวจว่าอยู่ในช่วงที่สร้างความเสียหายได้หรือไม่
    public boolean active() {
        return delay <= 0
            && life > 0
            && impactTicks == 0;
    }

    /**
     * ตรวจการซ้อนทับกับกรอบผู้เล่นแบบ AABB
     */
    public boolean touches(Player player) {
        return active()
            && player.getX() < x + width
            && player.getX() + player.getWidth() > x
            && player.getY() < y + height
            && player.getY() + player.getHeight() > y;
    }

    // หยุดกระสุนและเริ่มเอฟเฟกต์กระทบ
    public void consume() {
        if (projectile) {
            life = 0;
            impactTicks = 12;
        }
    }

    /**
     * วาดสกิลหรือเอฟเฟกต์กระทบ
     */
    public void draw(Graphics2D g, int camera) {
        if (impactTicks > 0) {
            Graphics2D copy = (Graphics2D) g.create();

            int centerX = (int) x - camera + width / 2;
            int centerY = (int) y + height / 2;
            int radius = 12 + (12 - impactTicks) * 3;

            BossSkillEffects.impact(
                copy,
                centerX,
                centerY,
                radius,
                color,
                impactTicks
            );

            copy.dispose();
            return;
        }

        if (life <= 0) {
            return;
        }

        BossSkillEffects.draw(
            g,
            skill,
            (int) x - camera,
            (int) y,
            width,
            height,
            age,
            delay > 0,
            projectile,
            vx,
            vy
        );
    }
}