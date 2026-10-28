import java.awt.*;
public class Zombie {
    private boolean dying = false;
    protected double x; protected final int y=368;
    protected int hp,maxHp,damage,width=55,height=102;
    protected double speed; protected String kind;
    private long lastHit=0;
    public Zombie(int x,int y){this(x,"Walker",1);}
    public Zombie(int x,String kind,int level){
        this.x=x;this.kind=kind;maxHp=34+level*7;speed=1.35+level*.09;damage=10+level;
        if(kind.equals("Runner")){speed*=2.1;maxHp=(int)(maxHp*.75);}
        if(kind.equals("Brute")){speed*=.65;maxHp*=3;damage*=2;width=75;height=118;}
        hp=maxHp;
    }
    public void update(Player p){if(x>p.getX()+25)x-=speed;else if(x<p.getX()-25)x+=speed;}
    public void move(){x-=speed;}
    public boolean canHit(long now){if(now-lastHit>820){lastHit=now;return true;}return false;}
    public void hurt(int dmg){hp-=dmg;}
    public void knockBackFrom(double origin,int distance,int worldWidth){double direction=x+width/2.0>=origin?1:-1;x=Math.max(0,Math.min(worldWidth-width,x+direction*distance));}
    public boolean dead(){return hp<=0;}
    public int getX(){return (int)x;} public int getY(){return 470-height;}
    public int getWidth(){return width;} public int getHeight(){return height;}
    public int getHp(){return hp;} public int getMaxHp(){return maxHp;}
    public int getDamage(){return damage;} public String getKind(){return kind;}
    public int getReward(){
        return kind.equals("Brute")?75:
        kind.equals("Runner")?35:25;
    }
    public boolean isDying(){
        return dying;
    }
    public void startDeath(){
        dying = true;
    }
    
    public double getDropChance() {
        return 0.12;
    }

}

