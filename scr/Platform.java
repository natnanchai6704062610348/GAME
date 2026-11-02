import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * คลาสพื้นฐานของแท่นยืน
 * ใช้เป็นแท่นทางเดียว โดยระบบผู้เล่นตรวจการลงบนผิวด้านบน
 */
public class Platform {

    // ตำแหน่งปัจจุบัน คลาสลูกสามารถเข้าถึงได้
    protected int x;
    protected int y;

    protected final int width;

    // ตำแหน่งก่อนอัปเดต ใช้คำนวณระยะเคลื่อนที่
    private int previousX;
    private int previousY;

    public Platform(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;

        previousX = x;
        previousY = y;
    }

    /**
     * บันทึกตำแหน่งก่อนเคลื่อนที่
     * คลาสลูกเรียก super.update() ก่อนเปลี่ยนตำแหน่ง
     */
    public void update() {
        previousX = x;
        previousY = y;
    }

    public int getDeltaX() {
        return x - previousX;
    }

    public int getDeltaY() {
        return y - previousY;
    }

    public int getPreviousY() {
        return previousY;
    }

    // แท่นพื้นฐานรองรับผู้เล่นได้ตลอดเวลา
    public boolean isSolid() {
        return true;
    }

    /**
     * เรียกเมื่อผู้เล่นลงบนแท่น
     * คลาสลูกสามารถ Override เพื่อเพิ่มพฤติกรรมได้
     */
    public void onLand() {
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    /**
     * ตรวจว่าผู้เล่นกับแท่นซ้อนทับกันในแนวนอนหรือไม่
     */
    public boolean overlaps(double left, int actorWidth) {
        return left + actorWidth > x
            && left < x + width;
    }

    /**
     * วาดแท่นและขาค้ำ โดยหักตำแหน่งกล้องออก
     */
    public void draw(Graphics2D g, int camera, Color accent) {
        int screenX = x - camera;

        // ตัวแท่น
        g.setColor(new Color(23, 26, 32));
        g.fillRect(screenX, y, width, 20);

        // ขอบด้านบน
        g.setColor(new Color(100, 105, 110));
        g.fillRect(screenX, y, width, 5);

        // แถบสีประจำด่าน
        g.setColor(accent);
        g.fillRect(screenX, y + 5, width, 3);

        // รายละเอียดบนตัวแท่น
        g.setColor(new Color(58, 62, 67));

        for (int i = 12; i < width - 8; i += 32) {
            g.fillRect(
                screenX + i,
                y + 10,
                18,
                4
            );
        }

        // ขาค้ำด้านซ้ายและขวา
        g.setStroke(new BasicStroke(4));

        g.drawLine(
            screenX + 12,
            y + 20,
            screenX + 38,
            y + 45
        );

        g.drawLine(
            screenX + width - 12,
            y + 20,
            screenX + width - 38,
            y + 45
        );

        g.setStroke(new BasicStroke(1));
    }
}