import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * จัดการตำแหน่ง การเคลื่อนที่ ความเสียหาย และภาพกระสุน
 */
public class Bullet {

    // ตำแหน่งกึ่งกลางกระสุน
    private double x;
    private double y;

    // ความเร็วแต่ละแกน
    private final double vx;
    private final double vy;

    private final int damage;

    // ขนาดกรอบตรวจการชน
    private final int width = 8;
    private final int height = 8;

    /**
     * สร้างกระสุนที่วิ่งไปทางขวา
     * ความเร็วเริ่มต้น 12 และความเสียหาย 18
     */
    public Bullet(int x, int y) {
        this(x, y, 12, 18);
    }

    /**
     * สร้างกระสุนที่เคลื่อนที่ในแนวนอน
     */
    public Bullet(int x, int y, int velocity, int damage) {
        this(x, y, velocity, 0, damage);
    }

    /**
     * สร้างกระสุนโดยกำหนดความเร็วได้ทั้งสองแกน
     */
    public Bullet(
            double x,
            double y,
            double vx,
            double vy,
            int damage) {

        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.damage = damage;
    }

    // อัปเดตตำแหน่งตามความเร็ว
    public void update() {
        x += vx;
        y += vy;
    }

    // คืนพิกัดขอบซ้ายของกรอบตรวจการชน
    public int getX() {
        return (int) Math.round(x) - width / 2;
    }

    // คืนพิกัดขอบบนของกรอบตรวจการชน
    public int getY() {
        return (int) Math.round(y) - height / 2;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDamage() {
        return damage;
    }

    /**
     * ตรวจว่ากระสุนออกนอกฉากเกินระยะเผื่อ 100 หน่วยหรือไม่
     */
    public boolean outside(int worldWidth, int worldHeight) {
        return x < -100
            || x > worldWidth + 100
            || y < -100
            || y > worldHeight + 100;
    }

    /**
     * วาดกระสุนเป็นเส้นสั้นตามทิศทางการเคลื่อนที่
     */
    public void draw(Graphics2D g, int camera) {
        Graphics2D copy = (Graphics2D) g.create();

        double speed = Math.max(1, Math.hypot(vx, vy));

        copy.setColor(new Color(255, 215, 100));

        copy.setStroke(new BasicStroke(
            3,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND
        ));

        // จุดหัวกระสุนบนหน้าจอ
        int headX = (int) x - camera;
        int headY = (int) y;

        // จุดหาง ย้อนทิศทางการเคลื่อนที่ 12 หน่วย
        int tailX = (int) (x - vx / speed * 12) - camera;
        int tailY = (int) (y - vy / speed * 12);

        copy.drawLine(headX, headY, tailX, tailY);
        copy.dispose();
    }
}