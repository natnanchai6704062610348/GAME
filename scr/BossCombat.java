import java.awt.Graphics2D;
public interface BossCombat {
    boolean specialHits(Player player);
    void draw(Graphics2D g,int camera,boolean facingRight);
}
