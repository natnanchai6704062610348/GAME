import java.awt.Graphics2D;

/**
 * คลาสแม่แบบของบอสในเกม
 * สืบทอดจาก Zombie และ implements BossCombat
 */
public abstract class Boss extends Zombie implements BossCombat {

    // เรียก Constructor ของ Zombie เพื่อกำหนดข้อมูลเริ่มต้น
    protected Boss(int x, String name, int level) {
        super(x, name, level);
    }

    // ตรวจว่าพลังชีวิตต่ำกว่าครึ่งหนึ่งหรือไม่
    public final boolean isEnraged() {
        return hp < maxHp / 2;
    }

    // ใช้พฤติกรรมการเคลื่อนที่ของ Zombie
    protected final void pursue(Player player) {
        super.update(player);
    }

    // คลาสลูกต้องกำหนดพฤติกรรมการอัปเดตของบอส
    @Override
    public abstract void update(Player player);

    // คลาสลูกต้องตรวจสอบการโจมตีพิเศษที่โดนผู้เล่น
    @Override
    public abstract boolean specialHits(Player player);

    // คลาสลูกต้องกำหนดวิธีวาดบอส
    @Override
    public abstract void draw(
        Graphics2D g,
        int camera,
        boolean facingRight
    );
}