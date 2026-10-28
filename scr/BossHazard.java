import java.awt.*;
/** Simulation owns hitboxes; BossSkillEffects renders their matching danger zones. */
public final class BossHazard {
    double x,y,vx,vy,gravity;
    final int width,height;
    private int delay,life,age,skill=1,impactTicks;
    private final boolean projectile;
    final Color color;
    public BossHazard(double x,double y,int w,int h,int delay,int life,Color color){this(x,y,w,h,delay,life,color,false);}
    public BossHazard(double x,double y,int w,int h,int delay,int life,Color color,boolean projectile){this.x=x;this.y=y;width=w;height=h;this.delay=delay;this.life=life;this.color=color;this.projectile=projectile;}
    public BossHazard withSkill(int level){skill=level;return this;}
    public void update(){age++;if(impactTicks>0){impactTicks--;return;}if(delay>0){delay--;return;}x+=vx;y+=vy;vy+=gravity;life--;}
    public boolean expired(){return (life<=0&&impactTicks<=0) || y>750 || x< -600;}
    public boolean active(){return delay<=0&&life>0&&impactTicks==0;}
    public boolean touches(Player p){return active()&&p.getX()<x+width&&p.getX()+p.getWidth()>x&&p.getY()<y+height&&p.getY()+p.getHeight()>y;}
    public void consume(){if(projectile){life=0;impactTicks=12;}}
    public void draw(Graphics2D g,int camera){
        if(impactTicks>0){Graphics2D copy=(Graphics2D)g.create();BossSkillEffects.impact(copy,(int)x-camera+width/2,(int)y+height/2,12+(12-impactTicks)*3,color,impactTicks);copy.dispose();return;}
        if(life<=0)return;
        BossSkillEffects.draw(g,skill,(int)x-camera,(int)y,width,height,age,delay>0,projectile,vx,vy);
    }
}
