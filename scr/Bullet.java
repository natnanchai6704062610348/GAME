import java.awt.*;
public class Bullet {
    private double x,y;
    private final double vx,vy;
    private final int damage,width=8,height=8;
    public Bullet(int x,int y){this(x,y,12,18);}
    public Bullet(int x,int y,int velocity,int damage){this(x,y,velocity,0,damage);}
    public Bullet(double x,double y,double vx,double vy,int damage){this.x=x;this.y=y;this.vx=vx;this.vy=vy;this.damage=damage;}
    public void update(){x+=vx;y+=vy;}
    public int getX(){return (int)Math.round(x)-width/2;}
    public int getY(){return (int)Math.round(y)-height/2;}
    public int getWidth(){return width;}public int getHeight(){return height;}
    public int getDamage(){return damage;}
    public boolean outside(int worldWidth,int worldHeight){return x< -100||x>worldWidth+100||y< -100||y>worldHeight+100;}
    public void draw(Graphics2D g,int camera){
        Graphics2D copy=(Graphics2D)g.create();double speed=Math.max(1,Math.hypot(vx,vy));
        copy.setColor(new Color(255,215,100));copy.setStroke(new BasicStroke(3,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        copy.drawLine((int)x-camera,(int)y,(int)(x-vx/speed*12)-camera,(int)(y-vy/speed*12));copy.dispose();
    }
}
