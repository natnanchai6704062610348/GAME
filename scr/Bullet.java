public class Bullet {
    private double x,y;private final int velocity,damage,width=14,height=5;
    public Bullet(int x,int y){this(x,y,12,18);}
    public Bullet(int x,int y,int velocity,int damage){this.x=x;this.y=y;this.velocity=velocity;this.damage=damage;}
    public void update(){x+=velocity;}
    public int getX(){return (int)x;} public int getY(){return (int)y;}
    public int getWidth(){return width;} public int getHeight(){return height;}
    public int getDamage(){return damage;}
}