import java.awt.*;

/** One-way ledge: actors may jump through it and land on its upper surface. */
public final class Platform {
    private final int x, y, width;
    public Platform(int x, int y, int width) {
        this.x = x; this.y = y; this.width = width;
    }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public boolean overlaps(double left, int actorWidth) {
        return left + actorWidth > x && left < x + width;
    }
    public void draw(Graphics2D g, int camera, Color accent) {
        int sx = x - camera;
        g.setColor(new Color(23, 26, 32)); g.fillRect(sx, y, width, 20);
        g.setColor(new Color(100, 105, 110)); g.fillRect(sx, y, width, 5);
        g.setColor(accent); g.fillRect(sx, y + 5, width, 3);
        g.setColor(new Color(58, 62, 67));
        for (int i = 12; i < width - 8; i += 32) g.fillRect(sx + i, y + 10, 18, 4);
        g.setStroke(new BasicStroke(4));
        g.drawLine(sx + 12, y + 20, sx + 38, y + 45);
        g.drawLine(sx + width - 12, y + 20, sx + width - 38, y + 45);
        g.setStroke(new BasicStroke(1));
    }
}
