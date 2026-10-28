import java.awt.*;
public final class ExplosiveBarrel {
    private final int x;
    private boolean exploded;
    private long explodedAt;
    public ExplosiveBarrel(int x){this.x=x;}
    public boolean hit(Bullet b){return !exploded && b.getX()+b.getWidth()>x && b.getX()<x+38 && b.getY()+b.getHeight()>390 && b.getY()<470;}
    public boolean explode(long now){if(exploded)return false;exploded=true;explodedAt=now;return true;}
    public boolean inBlast(int cx,int cy){return Math.hypot(cx-(x+19),cy-430)<=185;}
    public int centerX(){return x+19;}
    public boolean isExploded(){return exploded;}
    public void draw(Graphics2D g,int camera,long now){
        int sx=x-camera;
        if(exploded){
            long age=now-explodedAt;if(age<0||age>450)return;
            int r=30+(int)(age*155/450);
            g.setColor(new Color(255,100,20,(int)(200*(450-age)/450)));g.fillOval(sx+19-r,430-r,r*2,r*2);
            g.setColor(new Color(255,225,100,(int)(220*(450-age)/450)));g.fillOval(sx+19-r/2,430-r/2,r,r);return;
        }
        g.setColor(new Color(130,42,33));g.fillRoundRect(sx,390,38,80,8,8);
        g.setColor(new Color(240,120,50));g.fillRect(sx,398,38,5);g.fillRect(sx,454,38,5);
        g.setFont(new Font(Font.MONOSPACED,Font.BOLD,23));g.setColor(Color.YELLOW);g.drawString("!",sx+12,439);
    }
}
