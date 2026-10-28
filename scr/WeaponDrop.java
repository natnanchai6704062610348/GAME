import java.awt.*;
import java.awt.image.BufferedImage;
public class WeaponDrop {

    private final int x;
    private final int y;
    private final int weaponIndex;
    private final long createdAt;

    private static final long LIFE_TIME = 20000;

    public WeaponDrop(int x, int y, int weaponIndex) {
        this.x = x;
        this.y = y;
        this.weaponIndex = weaponIndex;
        this.createdAt = System.currentTimeMillis();
    }

    public int getWeaponIndex() {
        return weaponIndex;
    }

    public boolean isExpired(long now) {
        return now - createdAt >= LIFE_TIME;
    }

    public boolean canPickup(Player player) {
        int playerCenter = player.getX() + player.getWidth() / 2;
        return Math.abs(playerCenter - (x + 16)) <= 65
            && player.getY() + player.getHeight() >= y - 15
            && player.getY() <= y + 32;
    }

    public void draw(Graphics2D g, int camera) {
        int sx = x - camera;

        // กล่องอาวุธสีทอง
        g.setColor(new Color(30, 25, 15));
        g.fillRect(sx - 2, y - 2, 36, 36);

        g.setColor(new Color(225, 165, 35));
        g.fillRect(sx, y, 32, 32);

        g.setColor(new Color(255, 225, 95));
        g.drawRect(sx + 3, y + 3, 25, 25);

        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        g.setColor(Color.BLACK);
        g.drawString("W", sx + 9, y + 23);
    }
    
    public void draw(Graphics2D g, int camera, BufferedImage image) {
        int sx = x - camera;

        // เงาใต้ปืน
        g.setColor(new Color(0, 0, 0, 100));
        g.fillOval(sx - 8, y + 26, 52, 9);

        if (image != null) {
            // วาดรูปปืน
            g.drawImage(image, sx - 12, y - 6, 58, 38, null);
        } else {
            // สำรองกรณีภาพไม่โหลด
            g.setColor(Color.ORANGE);
            g.fillRect(sx, y, 32, 32);
        }
    }

}
