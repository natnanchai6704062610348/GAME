import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * ไอเทมที่ผู้เล่นเก็บได้ตามพื้นและแท่นยืน
 */
public final class SupplyPickup {

    public enum Type {
        HEALTH,
        ARMOR,
        AMMO,
        CREDITS
    }

    private final int x;
    private final int y;
    private final Type type;

    private boolean collected;

    public SupplyPickup(int x, int y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public boolean isCollected() {
        return collected;
    }

    /**
     * ตรวจว่าผู้เล่นสัมผัสและเก็บไอเทมได้หรือไม่
     * คืนค่า true เมื่อเก็บสำเร็จครั้งแรก
     */
    public boolean collect(Player player) {

        // ไม่ให้เก็บซ้ำหรือเก็บจากระยะที่ไม่สัมผัส
        if (collected
                || player.getX() + player.getWidth() <= x - 14
                || player.getX() >= x + 14
                || player.getY() + player.getHeight() <= y - 14
                || player.getY() >= y + 14) {

            return false;
        }

        // ไม่เก็บไอเทมฟื้นพลังหากพลังชีวิตเต็ม
        if (type == Type.HEALTH && player.getHp() >= 100) {
            return false;
        }

        // ไม่เก็บไอเทมเกราะหากเกราะเต็ม
        if (type == Type.ARMOR && player.getArmor() >= 100) {
            return false;
        }

        if (type == Type.HEALTH) {
            player.heal(25);
        }

        if (type == Type.ARMOR) {
            player.addArmor(25);
        }

        // กระสุนและเงินจะให้ GamePanel จัดการหลังเก็บสำเร็จ
        collected = true;

        return true;
    }

    /**
     * วาดไอเทมพร้อมแสงและการลอยขึ้นลงเล็กน้อย
     */
    public void draw(Graphics2D g, int camera, long now) {
        if (collected) {
            return;
        }

        int screenX = x - camera;

        int screenY = y + (int) (
            Math.sin(now / 220.0 + x) * 3
        );

        // สีประจำประเภทไอเทม
        Color color = type == Type.HEALTH
            ? new Color(85, 220, 135)
            : (type == Type.ARMOR
                ? new Color(90, 175, 255)
                : (type == Type.AMMO
                    ? new Color(255, 190, 65)
                    : new Color(255, 230, 100)));

        // แสงรอบไอเทม
        g.setColor(new Color(
            color.getRed(),
            color.getGreen(),
            color.getBlue(),
            50
        ));

        g.fillOval(
            screenX - 23,
            screenY - 23,
            46,
            46
        );

        // พื้นหลังไอเทม
        g.setColor(new Color(18, 25, 33));

        g.fillRoundRect(
            screenX - 14,
            screenY - 14,
            28,
            28,
            6,
            6
        );

        // กรอบไอเทม
        g.setColor(color);

        g.drawRoundRect(
            screenX - 14,
            screenY - 14,
            28,
            28,
            6,
            6
        );

        // สัญลักษณ์ประจำประเภท
        String symbol = type == Type.HEALTH
            ? "+"
            : (type == Type.ARMOR
                ? "S"
                : (type == Type.AMMO ? "A" : "$"));

        g.setFont(
            new Font(Font.MONOSPACED, Font.BOLD, 19)
        );

        g.drawString(
            symbol,
            screenX - 6,
            screenY + 7
        );
    }
}