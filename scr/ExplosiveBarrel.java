import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * ถังระเบิดที่ถูกยิงได้
 * เก็บสถานะการระเบิด ตรวจระยะ และวาดเอฟเฟกต์
 */
public final class ExplosiveBarrel {

    private final int x;

    private boolean exploded;
    private long explodedAt;

    public ExplosiveBarrel(int x) {
        this.x = x;
    }

    /**
     * ตรวจว่ากระสุนชนถังที่ยังไม่ระเบิดหรือไม่
     * ถังมีขนาด 38 × 80 และอยู่ที่ระดับ y = 390
     */
    public boolean hit(Bullet bullet) {
        return !exploded
            && bullet.getX() + bullet.getWidth() > x
            && bullet.getX() < x + 38
            && bullet.getY() + bullet.getHeight() > 390
            && bullet.getY() < 470;
    }

    /**
     * เริ่มระเบิดได้เพียงครั้งเดียว
     * คืนค่า true เมื่อเริ่มระเบิดสำเร็จ
     */
    public boolean explode(long now) {
        if (exploded) {
            return false;
        }

        exploded = true;
        explodedAt = now;

        return true;
    }

    /**
     * ตรวจว่าจุดเป้าหมายอยู่ในรัศมีระเบิด 185 หน่วยหรือไม่
     */
    public boolean inBlast(int centerX, int centerY) {
        double distance = Math.hypot(
            centerX - (x + 19),
            centerY - 430
        );

        return distance <= 185;
    }

    public int centerX() {
        return x + 19;
    }

    public boolean isExploded() {
        return exploded;
    }

    /**
     * วาดถัง หรือเอฟเฟกต์ระเบิดตามสถานะ
     */
    public void draw(Graphics2D g, int camera, long now) {
        int screenX = x - camera;

        if (exploded) {
            long age = now - explodedAt;

            // แสดงเอฟเฟกต์เป็นเวลา 450 มิลลิวินาที
            if (age < 0 || age > 450) {
                return;
            }

            int radius = 30 + (int) (age * 155 / 450);

            // วงระเบิดชั้นนอก ขยายและค่อย ๆ จางลง
            int outerAlpha = (int) (200 * (450 - age) / 450);

            g.setColor(new Color(255, 100, 20, outerAlpha));
            g.fillOval(
                screenX + 19 - radius,
                430 - radius,
                radius * 2,
                radius * 2
            );

            // แสงระเบิดชั้นใน
            int innerAlpha = (int) (220 * (450 - age) / 450);

            g.setColor(new Color(255, 225, 100, innerAlpha));
            g.fillOval(
                screenX + 19 - radius / 2,
                430 - radius / 2,
                radius,
                radius
            );

            return;
        }

        // ตัวถัง
        g.setColor(new Color(130, 42, 33));
        g.fillRoundRect(screenX, 390, 38, 80, 8, 8);

        // แถบสีบนและล่าง
        g.setColor(new Color(240, 120, 50));
        g.fillRect(screenX, 398, 38, 5);
        g.fillRect(screenX, 454, 38, 5);

        // สัญลักษณ์อันตราย
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 23));
        g.setColor(Color.YELLOW);
        g.drawString("!", screenX + 12, 439);
    }
}