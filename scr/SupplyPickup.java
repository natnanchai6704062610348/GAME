import java.awt.*;
/** Collectible supplies placed on both the street and elevated routes. */
public final class SupplyPickup {
    public enum Type { HEALTH, ARMOR, AMMO, CREDITS }
    private final int x, y;
    private final Type type;
    private boolean collected;
    public SupplyPickup(int x, int y, Type type) { this.x=x; this.y=y; this.type=type; }
    public Type getType() { return type; }
    public boolean isCollected() { return collected; }
    public boolean collect(Player p) {
        if (collected || p.getX()+p.getWidth()<=x-14 || p.getX()>=x+14
                || p.getY()+p.getHeight()<=y-14 || p.getY()>=y+14) return false;
        if (type==Type.HEALTH && p.getHp()>=100) return false;
        if (type==Type.ARMOR && p.getArmor()>=100) return false;
        if (type==Type.HEALTH) p.heal(25);
        if (type==Type.ARMOR) p.addArmor(25);
        collected=true;
        return true;
    }
    public void draw(Graphics2D g,int camera,long now) {
        if(collected)return;
        int sx=x-camera, sy=y+(int)(Math.sin(now/220.0+x)*3);
        Color color=type==Type.HEALTH?new Color(85,220,135):type==Type.ARMOR?new Color(90,175,255):type==Type.AMMO?new Color(255,190,65):new Color(255,230,100);
        g.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),50));g.fillOval(sx-23,sy-23,46,46);
        g.setColor(new Color(18,25,33));g.fillRoundRect(sx-14,sy-14,28,28,6,6);
        g.setColor(color);g.drawRoundRect(sx-14,sy-14,28,28,6,6);
        g.setFont(new Font(Font.MONOSPACED,Font.BOLD,19));
        String symbol=type==Type.HEALTH?"+":type==Type.ARMOR?"S":type==Type.AMMO?"A":"$";
        g.drawString(symbol,sx-6,sy+7);
    }
}
